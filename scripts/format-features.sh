#!/bin/sh
# Reformats .feature files in place to the project's gherkin-utils-based style.
# Usage: scripts/format-features.sh [file ...]   (defaults to all feature files)
set -e

FORMATTER="$(dirname "$0")/format-feature.mjs"

if [ ! -x "$(dirname "$0")/../node_modules/.bin/gherkin-utils" ]; then
  echo "gherkin-utils not found — run 'npm install' first."
  exit 1
fi

if [ "$#" -gt 0 ]; then
  node "$FORMATTER" "$@"
else
  find src/test/resources/features -name "*.feature" -exec node "$FORMATTER" {} +
fi

echo "Formatted feature files."
