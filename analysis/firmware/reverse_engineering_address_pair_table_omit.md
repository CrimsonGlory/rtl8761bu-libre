# Address-pair table @ file `0xA0` — intentional OMIT (2026-06-10)

## Summary

**Verdict: OMIT for all libre profiles (P1–P4).** Do not embed the vendor
15×8-byte `(data_ptr, fn_ptr)` table at file offset `0xA0` / runtime
`0x801000A0`. No libre source, linker section, or pack step should copy it.

Protocol interception is fully covered by `FUN_8010e27c` (six direct struct
installs) plus the 44 DRAM hook installs in `FUN_8010a000`. The table is a
separate, unused artifact in the vendor patch0 header region.

---

## What the vendor table contains

Vanilla `rtl8761bu_fw.bin` (dual-patch, 42,088 B) places 120 bytes at file
offset `0xA0` (runtime `0x801000A0` when patch block is mapped at
`0x80100000`):

| # | data_ptr | fn_ptr | fn in `FUN_8010a000` body? |
|---|----------|--------|------------------------------|
| 0 | `0x8010A02C` | `0x8012005A` | no (data in entry prologue) |
| 1 | `0x80120058` | `0x8010C961` | no |
| 2 | `0x801211A4` | `0x8010A34D` | no (past 578 B body) |
| 3 | `0x8012141C` | `0x8010AC9D` | no |
| 4 | `0x80120D54` | `0x8010AA5D` | no |
| 5 | `0x80120E98` | `0x8010A5E9` | no |
| 6 | `0x80120FC0` | `0x8010A4C1` | no |
| 7 | `0x80120E70` | `0x8010A185` | **yes** (mid-instruction) |
| 8 | `0x801214D0` | `0x8010A175` | **yes** (mid-instruction) |
| 9 | `0x80121498` | `0x8010CA79` | no |
| 10 | `0x80121744` | `0x8010A1D9` | **yes** (mid-instruction) |
| 11 | `0x80120FFC` | `0x8010A209` | **yes** (mid-instruction) |
| 12 | `0x8012506C` | `0x8010A305` | no |
| 13 | `0x80121184` | `0x8010A2B9` | no |
| 14 | `0x80121180` | `0x8010C9D1` | no |

Only four of fifteen `fn_ptr` values fall inside the 578-byte `FUN_8010a000`
code body (`0x8010A000`–`0x8010A241`). None land on MIPS16e prologues; they
are odd addresses into compare/branch sequences — not callable entry points.

Kovah's early label
`patch_that_installs_all_the_string_associated_function_patches__including_LMP`
was a **misidentification**. The real string-assoc installer is `FUN_8010e27c`
(52 B), which writes six handlers to struct `0x8012ae8c` by fixed offsets and
does **not** walk this table.

---

## RE evidence (GZF process mode, 2026-06-10)

### `FindTableProcessor.java`

Full ROM + patch + DATA scan:

- **Zero Ghidra xrefs** to `0x800000A0` / `0x801000A0`
- **Zero literal-pool loads** of `0x801000A0` (or odd `0x801000A1`)
- **Zero functions** in ROM or patch blocks that iterate a table at that address
- Instruction-pattern scan hits only unrelated branch displacements containing
  `0xa0` in the low bits

### DATA block runtime snapshot

Kovah's DATA block at `0x801000A0` is **all zeros** — consistent with either
never consumed, or cleared before the snapshot. No live registrations remain.

### `FUN_8010e27c` is the real dispatch installer

Documented in `reverse_engineering_string_assoc_installer.md` and
`reverse_engineering_protocol_dispatch_layer.md`. All six protocol surfaces
(LMP / LC TX/RX / HCI / passthrough / sequencer) are installed explicitly;
libre `callees.S` / `shims.S` / `protocol_dispatch.S` replicate this path.

### ROM `FUN_80009990` is unrelated

`interesting_string_user_fptr_registration_function` registers ROM hook slots;
it calls patch `FUN_8010e27c`, not the file-`0xA0` table.

---

## Libre firmware layout — table address is out of scope

Single-patch EPatch v2 (linux-libre ship layout):

- FC20 loads **27,808 B** at PRAM **`0x80103780`**, not `0x80100000`
- File offset `0xA0` in the shipped blob is **inside patch1 code** (MIPS16e
  instructions from `patch_entry_code.S`), not a data table
- Runtime `0x801000A0` is **outside** the loaded PRAM window entirely

Hardware validation on profile `full-inject-t3-pe5-vendor-tail` (`7f051e64…`)
achieved FC20 OK, fw `0x09a98a6b`, and ACL connect without embedding any
copy of the vendor table — confirming the connect path does not depend on it.

---

## OMIT decision matrix

| Criterion | Result |
|-----------|--------|
| Consumer in ROM or patch | **None found** |
| Required for FC20 / HCI / ACL | **No** (hardware evidence) |
| Required for eSCO / AFH | **No** (covered by hook + dispatch impl) |
| Fits 27,808 B PRAM window at `0x801000A0` | **No** (wrong load base) |
| linux-libre compliance | Omitting avoids unexplained vendor data |
| Re-open if | Future xref found, or HW regression tied to patch0 header |

---

## Libre implementation note

**Action: none.** No `.incbin`, no `.byte` block, no BSS at `0x801000A0`.

If a future ROM xref appears, the table would need a **dual-section** layout
(expanded envelope with patch0 header region) — explicitly deferred and not
required for UB500 bring-up documented in Phase 7/8.

---

## Cross-references

- `reverse_engineering_sub_installers.md` — original table dump (superseded verdict here)
- `reverse_engineering_string_assoc_installer.md` — `FUN_8010e27c` vs table correction
- `../reverse_engineering_mandatory_hooks.md` — OMIT-T1 through P4
- `../reverse_engineering_libre_patch_layout.md` §6 — layout note
