#!/usr/bin/env bash
# Check wairz_requested_changes.txt for [TODO] items blocking progress.
# Exit 0 if clear, 1 if blocked.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../../../" && pwd)"
blockers_file="$ROOT/wairz_requested_changes.txt"

if [[ ! -f "$blockers_file" ]]; then
  exit 0
fi

if grep -q '^\[TODO\]' "$blockers_file"; then
  echo "❌ BLOCKED: wairz_requested_changes.txt has [TODO] items."
  echo "   Fix these blockers before proceeding:"
  grep '^\[TODO\]' "$blockers_file" | sed 's/^/   /'
  exit 1
else
  echo "✅ Blocker check passed: wairz_requested_changes.txt clear"
  exit 0
fi
