# RTL8761BU Libre Firmware — Project Context for Claude

## Goal

Produce a free/libre replacement for `rtl8761bu_fw.bin` suitable for inclusion in
linux-libre. The non-free binary is a runtime patch for the RTL8761BU Bluetooth 5.1
USB chip (UB500 dongle). The Linux `btrtl` driver loads it at boot via HCI VSC `0xFC20`.

This work continues Xeno Kovah's (Dark Mentor LLC) public reverse-engineering research
presented at Hardwear.io NL 2025.

### Secondary goal (nice-to-have): documenting the ROM

The RTL8761BU mask ROM (`0x80000000 – 0x8007ffff`) is fixed in chip silicon —
it cannot be reflashed, replaced, or otherwise modified on real hardware. RE'ing
it produces no shipping deliverable; it is purely theoretical/educational, done
to build a complete documented understanding of the chip's Bluetooth stack
alongside the firmware replacement work. Lower priority than the primary goal.
ROM-only findings live in `analysis/rom/`.

---

## Reverse-Engineering Tool Stack

All disassembly and decompilation is done through **wairz** (MCP server) using
`mcp__wairz__run_ghidra_headless`.

**CRITICAL — MCP tools must be called as tool calls, never reimplemented manually.**
`run-wip-loop-unattended.sh` sets `ENABLE_TOOL_SEARCH=false`, so `mcp__wairz__*` tools are
pre-loaded and directly callable from turn 1 — just call `mcp__wairz__run_ghidra_headless(...)`
like any other tool (Read, Bash, etc.). Do not write Python/bash that manually does JSON-RPC
or `docker exec` against the wairz container — that is never correct; the tool-calling
interface (this harness) owns the transport, and hand-rolling it will hang or fail in ways
that look like "MCP unavailable" but are actually self-inflicted.

If running interactively without `ENABLE_TOOL_SEARCH=false` and a direct `mcp__wairz__*` call
errors as "unknown tool," call `ToolSearch({ query: "select:mcp__wairz__<name>" })` once to load
its schema, then retry the real call. Only conclude MCP is genuinely unavailable if that real
call (after loading, if needed) errors with an actual backend failure — never because a manual
subprocess/stdio workaround didn't pan out. Do **not** exit or ask to "respawn with isolation:
false" based on a direct-call or manual-workaround failure alone.

**SUPERVISOR NOTE — Unattended loops must stay in supervisor context**: For overnight unattended
work, use `./run-wip-loop-unattended.sh` (shell-level loop at supervisor scope) rather than
`/wip-loop-overnight` (which spawns workers). The shell loop keeps MCP access available by running
each iteration with `claude -p /wip-iteration` in the supervisor context. **The `/wip-iteration`
skill MUST NOT spawn any workers or agents** — all RE/decompilation/analysis work is done directly
in that session. Worker spawning introduces isolation barriers that block MCP access, breaking
the entire pipeline. If you must run `wip-iteration` interactively (not via shell loop), ensure
the Claude Code environment itself has MCP access enabled before starting.

**IMPORTANT — wairz modifications**: If any limitation in wairz blocks analysis
(missing tool, unsupported mode, wrong behavior), do NOT try to work around it
silently. Instead, tell the user explicitly what change is needed in wairz and
they will implement it. Do not attempt to patch wairz yourself.

**wairz limitation tracking**: Document any needed wairz fixes in `wairz_requested_changes.txt`
with a `[TODO]` entry. Mark with `[DONE]` when the fix is confirmed to be in place.
The `wip-loop-overnight` skill will check this file on startup and exit if there are
any `[TODO]` items, preventing work from proceeding while blocking issues remain.

### Primary binary

```
2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
```

This is Kovah's annotated Ghidra project archive (.gzf). It contains **three memory
blocks**:

| Block | Address range | Content |
|-------|--------------|---------|
| `patch` | `0x00000000 – 0x0000adc3` | Patch firmware at file-relative addresses |
| `data`  | `0x80100000 – 0x8013ffff` | Runtime RAM image (patch loaded by ROM) |
| `rom`   | `0x80000000 – 0x8007ffff` | ROM dump with Kovah's annotations |

**Use this GZF as the binary_path**, not the raw `rtl8761bu_fw.bin`.

### Address mapping

When the chip runs, the patch is loaded at `0x80100000`. The patch file itself is
44484 bytes (`0x0000 – 0x0000adc3`). File-relative address to runtime:

```
runtime = 0x80100000 + file_offset
```

ROM functions are at `0x8000xxxx` in both the GZF and on the real chip.

### Ghidra scripts

Scripts live in wairz at `/root/wairz/ghidra/scripts/`. Full index with run
instructions and one-line purpose per script:
[`GHIDRA_SCRIPTS.md`](GHIDRA_SCRIPTS.md). Keep that index current, not this
file — see the rule at the bottom of `GHIDRA_SCRIPTS.md`.

---

## Key Runtime Addresses

| Address | Description |
|---------|-------------|
| `0x80103780` | Master patch installer (`thing_that_calls_thing_that_installs_LMP_Patch`) |
| `0x8010bba4` | LMP VSC hook (installed at `bos_base+0xd8`) |
| `0x8004f824` | ROM: slot-budget validator + hardware hook dispatcher |
| `0x801212e4` | **RAM hook slot**: checked by `FUN_8004f824`; patch installs hw-write fn here |
| `0x801212e0` | RAM hook slot: checked by ROM `FUN_80050810` |
| `0x80121200` | Approx. `bos_base` (BT state struct base; `bos_base+0xd8 = 0x801212d8`) |
| `0x8000fd38` | ROM: `copies_config_bdaddr` — reads BD_ADDR from config blob |
| `0x8001136c` | ROM: HW register-read fn (used by FUN_80009980 via thunk/s0) |
| `0x8001139c` | ROM: HW register-write fn (used by FUN_80009980 via s1) |
| `0x80109980` | HW register init (programs 7 baseband regs; reads bos+0x168/0x16a) |
| `0x80109c08` | eSCO packet type negotiator (connection types 0xA000/0xB000/0xE/0xF000) |
| `0x80109de0` | Hamming-weight checksum (popcount of first 10 bytes) |
| `0x8010A160` | MIPS16e handler stubs: 8-byte bteqz/lw/bteqz/addiu pattern, 36 entries |
| `0x801000A0` | Address-pair table (15 × 8-byte entries: data_ptr, fn_ptr) |
| `0x80009990` | ROM: `interesting_string_user_fptr_registration_function` (table processor) |

## Key Struct Offsets (`bos_base` / `puVar6`)

| Offset | Hook installed |
|--------|---------------|
| `+0x1c` | `LAB_8010bc74` |
| `+0xd8` | LMP VSC dispatch → `LAB_8010bba4` |
| `+0xe0` | `FUN_80050810` type-dispatch hook |
| `+0xe4` | `FUN_8004f824` hardware-write hook ← **target of current investigation** |
| `+0x20/0x24` | HCI event handlers |
| `+0x30` | Connection handler → `LAB_8010c088` |
| `+0x50` | `LAB_8010f884` |

---

## Findings — Already Documented

All findings go in `/root/rtl8761bu-libre/analysis/`, split into `firmware/`
(patch/RAM-loaded firmware — primary goal) and `rom/` (silicon ROM —
secondary/nice-to-have goal), with cross-cutting design and process docs at
the `analysis/` root. Full index with one-line summaries:
[`analysis/INDEX.md`](analysis/INDEX.md). Keep that index current, not this
file — see the rule at the bottom of `INDEX.md`.

**IMPORTANT — Documentation Always Comes After Work:** After completing any RE task
(decompilation, analysis, fix), immediately update:
1. **Phase-specific analysis docs** (e.g., `analysis/rom/reverse_engineering_region_0x80030000.md`)
   — add findings in a timestamped Pass section
2. **ROM function index** (`analysis/rom/rom_function_index.md`)
   — update summary counts + add/update function entries with HIGH/MEDIUM/LOW confidence
3. **Work-in-progress.txt** — mark task [DONE] + document findings

Do not leave documentation as "step 1 and 2" for later — update docs immediately
after executing analysis work, before committing. This keeps the authoritative
analysis trail current and prevents stale TODOs.

---

## Firmware Format (raw `rtl8761bu_fw.bin`)

- 42088 bytes total
- Header: 9-byte magic `Realtechk`, 4-byte fw_version, `num_patches=2`, `0x30` = code start
- Master installer at file offset `0x3780` (runtime `0x80103780`)
- Loaded by Linux `btrtl` driver via HCI VSC `0xFC20`
- Config blob (BD_ADDR + HW config) appended after binary; read by ROM `copies_config_bdaddr`

---

## Non-free reference files

| Path | Notes |
|------|-------|
| `rtl8761bu-non-free/rtl8761bu_fw.bin` | Original Realtek binary (for diff/reference only) |
| `rtl8761bu-libre/rtl8761bu_fw.bin` | Current libre build output |
| `rtl8761bu-libre/build/patch.bin` | Compiled patch blob |

Build system is in `rtl8761bu-libre/` (Makefile + MIPS16e toolchain, Dockerized).

---

## ISA Notes

- MIPS16e (16-bit compressed MIPS). All chip code is MIPS16e.
- PC-relative `lw rx, imm(pc)` loads from literal pool embedded in code.
- 32-bit stores use 4 sequential `sb` instructions (unaligned peripheral workaround).
- Branch delay slots present after `jalr`/`jr`.
