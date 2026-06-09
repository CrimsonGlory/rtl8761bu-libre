---
name: byte-split-bisect
description: >-
  Binary-search byte regions in firmware images via SPLIT builds (vendor prefix +
  libre/tail splice), stage UB500 tests, log results, narrow critical intervals.
  Use when bisecting patch1 installer bytes, prefix/tail splits, inject profiles,
  or when the user asks for byte split testing / SPLIT bisect. Main agent delegates
  to a Task subagent; subagent returns a concise report when the interval is found.
disable-model-invocation: true
---

# Byte split bisect

Binary-search **which file offsets must match vendor** by building profiles with a
contiguous **vendor prefix** `[0, SPLIT)` and holding other regions fixed (libre or
vendor tail per profile).

**Do not iterate the full bisect in the main agent** — delegate to a worker subagent
and only relay user hardware pastes + final summaries.

## Roles

| Role | Who | Job |
|------|-----|-----|
| **Supervisor** | Main agent | Launch worker, pass hardware results, show user handoff SHA |
| **Worker** | Task subagent (`generalPurpose`) | Stage next SPLIT, log results, narrow interval, final diff report |

Read [ub500-hardware-test](../ub500-hardware-test/SKILL.md) for `results.md` / NeoPC rules.

## Persistent state

**File:** `fw_to_test/bisect-state.yaml` (repo root, tracked in git).

Workers **read on start** and **write after every step** — supervisors resume from
this file instead of re-parsing chat history.

```bash
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh show
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh init 0x700 0x800   # fresh
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh apply-result fail 0x770
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh stage-pending 0x778 <sha256>
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh next-split
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh is-done
```

| `status` | Meaning |
|----------|---------|
| `active` | Narrowing; may stage next SPLIT |
| `waiting_hardware` | Build staged; user must flash NeoPC |
| `interval_found` | `pass_hi - fail_lo <= min_interval`; emit FINAL REPORT |

## SPLIT semantics (critical)

`SPLIT=N` on `inject-t3-vendor-tail-split` (and similar) means:

| Region | Source |
|--------|--------|
| `[0, N)` | **vendor** |
| `[N, 0xE4C)` | **libre** (linked installer + callees) |
| `[0xE4C, footer)` | **vendor tail** (when `VENDOR_TAIL_FILL=1`) |

Plus hook overlays from `inject_vendor.py` (T1–T3) unless profile says otherwise.

### Interpreting hardware results

| Outcome at SPLIT=N | Meaning |
|--------------------|---------|
| **FAIL** (FC20 timeout, no controller) | Prefix **N too short** — critical bytes include `[N, pass_hi)` |
| **OK** (FC20 loads) | Prefix **N suffices** for load — update `pass_hi = N` |
| Connect OK at N | Strong pass — libre may start at N |

**Narrowing rule:** when `FAIL` at `fail_lo` and `OK` at `pass_hi`, the **last**
vendor bytes required are **`[fail_lo, pass_hi)`** — the chunk that was still libre
at `fail_lo` and became vendor at `pass_hi`. Do **not** say “only those bytes matter
in the whole image”; earlier prefix length is still required.

## Default profile (RTL8761BU connect bisect)

| Constant | Value |
|----------|-------|
| `INSTALLER_END` | `0xE4C` |
| `PATCH1_BASE` | `0x8010A000` |
| Make target | `full-inject-t3-vendor-tail-split` |
| Stage | `.cursor/skills/ub500-hardware-test/scripts/stage-test.sh 'full-inject-t3-vendor-tail-split SPLIT=0xN'` |
| `MIN_INTERVAL` | `0x10` (16 B) — stop bisect when `pass_hi - fail_lo <= MIN_INTERVAL` |

Known anchors from UB500 work (2026-06):

- `fail_lo=0`, `pass_hi=0xE4C` bracketed installer; vendor tail required for HCI
- Prefix bisect found critical **`[0x770, 0x780)`** inside `sub_installer_2`

## Worker workflow

Copy this checklist:

```
- [ ] Read bisect state (below)
- [ ] If user pasted hardware result → log results.md + update fail_lo/pass_hi
- [ ] If interval done → diff + symbol map → return FINAL REPORT
- [ ] Else compute next SPLIT → stage → return HANDOFF
```

### 1. Load bisect state

```bash
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh show
```

Fields in `fw_to_test/bisect-state.yaml`:

```yaml
fail_lo: 0x770      # largest SPLIT that still FAILs
pass_hi: 0x780      # smallest SPLIT that OKs
metric: connect     # fc20 | connect
min_interval: 0x10
pending_split:      # set when waiting_hardware
```

Fresh bisect: `init 0x0 0xe4c` (or user-supplied bracket). Verify upper bound with
`test-nf` / `full-vendor-patch1` once if unsure.

### 2. Log hardware result

On user paste (supervisor forwards verbatim):

1. Parse SHA256 from `sha256sum` line
2. Classify: `fc20_fail` | `fc20_ok` | `connect_ok` | `hci_timeout`
3. Append row to `.cursor/skills/ub500-hardware-test/results.md` (full 64-char hash)
4. Run `split-bisect-state.sh apply-result <fail|ok|connect_ok> 0x<N>`
5. Do not re-test the same SHA unless code changed

### 3. Stop condition

Done when `pass_hi - fail_lo <= min_interval`:

```bash
.cursor/skills/byte-split-bisect/scripts/split-bisect.sh diff \
  $fail_lo $pass_hi build/patch_padded.bin ../rtl8761bu-non-free/rtl8761bu_fw.bin
```

Also map symbols (docker):

```bash
docker run --rm -v "$(pwd)":/work rtl8761bu-libre \
  mipsel-linux-gnu-nm build/patch.elf | grep -E 'patch_entry|sub_installer|fn_bss'
```

Return **FINAL REPORT** (template below). Do not stage another SPLIT.

### 4. Next SPLIT

```bash
.cursor/skills/byte-split-bisect/scripts/split-bisect.sh next <fail_lo> <pass_hi>
```

Stage, then record pending handoff:

```bash
.cursor/skills/ub500-hardware-test/scripts/stage-test.sh \
  "full-inject-t3-vendor-tail-split SPLIT=0x<N>"
sha256sum /root/rtl8761bu-libre/fw_to_test/rtl8761bu_fw.bin
.cursor/skills/byte-split-bisect/scripts/split-bisect-state.sh stage-pending 0x<N> <sha256>
```

Return **HANDOFF** (template below).

## Supervisor workflow

### Start bisect

```
Run byte-split bisect worker.

Goal: find critical [fail_lo, pass_hi) for fc20+connect.
Profile: full-inject-t3-vendor-tail-split.
Initial: fail_lo=0x700, pass_hi=0x800 (or fresh per user).
Stage first midpoint and hand off SHA for NeoPC.
```

Use Task tool: `subagent_type=generalPurpose`, `description=byte-split bisect worker`.

### After user pastes NeoPC output

```
Byte-split bisect worker — hardware result:

<paste>

State: fail_lo=0x770 pass_hi=0x780 metric=connect
```

Worker logs, updates bounds, either stages next SPLIT or returns FINAL REPORT.

### Main agent to user (handoff only)

Keep short — do not replay bisect theory:

```markdown
Staged `SPLIT=0x___` → SHA256 `…`
Run `./try_new_firmware.sh`, replug, test connect.
Paste dmesg + result back.
```

## Worker return templates

### HANDOFF (need hardware)

```markdown
## Byte-split handoff
- SPLIT: 0x___
- SHA256: …
- fail_lo: 0x___  pass_hi: 0x___  (width 0x___)
- Expect: [fc20 OK / FAIL / connect OK]

User: ./try_new_firmware.sh → replug → bluetoothctl connect …
```

### FINAL REPORT (interval found)

```markdown
## Byte-split complete
- Critical interval: [0x___, 0x___) — N bytes
- Runtime: 0x8010A___ – 0x8010A___
- Libre vs vendor: (spans from split-bisect.sh diff)
- Symbols: (nm hits in interval)
- Libre fix target: (file:function)
- Recommended: vendor bytes at offset / decompile FUN_…
```

## Build reference

```bash
cd /root/rtl8761bu-libre/rtl8761bu-libre
make full-inject-t3-vendor-tail-split SPLIT=0x800
# env: VENDOR_TAIL_FILL=1 VENDOR_PREFIX_SPLIT=$(SPLIT)
```

| Target | Purpose |
|--------|---------|
| `full-inject-t3-vendor-tail` | libre `[0,E4C)` + vendor tail (no prefix bisect) |
| `full-inject-t3-vendor-tail-split` | prefix bisect |
| `full-vendor-patch1` | entire patch1 vendor (upper bound) |

## Anti-patterns

- **Do not** binary-search in the main agent across many user paste turns
- **Do not** confuse “FAIL at 0x770 → need 32 B” with “additional bytes `[0x770,0x780)`”
- **Do not** commit unless user asked
- **Do not** flash a failed SHA again without a new build

## Quick start (user)

```
Run the byte-split-bisect skill. Continue installer prefix bisect from fail_lo=0x770 pass_hi=0x780.
```

Or fresh:

```
Run byte-split-bisect skill. Bisect installer prefix for connect; hold vendor tail + T3 inject fixed.
```
