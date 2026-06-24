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

One iteration of work-in-progress.txt management. **All work is done in the main supervisor context — NO WORKERS, NO AGENTS, NO SUBAGENTS.**

**REQUIRES:** When calling via `claude -p`, ensure your environment has MCP access enabled
(MCP tools must be configured and available to the Claude Code session).

## ⚠️ CRITICAL CONSTRAINT: No Worker Spawning

**This skill MUST NOT spawn any workers, agents, or subagents.** All work happens directly in this context:

- ❌ Do NOT use the `Agent` tool to spawn workers
- ❌ Do NOT delegate work to subagents
- ❌ Do NOT invoke `/wip-loop-overnight` or other skills that create workers
- ✅ DO all RE work, analysis, decompilation, documentation directly in this supervisor session

The reason: Worker spawning introduces isolation, which blocks MCP access. The shell loop in `run-wip-loop-unattended.sh` keeps everything in the supervisor context where MCP is available.

## Startup check

🔴 **First:** Verify MCP access
MCP tools (like `mcp__wairz__list_projects` and `mcp__wairz__run_ghidra_headless`) are Claude Code tools, not shell commands. Access is verified when the skill invokes its first MCP tool call. If that call fails, MCP is unavailable and the skill will exit with an error.

```bash
mcp__wairz__run_ghidra_headless --help
```

If this fails → EXIT IMMEDIATELY with "MCP access unavailable — cannot proceed." Do not attempt workarounds.

🔴 **Second:** Check wairz blockers
```bash
.claude/skills/wip-iteration/scripts/check-blockers.sh
```
If [TODO] items exist in `wairz_requested_changes.txt` → EXIT with error. Fix those blockers first before retrying the skill.

## Execution

1. **Read work-in-progress.txt** — find current [NEXT] item
2. **If no [NEXT]:** Promote first [TODO] → [NEXT], then execute it
3. **If [NEXT] exists:** Execute it as written

**Execution means:** Do the work described in that line directly (in this session, not via agents). For RE tasks: decompile, 
analyze, document findings. For prep tasks: stage scripts, create analysis docs.

4. **When done:**
   - ✅ Completed → Rename `[NEXT]` → `[DONE]` (add date + one-line summary if needed)
   - ❌ Blocked → Rename `[NEXT]` → `[BLOCKED]` (add date + one-line reason)

5. **Promote:** If any [TODO] remains → rename first `[TODO]` → `[NEXT]`

6. **Commit:** Stage files, create commit with summary of work done

**DO NOT leave [NEXT] as [NEXT] when exiting.** Rename to [DONE] or [BLOCKED].
