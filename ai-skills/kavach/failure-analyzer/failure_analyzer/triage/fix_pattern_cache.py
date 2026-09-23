"""Fix-pattern cache: skip re-diagnosing a failure entirely when a prior run
already proposed (and this run can verify is still applied) a concrete fix
for the exact same scenario or failure group.

Reads `fix-history.json` directly rather than `.claude/fix-patterns/*.md` —
the per-feature markdown files are a separate, currently-empty knowledge
layer; the JSON history is intact and has everything this cache needs.

Product-bug history entries are never a skip trigger — only a genuine,
still-present fix diff is, per `.claude/commands/analyze-failure.md`'s
existing rule that historical findings are hints, not evidence.
"""

from __future__ import annotations

from pathlib import Path
from typing import Any, Optional

from failure_analyzer.packet_utils import text_present_near_line

__all__ = ["find_cached_fix", "verify_fix_still_present"]

_FIX_VERDICTS = {"Script Issue — Fix Proposed", "Script Issue — Fix Applied"}


def find_cached_fix(
    scenario_name: str,
    group_id: Optional[str],
    fix_history: list[dict[str, Any]],
) -> Optional[dict[str, Any]]:
    """Most recent fix-history entry for this scenario (or, failing that, this
    exact groupId) with a fix verdict and a concrete `change`. Returns None if
    no such entry exists — this function alone does not mean "skip"; the
    caller must still call `verify_fix_still_present` before trusting it.
    """
    candidates = [
        h for h in fix_history
        if h.get("scenarioName") == scenario_name
        and h.get("verdict") in _FIX_VERDICTS
        and h.get("change")
    ]
    if not candidates and group_id:
        candidates = [
            h for h in fix_history
            if h.get("groupId") == group_id
            and h.get("verdict") in _FIX_VERDICTS
            and h.get("change")
        ]
    if not candidates and group_id:
        # Fallback: old history entries may use a coarser groupId (just
        # exception_type|normalized_step) without the newer optional dimensions
        # (locator=, route=, etc.).  Match when the current groupId starts with
        # the historical one or vice-versa.
        backbone = group_id.split("|")[:2]  # exception_type|normalized_step
        prefix = "|".join(backbone)
        candidates = [
            h for h in fix_history
            if h.get("groupId")
            and (h["groupId"].startswith(prefix) or group_id.startswith(h["groupId"]))
            and h.get("verdict") in _FIX_VERDICTS
            and h.get("change")
        ]
    if not candidates:
        return None
    return max(candidates, key=lambda h: h.get("timestamp") or "")


def verify_fix_still_present(change: dict[str, Any], repo_root: Path) -> bool:
    """Is `change["after"]` still present in the source file, somewhere near
    the recorded line (tolerant of drift from unrelated edits since)? A prior
    "applied"/"proposed" status field is not trusted on its own — this
    re-checks the actual file on disk.
    """
    return text_present_near_line(repo_root, change.get("file"), change.get("line"), change.get("after"))
