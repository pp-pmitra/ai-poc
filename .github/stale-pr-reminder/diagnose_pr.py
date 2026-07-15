"""
Diagnostic script: calls the REAL functions from stale_pr_reminder.py against a live PR.

Usage:
    GITHUB_TOKEN=ghp_xxx GITHUB_REPOSITORY=owner/repo PR_NUMBER=4 python3 diagnose_pr.py
"""

from __future__ import annotations
import os
from github import Github

from stale_pr_reminder import (
    get_unresolved_review_threads,
    get_issue_commenters,
    get_review_feedback,
    get_discussion_comments,
    determine_status,
    get_status_age_days,
    get_last_reminder_days,
    STATUS_THRESHOLDS,
)

TOKEN  = os.environ["GITHUB_TOKEN"]
REPO   = os.environ["GITHUB_REPOSITORY"]
PR_NUM = int(os.environ["PR_NUMBER"])

g    = Github(TOKEN)
repo = g.get_repo(REPO)
pr   = repo.get_pull(PR_NUM)

print(f"\n{'='*60}")
print(f"PR #{PR_NUM}: {pr.title}")
print(f"Author : {pr.user.login}")
print(f"Draft  : {pr.draft}")
print(f"{'='*60}\n")

# Raw reviews (for visibility)
print("RAW REVIEWS (chronological):")
print(f"{'User':25} {'Type':6} {'State':25} {'Submitted'}")
print("-"*80)
for r in pr.get_reviews():
    utype  = r.user.type  if r.user else "N/A"
    ulogin = r.user.login if r.user else "N/A"
    print(f"{ulogin:25} {utype:6} {r.state:25} {r.submitted_at}")
print()

# Run actual pipeline
unresolved_reviewers, summary_lines, thread_reviewers, thread_timestamps, review_decision = (
    get_unresolved_review_threads(REPO, PR_NUM)
)
issue_commenters = get_issue_commenters(pr)

print(f"GitHub reviewDecision : {review_decision!r}")
print(f"unresolved_reviewers  : {unresolved_reviewers}")
print(f"thread_timestamps     : {thread_timestamps}")
print(f"issue_commenters      : {issue_commenters}")
print()

status, responsible, approved_by = determine_status(
    pr, unresolved_reviewers, issue_commenters, review_decision
)

review_feedback     = get_review_feedback(pr, approved_by)
discussion_comments = get_discussion_comments(pr)

approved_set = set(approved_by)
active_lines = [
    line for line, reviewer in zip(summary_lines, thread_reviewers)
    if reviewer not in approved_set
]
active_timestamps = [
    ts for ts, reviewer in zip(thread_timestamps, thread_reviewers)
    if reviewer not in approved_set
]

print(f"review_feedback       : {len(review_feedback)} items")
print(f"discussion_comments   : {len(discussion_comments)} items")
print(f"active_thread_lines   : {len(active_lines)} (of {len(summary_lines)} total unresolved)")

REQUIRED_APPROVALS = int(os.getenv("REQUIRED_APPROVALS", "2"))
if status == "Comments Received":
    feedback_count = len(review_feedback) + len(discussion_comments)
    threshold_days = 3 if feedback_count <= 3 else 5
else:
    threshold_days = STATUS_THRESHOLDS[status]

status_age_days    = get_status_age_days(status, pr, active_timestamps)
last_reminder_days = get_last_reminder_days(pr)

print(f"STATUS               : >>> {status} <<<")
print(f"Responsible          : {responsible}")
print(f"Approved by          : {approved_by}")
print(f"Threshold (days)     : {threshold_days:.4f}  (~{threshold_days*1440:.1f} min)")
print(f"Status age (days)    : {status_age_days:.4f}  (~{status_age_days*1440:.1f} min)")
print(f"Last reminder (days) : {last_reminder_days}")
print()

will_post = (
    status_age_days >= threshold_days
    and (last_reminder_days is None or last_reminder_days >= threshold_days)
)
print(f"WILL BOT POST?       : {'YES ✓' if will_post else 'NO (skipped)'}")
if not will_post:
    if status_age_days < threshold_days:
        print(f"  → Status too fresh: {status_age_days*1440:.1f} min < {threshold_days*1440:.1f} min threshold")
    elif last_reminder_days is not None and last_reminder_days < threshold_days:
        print(f"  → Reminded too recently: {last_reminder_days*1440:.1f} min ago < {threshold_days*1440:.1f} min threshold")