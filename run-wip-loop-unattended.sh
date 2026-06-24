#!/usr/bin/env bash
# Unattended driver for wip-iteration.
#
# Shell-level loop (NOT worker spawning) that repeatedly invokes `claude -p`
# (non-interactive mode) to work through work-in-progress.txt's [NEXT]/[TODO]
# tickets without any human in the loop.
#
# **DESIGN PRINCIPLE**: This loop keeps everything in the SUPERVISOR CONTEXT.
# Each `claude -p /wip-iteration` invocation runs with MCP access available
# (isolation: false in SKILL.md). The /wip-iteration skill MUST NOT spawn
# any Agent workers or subagents — all RE/analysis work is done directly in
# the supervisor session. This ensures wairz MCP tools are always accessible.
#
# Each invocation is a fresh, stateless process — progress persists via
# work-in-progress.txt + git commits, not conversation memory, so restarting
# after a crash/timeout/iteration-cap just picks back up where it left off.
#
# Stops when work-in-progress.txt has no [NEXT] and no [TODO] left, or after
# MAX_RESTARTS fresh `claude -p` invocations (safety valve against infinite
# retry if something is permanently broken).
#
# Usage:
#   ./run-wip-loop-unattended.sh
#   nohup ./run-wip-loop-unattended.sh > /dev/null 2>&1 &

set -uo pipefail
# Do NOT force sandboxing — MCP needs full access
unset IS_SANDBOX

# --- Tunables: change these if you want a different model/effort/cadence ---
CLAUDE_MODEL="claude-haiku-4-5"
CLAUDE_EFFORT="high"          # low | medium | high | xhigh | max
MAX_ITERATIONS_PER_RUN=300    # passed into the skill's own prompt
MAX_RESTARTS=50               # how many times this script will re-launch `claude -p`
SLEEP_BETWEEN_RESTARTS=30     # seconds, only used between restarts (not between skill iterations)
# ---------------------------------------------------------------------------

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="${REPO_DIR}/wip-loop.log"
STATE_SCRIPT="${REPO_DIR}/.claude/skills/wip-loop-overnight/scripts/wip-state-overnight.sh"

cd "$REPO_DIR" || exit 1

log() {
  printf '%s %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$1" | tee -a "$LOG_FILE"
}

if [ ! -x "$STATE_SCRIPT" ]; then
  log "ERROR: state script not found/executable at $STATE_SCRIPT"
  exit 1
fi

log "=== run-wip-loop-unattended.sh starting (model=$CLAUDE_MODEL effort=$CLAUDE_EFFORT) ==="

PROMPT=$(cat <<EOF
/wip-iteration
EOF
)

for ((i = 1; i <= MAX_RESTARTS; i++)); do
  log "--- claude -p invocation ${i}/${MAX_RESTARTS} ---"

  claude \
    --dangerously-skip-permissions \
    --allow-dangerously-skip-permissions \
    -p "$PROMPT" \
    --model "$CLAUDE_MODEL" \
    --effort "$CLAUDE_EFFORT" \
    --output-format text \
    --mcp-config "$REPO_DIR/.mcp.json" \
    >> "$LOG_FILE" 2>&1
  exit_code=$?

  log "claude -p exited with code ${exit_code}"

  eval "$("$STATE_SCRIPT" count)"

  log "WIP state: DONE=${DONE:-?} NEXT=${NEXT:-?} TODO=${TODO:-?} BLOCKED=${BLOCKED:-?} FAILED=${FAILED:-?}"

  if [ "${FAILED:-0}" != "0" ]; then
    log "STOP: FAILED item present — needs human review, not retrying."
    exit 1
  fi

  if [ "${NEXT:-0}" = "0" ] && [ "${TODO:-0}" = "0" ]; then
    log "STOP: no [NEXT] and no [TODO] remain — all work done."
    exit 0
  fi

  log "Work remains (NEXT=${NEXT} TODO=${TODO}); sleeping ${SLEEP_BETWEEN_RESTARTS}s before restart."
  sleep "$SLEEP_BETWEEN_RESTARTS"
done

log "STOP: reached MAX_RESTARTS (${MAX_RESTARTS}) without exhausting [NEXT]/[TODO] — needs human review."
exit 2
