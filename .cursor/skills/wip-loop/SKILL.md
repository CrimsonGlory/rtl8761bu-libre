---
name: wip-loop
description: >-
  Orchestrate repeated WIP steps: launch subagents to execute work-in-progress.txt
  [NEXT], commit when [DONE] rises, loop until no progress or no [NEXT] remains.
  Use when the user asks to run the WIP loop, auto-continue NEXT steps, or
  hands-off iteration on work-in-progress.txt.
---

# WIP loop orchestrator

Run this skill when the user wants unattended iteration over `work-in-progress.txt`
instead of manually opening a new agent after each step.

## Roles

| Role | Who | Job |
|------|-----|-----|
| **Worker** | Task subagent (`generalPurpose`) | Execute exactly one `[NEXT]` item |
| **Supervisor** | This agent (outer) | Snapshot state, launch worker, commit, decide loop/stop |

## Worker prompt (verbatim)

Pass this exact text to each subagent:

```
Check work-in-progress.txt continue with NEXT step
```

Add project context only if the subagent needs it (repo root, `CLAUDE.md`, wairz for Ghidra).
Do not change the worker prompt unless the user asks.

## Supervisor loop

Repeat until a stop condition fires:

### 1. Snapshot

```bash
.cursor/skills/wip-loop/scripts/wip-state.sh snapshot /tmp/wip-before.txt
.cursor/skills/wip-loop/scripts/wip-state.sh count
```

### 2. Launch worker

Use the **Task** tool:

- `subagent_type`: `generalPurpose`
- `description`: short label for the current `[NEXT]` line (read from `work-in-progress.txt`)
- `prompt`: the worker prompt above, plus any minimal paths the subagent needs

Wait for the subagent to finish before continuing.

### 3. Evaluate progress

```bash
.cursor/skills/wip-loop/scripts/wip-state.sh delta /tmp/wip-before.txt
```

Parse `SHOULD_COMMIT`, `SHOULD_CONTINUE`, `STOP_REASON`, and `DONE_DELTA`.

| `STOP_REASON` | Meaning |
|---------------|---------|
| `continue` | `[DONE]` rose by ≥1, `[NEXT]` still exists → commit, then loop |
| `complete` | `[DONE]` rose by ≥1, no `[NEXT]` left → commit, then stop |
| `no_progress` | `[DONE]` did not rise → stop without commit |
| `blocked` | `[BLOCKED]` count rose → stop without commit; report to user |

**Progress rule:** `[DONE]` must increase by **at least 1**. Multiple new `[DONE]` lines in one step is fine.

### 4. Commit (when `SHOULD_COMMIT=yes`)

Follow the repository commit protocol:

1. `git status`, `git diff`, `git log -1` (parallel)
2. Stage only relevant changes (analysis docs, `work-in-progress.txt`, scripts, etc.)
3. Commit message: summarize the step that moved from `[NEXT]` to `[DONE]` (read the file for the title line)
4. `git status` after commit

Do **not** push unless the user asked.

### 5. Loop or stop

- `SHOULD_CONTINUE=yes` → go to step 1
- Otherwise → report final counts, `STOP_REASON`, and what the worker last did

## Safety limits

- Default **max iterations: 10** per invocation unless the user sets another limit.
- Stop immediately if the same `STOP_REASON=no_progress` happens twice in a row.
- Never force-push or skip hooks.

## Quick start (user)

Paste into a **new outer agent** chat:

```
Run the wip-loop skill. Loop until stop.
```

Optional:

```
Run the wip-loop skill. Max 5 iterations.
```

## Manual single step (unchanged workflow)

For one step without the loop, paste only the worker prompt:

```
Check work-in-progress.txt continue with NEXT step
```

Then commit yourself after verifying `[NEXT]` moved to `[DONE]`.
