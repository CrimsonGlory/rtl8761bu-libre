---
name: wip-loop-overnight
description: >-
  Unattended overnight WIP iteration: execute work-in-progress.txt [NEXT] steps,
  mark items [BLOCKED] when blocked (hardware, user paste, missing tools), promote
  the next [TODO], commit, and keep looping. Use when the user runs wip-loop before
  sleep, wants maximum reverse-engineering progress, or says overnight/unattended
  wip-loop with skip-blocked.
disable-model-invocation: true
---

# WIP loop — overnight / unattended

Like [wip-loop](../wip-loop/SKILL.md), but **never stop** because a step needs
hardware, user input, or an external dependency. Mark it `[BLOCKED]` and continue
with the next `[TODO]`.

Designed to run before sleep so RE/implementation work advances as far as possible.

## Roles

| Role | Who | Job |
|------|-----|-----|
| **Worker** | Task subagent (`generalPurpose`) | One `[NEXT]` item — finish, or block + promote |
| **Recovery worker** | Task subagent | Mark stuck `[NEXT]` → `[BLOCKED]`, promote first `[TODO]` |
| **Supervisor** | Outer agent | Snapshot, launch worker, `delta-overnight`, commit, loop |

## Scripts

```bash
.cursor/skills/wip-loop-overnight/scripts/wip-state-overnight.sh count
.cursor/skills/wip-loop-overnight/scripts/wip-state-overnight.sh snapshot /tmp/wip-before.txt
.cursor/skills/wip-loop-overnight/scripts/wip-state-overnight.sh delta /tmp/wip-before.txt
.cursor/skills/wip-loop-overnight/scripts/wip-state-overnight.sh bisect-mode
```

## When to mark `[BLOCKED]`

Worker marks `[NEXT]` → `[BLOCKED]` (with date + reason) and promotes first
`[TODO]` → `[NEXT]` when the step **cannot** be finished in this environment:

- NeoPC / UB500 hardware test, `journalctl` paste, `try_new_firmware.sh`
- Bisect SPLIT waiting on flash result
- Missing MCP/tool access the worker cannot obtain
- User decision or credentials required

Do **not** block for hard failures (build broken, decompile error, wrong approach) —
use `[FAILED]` or fix inline. Supervisor **stops** on `[FAILED]`.

## Worker prompts

**Default** (`BISECT_MODE=no`):

```
Overnight wip-loop worker step.

Check work-in-progress.txt and continue with the [NEXT] item.

If you complete the step: mark [NEXT] → [DONE], promote the first [TODO] → [NEXT]
(if applicable), write analysis/docs, build when relevant.

If you CANNOT complete the step (hardware, user paste, missing device, external
dependency): mark [NEXT] → [BLOCKED] with today's date and a one-line reason,
promote the first [TODO] → [NEXT]. Do not leave the item stuck as [NEXT].

Repo: /root/rtl8761bu-libre — read CLAUDE.md; use wairz MCP for Ghidra RE.
```

**First TODO** (no `[NEXT]`, but `[TODO]` exists):

```
Overnight wip-loop worker step.

Check work-in-progress.txt and work on the first [TODO] item (promote it to
[NEXT] first if needed). Same completion and [BLOCKED] rules as the default
overnight worker prompt.
```

**Bisect** (`BISECT_MODE=yes`) — same as wip-loop bisect worker, except:

- No hardware paste in prompt → **block** the bisect `[NEXT]` (reason: needs NeoPC
  flash), promote first `[TODO]`, do **not** stage HANDOFF and stop the loop.
- Hardware paste provided → normal bisect flow (log, apply-result, continue).

**Recovery** (supervisor runs when `STOP_REASON=needs_block`):

```
Overnight wip-loop recovery step.

The previous worker made no progress on the active [NEXT] in work-in-progress.txt.
Mark that item [BLOCKED] with today's date and a one-line reason. Promote the
first [TODO] to [NEXT]. Only edit work-in-progress.txt — no feature work.
```

## Supervisor loop

Repeat until stop:

### 1. Snapshot

```bash
.cursor/skills/wip-loop-overnight/scripts/wip-state-overnight.sh snapshot /tmp/wip-before.txt
.cursor/skills/wip-loop-overnight/scripts/wip-state-overnight.sh count
```

### 2. Launch worker

Pick prompt: bisect (overnight rules) / default / first TODO / stop if no work.

Use **Task** tool (`generalPurpose`). Wait for completion.

### 3. Evaluate

```bash
.cursor/skills/wip-loop-overnight/scripts/wip-state-overnight.sh delta /tmp/wip-before.txt
```

| `STOP_REASON` | Action |
|---------------|--------|
| `continue` / `todo_continue` | Commit if `SHOULD_COMMIT=yes`, loop |
| `complete` | Commit, stop (all work finished) |
| `blocked_continue` | Commit (WIP blocked + promoted), loop |
| `blocked_complete` | Commit, stop (only blocked items left) |
| `needs_block` | Run **recovery worker**, then loop (no commit unless recovery changed files) |
| `failed` | Stop, report |
| `no_work` | Stop |

If recovery runs but `delta` is `needs_block` again on the **same** line twice in a
row → stop (`STOP_REASON=stuck`).

**Bisect:** skip `split-bisect-state.sh post-worker` handoff stop; use
`delta-overnight` only. On hardware paste, use normal bisect worker (not overnight
block rule).

### 4. Commit

Same protocol as wip-loop. Commit messages for blocked steps:

```
Block <title>: needs NeoPC hardware test

Promote next TODO; overnight wip-loop.
```

### 5. Loop or stop

`SHOULD_CONTINUE=yes` → step 1. Otherwise report counts and list new `[BLOCKED]` items.

## Safety limits

- Default **max iterations: 300** unless the user sets another limit.
- Stop on `[FAILED]` or `stuck` (recovery failed twice).
- Do **not** stop on `no_progress` — run recovery instead.
- Never force-push or skip hooks.

## Quick start

```
Run the wip-loop-overnight skill. Loop until stop. Max 300 iterations.
```

## vs wip-loop

| Situation | wip-loop | wip-loop-overnight |
|-----------|----------|-------------------|
| Hardware / handoff | Stop (`no_progress` / `bisect_handoff`) | `[BLOCKED]`, next TODO |
| `[BLOCKED]` rises | Stop | Commit + continue |
| `no_progress` | Stop | Recovery worker → continue |
| Default max iter | 10 | 300 |

Manual single step: use the **default overnight worker** prompt above (not the
short wip-loop prompt).
