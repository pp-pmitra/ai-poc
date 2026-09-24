#!/usr/bin/env python3
"""Build compact live-replay worker packets from failures-for-replay.json.

The live analyzer is most reliable when each failure group is diagnosed in a
fresh Claude CLI session with only the context needed for that group. This
script is mechanical: it does not classify failures and does not call an LLM
unless --write-runner is used and the generated shell script is run later.
"""

from __future__ import annotations

import argparse
import json
import shlex
from datetime import datetime
from pathlib import Path
from typing import Any

from failure_analyzer.packet_utils import (
    all_likely_intermittent,
    code_context_for_failure,
    redact_text,
    safe_slug,
    select_representative,
    truncate,
)

_ANALYZER_DIR = Path(__file__).resolve().parent
_HISTORY_DIR = _ANALYZER_DIR.parents[3] / "kavach-data" / "history"
_REPO_ROOT = _ANALYZER_DIR.parent

DEFAULT_INPUT = _HISTORY_DIR / "failures-for-replay.json"
DEFAULT_OUT_ROOT = _HISTORY_DIR / "replay-packets"
DEFAULT_PAGE_TEXT_CAP = 1200

__all__ = [
    "redact_text",
    "truncate",
    "safe_slug",
    "code_context_for_failure",
    "build_packet",
    "build_prompt",
    "group_failures",
]


RECEIPT_SCHEMA = {
    "groupId": "string",
    "representativeScenario": "string",
    "affectedScenarios": ["string"],
    "distinctIssueCount": 1,
    "verdict": (
        "script_issue_fix_proposed | confirmed_product_bug | suspected_product_bug | "
        "not_reproduced_intermittent | not_reproduced_passed_live | needs_investigation"
    ),
    "confidence": "high | medium | low",
    "backgroundMatched": True,
    "liveReplayPerformed": True,
    "alternateValidPathFound": False,
    "productBugGate": {
        "userLevelBehaviorReproduced": True,
        "targetAffordanceMissingOrBroken": True,
        "domStructureChecked": True,
        "staleLocatorRuledOut": True,
        "testDataOrEnvironmentRuledOut": True,
    },
    "productBugArtifacts": (
        "REQUIRED for confirmed_product_bug (validate_replay_receipts.py downgrades to suspected_product_bug "
        "without these): {domStructureChecked: '<the actual outerHTML/DOM excerpt you observed via "
        "evaluate(el => el.outerHTML), not a boolean restated as text>', staleLocatorRuledOut: '<the actual "
        "count()/evaluate() output from the corrected locator attempt you tried>'} — both must be the real "
        "observed text, at least ~20 chars, not a placeholder like 'true'/'verified'/'checked'."
    ),
    "evidence": ["2-5 concrete observed facts"],
    "recommendedAction": "short action",
}


def compact_failure(failure: dict[str, Any], repo_root: Path, page_text_cap: int) -> dict[str, Any]:
    keep = {
        "scenarioName": failure.get("scenarioName"),
        "featureFile": failure.get("featureFile"),
        "tags": failure.get("tags") or [],
        "failedStep": failure.get("failedStep"),
        "errorMessage": truncate(failure.get("errorMessage"), 900),
        "expectedValue": failure.get("expectedValue"),
        "actualValue": failure.get("actualValue"),
        "errorLocation": failure.get("errorLocation"),
        "playwrightCallLog": truncate(failure.get("playwrightCallLog"), 900),
        "isBackgroundFailure": failure.get("isBackgroundFailure", False),
        "backgroundReliability": failure.get("backgroundReliability"),
        "stepReliability": failure.get("stepReliability"),
        "groupId": failure.get("groupId"),
        "tracePath": failure.get("tracePath"),
        "lastBrowserAction": failure.get("lastBrowserAction"),
        "pageUrl": failure.get("pageUrl"),
        "pageText": truncate(failure.get("pageText"), page_text_cap),
        "screenshotFile": failure.get("screenshotFile"),
    }
    keep["codeContext"] = code_context_for_failure(repo_root, failure)
    return redact_text(keep)


def group_failures(data: dict[str, Any]) -> list[dict[str, Any]]:
    by_id: dict[str, list[dict[str, Any]]] = {}
    for failure in data.get("failures", []):
        group_id = failure.get("groupId") or failure.get("scenarioName") or "ungrouped"
        by_id.setdefault(group_id, []).append(failure)

    group_meta = {g.get("groupId"): g for g in data.get("groups", []) if g.get("groupId")}
    grouped: list[dict[str, Any]] = []
    for idx, (group_id, failures) in enumerate(by_id.items(), 1):
        meta = group_meta.get(group_id, {})
        grouped.append({
            "index": idx,
            "groupId": group_id,
            "rootCauseLabel": meta.get("rootCauseLabel"),
            "normalizedStep": meta.get("normalizedStep"),
            "affectedFeatures": meta.get("affectedFeatures") or sorted({f.get("featureFile") for f in failures if f.get("featureFile")}),
            "failures": failures,
        })
    return grouped


def build_packet(group: dict[str, Any], repo_root: Path, page_text_cap: int) -> dict[str, Any]:
    failures = group["failures"]
    representative = select_representative(failures)
    all_intermit = all_likely_intermittent(failures)
    packet = {
        "groupId": group["groupId"],
        "rootCauseLabel": group.get("rootCauseLabel"),
        "normalizedStep": group.get("normalizedStep"),
        "affectedFeatures": group.get("affectedFeatures") or [],
        "representativeScenario": representative.get("scenarioName"),
        "allFailuresLikelyIntermittent": all_intermit,
        "productBugGate": [
            "Faithfully replay Background and scenario/example steps unless all failures are mechanically intermittent.",
            "Before confirmed_product_bug, prove user-level behavior still fails live.",
            "Check DOM structure around the target/broader match; count-only evidence is not enough.",
            "Rule out stale locator, test data, environment, and a valid alternate UI path.",
            "Report affected scenario count separately from distinct issue count.",
        ],
        "failures": [compact_failure(f, repo_root, page_text_cap) for f in failures],
        "receiptSchema": RECEIPT_SCHEMA,
    }
    return redact_text(packet)


def build_prompt(packet: dict[str, Any]) -> str:
    return "\n".join([
        "You are a fresh live-replay worker for exactly one failure group.",
        "Use only this packet plus live replay evidence you collect yourself.",
        "Do not use prior verdict reports, old conclusions, or unrelated failures.",
        "",
        "Rules:",
        "- If allFailuresLikelyIntermittent is true, do not spend a browser replay; return not_reproduced_intermittent with the reliability evidence.",
        "- Otherwise faithfully replay the feature Background and representative scenario/example row before judging the failed step.",
        "- For confirmed_product_bug, every productBugGate item must be satisfied. If any gate item is not proven, downgrade to suspected_product_bug or needs_investigation.",
        "- If a corrected locator/action works live, classify script_issue_fix_proposed, not product bug.",
        "- Keep evidence concrete: locator counts, visible text, URL/state changes, DOM snippets, or API/status observations.",
        "- Return JSON only. No markdown, no prose outside JSON.",
        "",
        "Receipt schema:",
        json.dumps(RECEIPT_SCHEMA, indent=2, sort_keys=True),
        "",
        "Worker packet:",
        json.dumps(packet, indent=2, sort_keys=True),
    ])


def write_runner(out_dir: Path, prompt_paths: list[Path], claude_args: list[str]) -> Path:
    receipts_dir = out_dir / "receipts"
    lines = [
        "#!/usr/bin/env bash",
        "set -u",
        f"mkdir -p {shlex.quote(str(receipts_dir))}",
        "",
    ]
    quoted_args = " ".join(shlex.quote(arg) for arg in claude_args)
    for prompt in prompt_paths:
        receipt = receipts_dir / (prompt.stem.replace(".prompt", "") + ".receipt.json")
        lines.extend([
            f"echo '==> {prompt.name}'",
            (
                f"if ! claude -p --output-format text {quoted_args} "
                f"< {shlex.quote(str(prompt))} > {shlex.quote(str(receipt))}; then"
            ),
            f"  echo '{{\"verdict\":\"needs_investigation\",\"confidence\":\"low\",\"evidence\":[\"worker failed\"]}}' > {shlex.quote(str(receipt))}",
            "fi",
            "",
        ])
    path = out_dir / "run-workers.sh"
    path.write_text("\n".join(lines), encoding="utf-8")
    path.chmod(0o755)
    return path


def build_packets(
    input_path: Path,
    out_root: Path,
    repo_root: Path,
    page_text_cap: int,
    write_runner_script: bool,
    claude_args: list[str],
    only_group_ids: set[str] | None = None,
) -> Path:
    data = json.loads(input_path.read_text(encoding="utf-8"))
    timestamp = datetime.now().strftime("%Y-%m-%d-%H%M%S")
    out_dir = out_root / timestamp
    out_dir.mkdir(parents=True, exist_ok=True)

    groups = group_failures(data)
    if only_group_ids is not None:
        groups = [g for g in groups if g["groupId"] in only_group_ids]

    prompt_paths: list[Path] = []
    manifest_groups: list[dict[str, Any]] = []
    for group in groups:
        packet = build_packet(group, repo_root, page_text_cap)
        prefix = f"group-{group['index']:03d}-{safe_slug(group['groupId'], 'group')}"
        packet_path = out_dir / f"{prefix}.json"
        prompt_path = out_dir / f"{prefix}.prompt.md"
        packet_path.write_text(json.dumps(packet, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        prompt_path.write_text(build_prompt(packet) + "\n", encoding="utf-8")
        prompt_paths.append(prompt_path)
        manifest_groups.append({
            "groupId": group["groupId"],
            "packet": str(packet_path),
            "prompt": str(prompt_path),
            "scenarioCount": len(group["failures"]),
            "representativeScenario": packet["representativeScenario"],
            "allFailuresLikelyIntermittent": packet["allFailuresLikelyIntermittent"],
        })

    manifest = {
        "generatedAt": timestamp,
        "source": str(input_path),
        # Scoped to what THIS manifest actually covers, not the full input
        # file's total — matches groupCount, and stays correct whether or not
        # --only-groups filtered the run down to an escalated subset.
        "totalFailures": sum(g["scenarioCount"] for g in manifest_groups),
        "groupCount": len(manifest_groups),
        "groups": manifest_groups,
    }
    if write_runner_script:
        runner = write_runner(out_dir, prompt_paths, claude_args)
        manifest["runner"] = str(runner)
    (out_dir / "manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return out_dir


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-i", "--input", type=Path, default=DEFAULT_INPUT)
    parser.add_argument("-o", "--out-root", type=Path, default=DEFAULT_OUT_ROOT)
    parser.add_argument("--repo-root", type=Path, default=_REPO_ROOT)
    parser.add_argument("--page-text-cap", type=int, default=DEFAULT_PAGE_TEXT_CAP)
    parser.add_argument("--write-runner", action="store_true", help="also write run-workers.sh")
    parser.add_argument(
        "--claude-arg",
        action="append",
        default=[],
        help="extra arg to put after claude -p --output-format text in run-workers.sh; repeat for each arg",
    )
    parser.add_argument(
        "--only-groups",
        type=Path,
        default=None,
        help=(
            "path to a JSON file containing a list of groupIds (or {\"groupIds\": [...]}); "
            "when given, only these groups get packets/prompts built — used to send only "
            "triage-escalated groups into live replay instead of every non-intermittent group"
        ),
    )
    return parser.parse_args()


def load_only_group_ids(path: Path | None) -> set[str] | None:
    if path is None:
        return None
    data = json.loads(path.read_text(encoding="utf-8"))
    ids = data.get("groupIds") if isinstance(data, dict) else data
    return set(ids or [])


def main() -> None:
    args = parse_args()
    out_dir = build_packets(
        input_path=args.input,
        out_root=args.out_root,
        repo_root=args.repo_root.resolve(),
        page_text_cap=max(200, args.page_text_cap),
        write_runner_script=args.write_runner,
        claude_args=args.claude_arg,
        only_group_ids=load_only_group_ids(args.only_groups),
    )
    print(f"Wrote replay worker packets: {out_dir}")


if __name__ == "__main__":
    main()
