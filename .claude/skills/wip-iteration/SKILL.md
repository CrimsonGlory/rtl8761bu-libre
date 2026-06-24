---
name: wip-iteration
description: >-
  Single WIP iteration: execute one [NEXT] item from work-in-progress.txt,
  mark done/blocked, promote first [TODO].
disable-model-invocation: false
isolation: false
env:
  MCP_ALLOW_UNSAFE_LOCALHOST: "true"
---

# WIP Iteration — Single Step

One iteration of work-in-progress.txt management.

**REQUIRES:** When calling via `claude -p`, ensure your environment has MCP access enabled
(MCP tools must be configured and available to the Claude Code session).

## Startup check

🔴 **First:** Verify MCP access
MCP tools (like `mcp__wairz__list_projects`) are Claude Code tools, not shell commands. Access is verified when the skill invokes its first MCP tool call. If that call fails, MCP is unavailable and the skill will exit with an error.

🔴 **Second:** Check wairz blockers
```bash
.claude/skills/wip-iteration/scripts/check-blockers.sh
```
If [TODO] items exist in `wairz_requested_changes.txt` → EXIT with error. Fix those blockers first before retrying the skill.

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
