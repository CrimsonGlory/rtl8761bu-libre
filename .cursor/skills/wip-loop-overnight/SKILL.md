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

## Mandatory ticket closure (every worker step)

**Every worker MUST close the active ticket before finishing.** Leaving `[NEXT]`
unchanged is a violation — the supervisor runs a recovery worker.

When you pick up the current `[NEXT]` item:

1. **Finish it** → rename `[NEXT]` to `[DONE]` (add date + summary if the line
   warrants it).
2. **Cannot finish** (hardware, NeoPC paste, user decision, missing device/tool) →
   rename `[NEXT]` to `[BLOCKED]` (add today's date + one-line reason).
3. **Always promote** → rename the **first** remaining `[TODO]` to `[NEXT]` (when
   any `[TODO]` exists).

There is no third outcome. Do **not** leave an item as `[NEXT]` when your turn ends.
Do **not** skip promotion when `[TODO]` items remain.

| Outcome | Tag change | Promote first `[TODO]`? |
|---------|------------|-------------------------|
| Step completed | `[NEXT]` → `[DONE]` | Yes, if any `[TODO]` left |
| Needs user / hardware / external | `[NEXT]` → `[BLOCKED]` | Yes, if any `[TODO]` left |
| Build/RE hard failure | `[FAILED]` (rare) | No — supervisor stops loop |

**Wrong:** hardware step still `[NEXT]` with “awaiting journalctl”.  
**Right:** `[BLOCKED]` “needs NeoPC journalctl (2026-06-10)” + first `[TODO]` → `[NEXT]`.

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

## When to mark `[BLOCKED]` (not leave `[NEXT]`)

Use `[BLOCKED]` — never an open `[NEXT]` — when the step **cannot** be finished in
this environment:

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

Repo: /root/rtl8761bu-libre — read CLAUDE.md; use wairz MCP for Ghidra RE.

Open work-in-progress.txt. Execute exactly ONE item — the current [NEXT] line.

MANDATORY before you finish (no exceptions):
  • Done with the work → [NEXT] → [DONE]
  • Blocked on user/hardware/external → [NEXT] → [BLOCKED] (date + one-line reason)
  • Either way, if any [TODO] remains → rename the FIRST [TODO] to [NEXT]
  • NEVER leave the ticket as [NEXT] when your turn ends

If you completed implementation/RE: write analysis/docs, build when relevant, then
apply the tag changes above.

If blocked (NeoPC flash, journalctl paste, missing UB500, user decision, tool you
cannot access): still apply [BLOCKED] + promote — do not hand off with an open [NEXT].
```

**First TODO** (no `[NEXT]`, but `[TODO]` exists):

```
Overnight wip-loop worker step.

Repo: /root/rtl8761bu-libre — read CLAUDE.md; use wairz MCP for Ghidra RE.

No [NEXT] is set. Rename the FIRST [TODO] to [NEXT], then execute it.

MANDATORY before you finish:
  • [NEXT] → [DONE] if completed, or [NEXT] → [BLOCKED] if needs user/hardware
  • Promote the next first [TODO] → [NEXT] when any [TODO] remains
  • NEVER leave your ticket as [NEXT]
```

**Bisect** (`BISECT_MODE=yes`) — same as wip-loop bisect worker, except:

- No hardware paste in prompt → `[NEXT]` → `[BLOCKED]` (reason: needs NeoPC flash),
  promote first `[TODO]` → `[NEXT]`; do **not** stage HANDOFF or leave `[NEXT]` open.
- Hardware paste provided → normal bisect flow (log, apply-result); on completion
  `[NEXT]` → `[DONE]` and promote next `[TODO]`.

**Recovery** (supervisor runs when `STOP_REASON=needs_block`):

```
Overnight wip-loop recovery step.

The previous worker violated ticket closure: the active item is still [NEXT] in
work-in-progress.txt.

Fix ONLY work-in-progress.txt:
  1. Rename that [NEXT] → [BLOCKED] (today's date + reason: worker left ticket open)
  2. Rename the first [TODO] → [NEXT]
Do not do feature work.
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

**Verify ticket closure:** after each worker, the active line must not still be
`[NEXT]` unless `[DONE]` or `[BLOCKED]` rose. Open `[NEXT]` → `needs_block` → recovery.

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
