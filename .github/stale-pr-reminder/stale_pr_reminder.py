"""
Stale PR Reminder Bot - V3 (GitHub reviewDecision + per-status thresholds)
"""

from __future__ import annotations

import os
from datetime import datetime, timezone
from typing import List, Optional, Tuple

import requests
from github import Github
from github.PullRequest import PullRequest

# ============================================================================
# CONFIGURATION
# ============================================================================
# This section defines the tunable behavior of the bot: how many days a PR
# must sit in a given status before we nag about it, how many approvals are
# required before a PR is considered mergeable, who the designated "merger"
# is, and what action text is shown to whoever is responsible for the PR at
# each status. Everything below is data, not logic — the actual decision
# tree that assigns a status to a PR lives further down in determine_status().

# Number of days a PR must remain in each status before the bot will post a
# reminder comment about it. Each status is keyed by the exact string that
# determine_status() returns for that state.
STATUS_THRESHOLDS = {
    "No Reviewers Assigned":         3,
    "Waiting For Review":            2,
    "Changes Requested":             5,
    "Review Discussion In Progress": 1,
    "Awaiting Additional Approval":  1,
    "Ready To Merge":                1,
}
# "Comments Received" threshold is feedback-count dependent:
#   <= 3 items → 3 days
#    > 3 items → 5 days

# Minimum number of distinct approving reviewers before a PR is treated as
# ready to merge. Can be overridden per-repo via the REQUIRED_APPROVALS env
# var; defaults to 2 if not set.
REQUIRED_APPROVALS = int(os.getenv("REQUIRED_APPROVALS", "2"))

# GitHub login of the person who should be pinged to actually click "Merge"
# once a PR is fully approved. Overridable via the MERGER env var.
MERGER = os.getenv("MERGER", "pp-sdeodhar")

# Per-status guidance shown in the reminder comment. Each entry is the
# human-readable "Next Steps" text inserted into the comment body for PRs
# currently in that status, telling the responsible party what to do next.
STATUS_ACTIONS = {
    "Ready To Merge": "All required approvals are in. Please merge when ready.",
    "No Reviewers Assigned": "Please assign reviewers to get this moving.",
    "Waiting For Review": "Please review this PR.",
    "Changes Requested": "Please address the unresolved review threads.",
    "Review Discussion In Progress": (
        "**Author:** Please address any outstanding review feedback "
        "if any remains unresolved.\n\n"
        "**Reviewers:** Please review any updates that may have been "
        "provided. If satisfied, please approve the PR. Otherwise, "
        "request additional changes."
    ),
    "Comments Received": (
        "**Author:** Please review and address comments if needed.\n\n"
        "**Reviewers:** Please approve if satisfied or provide "
        "additional feedback."
    ),
    "Awaiting Additional Approval": (
        "One or more additional approvals are still needed."
    ),
}


# ============================================================================
# TIME / AGE HELPERS
# ============================================================================
# Small utility functions for computing how old a PR is and how long it's
# been since a real (non-bot) human touched it. These are used both for
# display in the reminder comment and, in some cases, as fallbacks when
# computing how long a PR has sat in its current status.

def get_now() -> datetime:
    # Centralized "current time" so every age calculation in this file uses
    # the exact same timestamp during a single run.
    return datetime.now(timezone.utc)


def get_pr_age_minutes(pr: PullRequest) -> float:
    # How long ago the PR was originally opened, in minutes. Used purely for
    # the "PR Age" line in the reminder comment (not for status timing logic).
    return (get_now() - pr.created_at).total_seconds() / 60


def get_last_human_activity_days(pr: PullRequest) -> float:
    """Return days since the last non-bot review or comment, falling back to PR creation."""
    now = get_now()
    latest = pr.created_at

    for review in pr.get_reviews():
        if not review.user or review.user.type == "Bot":
            continue
        if review.submitted_at > latest:
            latest = review.submitted_at

    for comment in pr.get_issue_comments():
        if not comment.user or comment.user.type == "Bot":
            continue
        if comment.created_at > latest:
            latest = comment.created_at

    return (now - latest).total_seconds() / 86400


# ============================================================================
# REVIEWER / COMMENTER LOOKUPS
# ============================================================================
# Functions that pull the raw lists of people involved with a PR — who's
# been requested as a reviewer, and who has left issue comments. These lists
# feed into determine_status() to figure out who is currently "responsible"
# for moving the PR forward.

def get_requested_reviewers(pr: PullRequest) -> List[str]:
    # GitHub logins of reviewers who have been requested but haven't yet
    # submitted a review (i.e. still sitting in the "requested" state).
    # Bots are excluded since they can't meaningfully "take action".
    try:
        users, _teams = pr.get_review_requests()
        return [user.login for user in users if user.type != "Bot"]
    except Exception as exc:
        print(f"Unable to fetch reviewers for PR #{pr.number}: {exc}")
        return []


def get_issue_commenters(pr: PullRequest) -> List[str]:
    # Distinct, non-bot logins that have left a top-level issue comment
    # (as opposed to an inline code review comment) on the PR.
    commenters = set()
    for comment in pr.get_issue_comments():
        if comment.user and comment.user.type != "Bot":
            commenters.add(comment.user.login)
    return list(commenters)


# ============================================================================
# REVIEW HISTORY ANALYSIS
# ============================================================================
# This section turns the PR's raw, chronological review history (which can
# contain multiple reviews per person, e.g. "commented" then later
# "approved") into a clean, deduplicated picture: who currently approves,
# who currently requests changes, who only left comments, and what the
# substance of that feedback was.

def analyze_reviews(
    pr: PullRequest,
) -> Tuple[List[str], List[str], List[str], int]:
    """Reduce a PR's review history to effective per-user verdicts.

    A COMMENTED review never overrides a standing APPROVED / CHANGES_REQUESTED
    verdict (this matches GitHub), so an approve-then-comment still counts as an
    approval. Returns (approved_by, changes_requested_by, commented_by,
    review_count).
    """
    decision = {}      # login -> APPROVED / CHANGES_REQUESTED / DISMISSED
    commented = set()  # logins that left a COMMENTED review
    seen = set()       # any login that submitted any review

    for review in pr.get_reviews():
        if not review.user or review.user.type == "Bot":
            continue
        login = review.user.login
        seen.add(login)
        if review.state in ("APPROVED", "CHANGES_REQUESTED", "DISMISSED"):
            decision[login] = review.state
        elif review.state == "COMMENTED":
            commented.add(login)

    approved_by = [u for u, s in decision.items() if s == "APPROVED"]
    changes_requested_by = [
        u for u, s in decision.items() if s == "CHANGES_REQUESTED"
    ]
    commented_by = [
        u
        for u in commented
        if decision.get(u) not in ("APPROVED", "CHANGES_REQUESTED")
    ]
    return approved_by, changes_requested_by, commented_by, len(seen)


def get_review_feedback(pr: PullRequest, approved_by: List[str]) -> List[str]:
    # Collects the actual written feedback text from reviews left by people
    # who have NOT approved the PR (their concerns are presumably still
    # outstanding). Only CHANGES_REQUESTED and COMMENTED reviews with a
    # non-empty body are included; this text is surfaced verbatim in the
    # "Review Feedback" section of the reminder comment.
    approved = set(approved_by)
    feedback = []
    for review in pr.get_reviews():
        if not review.user or review.user.type == "Bot":
            continue
        if review.user.login in approved:
            continue
        body = (review.body or "").strip()
        if not body:
            continue
        if review.state in ("CHANGES_REQUESTED", "COMMENTED"):
            feedback.append(
                f"- @{review.user.login} [{review.state}]\n  {body}"
            )
    return feedback


def get_discussion_comments(pr: PullRequest) -> List[str]:
    # Collects the text of top-level (non-bot) issue comments on the PR, for
    # display in the "Discussion Comments" section of the reminder. Unlike
    # get_review_feedback, this is not filtered by approval status — it's
    # general conversation, not a formal review verdict.
    comments = []
    for comment in pr.get_issue_comments():
        if not comment.user or comment.user.type == "Bot":
            continue
        body = (comment.body or "").strip()
        if not body:
            continue
        comments.append(f"- @{comment.user.login}\n  {body}")
    return comments


# ============================================================================
# UNRESOLVED CODE REVIEW THREADS (GitHub GraphQL API)
# ============================================================================
# GitHub's REST API (used everywhere else in this file via PyGithub) does
# not expose whether an inline review comment thread has been "resolved" or
# GitHub's official reviewDecision verdict for the PR. Both of those are
# only available through the GraphQL API, so this section makes a direct
# GraphQL query to fetch them.

def get_unresolved_review_threads(
    repository_name: str,
    pr_number: int,
) -> Tuple[List[str], List[str], List[str], List[datetime], Optional[str]]:
    """Return (unresolved_reviewer_logins, summary_lines, thread_reviewers, thread_timestamps, review_decision).

    summary_lines and thread_reviewers are parallel lists — each entry in
    summary_lines has the corresponding opener's login in thread_reviewers.
    Callers filter both by approved_by to drop stale threads from display.

    review_decision is GitHub's official PR review decision:
      "APPROVED" | "CHANGES_REQUESTED" | "REVIEW_REQUIRED" | None
    thread_timestamps holds the creation time of each unresolved thread's first
    comment, used to anchor the "Changes Requested" status clock.
    """
    owner, repo = repository_name.split("/")

    # GraphQL query pulling: the PR's overall reviewDecision, plus every
    # review thread (up to 100) and, within each thread, its first 20
    # comments — we only need each thread's resolved flag and its opening
    # comment (author/path/body/timestamp) to build our summary.
    query = """
    query($owner:String!, $repo:String!, $number:Int!) {
      repository(owner:$owner, name:$repo) {
        pullRequest(number:$number) {
          reviewDecision
          reviewThreads(first:100) {
            nodes {
              isResolved
              comments(first:20) {
                nodes {
                  body
                  path
                  createdAt
                  author {
                    login
                    __typename
                  }
                }
              }
            }
          }
        }
      }
    }
    """

    # Fire the GraphQL request directly with `requests`, authenticating with
    # the same GITHUB_TOKEN used elsewhere for the REST/PyGithub calls.
    response = requests.post(
        "https://api.github.com/graphql",
        headers={"Authorization": f"Bearer {os.environ['GITHUB_TOKEN']}"},
        json={
            "query": query,
            "variables": {
                "owner": owner,
                "repo": repo,
                "number": pr_number,
            },
        },
        timeout=30,
    )

    response.raise_for_status()

    pr_data = (
        response.json()["data"]["repository"]["pullRequest"]
    )

    review_decision: Optional[str] = pr_data.get("reviewDecision")
    threads = pr_data["reviewThreads"]["nodes"]

    unresolved_reviewers: set[str] = set()
    summary_lines: List[str] = []
    thread_reviewers: List[str] = []
    thread_timestamps: List[datetime] = []

    # Walk every review thread and keep only the ones still unresolved,
    # extracting the info needed from each thread's opening comment: who
    # started it, on what file, what they said, and when.
    for thread in threads:
        if thread["isResolved"]:
            continue

        comments = thread["comments"]["nodes"]
        if not comments:
            continue

        first_comment = comments[0]

        # Skip threads opened by bots or accounts that no longer exist
        if not first_comment["author"] or first_comment["author"]["__typename"] == "Bot":
            continue

        reviewer = first_comment["author"]["login"]
        unresolved_reviewers.add(reviewer)

        created_at = datetime.fromisoformat(
            first_comment["createdAt"].replace("Z", "+00:00")
        )
        thread_timestamps.append(created_at)
        thread_reviewers.append(reviewer)

        summary_lines.append(
            f"- **{first_comment['path']}** "
            f"(Reviewer: @{reviewer}) "
            f"{first_comment['body']}"
        )

    return list(unresolved_reviewers), summary_lines, thread_reviewers, thread_timestamps, review_decision


# ============================================================================
# STATUS DECISION TREE
# ============================================================================
# The heart of the bot: given everything we know about a PR (its reviews,
# unresolved threads, requested reviewers, commenters, and GitHub's own
# reviewDecision verdict), decide which single "status" bucket it falls
# into (e.g. "Waiting For Review", "Changes Requested", "Ready To Merge")
# and who is responsible for acting on it next. The returned status string
# is later used to look up a threshold (STATUS_THRESHOLDS) and an action
# message (STATUS_ACTIONS).

def determine_status(
    pr: PullRequest,
    unresolved_reviewers: List[str],
    issue_commenters: List[str],
    review_decision: Optional[str],
) -> Tuple[str, str, List[str]]:
    """Two-level status decision.

    Level 1: GitHub's reviewDecision (APPROVED / CHANGES_REQUESTED / REVIEW_REQUIRED / None)
    Level 2: our fine-grained logic within each bucket.

    Using reviewDecision as the outer gate means we stay in sync with what
    GitHub's UI actually shows, and avoids false "Changes Requested" when a
    reviewer opened threads but has since approved.
    """
    approved_by, changes_requested_by, commented_by, review_count = (
        analyze_reviews(pr)
    )
    requested_reviewers = get_requested_reviewers(pr)
    approval_count = len(approved_by)
    author = pr.user.login

    # ── GitHub says: all required reviews satisfied, no active change requests ──
    if review_decision == "APPROVED":
        # Still apply our REQUIRED_APPROVALS as an additional gate — our bar
        # may be higher than the repo's branch-protection setting.
        if approval_count >= REQUIRED_APPROVALS:
            return "Ready To Merge", MERGER, approved_by
        pending = [r for r in requested_reviewers if r not in set(approved_by)]
        responsible = ", ".join(f"@{r}" for r in pending) if pending else "@reviewers"
        return "Awaiting Additional Approval", responsible, approved_by

    # Exclude thread-openers who have since approved — their approval supersedes
    # their open threads regardless of which reviewDecision branch we land in.
    active_unresolved = [r for r in unresolved_reviewers if r not in set(approved_by)]

    # ── GitHub says: at least one reviewer has an active change request ──
    if review_decision == "CHANGES_REQUESTED":
        if active_unresolved:
            return "Changes Requested", author, approved_by
        # CHANGES_REQUESTED verdict exists but no active unresolved threads
        participants = sorted(
            set([author] + changes_requested_by + commented_by
                + requested_reviewers + issue_commenters)
            - set(approved_by)
        )
        responsible = ", ".join(f"@{user}" for user in participants)
        return "Review Discussion In Progress", responsible, approved_by

    # ── "REVIEW_REQUIRED" or None (no branch-protection review rules) ──
    # Use the full fine-grained V1 decision tree. This also ensures repos
    # without branch protection still get all statuses correctly.

    # There's an unresolved inline thread not superseded by an approval —
    # the author owes a response regardless of the overall review verdict.
    if active_unresolved:
        return "Changes Requested", author, approved_by

    # Our internal approval gate (may differ from branch-protection settings).
    if approval_count >= REQUIRED_APPROVALS and not changes_requested_by:
        return "Ready To Merge", MERGER, approved_by

    # Nobody has reviewed at all yet — split on whether reviewers were ever
    # requested (waiting on named people) vs. nobody assigned (waiting on
    # the author/team to assign someone).
    if review_count == 0:
        if requested_reviewers:
            responsible = ", ".join(f"@{r}" for r in requested_reviewers)
            return "Waiting For Review", responsible, approved_by
        return "No Reviewers Assigned", author, approved_by

    # A reviewer's active CHANGES_REQUESTED verdict exists (but no unresolved
    # thread caught it above) — ping everyone involved in the discussion.
    if changes_requested_by:
        participants = sorted(
            set([author] + changes_requested_by + commented_by
                + requested_reviewers + issue_commenters)
            - set(approved_by)
        )
        responsible = ", ".join(f"@{user}" for user in participants)
        return "Review Discussion In Progress", responsible, approved_by

    # Only non-blocking COMMENTED reviews exist — softer nudge than a formal
    # change request, but still needs eyes from author and reviewers alike.
    if commented_by:
        participants = sorted(
            set([author] + commented_by + requested_reviewers + issue_commenters)
            - set(approved_by)
        )
        responsible = ", ".join(f"@{user}" for user in participants)
        return "Comments Received", responsible, approved_by

    # At least one approval exists but not enough to hit REQUIRED_APPROVALS —
    # waiting on the remaining requested reviewers to weigh in.
    if approval_count > 0:
        pending = [r for r in requested_reviewers if r not in set(approved_by)]
        responsible = ", ".join(f"@{r}" for r in pending) if pending else "@reviewers"
        return "Awaiting Additional Approval", responsible, approved_by

    # Fallback: only dismissed/withdrawn reviews remain.
    if requested_reviewers:
        responsible = ", ".join(f"@{r}" for r in requested_reviewers)
        return "Waiting For Review", responsible, approved_by
    return "No Reviewers Assigned", author, approved_by


# ============================================================================
# REMINDER THROTTLING
# ============================================================================
# Functions that determine how long the PR has been sitting in its current
# status, and whether we've already nagged about it recently. Together
# these prevent the bot from spamming the same PR every single run — a
# reminder only goes out once the status has aged past its threshold AND
# enough time has passed since the last reminder.

def get_last_reminder_days(pr: PullRequest) -> Optional[float]:
    """Return days since the last bot reminder comment, or None if never reminded."""
    # Scan comments newest-first (reversed) so we stop at the most recent
    # bot reminder rather than the oldest one.
    comments = list(pr.get_issue_comments())
    for comment in reversed(comments):
        if (
            comment.user
            and comment.user.type == "Bot"
            and comment.body
            and "Stale PR Reminder" in comment.body
        ):
            return (get_now() - comment.created_at).total_seconds() / 86400
    return None


def get_status_age_days(
    status: str,
    pr: PullRequest,
    thread_timestamps: List[datetime],
) -> float:
    """Return how many days the PR has been in its current status.

    The clock is anchored to the event that caused the current status,
    not to generic last-activity or PR creation date.
    """
    now = get_now()

    # No review activity has happened yet, so the clock naturally starts at
    # PR creation — there's no more recent event to anchor to.
    if status in ("No Reviewers Assigned", "Waiting For Review"):
        return (now - pr.created_at).total_seconds() / 86400

    if status == "Changes Requested":
        # Clock from the most recently opened unresolved thread
        if thread_timestamps:
            return (now - max(thread_timestamps)).total_seconds() / 86400
        return 0.0

    # Review-verdict-based statuses: find the most recent matching review
    # state and anchor the clock to when it was submitted (e.g. "Ready To
    # Merge" is timed from the last APPROVED review, not from PR creation).
    verdict_map = {
        "Review Discussion In Progress": ("CHANGES_REQUESTED",),
        "Comments Received":            ("COMMENTED",),
        "Awaiting Additional Approval": ("APPROVED",),
        "Ready To Merge":               ("APPROVED",),
    }
    target_states = verdict_map.get(status)
    if target_states:
        latest: Optional[datetime] = None
        for review in pr.get_reviews():
            if not review.user or review.user.type == "Bot":
                continue
            if review.state in target_states:
                if latest is None or review.submitted_at > latest:
                    latest = review.submitted_at
        if latest:
            return (now - latest).total_seconds() / 86400

    # Fallback
    return (now - pr.created_at).total_seconds() / 86400


# ============================================================================
# COMMENT RENDERING
# ============================================================================
# Formats all the data gathered above into the Markdown comment that gets
# posted to the PR. Purely presentational — no decision-making happens here.

def build_comment(
    pr: PullRequest,
    status: str,
    responsible: str,
    approved_by: List[str],
    pr_age_minutes: float,
    last_activity_days: float,
    review_summary: str,
) -> str:

    pr_age_days = pr_age_minutes / 1440

    approvals = ", ".join(f"@{u}" for u in approved_by) if approved_by else "None"

    # "responsible" may already be a comma-separated "@user, @user" string
    # (multiple participants) or a bare login (single user) — only prefix
    # with "@" if it isn't already formatted as a mention.
    responsible_display = responsible
    if not responsible.startswith("@"):
        responsible_display = f"@{responsible}"

    action = STATUS_ACTIONS.get(status, "Please take a look.")

    return f"""
## 🤖 Stale PR Reminder

### PR Details

| Metric | Value |
|----------|----------|
| PR | #{pr.number} |
| Author | @{pr.user.login} |
| PR Age | {pr_age_days:.2f} days |
| Last Activity | {last_activity_days:.2f} days ago |
| Status | {status} |
| Action Item On | {responsible_display} |

### Next Steps

{action}

### Approvals

Required Approvals: {REQUIRED_APPROVALS}

Current Approvals: {len(approved_by)}

Approved By:
{approvals}

### PR Feedback

{review_summary}

---

_This is an automated reminder. Please take action to keep this PR moving._
"""


# ============================================================================
# ENTRY POINT
# ============================================================================
# Orchestrates the whole bot: iterate every open PR in the repo, work out
# its status and how long it's been stuck there, and post a reminder
# comment if it has exceeded that status's threshold and hasn't already
# been reminded about recently. Each PR is processed independently and
# wrapped in a try/except so one PR's failure (e.g. an API error) doesn't
# abort the run for the rest of the repository.

def main() -> None:

    github_client = Github(os.environ["GITHUB_TOKEN"])
    repository = github_client.get_repo(os.environ["GITHUB_REPOSITORY"])

    print(f"Scanning repository {repository.full_name}")

    for pr in repository.get_pulls(state="open"):
        try:
            # Draft PRs aren't ready for review yet, so skip them entirely.
            if pr.draft:
                continue

            # Step 1: fetch unresolved review threads + GitHub's official
            # reviewDecision via the GraphQL API (not available over REST).
            unresolved_reviewers, summary_lines, thread_reviewers, thread_timestamps, review_decision = (
                get_unresolved_review_threads(repository.full_name, pr.number)
            )

            issue_commenters = get_issue_commenters(pr)

            # Step 2: run the decision tree to get this PR's current status
            # bucket and who's responsible for the next action.
            status, responsible, approved_by = determine_status(
                pr, unresolved_reviewers, issue_commenters, review_decision
            )

            review_feedback = get_review_feedback(pr, approved_by)
            discussion_comments = get_discussion_comments(pr)

            # Filter both display lines and timestamps to exclude threads from
            # reviewers who have since approved — their threads are no longer blocking.
            approved_set = set(approved_by)
            active_lines = [
                line for line, reviewer in zip(summary_lines, thread_reviewers)
                if reviewer not in approved_set
            ]
            active_timestamps = [
                ts for ts, reviewer in zip(thread_timestamps, thread_reviewers)
                if reviewer not in approved_set
            ]

            # Step 3: assemble the "PR Feedback" section of the comment out
            # of whichever feedback types are actually present for this PR.
            feedback_sections = []

            if active_lines:
                feedback_sections.append("### Code Review Threads\n\n" + "\n".join(active_lines))

            if review_feedback:
                feedback_sections.append(
                    "### Review Feedback\n\n" + "\n\n".join(review_feedback)
                )

            if discussion_comments:
                feedback_sections.append(
                    "### Discussion Comments\n\n" + "\n\n".join(discussion_comments)
                )

            review_summary = (
                "\n\n".join(feedback_sections) if feedback_sections else "_No feedback found_"
            )

            # Step 4: figure out how many days this status must persist
            # before we're allowed to remind about it. "Comments Received"
            # is special-cased — more feedback items means more time is
            # given for the author/reviewers to work through them.
            if status == "Comments Received":
                feedback_count = len(review_feedback) + len(discussion_comments)
                threshold_days = 3 if feedback_count <= 3 else 5
            else:
                threshold_days = STATUS_THRESHOLDS[status]

            status_age_days = get_status_age_days(status, pr, active_timestamps)
            last_reminder_days = get_last_reminder_days(pr)

            print(
                f"PR #{pr.number} | reviewDecision={review_decision!r} | "
                f"Status={status!r} | "
                f"Threshold={threshold_days:.4f}d | "
                f"StatusAge={status_age_days:.4f}d | "
                f"LastReminder={last_reminder_days}"
            )

            # Step 5: throttle — only post if the status has been sitting
            # long enough AND we haven't already reminded within that same
            # threshold window (prevents reminder spam on every run).

            # Skip if the PR hasn't been in this status long enough
            if status_age_days < threshold_days:
                continue

            # Skip if a reminder was posted recently — wait another full threshold interval
            if last_reminder_days is not None and last_reminder_days < threshold_days:
                continue

            # Step 6: build and post the reminder comment.
            comment = build_comment(
                pr=pr,
                status=status,
                responsible=responsible,
                approved_by=approved_by,
                pr_age_minutes=get_pr_age_minutes(pr),
                last_activity_days=get_last_human_activity_days(pr),
                review_summary=review_summary,
            )

            pr.create_issue_comment(comment)
            print(f"Reminder posted for PR #{pr.number}")

        except Exception as exc:
            # Don't let one problematic PR (e.g. a transient API error)
            # kill the whole scan — log it and move on to the next PR.
            print(f"PR #{pr.number}: skipping due to error — {exc}")


if __name__ == "__main__":
    main()