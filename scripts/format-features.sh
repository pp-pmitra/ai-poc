#!/bin/sh
# Reformats .feature files in place to gherkin-utils' canonical style.
# Usage: scripts/format-features.sh [file ...]   (defaults to all feature files)
set -e

GHERKIN_UTILS="$(dirname "$0")/../node_modules/.bin/gherkin-utils"

if [ ! -x "$GHERKIN_UTILS" ]; then
  echo "gherkin-utils not found — run 'npm install' first."
  exit 1
fi

if [ "$#" -gt 0 ]; then
  FILES="$*"
else
  FILES=$(find src/test/resources/features -name "*.feature")
fi

# shellcheck disable=SC2086
"$GHERKIN_UTILS" format $FILES
echo "Formatted feature files."
