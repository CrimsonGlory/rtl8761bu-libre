#!/usr/bin/env bash
# Snapshot and compare work-in-progress.txt status tag counts.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../../../" && pwd)"
WIP_FILE="${WIP_FILE:-$ROOT/work-in-progress.txt}"

count_tag() {
  local status="$1"
  local n
  n="$(grep -cE "^[[:space:]]*\\[${status}\\]" "$WIP_FILE" 2>/dev/null || true)"
  echo "${n:-0}"
}

read_counts() {
  echo "DONE=$(count_tag DONE)"
  echo "NEXT=$(count_tag NEXT)"
  echo "BLOCKED=$(count_tag BLOCKED)"
}

get_var() {
  local file="$1" key="$2"
  grep -E "^${key}=" "$file" | head -n1 | cut -d= -f2-
}

usage() {
  cat <<'EOF'
Usage: wip-state.sh count
       wip-state.sh snapshot <file>
       wip-state.sh delta <snapshot-file>

Environment:
  WIP_FILE   Path to work-in-progress.txt (default: repo root)

delta exit codes:
  0  SHOULD_CONTINUE=yes  (done rose, at least one [NEXT] remains)
  1  SHOULD_CONTINUE=no   (stop: no progress, complete, or blocked)
EOF
}

cmd="${1:-}"
case "$cmd" in
  count)
    read_counts
    ;;
  snapshot)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    read_counts >"$2"
    ;;
  delta)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    [[ -f "$2" ]] || { echo "missing snapshot: $2" >&2; exit 2; }

    before_done="$(get_var "$2" DONE)"
    before_next="$(get_var "$2" NEXT)"
    before_blocked="$(get_var "$2" BLOCKED)"

    after_done="$(count_tag DONE)"
    after_next="$(count_tag NEXT)"
    after_blocked="$(count_tag BLOCKED)"

    done_delta=$((after_done - before_done))
    blocked_delta=$((after_blocked - before_blocked))

    echo "DONE_BEFORE=$before_done"
    echo "DONE_AFTER=$after_done"
    echo "DONE_DELTA=$done_delta"
    echo "NEXT_BEFORE=$before_next"
    echo "NEXT_AFTER=$after_next"
    echo "BLOCKED_BEFORE=$before_blocked"
    echo "BLOCKED_AFTER=$after_blocked"
    echo "BLOCKED_DELTA=$blocked_delta"

    if (( blocked_delta > 0 )); then
      echo "SHOULD_COMMIT=no"
      echo "SHOULD_CONTINUE=no"
      echo "STOP_REASON=blocked"
      exit 1
    fi

    if (( done_delta >= 1 )); then
      echo "SHOULD_COMMIT=yes"
      if (( after_next >= 1 )); then
        echo "SHOULD_CONTINUE=yes"
        echo "STOP_REASON=continue"
        exit 0
      fi
      echo "SHOULD_CONTINUE=no"
      echo "STOP_REASON=complete"
      exit 1
    fi

    echo "SHOULD_COMMIT=no"
    echo "SHOULD_CONTINUE=no"
    echo "STOP_REASON=no_progress"
    exit 1
    ;;
  *)
    usage
    exit 2
    ;;
esac
