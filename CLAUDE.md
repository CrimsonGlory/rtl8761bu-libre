# RTL8761BU Libre Firmware — Project Context for Claude

## Goal

Produce a free/libre replacement for `rtl8761bu_fw.bin` suitable for inclusion in
linux-libre. The non-free binary is a runtime patch for the RTL8761BU Bluetooth 5.1
USB chip (UB500 dongle). The Linux `btrtl` driver loads it at boot via HCI VSC `0xFC20`.

This work continues Xeno Kovah's (Dark Mentor LLC) public reverse-engineering research
presented at Hardwear.io NL 2025.

---

## Reverse-Engineering Tool Stack

All disassembly and decompilation is done through **wairz** (MCP server) using
`mcp__wairz__run_ghidra_headless`.

**IMPORTANT — wairz modifications**: If any limitation in wairz blocks analysis
(missing tool, unsupported mode, wrong behavior), do NOT try to work around it
silently. Instead, tell the user explicitly what change is needed in wairz and
they will implement it. Do not attempt to patch wairz yourself.

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

All findings go in `/root/rtl8761bu-libre/analysis/`. Full index with one-line
summaries: [`analysis/INDEX.md`](analysis/INDEX.md). Keep that index current,
not this file — see the rule at the bottom of `INDEX.md`.

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
