# RTL8761BU — Sub-Installers and Patch Boot Sequence

Source: `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`
Analysis session: 2026-06-07

---

## Summary

The master installer `FUN_80103780` calls five sub-installer functions at boot.
These functions are **not accessible** from the 42088-byte `rtl8761bu_fw.bin`
binary — their file offsets exceed the binary size.

**Update 2026-06-08 (GZF process mode analysis):** The sub-installers are now fully
decompiled via the GZF data block.  The hardware-write hook at `bos_base+0xe4` is
**not installed by any of the 6 sub-installers** — it is installed by ROM function
`FUN_80025b68` at SCO/eSCO connection setup time (see `reverse_engineering_hardware_layer.md`).

---

## Firmware Binary Layout

Header parse of `rtl8761bu_fw.bin` (42088 bytes):

| Offset | Size | Field | Value |
|--------|------|-------|-------|
| `0x00` | 9 B | magic | `"Realtechk"` |
| `0x09` | 4 B | fw_version | `0x0209A98A` |
| `0x10` | 2 B | num_patches | 2 |
| `0x12` | 2×2 B | IC subver array | `0x36E0`, `0x6CA0` |
| `0x16` | 2×4 B | patch_offset array | `0x00000030`, `0x00003780` |
| `0x30` | 14160 B | Patch 0 code | IC subver `0x36E0` |
| `0x3780` | 27880 B | Patch 1 code | IC subver `0x6CA0` (master installer here) |
| `~0xA420` | 72 B | Config blob | BD_ADDR etc. |

The btrtl driver selects the patch matching the chip's IC subversion and loads
it into RAM at runtime `0x80100000`. The complete file maps `file_offset → runtime`
as `runtime = file_offset + 0x80100000`.

---

## Sub-Installer Addresses — From Master Installer Literal Pool

The master installer's literal pool (`0x800039F4–0x80003BC0` in Ghidra) contains
five call-through function pointers:

| Pool offset | Pool value (runtime ptr) | File offset | In binary? | Purpose |
|-------------|--------------------------|-------------|------------|---------|
| `[0x80003A30]` | `0x8010B255` (odd → fn@`0x8010B254`) | `0xB254` = 45652 | **NO** | sub-call #1 |
| `[0x80003A34]` | `0x8010F7C9` (odd → fn@`0x8010F7C8`) | `0xF7C8` = 63432 | **NO** | sub-call #2 |
| `[0x80003A94]` | `0x8010F305` (odd → fn@`0x8010F304`) | `0xF304` = 62212 | **NO** | sub-call #3 |
| `[0x80003B1C]` | `0x8010EA1D` (odd → fn@`0x8010EA1C`) | `0xEA1C` = 59932 | **NO** | sub-call #4 |
| `[0x80003B20]` | `0x8010E8A9` (odd → fn@`0x8010E8A8`) | `0xE8A8` = 59560 | **NO** | sub-call #5 |

All five file offsets exceed 42088 (= `0xA468` = binary end). These functions
are **not in the firmware file**.

> **Note:** Earlier analysis docs listed different sub-installer addresses
> (`FUN_8010AF40`, `FUN_8011011C`, etc.). Those came from a different/larger
> version of the firmware or from the GZF's runtime RAM snapshot. The addresses
> above are correct for the 42088-byte `rtl8761bu_fw.bin`.

---

## Why the Sub-Installers Are Missing

Three hypotheses (in order of likelihood):

### 1. ROM-provided RAM templates (most likely)
The RTL8761BU ROM initialises certain RAM regions before calling the patch entry
point at `0x8010A001`. It may populate the addresses `0x8010B254`, `0x8010F7C8`,
etc. with default function implementations ("templates"), which the patch then
calls to install further hooks. These functions are not in the patch binary
because they originate in the ROM's initialisation sequence.

### 2. Version mismatch
Kovah's GZF was built from a different/larger firmware version. The GZF's DATA
block (`0x80100000–0x8013FFFF`) is a runtime RAM snapshot that includes ROM-placed
code, giving access to functions beyond the file boundary.

### 3. Multi-part load
The btrtl driver might load additional blobs in sequence. Not confirmed.

---

## Confirmed: bos_base+0xe4 NOT set by any sub-installer (GZF analysis 2026-06-08)

`ScanStoreOffsets.java` over all 213,799 instructions across the full GZF (patch +
data + ROM blocks) found **zero** direct stores to `0x801212e4`.  The only
non-stack `sw` to offset `0xe4` is in ROM `FUN_80025b68` at `0x80025bb0` — which
writes to a per-connection sub-struct field, not to the global `bos_base+0xe4`.

**Conclusion:** The hook at `0x801212e4` is installed by the ROM (`FUN_80025b68`)
at connection setup time.  No sub-installer or master installer sets it.  The 6
sub-installers perform these functions instead (see next section).

---

## Address-Pair Table at File Offset 0xA0

At file offset `0xA0` (Ghidra `0x800000A0`, runtime `0x801000A0`) there is a
15-entry table of `(data_ptr, fn_ptr)` pairs. This appears to be the
"string associations" table that a boot function uses to register patch handlers.

| Table addr | data_ptr | fn_ptr (runtime) | Notes |
|------------|----------|-----------------|-------|
| `0x800000A0` | `0x8010A02C` | `0x8012005A` | header or special entry |
| `0x800000A8` | `0x80120058` | `0x8010C960` | MIPS16e fn |
| `0x800000B0` | `0x801211A4` | `0x8010A34C` | MIPS16e fn (in binary) |
| `0x800000B8` | `0x8012141C` | `0x8010AC9C` | MIPS16e fn (out of range) |
| `0x800000C0` | `0x80120D54` | `0x8010AA5C` | MIPS16e fn (out of range) |
| `0x800000C8` | `0x80120E98` | `0x8010A5E8` | MIPS16e fn (out of range) |
| `0x800000D0` | `0x80120FC0` | `0x8010A4C0` | MIPS16e fn (out of range) |
| `0x800000D8` | `0x80120E70` | `0x8010A184` | MIPS16e fn (in binary) |
| `0x800000E0` | `0x801214D0` | `0x8010A174` | MIPS16e fn (in binary) |
| `0x800000E8` | `0x80121498` | `0x8010CA78` | MIPS16e fn (out of range) |
| `0x800000F0` | `0x80121744` | `0x8010A1D8` | MIPS16e fn (in binary) |
| `0x800000F8` | `0x80120FFC` | `0x8010A208` | MIPS16e fn (in binary) |
| `0x80000100` | `0x8012506C` | `0x8010A304` | MIPS16e fn (in binary) |
| `0x80000108` | `0x80121184` | `0x8010A2B8` | MIPS16e fn (in binary) |
| `0x80000110` | `0x80121180` | `0x8010C9D0` | MIPS16e fn (out of range) |

The `data_ptr` values point to RAM structs (`0x8012xxxx`); the `fn_ptr` values
are MIPS16e function pointers (odd, `0x8010xxxx`). This matches Kovah's label
`patch_that_installs_all_the_string_associated_function_patches__including_LMP`.
The table processor is likely one of the functions near file end (0x9000+).

---

## Patch Boot Sequence (What IS Accessible)

Functions confirmed present in the 42088-byte binary:

| Ghidra addr | Runtime addr | Size | Notes |
|-------------|-------------|------|-------|
| `0x80000030`–`0x80003780` | `0x80100030`–`0x80103780` | ~14KB | Patch 0 (IC `0x36E0`) |
| `0x800000A0` | `0x801000A0` | 120B | Address-pair table (15 entries) |
| `0x80003780` | `0x80103780` | ~2KB | Master installer + literal pool |
| `0x80009048`–`0x8000A467` | `0x80109048`–`0x8010A467` | ~6.8KB | Late patch code (37 fns) |

The patch entry point the ROM calls first is at runtime `0x8010A001` (Ghidra
`0x8000A001`), near the end of the binary. Ghidra fails to properly decode this
region — pervasive `"Unable to resolve constructor"` errors indicate that the
MIPS16e EXTEND instruction prefix sequences here are not supported by the
Ghidra MIPS16e backend. This is a known Ghidra limitation with complex EXTEND
combinations.

---

## GZF Data Block: 6 Sub-Installer Functions (decompiled 2026-06-08)

Analysis via `use_saved_project=True` (GZF process mode).  The pre-existing
`DecompileSubInstallers.java` ran against the GZF DATA block and produced 6 functions.
Note: these addresses are from Kovah's runtime snapshot (GZF data block); the raw
binary's literal pool points to different addresses (0x8010B254 etc.) which correspond
to the same logical functions in a different firmware variant.

### Sub-installer #1 — FUN_8010af40 (40 bytes): HW register bit-clear

```c
void FUN_8010af40(void) {
    uint uVar1 = (*DAT_8010af68)(0x108, 2);          // read HW reg 0x108
    (*DAT_8010af6c)(0x108, uVar1 & 0xffffffbf, 2);   // write back, clearing bit 6
}
```

Reads baseband register 0x108, clears bit 6 (= 0x40), writes back.  Uses two
hardware fn-ptrs from its literal pool.  **Does not install bos_base+0xe4.**

### Sub-installer #2 — FUN_8011011c (112 bytes): Mass function-pointer installer

Installs 19 function pointers into RAM locations (all in the 0x80120600–0x80121100 range):

| Fn-ptr installed (odd addr) | Destination RAM addr | Function |
|---------------------------|---------------------|----------|
| `0x801102f1` → FUN_801102f0 | `0x80121100` | unknown |
| `0x801100bd` → FUN_801100bc | `0x801210f4` | unknown |
| `0x80110725` → FUN_80110724 | `0x801205f8` | unknown |
| `0x8011021d` → FUN_8011021c | `0x801205b0` | unknown |
| `0x8010ff09` → FUN_8010ff08 | `0x801205a0` | unknown |
| `0x801106bd` → FUN_801106bc | `0x801205ac` | unknown |
| `0x801105e9` → FUN_801105e8 | `0x8012063c` | unknown |
| `0x8010ff29` → FUN_8010ff28 | `0x80120644` | unknown |
| `0x801105bd` → FUN_801105bc | `0x80120628` | unknown |
| `0x8010fed9` → FUN_8010fed8 | `0x80120634` | unknown |
| `0x8010ffcd` → FUN_8010ffcc | `0x80120624` | unknown |
| `0x80110311` → FUN_80110310 | `0x80120608` | unknown |
| `0x8011006d` → FUN_8011006c | `0x80120648` | unknown |
| `0x80110701` → FUN_80110700 | `0x801205d0 + 0x00` | struct +0 |
| `0x8011057d` → LAB_8011057c | `0x801205d0 + 0x08` | struct +8 |
| `0x80110045` → FUN_80110044 | `0x801205d0 + 0x10` | struct +0x10 |
| `0x8010fe85` → LAB_8010fe84 | `0x801205b8 + 0x0c` | struct +0xc |
| `0x80110641` → FUN_80110640 | `0x801205fc` | unknown |
| `0x80110365` → FUN_80110364 | `0x80120600` | unknown |

None of these destination addresses is `0x801212e4`.

#### Libre implementation (2026-06-10)

All 19 handler bodies transcribed byte-identically from vendor patch1 into
`src/sub2_hooks.S` (`sub2_fn_00` … `sub2_fn_18`). Regenerator:
`scripts/gen_sub2_hooks_asm.py`. Linker scatter in `rtl8761bu.ld` places each
section at native PRAM offset (runtime `0x8010FE84`–`0x80110740`); `sub_installer_2`
literal pool already points at these addresses (+1 MIPS16e).

| Libre symbol | Runtime | Size | DRAM slot (even) |
|--------------|---------|------|------------------|
| sub2_fn_00 | 0x801102F0 | 32 | 0x80121100 |
| sub2_fn_01 | 0x801100BC | 20 | 0x801210F4 |
| sub2_fn_02 | 0x80110724 | 28 | 0x801205F8 |
| sub2_fn_03 | 0x8011021C | 36 | 0x801205B0 |
| sub2_fn_04 | 0x8010FF08 | 32 | 0x801205A0 |
| sub2_fn_05 | 0x801106BC | 28 | 0x801205AC |
| sub2_fn_06 | 0x801105E8 | 24 | 0x8012063C |
| sub2_fn_07 | 0x8010FF28 | 32 | 0x80120644 |
| sub2_fn_08 | 0x801105BC | 28 | 0x80120628 |
| sub2_fn_09 | 0x8010FED8 | 32 | 0x80120634 |
| sub2_fn_10 | 0x8010FFCC | 28 | 0x80120624 |
| sub2_fn_11 | 0x80110310 | 32 | 0x80120608 |
| sub2_fn_12 | 0x8011006C | 28 | 0x80120648 |
| sub2_fn_13 | 0x80110700 | 28 | 0x801205D0 |
| sub2_fn_14 | 0x8011057C | 28 | 0x801205D8 |
| sub2_fn_15 | 0x80110044 | 28 | 0x801205E0 |
| sub2_fn_16 | 0x8010FE84 | 28 | 0x801205C4 |
| sub2_fn_17 | 0x80110640 | 28 | 0x801205FC |
| sub2_fn_18 | 0x80110364 | 28 | 0x80120600 |

548 B total code. `make diff-prefix` equivalent: 19/19 regions 0 diffs vs vendor
patch1 at native offsets (2026-06-10).

**Linker note:** T2 callees `fn_a84c` / `fn_c854` / `fn_10868` / `fn_b4d0` /
`fn_aa58` previously packed into `0x8010FD60+` overlapped this region; relocated
to `0x8010FD60` (b4d0), `0x8010FF48` (aa58 gap), `0x80106740+` (c854/a84c/10868).
DRAM installs use symbol `+1` — relocation is safe.

Per-function decompile summaries remain T4 follow-up (Ghidra DATA block); bodies
are vendor-identical pending individual RE tickets.

### Sub-installer #3 — FUN_8010fc58 (22 bytes): Single fn-ptr install + call

```c
void FUN_8010fc58(void) {
    *(undefined **)PTR_PTR_8010fc74 = PTR_FUN_80110ca4_1;   // install one fn-ptr
    (*DAT_8010fc78)();                                        // call another fn via ptr
}
```

### Sub-installer #4 — FUN_8010f370 (44 bytes): Data-pointer array installer

```c
void FUN_8010f370(void) {
    undefined *base = PTR_PTR_8010f39c;   // pointer to struct base
    *(base + 4)  = PTR_DAT_8010f3a0;
    *(base + 8)  = PTR_DAT_8010f3a4;
    *(base + 0)  = PTR_DAT_8010f3a8;
    *(base + 0xc) = PTR_DAT_8010f3ac;
    *(base + 0x10) = PTR_DAT_8010f3b0;
    *(base + 0x18) = PTR_base_of_0x1ac_struct_array;   // Kovah label: large struct array
    *(base + 0x1c) = PTR_DAT_8010f3b8;
    *(base + 0x14) = PTR_DAT_8010f3bc;
    *(base + 0x20) = PTR_DAT_8010f3c0;
    *(base + 0x24) = PTR_DAT_8010f3c4;
}
```

Installs 10 data pointers into a struct.  Offsets are 0x0–0x24 (10 dwords in a 0x28 struct).
Likely initialises a connection-record sub-struct or eSCO params block.

### Sub-installer #5 — FUN_8010e81c (8 bytes): Minimal fn-ptr install

```c
void FUN_8010e81c(void) {
    *(undefined **)PTR_PTR_8010e828 = PTR_FUN_8010e82c_1;   // one fn-ptr
}
```

### Sub-installer #6 — FUN_8010eac0 (24 bytes): Write -1 + call

```c
void FUN_8010eac0(void) {
    *(undefined4 *)PTR_DAT_8010ead8 = 0xffffffff;   // write -1 to global
    (*DAT_8010eadc)();                               // call via fn-ptr
}
```

---

## ROM Functions Decompiled (2026-06-08)

### `interesting_string_user_fptr_registration_function` @ 0x80009990

The string-association table processor.  Called to register a function pointer
associated with a data pointer (the address-pair table entries):

```c
ushort interesting_string_user_fptr_registration_function(
    int *result_out, ..., ushort param_6) {
    if (*PTR_DAT_80009a00 != NULL)
        if (hook(params) != 0) return hook_result;
    if (result_out != NULL) {
        *result_out = FUN_80075ee0(param_2, param_3, param_4, param_5, param_6);
        return -(*result_out == -1) & 5;   // 5 on failure, 0 on success
    }
    return 1;
}
```

`FUN_80075ee0` is the actual string association lookup/registration engine.

### ROM register read/write (0x8001136c / 0x8001139c)

See `reverse_engineering_hardware_layer.md` and `reverse_engineering_rom_regs.md`.

---

## Tooling Limitations (Updated)

| Limitation | Status |
|------------|--------|
| wairz raw-binary mode: GZF three-block layout inaccessible | **RESOLVED** — `use_saved_project=True` GZF process mode |
| `script_file_id` in process mode: broken `-scriptPath` | **RESOLVED** — user fixed separate path args |
| MIPS16e EXTEND instruction decoding incomplete in Ghidra 12.1.2 | Still active (entry-point region 0x8000a000+) |
| script_args with hex addresses rejected by analyzeHeadless | Still active — hardcode in scripts |

---

## What the Libre Replacement Must Provide (Revised)

The per-connection `crypto_struct+0xe4` hook is installed by ROM `FUN_80025b68` at
SSP pairing time.  The global `bos_base+0xe4` slot at `0x801212e4` is separate and
stays NULL.  The libre firmware does **not** need to install either.  See
`reverse_engineering_hardware_layer.md` Section 12 for full decompile + verdict.

---

## Next Steps (Phase 2 Remaining)

- [ ] Analyze patch 0 (file `0x30`–`0x3780`) — different IC subversion variant
- [x] Read ROM codec templates — unscramble algorithm + first-insn decode confirmed (2026-06-09)
- [x] Identify what calls ROM `FUN_80025b68` — SSP IO-cap exchange (2026-06-08)
- [x] Document exact prototype and behavior of hardware-write hook — Section 12 (2026-06-09)
