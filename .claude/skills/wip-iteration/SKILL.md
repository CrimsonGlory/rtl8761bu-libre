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

# Work in progress

Do NEXT item in work-in-progress.txt. **All work is done in the main supervisor context — NO WORKERS, NO AGENTS, NO SUBAGENTS.**

This skill works fine via `claude -p` (non-interactive mode) as long as `.mcp.json` is loaded
(pass `--mcp-config .mcp.json`) and wairz is not in `disabledMcpjsonServers`.

## ⚠️ CRITICAL CONSTRAINT: No Worker Spawning

**This skill MUST NOT spawn any workers, agents, or subagents.** All work happens directly in this context:

- ❌ Do NOT use the `Agent` tool to spawn workers
- ❌ Do NOT delegate work to subagents
- ❌ Do NOT invoke `/wip-loop-overnight` or other skills that create workers
- ✅ DO all RE work, analysis, decompilation, documentation directly in this supervisor session

The reason: Worker spawning introduces isolation, which blocks MCP access. The shell loop in `run-wip-loop-unattended.sh` keeps everything in the supervisor context where MCP is available.

## Startup check

🔴 **First:** `mcp__wairz__*` tools should be directly callable as normal tool calls — the
unattended runner sets `ENABLE_TOOL_SEARCH=false` precisely so these tools are pre-loaded and
callable from turn 1, with no `ToolSearch` round-trip needed. **Just call them directly**,
e.g. `mcp__wairz__list_projects()` or `mcp__wairz__run_ghidra_headless(...)` — the same way you'd
call `Read` or `Bash`.

If a direct call to an `mcp__wairz__*` tool fails as "unknown tool" (only possible if running
interactively without `ENABLE_TOOL_SEARCH=false`), call `ToolSearch` once to load it, then
retry the real call:
```
ToolSearch({ query: "select:mcp__wairz__run_ghidra_headless,mcp__wairz__list_projects", max_results: 5 })
```

**It is FORBIDDEN to mark the [NEXT] item `[BLOCKED]` for any of these reasons:**
- "MCP tools are deferred / require ToolSearch / not directly callable"
- "harness tool-calling mechanism" prevents invocation, or any rephrasing of the above
- Manually scripting JSON-RPC / `docker exec` / subprocess plumbing to talk to wairz instead of
  just calling the `mcp__wairz__*` tool as a normal tool call — **never do this**, it is always
  wrong; the tool call interface handles the transport, you never touch stdio/docker yourself

If you find yourself about to write a blocked-reason that describes *the tool-calling mechanism
itself* rather than a real external constraint (hardware, user input, credentials), stop — just
call the tool directly (ToolSearch first only if the direct call errors) and continue the actual
RE work instead.

Only treat MCP as genuinely unavailable if a direct `mcp__wairz__*` call (after `ToolSearch` if
needed) errors with a real backend failure — not a "not found"/"not callable" error, which means
you skipped loading it. That is the only legitimate basis for an MCP-unavailable `[BLOCKED]`.

🔴 **Second:** Check wairz blockers
```bash
.claude/skills/wip-iteration/scripts/check-blockers.sh
```
If [TODO] items exist in `wairz_requested_changes.txt` → EXIT with error. Fix those blockers first before retrying the skill.

**[DONE] entries in `wairz_requested_changes.txt` describe fixes that already shipped — read
them before blocking.** If the [NEXT] item's blocker description matches a `[DONE]` entry's
"Issue:" text (e.g. "class not found"/`DecompilerCallback` errors when writing a custom batch
Ghidra script), use the fix in that entry's "Resolution:" line instead of re-blocking. For batch
decompilation specifically: call `mcp__wairz__batch_decompile_functions` directly (5-10
functions/call) — **never write a new `BatchDecompile*.java` script**, that path is unsupported
in Ghidra headless mode and will always reproduce the already-fixed ClassNotFoundException.

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

6. **Update the real analysis docs BEFORE committing — not just work-in-progress.txt.** Per
   CLAUDE.md ("Documentation Always Comes After Work"), every RE iteration that decompiles or
   renames anything must update **all four**, whether or not your commit message happens to
   mention them:
   - The phase-specific `analysis/rom/reverse_engineering_region_0x*.md` doc
   - `analysis/rom/rom_function_index.md`
   - **`analysis/INDEX.md`** — update that region's one-line summary to match what you just
     wrote in the region doc (latest Pass, coverage counts, [NEXT] pointer). This one is easy to
     silently skip because nothing forces you to *claim* it in the commit message the way the
     other two get cited with `→ file.md` notes — that silence is exactly how it gets missed.
   - `work-in-progress.txt`

   **Before running `git commit`, run `git status` / `git diff --stat` and confirm all four
   files above appear as modified** — not just the ones your own summary happened to claim.
   If `analysis/INDEX.md` isn't in the diff, go update it now before committing. A commit
   message or work-in-progress.txt entry that *claims* documentation was updated is not a
   substitute for actually updating it, and an *absence* of a claim is not a license to skip
   a file either.

7. **Commit:** Stage files, create commit with summary of work done

**DO NOT leave [NEXT] as [NEXT] when exiting.** Rename to [DONE] or [BLOCKED].
