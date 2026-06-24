---
name: wip-loop-overnight
description: >-
  Unattended overnight WIP iteration: execute work-in-progress.txt [NEXT] steps,
  mark items [BLOCKED] when blocked (hardware, user paste, missing tools), promote
  the next [TODO], commit, and keep looping. Use when the user runs wip-loop before
  sleep, wants maximum reverse-engineering progress, or says overnight/unattended
  wip-loop with skip-blocked.
disable-model-invocation: false
---

# WIP loop — overnight / unattended

Ported from the Cursor skill at `.cursor/skills/wip-loop-overnight/SKILL.md` —
same logic, adapted for the Claude Code Agent tool. Conceptually like the
daytime `.cursor/skills/wip-loop/SKILL.md`, but **never stop** because a step
needs hardware, user input, or an external dependency. Mark it `[BLOCKED]` and
continue with the next `[TODO]`.

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

**Wrong:** hardware step still `[NEXT]` with "awaiting journalctl".
**Right:** `[BLOCKED]` "needs NeoPC journalctl (2026-06-10)" + first `[TODO]` → `[NEXT]`.

## Roles

| Role | Who | Job |
|------|-----|-----|
| **Worker** | `Agent` tool, `subagent_type: general-purpose` | One `[NEXT]` item — finish, or block + promote |
| **Recovery worker** | `Agent` tool, `subagent_type: general-purpose` | Mark stuck `[NEXT]` → `[BLOCKED]`, promote first `[TODO]` |
| **Supervisor** | Outer agent (you, running this skill) | Snapshot, launch worker, `delta-overnight`, commit, loop |

## Scripts

```bash
.claude/skills/wip-loop-overnight/scripts/wip-state-overnight.sh count
.claude/skills/wip-loop-overnight/scripts/wip-state-overnight.sh snapshot /tmp/wip-before.txt
.claude/skills/wip-loop-overnight/scripts/wip-state-overnight.sh delta /tmp/wip-before.txt
.claude/skills/wip-loop-overnight/scripts/wip-state-overnight.sh bisect-mode
```

These are self-contained copies (including the base `wip-state.sh`) — they do
not depend on `.cursor/` being present.

## When to mark `[BLOCKED]` (not leave `[NEXT]`)

Use `[BLOCKED]` — never an open `[NEXT]` — when the step **cannot** be finished in
this environment:

- NeoPC / UB500 hardware test, `journalctl` paste, `try_new_firmware.sh`
- Bisect SPLIT waiting on flash result
- **Missing MCP/tool access** (but first verify: use `isolation: false` when spawning workers — worktree isolation blocks MCP access)
- User decision or credentials required

Do **not** block for hard failures (build broken, decompile error, wrong approach) —
use `[FAILED]` or fix inline. Supervisor **stops** on `[FAILED]`.

**MCP access note:** If a worker can't access wairz tools due to "worktree isolation"
or "CLI sandboxing," the supervisor should re-spawn with `isolation: false` instead
of marking the item `[BLOCKED]`. The worker can then execute MCP tool invocations.

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

**Bisect** (`BISECT_MODE=yes`) — read `.cursor/skills/byte-split-bisect/SKILL.md`
for the bisect mechanics (the doc is tool-agnostic; only this orchestration layer
is Claude-Code-specific), then run the same flow as the default worker except:

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
.claude/skills/wip-loop-overnight/scripts/wip-state-overnight.sh snapshot /tmp/wip-before.txt
.claude/skills/wip-loop-overnight/scripts/wip-state-overnight.sh count
```

### 2. Launch worker

Pick prompt: bisect (overnight rules) / default / first TODO / stop if no work.

Use the **Agent** tool with `subagent_type: general-purpose` and **`isolation: false`**
to enable MCP access (workers need full environment to invoke wairz tools). Wait for
completion before continuing (do not run the next iteration in parallel).

Example:
```
Agent({
  description: "Overnight wip-loop worker",
  subagent_type: "general-purpose",
  isolation: false,
  prompt: "..."
})
```

**Verify ticket closure:** after each worker, the active line must not still be
`[NEXT]` unless `[DONE]` or `[BLOCKED]` rose. Open `[NEXT]` → `needs_block` → recovery.

### 3. Evaluate

```bash
.claude/skills/wip-loop-overnight/scripts/wip-state-overnight.sh delta /tmp/wip-before.txt
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

**Bisect:** skip the Cursor-side `split-bisect-state.sh post-worker` handoff stop;
use `delta` (above) only. On hardware paste, use the normal bisect worker flow (not
the overnight block rule).

### 4. Commit

Follow the repository's standard commit protocol (`git status`/`diff`/`log` first,
stage only relevant files, create a new commit — never amend). Commit messages for
blocked steps:

```
Block <title>: needs NeoPC hardware test

Promote next TODO; overnight wip-loop.
```

### 5. Loop or stop

**CRITICAL: NEVER STOP WHILE [NEXT] EXISTS.**

- If `NEXT=1` (exactly 1 [NEXT] item): **MUST loop** → go to step 1 (no exceptions)
- If `NEXT=0` AND `TODO>0`: Launch recovery worker to fix ticket closure, then loop
- If `NEXT=0` AND `TODO=0`: All work done → stop and report final counts
- Progress tracking: Write detailed completion notes to commit messages (do NOT stop to report)

Stop ONLY on:
  - `[FAILED]` (hard build/RE failure)
  - `stuck` (recovery failed twice on same [NEXT] item)
  - MAX_ITERATIONS reached AND [NEXT] item completed
  - `NEXT=0` AND `TODO=0` (all work done)

**DO NOT STOP for**:
  - `[BLOCKED]` items (mark as blocked, promote [TODO], continue)
  - `no_progress` (run recovery worker, continue)
  - Pauses for progress reporting (write to commit, continue looping)

## Safety limits

- Default **max iterations: 300** unless the user sets another limit.
- **Loop continues indefinitely while [NEXT] exists** — max iterations only constrains total attempts, does NOT stop mid-work.
- Stop on `[FAILED]` or `stuck` (recovery failed twice).
- Do **not** stop on `no_progress` — run recovery instead.
- Never force-push or skip hooks.
- Never commit unless this loop's own commit step calls for it — do not push.
- **All progress (DONE count, findings, analysis) is recorded in commit messages and work-in-progress.txt.**

## Quick start

```
/wip-loop-overnight
Loop until stop. Max 300 iterations.
```

or, conversationally:

```
Run the wip-loop-overnight skill. Loop until stop. Max 300 iterations.
```

## vs. daytime wip-loop (Cursor: `.cursor/skills/wip-loop/SKILL.md`)

| Situation | wip-loop (daytime) | wip-loop-overnight |
|-----------|---------------------|---------------------|
| Hardware / handoff | Stop (`no_progress` / `bisect_handoff`) | `[BLOCKED]`, next TODO |
| `[BLOCKED]` rises | Stop | Commit + continue |
| `no_progress` | Stop | Recovery worker → continue |
| Default max iter | 10 | 300 |

Manual single step: use the **default overnight worker** prompt above (not the
short wip-loop prompt).
