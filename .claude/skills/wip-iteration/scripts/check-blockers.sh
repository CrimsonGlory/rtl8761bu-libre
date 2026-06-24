#!/usr/bin/env bash
# Check wairz_requested_changes.txt for [TODO] items. Exit 0 if clear, 1 if blocked.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../../" && pwd)"
blockers_file="$ROOT/wairz_requested_changes.txt"

if [[ ! -f "$blockers_file" ]]; then
  exit 0
fi

if grep -q '^\[TODO\]' "$blockers_file"; then
  echo "ERROR: wairz_requested_changes.txt has [TODO] items. Cannot proceed."
  grep '^\[TODO\]' "$blockers_file" | head -5
  exit 1
else
  exit 0
fi
