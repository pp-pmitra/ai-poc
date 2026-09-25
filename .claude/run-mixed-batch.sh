#!/usr/bin/env bash
# .claude/run-mixed-batch.sh
#
# Ordered two-invocation Kavach runner for batches that mix Life and Studio
# failures.  Never starts both bootstraps simultaneously — a single Playwright
# MCP server can only attach to one --cdp-endpoint at a time.
#
# Sequence enforced:
#   1. Start Life bootstrap (port 9223) → run kavach-diagnose (Life MCP config)
#      → touch .claude/auth/life-cdp-done → bootstrap exits
#   2. Start Studio bootstrap (port 9224) → run kavach-diagnose (Studio MCP config)
#      → touch .claude/auth/studio-cdp-done → bootstrap exits
#
# Usage:
#   .claude/run-mixed-batch.sh \
#     --environment <Demo|Pre-release> \
#     --user-type <Internal|External> \
#     [--hold-seconds <N>]          # default: 3600
#     [--life-only | --studio-only] # skip one phase; default: both
#
# Prerequisites:
#   - failure-analyzer/history/failures-for-replay.json already exists
#     (run Phase 1 / list_failures.py before calling this script)
#   - mvn and claude are on PATH

set -euo pipefail

# kavach-diagnose is never meant to touch application/test source — both
# Write and Edit are scoped to kavach-data/** here, matching the agent
# definition's own tool list exactly (Write(kavach-data/**), Edit(kavach-data/**)).
# This --allowedTools string is not the real enforcement boundary on its own
# (Bash(*) is unrestricted and could write anywhere via shell redirection) —
# the verify_boundaries() check below (mirroring kavach.yml's CI-only "verify
# repository boundaries" step) is what actually detects and fails on an
# out-of-scope write for this manual entry point. Do not widen ALLOWED_TOOLS
# back to Write(*)/Edit(*), and do not remove the verify_boundaries() calls.
ALLOWED_TOOLS="Bash(*),Read,Write(kavach-data/**),Edit(kavach-data/**),Grep,Glob,\
mcp__playwright__browser_navigate,\
mcp__playwright__browser_tabs,\
mcp__playwright__browser_snapshot,\
mcp__playwright__browser_evaluate,\
mcp__playwright__browser_click,\
mcp__playwright__browser_fill_form,\
mcp__playwright__browser_type,\
mcp__playwright__browser_press_key,\
mcp__playwright__browser_wait_for,\
mcp__playwright__browser_run_code_unsafe,\
mcp__playwright__browser_take_screenshot,\
mcp__playwright__browser_close,\
mcp__playwright__browser_select_option,\
mcp__playwright__browser_hover,\
mcp__playwright__browser_find"

# ── Defaults ─────────────────────────────────────────────────────────────────
ENVIRONMENT=""
USER_TYPE=""
HOLD_SECONDS=3600
RUN_LIFE=true
RUN_STUDIO=true

# ── Argument parsing ──────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --environment)  ENVIRONMENT="$2";  shift 2 ;;
    --user-type)    USER_TYPE="$2";    shift 2 ;;
    --hold-seconds) HOLD_SECONDS="$2"; shift 2 ;;
    --life-only)    RUN_STUDIO=false;  shift   ;;
    --studio-only)  RUN_LIFE=false;    shift   ;;
    *) echo "Unknown argument: $1" >&2; exit 1 ;;
  esac
done

[[ -z "$ENVIRONMENT" ]] && { echo "ERROR: --environment is required (Demo|Pre-release)" >&2; exit 1; }
[[ -z "$USER_TYPE" ]]   && { echo "ERROR: --user-type is required (Internal|External)"  >&2; exit 1; }

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

mkdir -p .claude/auth

# Helper: wait up to 60s for a TCP port to open
wait_for_port() {
  local port="$1" label="$2"
  echo "    Waiting for CDP on port $port ($label)..."
  for i in $(seq 1 60); do
    nc -z localhost "$port" 2>/dev/null && return 0
    sleep 1
  done
  echo "ERROR: CDP port $port never became available (waited 60s)" >&2
  return 1
}

# Helper: fail loudly if kavach-diagnose wrote outside its allowed locations.
# Mirrors kavach.yml's CI-only "Verify repository boundaries" step -- this is
# the one invocation path for kavach-diagnose with no such check otherwise,
# since ALLOWED_TOOLS's Write/Edit scoping isn't a real boundary on its own
# (Bash(*) can write anywhere via shell redirection).
verify_boundaries() {
  local label="$1"
  local changes
  changes=$(git status --porcelain --untracked-files=all -- . \
    ':(exclude)target/**' \
    ':(exclude)kavach-data/history/**' \
    ':(exclude)kavach-data/fix-patterns/**')
  if [[ -n "$changes" ]]; then
    echo "ERROR: kavach-diagnose ($label) modified files outside its allowed write locations:" >&2
    printf '%s\n' "$changes" >&2
    return 1
  fi
  return 0
}

# ── Phase A: Life failures ────────────────────────────────────────────────────
if $RUN_LIFE; then
  echo ""
  echo "=== [1/4] Clearing stale done-marker for Life ==="
  rm -f .claude/auth/life-cdp-done

  echo ""
  echo "=== [2/4] Starting Life bootstrap (port 9223) ==="
  mvn test \
    -Dtest=LifeAuthStateBootstrapTest \
    -Dauth.environment="$ENVIRONMENT" \
    -Dauth.userType="$USER_TYPE" \
    -Dauth.holdSeconds="$HOLD_SECONDS" &
  LIFE_BOOTSTRAP_PID=$!

  wait_for_port 9223 "LifeAuthStateBootstrapTest" || {
    kill "$LIFE_BOOTSTRAP_PID" 2>/dev/null; exit 1
  }

  echo ""
  echo "=== [3/4] Running kavach-diagnose — Life failures ==="
  # There is no /analyze-failure or /kawach slash command anywhere in this
  # repo — the only proven invocation is loading the kavach-diagnose agent
  # directly, exactly as kavach.yml's CI job does.
  claude \
    --agent kavach-diagnose \
    --print "Diagnose the failures from the Cucumber run for the Life app (environment: ${ENVIRONMENT}, user type: ${USER_TYPE}). The auth bootstrap is already logged in and holding a browser open on CDP port 9223; the Playwright MCP server is already configured to attach to it — never attempt your own login. Run your full pipeline (failure-triage -> live-replay-diagnosis -> verdict-reporting) end to end and stop once the verdict report and combined-receipts.json are written and validated against .claude/contracts/kavach-verdict.schema.json." \
    --mcp-config .claude/mcp-ci-life.json \
    --strict-mcp-config \
    --permission-mode auto \
    --allowedTools "$ALLOWED_TOOLS" \
    --verbose \
    --output-format text

  verify_boundaries "Life" || { kill "$LIFE_BOOTSTRAP_PID" 2>/dev/null; exit 1; }

  echo ""
  echo "=== Releasing Life bootstrap ==="
  touch .claude/auth/life-cdp-done
  wait "$LIFE_BOOTSTRAP_PID" || true
fi

# ── Phase B: Studio failures ──────────────────────────────────────────────────
if $RUN_STUDIO; then
  echo ""
  echo "=== [1/4] Clearing stale done-marker for Studio ==="
  rm -f .claude/auth/studio-cdp-done

  echo ""
  echo "=== [2/4] Starting Studio bootstrap (port 9224) ==="
  mvn test \
    -Dtest=StudioAuthStateBootstrapTest \
    -Dauth.holdForCdp=true \
    -Dauth.holdSeconds="$HOLD_SECONDS" &
  STUDIO_BOOTSTRAP_PID=$!

  wait_for_port 9224 "StudioAuthStateBootstrapTest" || {
    kill "$STUDIO_BOOTSTRAP_PID" 2>/dev/null; exit 1
  }

  echo ""
  echo "=== [3/4] Running kavach-diagnose — Studio failures ==="
  claude \
    --agent kavach-diagnose \
    --print "Diagnose the failures from the Cucumber run for the Studio app (environment: ${ENVIRONMENT}, user type: ${USER_TYPE}). The auth bootstrap is already logged in and holding a browser open on CDP port 9224; the Playwright MCP server is already configured to attach to it — never attempt your own login. Run your full pipeline (failure-triage -> live-replay-diagnosis -> verdict-reporting) end to end and stop once the verdict report and combined-receipts.json are written and validated against .claude/contracts/kavach-verdict.schema.json." \
    --mcp-config .claude/mcp-ci-studio.json \
    --strict-mcp-config \
    --permission-mode auto \
    --allowedTools "$ALLOWED_TOOLS" \
    --verbose \
    --output-format text

  verify_boundaries "Studio" || { kill "$STUDIO_BOOTSTRAP_PID" 2>/dev/null; exit 1; }

  echo ""
  echo "=== Releasing Studio bootstrap ==="
  touch .claude/auth/studio-cdp-done
  wait "$STUDIO_BOOTSTRAP_PID" || true
fi

echo ""
echo "=== Mixed-batch run complete. ==="
