"""
Main orchestrator for the Failure Analyzer.

Flow:
  1. Parse cucumber.json -> failures + total scenario count
  2. Enrich failures with Playwright trace data
  3. Group similar failures by error type + step pattern
  4. Filter groups to only the configured error types
  5. Build rich prompts (gherkin + step defs + history + context)
  6. Call LLM -> generate analysis
  7. Write markdown reports + script-fixes.json
"""

from __future__ import annotations

import asyncio
import re
import sys
from datetime import datetime
from pathlib import Path
from typing import Optional

from failure_analyzer.config import load_config
from failure_analyzer.parsers.cucumber_parser import parse_cucumber_json
from failure_analyzer.parsers.trace_parser import (
    enrich_failures_with_traces,
    extract_screenshot_base64,
    save_screenshot,
)
from failure_analyzer.parsers.feature_parser import get_gherkin_for_scenario
from failure_analyzer.grouping.failure_grouper import group_failures, extract_exception_type
from failure_analyzer.prompts.prompt_builder import (
    build_work_item_prompt,
    build_single_failure_prompt,
    build_maintenance_report,
)
from failure_analyzer.parsers.bug_report_parser import parse_single_failure_analysis
from failure_analyzer.analyzers.assertion_analyzer import build_deterministic_analyses
from failure_analyzer.llm.connector_factory import create_connector
from failure_analyzer.reporters.markdown_reporter import write_bug_report
from failure_analyzer.reporters.history_writer import (
    get_history_for_feature,
    get_scenario_run_pattern,
    append_to_history,
)
from failure_analyzer.fixers.script_fix_exporter import export_script_fixes

# A Background failure is only force-classified as intermittent/Script Issue when
# a MAJORITY of this feature's other Background executions succeeded this run.
# Below this rate (e.g. Creative's 5/30 = 17%), the step is failing far too often
# to call it a rare timing blip — leave the category to normal analysis instead
# of asserting "just add a wait".
_INTERMITTENT_PASS_RATE_THRESHOLD = 0.5


def clean_llm_output(raw: str) -> str:
    text = raw or ""
    text = re.sub(r"<think>[\s\S]*?</think>", "", text, flags=re.IGNORECASE)
    orphan_close = text.lower().rfind("</think>")
    if orphan_close != -1:
        text = text[orphan_close + len("</think>"):]
    else:
        orphan_open = text.lower().find("<think>")
        if orphan_open != -1:
            text = text[:orphan_open]
    text = re.sub(r"</?think>", "", text, flags=re.IGNORECASE)
    text = re.sub(r"^[\s\S]*?---\s*ANALYSIS STORY\s*---\s*", "", text, flags=re.IGNORECASE)
    return text.strip()


async def fetch_app_version(config) -> str | None:
    url = config.environment_info.get("versionUrl")
    if not url:
        return None
    try:
        import httpx

        async with httpx.AsyncClient(timeout=5.0) as client:
            resp = await client.get(url)
            if resp.status_code != 200:
                return None
            text = resp.text
            field = config.environment_info.get("versionField")
            if field:
                import json

                try:
                    return str(json.loads(text).get(field, "")).strip() or None
                except (json.JSONDecodeError, KeyError):
                    return None
            return text.strip()[:100] or None
    except Exception:
        return None


def collect_screenshot_for_failure(failure, config) -> list[str]:
    if not config.llm.send_screenshots:
        return []
    if not failure.trace_path or not failure.screenshot_name:
        return []
    b64 = extract_screenshot_base64(failure.trace_path, failure.screenshot_name)
    return [b64] if b64 else []


def partition_for_strategy(
    strategy: str,
    failures: list,
    deterministic: dict[str, dict],
) -> tuple[dict[str, dict], list]:
    """Decide, per ``analysis.assertionStrategy``, which failures need an LLM call.

    Returns ``(initial_analyses, llm_target_failures)``:

    - ``"deterministic"``: every failure is resolved from ``deterministic``;
      no failure is ever sent to the LLM.
    - ``"hybrid"``: ``initial_analyses`` starts pre-filled with ``deterministic``,
      so a failure whose LLM call errors or returns unusable output is left with
      its deterministic result untouched. Only failures whose deterministic
      ``cause`` is ``"unknown"`` (or that have no deterministic result at all)
      are LLM targets — ``analyze_failure()`` always supplies a ``category``,
      even for ``cause == "unknown"``, so confidence must be read from ``cause``,
      not ``category``.
    - ``"llm"`` (or any other value): unchanged — every failure is an LLM
      target and ``initial_analyses`` starts empty.
    """
    if strategy == "deterministic":
        return dict(deterministic), []
    if strategy == "hybrid":
        def _is_ambiguous(f) -> bool:
            det = deterministic.get(f.scenario_name)
            return det is None or det.get("cause") == "unknown"

        return dict(deterministic), [f for f in failures if _is_ambiguous(f)]
    return {}, list(failures)


def merge_llm_results(
    analyses: dict[str, dict], llm_results: list[tuple[str, Optional[dict]]]
) -> int:
    """Merge successful LLM results into ``analyses`` in place.

    An unusable result (``parsed is None``, from an LLM error or empty output)
    leaves whatever was already in ``analyses`` for that scenario untouched —
    the pre-filled deterministic fallback for ``hybrid``, or nothing for ``llm``.
    Returns the number of results actually merged.
    """
    ai_count = 0
    for scenario_name, parsed in llm_results:
        if parsed is not None:
            analyses[scenario_name] = parsed
            ai_count += 1
    return ai_count


async def analyze_failure_with_llm(failure, prompt: str, llm, config) -> tuple[str, Optional[dict]]:
    """Analyze one failure via the LLM, with a text-only pass first and a
    screenshot-escalation second pass only when needed.

    Pass 1 is always text-only (``images=[]``) regardless of
    ``config.llm.send_screenshots`` — a screenshot is never the first call.
    Pass 2 (screenshot attached) only happens when ALL of:
      - pass 1 parsed with ``confidence == "low"`` (an explicit signal from
        the model itself — never inferred from category/analysis presence).
      - ``config.llm.send_screenshots`` is enabled AND a screenshot actually
        exists for this failure (``collect_screenshot_for_failure`` already
        returns ``[]`` when the flag is off, so this is the single place both
        conditions are enforced).
    If pass 2 errors or returns unusable output, pass 1's result is kept —
    the escalation attempt never loses an already-usable answer.
    """
    try:
        raw = clean_llm_output(await llm.generate(prompt, images=[]))
        parsed = parse_single_failure_analysis(raw)
    except Exception as e:
        print(f'    LLM error for "{failure.scenario_name}": {e}')
        return failure.scenario_name, None

    if parsed.get("confidence") == "low":
        images = collect_screenshot_for_failure(failure, config)
        if images:
            try:
                raw2 = clean_llm_output(await llm.generate(prompt, images=images))
                parsed2 = parse_single_failure_analysis(raw2)
                if parsed2.get("analysis") or parsed2.get("fix"):
                    print(
                        f'    Escalated "{failure.scenario_name}" with screenshot '
                        f"(low text-only confidence)"
                    )
                    parsed = parsed2
            except Exception as e:
                print(
                    f'    Screenshot escalation failed for "{failure.scenario_name}": '
                    f"{e} — keeping text-only result"
                )

    if parsed.get("analysis") or parsed.get("fix"):
        return failure.scenario_name, parsed
    print(f'    No usable AI output for "{failure.scenario_name}"')
    return failure.scenario_name, None


async def run():
    print("\n======================================")
    print("  Failure Analyzer — Starting...")
    print("======================================\n")

    config = load_config()

    work_item_type = config.work_item.default_type.lower()
    is_aggregate = work_item_type == "maintenancestory"
    process_error_types = {e.lower() for e in config.work_item.error_types}

    print(f"  Work item type : {work_item_type}")
    print(f"  Error types    : {', '.join(process_error_types)}")
    print(f"  Mode           : {'aggregate' if is_aggregate else 'per-group'}\n")

    base_dir = Path(__file__).parent.parent
    cucumber_json_path = (base_dir / config.paths.cucumber_json).resolve()
    traces_dir = (base_dir / config.paths.traces_dir).resolve()
    output_dir = (base_dir / config.paths.output_dir).resolve()
    repo_root = base_dir.parent

    if not cucumber_json_path.exists():
        print(f"Error: cucumber.json not found at:\n  {cucumber_json_path}")
        print("Run your tests first (`mvn test`), then re-run this script.\n")
        sys.exit(1)

    # 1. Parse
    print("[1/5] Parsing test results...")
    failures, passed_scenarios, total_scenarios = parse_cucumber_json(
        str(cucumber_json_path)
    )
    if not failures:
        print("\n  No failures found — all tests passed!\n")
        return
    print(f"  {len(failures)} failure(s) out of {total_scenarios} scenario(s)\n")

    # Same-run reliability signal: each scenario gets its own independent
    # Background execution (see cucumber_parser), so we can measure exactly how
    # often THIS feature's Background succeeded elsewhere in this same run.
    # A Background execution "succeeded" whenever it wasn't itself the cause of
    # failure — that includes fully-passed scenarios AND scenarios that got past
    # the Background but failed later on their own step.
    bg_total_by_feature: dict[str, int] = {}
    bg_failed_by_feature: dict[str, int] = {}
    for p in passed_scenarios:
        ff = getattr(p, "feature_file", "") or ""
        bg_total_by_feature[ff] = bg_total_by_feature.get(ff, 0) + 1
    for f in failures:
        ff = f.feature_file or ""
        bg_total_by_feature[ff] = bg_total_by_feature.get(ff, 0) + 1
        if f.is_background_failure:
            bg_failed_by_feature[ff] = bg_failed_by_feature.get(ff, 0) + 1
    for f in failures:
        if not f.is_background_failure:
            continue
        ff = f.feature_file or ""
        total_bg = bg_total_by_feature.get(ff, 0)
        failed_bg = bg_failed_by_feature.get(ff, 0)
        passed_bg = total_bg - failed_bg
        f.background_reliability = {
            "passed": passed_bg,
            "total": total_bg,
            "rate": (passed_bg / total_bg) if total_bg else 0.0,
        }

    # 2. Trace enrichment
    print("[2/5] Reading Playwright traces...")
    enrich_failures_with_traces(
        failures,
        str(traces_dir),
        config.traces.min_match_score,
        config.traces.correlation_buffer_ms,
    )
    with_traces = sum(1 for f in failures if f.trace_path)
    with_url = sum(1 for f in failures if f.page_url)
    with_dom = sum(1 for f in failures if f.page_text)
    print(f"  Trace data attached to {with_traces}/{len(failures)} failure(s)")
    if with_url > 0:
        print(f"  Page URL extracted for {with_url}/{len(failures)} failure(s)")
    if with_dom > 0:
        print(f"  Page DOM text extracted for {with_dom}/{len(failures)} failure(s)")

    screenshots_dir = output_dir / "screenshots"
    saved_shots = 0
    for f in failures:
        if not f.trace_path or not f.screenshot_name:
            continue
        slug = re.sub(r"[^a-z0-9]+", "-", f.scenario_name.lower())[:80].strip("-")
        file_name = f"{slug}.jpeg"
        if save_screenshot(
            f.trace_path, f.screenshot_name, str(screenshots_dir / file_name)
        ):
            f.screenshot_file = f"screenshots/{file_name}"
            f.screenshot_abs_path = str(screenshots_dir / file_name)
            saved_shots += 1
    if saved_shots > 0:
        print(
            f"  Screenshots extracted for {saved_shots}/{len(failures)} failure(s)"
        )
    print()

    # 3. Group
    print("[3/5] Grouping failures...")
    all_groups = group_failures(failures)
    target_groups = [
        g for g in all_groups if g.root_cause_label.lower() in process_error_types
    ]
    skipped_groups = [
        g for g in all_groups if g.root_cause_label.lower() not in process_error_types
    ]

    print(f"  {len(failures)} failure(s) -> {len(all_groups)} group(s):")
    for g in all_groups:
        tag = (
            "[process]"
            if g.root_cause_label.lower() in process_error_types
            else "[skip]"
        )
        print(
            f"    * {g.root_cause_label:<25} -> {len(g.failures)} scenario(s) {tag}"
        )
    if skipped_groups:
        print(
            f"\n  Skipping {len(skipped_groups)} group(s) — not in workItem.errorTypes config"
        )
    print()

    if not target_groups:
        print("  No processable failures found — nothing to generate.\n")
        return

    # 4. LLM setup
    print("[4/5] Setting up LLM and context...")
    llm = create_connector(config.llm)
    print()

    # 5. Build context
    print("[5/5] Generating work item(s)...\n")
    gherkin_by_scenario: dict[str, str] = {}
    history_by_feature: dict[str, str] = {}
    frequency_by_scenario: dict[str, dict] = {}

    for group in target_groups:
        for failure in group.failures:
            if failure.scenario_name not in gherkin_by_scenario:
                try:
                    feature_path = repo_root / (failure.feature_file or "").lstrip(
                        "file:"
                    )
                    if feature_path.exists():
                        gherkin_by_scenario[failure.scenario_name] = (
                            get_gherkin_for_scenario(
                                str(feature_path), failure.scenario_name
                            )
                        )
                except Exception:
                    pass

            feature_key = Path(failure.feature_file or "").stem
            if feature_key not in history_by_feature:
                history_by_feature[feature_key] = get_history_for_feature(
                    failure.feature_file or ""
                )
            if failure.scenario_name not in frequency_by_scenario:
                frequency_by_scenario[failure.scenario_name] = get_scenario_run_pattern(
                    failure.feature_file or "",
                    failure.scenario_name,
                    current_error_type=extract_exception_type(failure.error_message),
                    current_failed_step=failure.failed_step,
                )

    app_version = await fetch_app_version(config)
    if app_version:
        print(f"  App version: {app_version}")

    run_meta = {
        "totalScenarios": total_scenarios,
        "failedCount": len(failures),
        "appVersion": app_version,
    }

    results = []

    if is_aggregate:
        total_failures = sum(len(g.failures) for g in target_groups)
        strategy = config.analysis.assertion_strategy.lower()
        print(f"  Analysis strategy: {strategy}")

        deterministic = build_deterministic_analyses(
            target_groups, frequency_by_scenario
        )
        all_target_failures = [f for g in target_groups for f in g.failures]
        analyses, llm_target_failures = partition_for_strategy(
            strategy, all_target_failures, deterministic
        )

        if strategy == "deterministic":
            print(
                f"  Deterministic analyses generated: {len(analyses)} scenario(s) (no LLM call)"
            )
        else:
            if strategy == "hybrid":
                resolved_count = len(all_target_failures) - len(llm_target_failures)
                if resolved_count:
                    print(
                        f"  Hybrid: {resolved_count}/{total_failures} failure(s) "
                        f"resolved deterministically (no LLM call)"
                    )

            if llm_target_failures:
                concurrency = max(1, config.llm.concurrency)
                print(
                    f"  Calling LLM per failure ({len(llm_target_failures)} call(s), "
                    f"up to {concurrency} concurrent)..."
                )
                semaphore = asyncio.Semaphore(concurrency)

                async def _analyze_one(failure):
                    frequency = frequency_by_scenario.get(failure.scenario_name, {})
                    gherkin_text = gherkin_by_scenario.get(failure.scenario_name, "")
                    history_text = history_by_feature.get(
                        Path(failure.feature_file or "").stem, ""
                    )
                    prompt = build_single_failure_prompt(
                        failure, frequency, gherkin_text, history_text
                    )
                    async with semaphore:
                        return await analyze_failure_with_llm(failure, prompt, llm, config)

                llm_results = await asyncio.gather(
                    *(_analyze_one(f) for f in llm_target_failures)
                )

                ai_count = merge_llm_results(analyses, llm_results)

                print(f"  AI analyses generated: {ai_count}/{len(llm_target_failures)}")
                if strategy == "hybrid":
                    fallback_count = len(llm_target_failures) - ai_count
                    if fallback_count:
                        print(
                            f"  Hybrid: {fallback_count} ambiguous failure(s) fell back "
                            f"to deterministic analysis (LLM error/no usable output)"
                        )
            else:
                print("  No LLM calls needed — every failure resolved deterministically")

        all_failures = all_target_failures
        for failure in all_failures:
            ai = analyses.get(failure.scenario_name)
            # Same-run intermittency override: only force a Background failure to
            # Script Issue when a MAJORITY of this feature's other Background runs
            # succeeded this run — that's decisive evidence of a flaky timing race.
            # A minority pass rate (e.g. 17%) is a frequently-reproducing problem,
            # not a rare blip, so leave the category to normal analysis instead.
            reliability = getattr(failure, "background_reliability", None)
            if (
                failure.is_background_failure
                and reliability
                and reliability["rate"] >= _INTERMITTENT_PASS_RATE_THRESHOLD
            ):
                if ai is None:
                    ai = {}
                    analyses[failure.scenario_name] = ai
                ai["category"] = "Script Issue"
                ai["cause"] = "same-run-intermittent"
            if ai and ai.get("category"):
                failure.issue_category = ai["category"]

        # Persist this run to history (with LLM analysis + fix included)
        append_to_history(all_failures, passed_scenarios, analyses)

        report_text = build_maintenance_report(
            target_groups, frequency_by_scenario, config, run_meta, analyses
        )
        output_dir.mkdir(parents=True, exist_ok=True)
        run_date = datetime.now().strftime("%Y-%m-%d")
        run_time = datetime.now().strftime("%H-%M")
        story_path = output_dir / f"maintenance-story-{run_date}_{run_time}.md"
        story_path.write_text(report_text)
        print(f"  Story saved: {story_path}")

        script_fix_path = export_script_fixes(
            target_groups, analyses, frequency_by_scenario, config
        )
        if script_fix_path:
            import json

            script_fix_data = json.loads(Path(script_fix_path).read_text())
            print(f"  Script fixes: {script_fix_path}")
            s = script_fix_data["summary"]
            print(
                f"    {s['expectedValueUpdates']} expected-value update(s), "
                f"{s['locatorUpdates']} locator update(s), "
                f"{s['manualReview']} manual-review"
            )
            print("    Run /fix-scripts in Claude Code to apply fixes.\n")

        results.append(
            {
                "label": "Maintenance Story",
                "count": sum(len(g.failures) for g in target_groups),
                "path": str(story_path),
            }
        )
    else:
        prompts = build_work_item_prompt(
            target_groups,
            work_item_type,
            gherkin_by_scenario,
            history_by_feature,
            frequency_by_scenario,
        )
        for i, group in enumerate(target_groups):
            print(
                f'  -> Group: "{group.root_cause_label}" ({len(group.failures)} scenario(s))'
            )
            try:
                print("    Calling LLM...")
                report_text = await llm.generate(prompts[i])
                print("    Response received.")
            except Exception as e:
                print(f"    LLM error: {e}")
                report_text = f"_AI generation failed: {e}_"
            report_path = write_bug_report(report_text, group, str(output_dir))
            print(f"    Report saved: {report_path}")
            results.append(
                {
                    "label": group.root_cause_label,
                    "count": len(group.failures),
                    "path": report_path,
                }
            )
            print()

    # Summary
    print("======================================")
    print("  Summary")
    print("======================================")
    print(f"{'Group / Item':<28} | {'Scen.':<5} | File")
    print("-" * 80)
    for r in results:
        print(f"{r['label']:<28} | {str(r['count']):<5} | {r['path']}")
    print("======================================\n")


def main():
    asyncio.run(run())


if __name__ == "__main__":
    main()
