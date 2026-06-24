---
name: wip-iteration
description: >-
  Single WIP iteration: execute one [NEXT] item from work-in-progress.txt,
  mark done/blocked, promote first [TODO]. No loop, no agents. Inline execution.
disable-model-invocation: false
---

# WIP Iteration — Single Step

One iteration of work-in-progress.txt management. No looping; meant to run repeatedly
via external orchestration (cron, user scripts, etc.).

## Startup check

🔴 **First:** Verify MCP access.
```bash
mcp__wairz__run_ghidra_headless --help
```
If fails → EXIT: "MCP unavailable."

🔴 **Second:** Check wairz blockers.
```bash
.claude/skills/wip-iteration/scripts/check-blockers.sh
```
If [TODO] items in `wairz_requested_changes.txt` → EXIT: "Wairz blockers present."

## Execution

1. **Read work-in-progress.txt** — find current [NEXT] item
2. **If no [NEXT]:** Promote first [TODO] → [NEXT], then execute it
3. **If [NEXT] exists:** Execute it as written

**Execution means:** Do the work described in that line. For RE tasks: decompile, 
analyze, document findings. For prep tasks: stage scripts, create analysis docs.

4. **When done:**
   - ✅ Completed → Rename `[NEXT]` → `[DONE]` (add date + one-line summary if needed)
   - ❌ Blocked → Rename `[NEXT]` → `[BLOCKED]` (add date + one-line reason)

5. **Promote:** If any [TODO] remains → rename first `[TODO]` → `[NEXT]`

6. **Commit:** Stage files, create commit with summary of work done

**DO NOT leave [NEXT] as [NEXT] when exiting.** Rename to [DONE] or [BLOCKED].

## Key differences from wip-loop-overnight

- **No spawned agents** — you run the work directly
- **No supervisor/worker roles** — just do the thing
- **One iteration only** — no loop logic
- **No loop config** — external tool orchestrates repetition
