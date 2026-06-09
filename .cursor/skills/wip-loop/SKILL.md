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

## Bisect `[NEXT]` lines

When the active `[NEXT]` line matches an installer-prefix / byte-split bisect task
(check with `wip-state.sh bisect-mode`), use the **bisect worker** instead of the
default WIP prompt:

```
Byte-split bisect worker (wip-loop step).

Read .cursor/skills/byte-split-bisect/SKILL.md.
Load fw_to_test/bisect-state.yaml via split-bisect-state.sh show.
Execute the [NEXT] line in work-in-progress.txt.

If interval already found (status=interval_found): write FINAL REPORT to analysis/,
mark [NEXT] → [DONE], promote next [TODO] → [NEXT] if applicable.
Else if no hardware paste in this prompt: compute next SPLIT, stage, stage-pending,
return HANDOFF (do not mark [DONE]).
If hardware result provided: log results.md, apply-result, then stage next or finalize.

Update bisect-state.yaml after every mutation.
```

**Matching `[NEXT]` examples** (case-insensitive):

- `Bisect installer prefix for connect`
- `byte-split bisect sub_installer_2`
- `SPLIT bisect until interval <= 0x10`

After a bisect worker finishes, evaluate with:

```bash
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh post-worker
```

| `STOP_REASON` | Supervisor action |
|---------------|-------------------|
| `bisect_handoff` | Stop loop; show user pending SHA from `bisect-state.yaml`; **do not** treat as `no_progress` |
| `bisect_complete` | Run normal `wip-state.sh delta`; commit if `[DONE]` rose |
| `bisect_no_handoff` | Fall through to normal `delta` (worker error or non-bisect outcome) |

When user returns with NeoPC paste, resume wip-loop — same `[NEXT]` stays active
until the worker marks `[DONE]` on `interval_found`.

## Worker prompts

**Default** — use when `work-in-progress.txt` has at least one `[NEXT]` line
and `bisect-mode` is `no`:

```
Check work-in-progress.txt continue with NEXT step
```

**First TODO** — use when there is no `[NEXT]` but at least one `[TODO]` remains (typical after a worker added `[DONE]` but forgot to promote the first `[TODO]` to `[NEXT]`):

```
Check work-in-progress.txt and work on the first [TODO] item
```

Add project context only if the subagent needs it (repo root, `CLAUDE.md`, wairz for Ghidra).
Do not change these prompts unless the user asks.

## Supervisor loop

Repeat until a stop condition fires:

### 1. Snapshot

```bash
.cursor/skills/wip-loop/scripts/wip-state.sh snapshot /tmp/wip-before.txt
.cursor/skills/wip-loop/scripts/wip-state.sh count
```

### 2. Launch worker

```bash
.cursor/skills/wip-loop/scripts/wip-state.sh bisect-mode
```

Pick the prompt before launching:

- `[NEXT]` exists + `BISECT_MODE=yes` → **bisect worker** prompt (above)
- `[NEXT]` exists + `BISECT_MODE=no` → **default** worker prompt
- no `[NEXT]` but `[TODO]` exists → **first TODO** worker prompt
- neither → stop (`STOP_REASON=no_work`)

Use the **Task** tool:

- `subagent_type`: `generalPurpose`
- `description`: short label for the current `[NEXT]` or first `[TODO]` line (read from `work-in-progress.txt`)
- `prompt`: the chosen worker prompt above, plus any minimal paths the subagent needs

Wait for the subagent to finish before continuing.

### 3. Evaluate progress

If bisect worker ran:

```bash
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh post-worker
```

When `STOP_REASON` is `bisect_handoff` or `bisect_complete`, use that output for
commit/continue (skip `delta` for handoff; for `bisect_complete` also run `delta`).

Otherwise:

```bash
.cursor/skills/wip-loop/scripts/wip-state.sh delta /tmp/wip-before.txt
```

Parse `SHOULD_COMMIT`, `SHOULD_CONTINUE`, `STOP_REASON`, and `DONE_DELTA`.

| `STOP_REASON` | Meaning |
|---------------|---------|
| `continue` | `[DONE]` rose by ≥1, `[NEXT]` still exists → commit, then loop |
| `todo_continue` | `[DONE]` rose by ≥1, no `[NEXT]`, but `[TODO]` remains (worker did not promote first `[TODO]` to `[NEXT]`) → commit, then loop with **first TODO** prompt |
| `complete` | `[DONE]` rose by ≥1, no `[NEXT]` and no `[TODO]` left → commit, then stop |
| `no_progress` | `[DONE]` did not rise → stop without commit |
| `bisect_handoff` | Bisect worker staged SPLIT; user must flash NeoPC → stop, commit if results updated |
| `bisect_complete` | Critical interval found; `[DONE]` should rise → commit, stop |
| `blocked` | `[BLOCKED]` count rose → stop without commit; report to user |
| `failed` | `[FAILED]` count rose → stop without commit; report to user |

**Progress rule:** `[DONE]` must increase by **at least 1**. Multiple new `[DONE]` lines in one step is fine.

**Forgot-to-promote rule:** If the worker added `[DONE]` but left the first item as `[TODO]` (no `[BLOCKED]` / `[FAILED]`), treat it as progress: commit and continue. The next iteration automatically uses the **first TODO** prompt (step 2).

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

For one step without the loop, paste the default worker prompt, or the **first TODO** prompt if there is no `[NEXT]`:

```
Check work-in-progress.txt continue with NEXT step
```

```
Check work-in-progress.txt and work on the first [TODO] item
```

Then commit yourself after verifying the active item moved to `[DONE]` and the next `[TODO]` became `[NEXT]` (when applicable).
