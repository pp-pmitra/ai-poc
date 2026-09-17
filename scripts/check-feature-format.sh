#!/bin/sh
# Verifies .feature files match the project's gherkin-utils-based formatting.
# Usage: scripts/check-feature-format.sh [file ...]   (defaults to all feature files)
set -e

FORMATTER="$(dirname "$0")/format-feature.mjs"
FAIL=0

if [ ! -x "$(dirname "$0")/../node_modules/.bin/gherkin-utils" ]; then
  echo "gherkin-utils not found — run 'npm install' first."
  exit 1
fi

if [ "$#" -gt 0 ]; then
  FILES="$*"
else
  FILES=$(find src/test/resources/features -name "*.feature")
fi

for f in $FILES; do
  formatted=$(node "$FORMATTER" < "$f")
  original=$(cat "$f")
  if [ "$formatted" != "$original" ]; then
    echo "Needs formatting: $f"
    FAIL=1
  fi
done

if [ "$FAIL" -eq 1 ]; then
  echo ""
  echo "Run 'npm run feature:format' to fix, then re-stage the files."
  exit 1
fi

echo "All feature files are correctly formatted."
