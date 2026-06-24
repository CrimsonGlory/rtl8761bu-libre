#!/usr/bin/env bash
# Overnight delta: [BLOCKED] + promote counts as progress; no_progress → needs_block.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../../../" && pwd)"
WIP_STATE="${WIP_STATE:-$ROOT/.claude/skills/wip-loop-overnight/scripts/wip-state.sh}"

usage() {
  cat <<'EOF'
Usage: wip-state-overnight.sh count
       wip-state-overnight.sh snapshot <file>
       wip-state-overnight.sh delta <snapshot-file>
       wip-state-overnight.sh bisect-mode
       wip-state-overnight.sh check-wairz-blockers

Delegates count/snapshot/bisect-mode to wip-state.sh.
delta differs from wip-state.sh delta:
  - BLOCKED_DELTA > 0 with [NEXT] or [TODO] remaining → commit + continue
  - DONE_DELTA >= 1 → same as wip-loop
  - FAILED_DELTA > 0 → stop (hard failure)
  - DONE_DELTA=0 and BLOCKED_DELTA=0 → STOP_REASON=needs_block (worker left [NEXT]
    open; run recovery to [BLOCKED] + promote first [TODO] → [NEXT])

check-wairz-blockers: check wairz_requested_changes.txt for [TODO] items.
  Exit 0 if clear, exit 1 if blocked. Print HAD_WAIRZ_BLOCKERS=yes/no.
EOF
}

get_field() {
  local key="$1" file="$2"
  grep -E "^${key}=" "$file" | head -n1 | cut -d= -f2-
}

cmd="${1:-}"
case "$cmd" in
  count|snapshot|bisect-mode)
    exec "$WIP_STATE" "$@"
    ;;
  check-wairz-blockers)
    # Check if wairz_requested_changes.txt has any [TODO] items
    blockers_file="$ROOT/wairz_requested_changes.txt"
    if [[ ! -f "$blockers_file" ]]; then
      echo "HAD_WAIRZ_BLOCKERS=no"
      exit 0
    fi

    if grep -q '^\[TODO\]' "$blockers_file"; then
      echo "HAD_WAIRZ_BLOCKERS=yes"
      grep '^\[TODO\]' "$blockers_file" | head -5
      exit 1
    else
      echo "HAD_WAIRZ_BLOCKERS=no"
      exit 0
    fi
    ;;
  delta)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    [[ -f "$2" ]] || { echo "missing snapshot: $2" >&2; exit 2; }

    base_out="$(mktemp)"
    set +e
    "$WIP_STATE" delta "$2" >"$base_out" 2>&1
    base_ec=$?
    set -e

    # Always print count deltas from base
    grep -E '^(DONE_|NEXT_|TODO_|BLOCKED_|FAILED_)' "$base_out" || true

    stop_reason="$(get_field STOP_REASON "$base_out")"
    done_delta="$(get_field DONE_DELTA "$base_out")"
    blocked_delta="$(get_field BLOCKED_DELTA "$base_out")"
    after_next="$(get_field NEXT_AFTER "$base_out")"
    after_todo="$(get_field TODO_AFTER "$base_out")"
    failed_delta="$(get_field FAILED_DELTA "$base_out")"
    rm -f "$base_out"

    if [[ "${failed_delta:-0}" -gt 0 ]]; then
      echo "SHOULD_COMMIT=no"
      echo "SHOULD_CONTINUE=no"
      echo "STOP_REASON=failed"
      exit 1
    fi

    if [[ "$stop_reason" == continue || "$stop_reason" == todo_continue ]]; then
      echo "SHOULD_COMMIT=yes"
      echo "SHOULD_CONTINUE=yes"
      echo "STOP_REASON=$stop_reason"
      exit 0
    fi

    if [[ "$stop_reason" == complete ]]; then
      echo "SHOULD_COMMIT=yes"
      echo "SHOULD_CONTINUE=no"
      echo "STOP_REASON=complete"
      exit 1
    fi

    if [[ "${blocked_delta:-0}" -gt 0 ]]; then
      if [[ "${after_next:-0}" -ge 1 || "${after_todo:-0}" -ge 1 ]]; then
        echo "SHOULD_COMMIT=yes"
        echo "SHOULD_CONTINUE=yes"
        echo "STOP_REASON=blocked_continue"
        exit 0
      fi
      echo "SHOULD_COMMIT=yes"
      echo "SHOULD_CONTINUE=no"
      echo "STOP_REASON=blocked_complete"
      exit 1
    fi

    if [[ "${done_delta:-0}" -eq 0 && "${blocked_delta:-0}" -eq 0 ]]; then
      echo "SHOULD_COMMIT=no"
      echo "SHOULD_CONTINUE=yes"
      echo "STOP_REASON=needs_block"
      echo "NEEDS_BLOCK_REASON=worker_left_NEXT_open"
      exit 0
    fi

    if [[ "$stop_reason" == no_progress || "$stop_reason" == blocked ]]; then
      echo "SHOULD_COMMIT=no"
      echo "SHOULD_CONTINUE=yes"
      echo "STOP_REASON=needs_block"
      exit 0
    fi

    # Fallback: pass through unknown base outcome
    echo "SHOULD_COMMIT=no"
    echo "SHOULD_CONTINUE=no"
    echo "STOP_REASON=${stop_reason:-unknown}"
    exit "$base_ec"
    ;;
  *)
    usage
    exit 2
    ;;
esac
