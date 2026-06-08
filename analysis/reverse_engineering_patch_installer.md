# Reverse Engineering: `thing_that_calls_thing_that_installs_LMP_Patch`

**File offset**: `0x3780`  
**Runtime address**: `0x80103780`  
**Size**: 578 bytes  
**Source**: Kovah `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`, decompiled via Ghidra 12.1.2 headless  
**Tool used**: `DecompileAddr.java` with `-noanalysis` flag  

---

## Summary

This is the **master patch installer** — the second function called after the patch firmware's entry point at `0x8010a001`. It performs the entire boot-time setup for the RTL8761BU patch:

1. Copies hardware config data into two half-word registers
2. Calls an early init function (`0x8010a6c8`)
3. Clears a hardware enable bit at `config_base+0xd8`
4. Installs **38+ function pointers** from the patch into ROM data structures
5. Calls 5 sub-installer functions directly
6. Clears a second hardware bit at `config_base+0xe0`
7. Reads the BD address from the firmware config blob (`copies_config_bdaddr` ROM call)
8. Performs a firmware version compatibility check via lookup table
9. Conditionally calls a boot-mode function based on connection type (USB/UART/other)
10. Installs a final batch of pointers and calls `FUN_8010c278` for late init

The function is **not reentrant** and is only called once at chip boot.

---

## Phase-by-phase Analysis

### Phase 1 — Hardware config copy (offset `0x3780`–`0x379e`)

```asm
lw  v0, PTR_DAT_000039c4   ; load source data ptr
lw  v0, 0x0(v0)            ; dereference → 32-bit config word
sh  (v0>>16), PTR_DAT_000039c8   ; write high 16 bits to dest A
sh  v0,       PTR_DAT_000039d0   ; write low  16 bits to dest B
jalr PTR_FUN_8010a6c8      ; call early-init
```

Reads one 32-bit word and splits it into two 16-bit hardware registers. Likely a clock or transport configuration that must be set before anything else. `FUN_8010a6c8` is the BSS-zeroing early-init — see Section below.

---

### Phase 2 — Clear bit 0 at `config_base+0xd8` (offset `0x37a0`–`0x37de`)

```c
uVar12 = *(uint *)(config_base + 0xd8) & 0xfffffffe;  // clear LSBit
// write back as 4 individual bytes (MIPS16e unaligned workaround)
config_base[0xd8] = (char)uVar12;
config_base[0xd9] = (char)(uVar12 >> 8);
config_base[0xda] = (char)(uVar12 >> 0x10);
config_base[0xdb] = (char)(uVar12 >> 0x18);
```

`config_base` is a global pointer to a hardware configuration/state struct. Clearing bit 0 of `+0xd8` likely disables a hardware feature (possibly "ROM patch enable" or "test mode") before installing new hooks. The 4-byte store pattern recurs throughout the function — it is the MIPS16e way of doing a 32-bit store to a potentially unaligned address in a byte-addressable peripheral.

---

### Phase 3 — First batch of function pointer installs (offset `0x37e2`–`0x384c`)

The core of the function. Each installation follows the pattern:

```c
*PTR_TO_ROM_FPTR = PTR_TO_PATCH_FUNCTION;
```

Where `PTR_TO_ROM_FPTR` is a pointer-to-pointer stored in the patch's own data section that resolves to a slot in a ROM dispatch table or struct.

| Patch function installed | Target struct / offset | Notes |
|--------------------------|----------------------|-------|
| `FUN_8010b118` | via `PTR_PTR_000039e4` | unknown dispatch slot |
| `LAB_8010b174` | `puVar8 + 0x14` | unknown struct at +0x14 |
| `LAB_8010bba4` | `puVar6 + 0xd8` | **LMP hook — see below** |
| `LAB_8010be20` | via `PTR_PTR_000039f4` | unknown dispatch slot |

Between these installs, two direct calls are made:

```c
(*PTR_FUN_8010af40)();    // sub-installer #1
(*PTR_FUN_8011011c)();    // sub-installer #2
```

**`LAB_8010bba4` at `puVar6+0xd8` is the critical LMP hook.** `puVar6` is loaded from `PTR_PTR_000039d8`, which Ghidra calls the connection/state-tracking base (likely the `bos` struct base or the main BT state object). Offset `+0xd8` corresponds to what Kovah labeled `LMP__268__most_common_for_VSCs2_checks_fptr_patch` in the ROM (`0x80009a6c`) — this is how the ROM dispatches vendor-specific LMP opcodes to the patch. After this install, any LMP VSC arriving at the ROM hits `0x8010bba4` in the patch instead of a null pointer.

---

### Phase 4 — Second batch (offset `0x3804`–`0x384c`)

Installs handlers for HCI and higher-level BT profiles:

| Patch function | Notes (from kovah_function_list.md cross-reference) |
|----------------|------------------------------------------------------|
| `LAB_8010c1e8` | struct+0x20 — likely HCI event handler slot |
| `LAB_8010c224` | struct+0x24 |
| `FUN_8010b3d8` | unknown |
| `FUN_8010b0a4` | unknown |
| `FUN_8010c198` | unknown |
| `FUN_8010d1f4` | unknown |
| `FUN_8010c780` | unknown |
| `FUN_8010c63c` | unknown |
| `LAB_8010b7f0` | `puVar8 + 0x1c` |
| **`patch_that_installs_all_the_string_associated_function_patches__including_LMP`** | **second-level installer — installed as a fptr** |
| `FUN_8010dd1c` | unknown |
| `FUN_8010d890` | unknown |
| `FUN_8010d618` | unknown |

The most important entry here is installing `patch_that_installs_all_the_string_associated_function_patches__including_LMP` as a function pointer. This means there is a **two-tier installation**: this function installs a pointer to a *second installer* that will later install per-LMP-opcode (or "per-string") dispatch table entries. The exact trigger point for the second installer is not yet known — it may be called by the ROM during the first LMP PDU or during a later boot phase.

---

### Phase 5 — Clear bit 14 at `config_base+0xe0` (offset `0x384e`–`0x388a`)

```c
uVar12 = *(uint *)(config_base + 0xe0) & 0xffffbfff;  // clear bit 14 (0x4000)
// write back as 4 bytes
```

A second hardware register modification. Bit 14 cleared at `+0xe0` — possibly disabling a second hardware feature or clearing a pending interrupt mask.

---

### Phase 6 — Third fptr batch + `config_base[0x128] = 0` (offset `0x388e`–`0x38ca`)

Installs more patch functions, then explicitly zeros `config_base+0x128`:

```c
config_base[0x128] = 0;
```

This field is likely a "patch not yet installed" flag that the ROM checks. Setting it to 0 may be what enables the ROM to start using the newly installed pointers.

Additional installs in this phase:

| Patch function | Notes |
|----------------|-------|
| `FUN_8010fc58` | called directly (sub-installer #3) |
| `FUN_8010a594` | installed |
| `FUN_8010c0f4` | installed |
| `FUN_8010a4ac` | installed |
| `FUN_8010a49c` | installed |

---

### Phase 7 — Fourth fptr batch (offset `0x38b2`–`0x38fe`)

Large block of pointer installations for BT stack infrastructure:

| Patch function | Target offset | Notes |
|----------------|--------------|-------|
| `FUN_8010bce0` | unknown | |
| `FUN_8010c49c` | unknown | |
| `FUN_8010c43c` | unknown | |
| `FUN_8010d168` | unknown | |
| `FUN_8010fa34` | unknown | |
| `FUN_8010f950` | unknown | |
| `FUN_8010fb08` | unknown | |
| `FUN_8010abd0` | unknown | |
| `LAB_8010f884` | `puVar6 + 0x50` | |
| `FUN_8010f85c` | unknown | |
| `FUN_8010a550` | unknown | |
| `FUN_8010c160` | unknown | |
| `FUN_8010c178` | unknown | |

---

### Phase 8 — Three direct sub-installer calls (offset `0x38fe`–`0x390e`)

```c
(*PTR_FUN_8010f370)();    // sub-installer #4
(*PTR_FUN_8010e81c)();    // sub-installer #5
(*PTR_FUN_8010eac0)();    // sub-installer #6
```

These likely install their own batches of function pointers (too complex to inline). Each is a separate subsystem installer (e.g., encryption handlers, SCO audio, eSCO).

---

### Phase 9 — HCI dispatch + connection struct installs (offset `0x3910`–`0x392e`)

```c
*(puVar6 + 0x30) = LAB_8010c088;   // install at bos_base+0x30
*(ptr + 0x30)    = LAB_8010b4d0;   // install at second struct+0x30
// then install FUN_8010a5ac and FUN_8010c854

// Critical: call via function pointer with 2 args
(*(code *)*PTR_DAT_00003b0c)(PTR_PTR_00003b10, 2);
```

The final call here passes a pointer and the constant `2` to an indirect function. The constant `2` likely selects USB mode (vs. 1=UART or 3=other).

---

### Phase 10 — Firmware version compatibility check (offset `0x3930`–`0x395a`)

```c
uVar12 = FUN_8010e214();      // returns detected firmware version byte
*PTR_DAT_00003b1c      = (byte)uVar12;    // store version
PTR_DAT_00003b1c[1]    = (*DAT_00003b18) & 0x1f;  // store masked version info

bool supported = false;
if (((*DAT_00003b18) & 0x1f) == uVar12) {
    if (uVar12 < 0x10) {
        supported = PTR_DAT_00003b20[uVar12];  // lookup table: is this version OK?
    }
    supported = (supported == 1);
}
```

`FUN_8010e214` reads some ROM version register or config byte. The result is compared against a value masked with `0x1f` (5-bit version field). If they match, a 16-entry lookup table at `PTR_DAT_00003b20` is consulted to determine if the version is "supported." This is the firmware version gating mechanism — the patch may behave differently on newer or older ROM revisions.

---

### Phase 11 — Install LMP hook + conditional boot-mode call (offset `0x395c`–`0x3992`)

```c
*PTR_PTR_00003b28 = FUN_8010c09c;             // install another handler
*(PTR_PTR_000039d8 + 0x1c) = LAB_8010bc74;   // install at bos_base+0x1c

bos_state[2] = supported;   // store version-check result in bos state
(*DAT_00003b30)();           // call via stored ptr (context-dependent init)
copies_config_bdaddr();      // ROM function 0x8000fd38: read BD addr from config blob

// connection-type check:
byte conn_type = bos_state[1] & 0x1f;
if (conn_type != 8 && conn_type != 6 && conn_type != 5) {
    if (FUN_8010ad38() == 1) {
        FUN_8010b04c();    // extra init for non-USB/UART modes
    }
}
```

- `copies_config_bdaddr` is a ROM function (`0x8000fd38`) that reads the Bluetooth device address from the 6-byte config blob appended after the firmware binary. This is how the dongle gets its unique BD_ADDR.
- The connection type check: values 8, 6, 5 map to USB, UART, and a third transport. If the transport is not one of these, an additional init path runs.
- `FUN_8010ad38` likely checks whether a secondary processor or coprocessor is present.

---

### Phase 12 — Final installs and late init (offset `0x3994`–`0x39c0`)

```c
*PTR_PTR_00003b44 = FUN_8010ce0c;
FUN_8010c278();     // late-stage init call
*PTR_DAT_00003b4c = 4;   // some global flag = 4 (patch fully installed?)
*PTR_PTR_00003b54 = FUN_80110ddc;
*PTR_PTR_00003b5c = FUN_8010bda0;
*PTR_PTR_00003b64 = FUN_8010e350;
return;
```

Setting the global to `4` is likely the "patch installation complete" sentinel. After this returns, the ROM's `calls_to_0x8010a001_as_fptr_to_install_patches` (`0x800109ac`) continues with the now-patched BT stack.

---

## Data Structures Touched

| Symbol name (Ghidra) | Inferred purpose |
|----------------------|-----------------|
| `config_base` | Base pointer to hardware config / state struct; at least 0x130 bytes |
| `config_base+0xd8` | 32-bit register: bit 0 = some HW enable (cleared at start) |
| `config_base+0xe0` | 32-bit register: bit 14 = another HW feature (cleared mid-init) |
| `config_base+0x128` | 8-bit flag: zeroed to signal "patch installed" |
| `puVar6` (aka `PTR_PTR_000039d8`) | Connection/BT-state base: at least 0xd8 bytes, holds LMP/HCI fptrs |
| `puVar6+0xd8` | LMP VSC dispatch hook — installed with `LAB_8010bba4` |
| `puVar6+0x1c` | Another hook slot — installed with `LAB_8010bc74` |
| `puVar6+0x20/0x24` | HCI event handler slots |
| `puVar6+0x30` | Connection handler slot |
| `puVar6+0x50` | Another handler slot |
| `bos_state` (PTR_DAT_00003b1c) | Small state array: [0]=version byte, [1]=conn_type&0x1f, [2]=version_ok |

---

## Complete List of Patch Functions Referenced

All addresses are runtime (patch base `0x80100000`):

| Address | Disposition | Notes |
|---------|-------------|-------|
| `0x8010a6c8` | called directly | early init |
| `0x8010b118` | installed as fptr | |
| `0x8010b174` | installed at struct+0x14 | |
| `0x8010bba4` | installed at `bos_base+0xd8` | **LMP VSC hook** |
| `0x8010be20` | installed as fptr | |
| `0x8010af40` | called directly | sub-installer #1 |
| `0x8011011c` | called directly | sub-installer #2 |
| `0x8010c1e8` | installed at struct+0x20 | |
| `0x8010c224` | installed at struct+0x24 | |
| `0x8010b3d8` | installed as fptr | |
| `0x8010b0a4` | installed as fptr | |
| `0x8010c198` | installed as fptr | |
| `0x8010d1f4` | installed as fptr | |
| `0x8010c780` | installed as fptr | |
| `0x8010c63c` | installed as fptr | |
| `0x8010b7f0` | installed at struct+0x1c | |
| `patch_that_installs_all_the_string_associated_function_patches__including_LMP` | installed as fptr | **second-tier installer** |
| `0x8010dd1c` | installed as fptr | |
| `0x8010d890` | installed as fptr | |
| `0x8010d618` | installed as fptr | |
| `0x8010fc58` | called directly | sub-installer #3 |
| `0x8010a594` | installed as fptr | |
| `0x8010c0f4` | installed as fptr | |
| `0x8010a4ac` | installed as fptr | |
| `0x8010a49c` | installed as fptr | |
| `0x8010bce0` | installed as fptr | |
| `0x8010c49c` | installed as fptr | |
| `0x8010c43c` | installed as fptr | |
| `0x8010d168` | installed as fptr | |
| `0x8010fa34` | installed as fptr | |
| `0x8010f950` | installed as fptr | |
| `0x8010fb08` | installed as fptr | |
| `0x8010abd0` | installed as fptr | |
| `0x8010f884` | installed at `bos_base+0x50` | |
| `0x8010f85c` | installed as fptr | |
| `0x8010a550` | installed as fptr | |
| `0x8010c160` | installed as fptr | |
| `0x8010c178` | installed as fptr | |
| `0x8010f370` | called directly | sub-installer #4 |
| `0x8010e81c` | called directly | sub-installer #5 |
| `0x8010eac0` | called directly | sub-installer #6 |
| `0x8010c088` | installed at `bos_base+0x30` | |
| `0x8010b4d0` | installed at second_ptr+0x30 | |
| `0x8010a5ac` | installed as fptr | |
| `0x8010c854` | installed as fptr | |
| `0x8010e214` | called directly | returns version byte |
| `0x8010c09c` | installed as fptr | |
| `0x8010bc74` | installed at `bos_base+0x1c` | |
| `0x8010ad38` | called directly | returns boot-mode flag |
| `0x8010b04c` | conditionally called | extra init for non-USB/UART |
| `0x8010ce0c` | installed as fptr | |
| `0x8010c278` | called directly | late-stage init |
| `0x80110ddc` | installed as fptr | |
| `0x8010bda0` | installed as fptr | |
| `0x8010e350` | installed as fptr | |

**ROM functions called directly** (not from patch):

| Address | Name |
|---------|------|
| `0x8000fd38` | `copies_config_bdaddr` — reads BD_ADDR from config blob |

---

## Implications for the Libre Replacement

To replace `rtl8761bu_fw.bin` entirely, a libre patch must:

1. **Implement all 50+ functions** listed above, or provide stubs that maintain ROM stability.
2. **Install the same set of function pointers** into the exact same ROM struct offsets. The offsets (`+0xd8`, `+0xd8`, `+0x1c`, `+0x20`, `+0x24`, `+0x30`, `+0x50`) are hard-coded in the ROM dispatch tables — they cannot change without ROM modification.
3. **Replicate the `config_base` register writes** (bits at `+0xd8` and `+0xe0`) to leave hardware in the expected state.
4. **Call `copies_config_bdaddr`** (ROM `0x8000fd38`) to populate the BD address — OR implement an independent BD_ADDR reader if the config blob format is documented. The config blob format is partially known from `btrtl.c` in the Linux kernel.
5. **The version check** (`FUN_8010e214` + lookup table) can likely be replaced with a simple "always supported" implementation for a libre build targeting known-good hardware.
6. **The two-tier installer design** (`thing_that_calls_thing_that_installs_LMP_Patch` installs `patch_that_installs_all_the_string_associated_function_patches__including_LMP` as a fptr) means the libre patch must implement both installers.

The highest priority next steps are:
- Decompile `LAB_8010bba4` (the LMP VSC hook) — this is the function the ROM calls for every VSC
- Decompile `patch_that_installs_all_the_string_associated_function_patches__including_LMP` — the second-tier installer
- Understand the exact layout of `config_base` and `bos_base` structs

---

## Appendix: Early-Init BSS Zeroing — `FUN_8010a6c8` (2026-06-08)

**Runtime address**: `0x8010a6c8`  
**Size**: 24 bytes (+ 12-byte literal pool at `0x8010a6e0`–`0x8010a6eb`)  
**Called from**: master installer at offset `0x379c` (28 bytes into `FUN_80103780`), and patch entry `FUN_8010a000` at `0x8010a01c`

### Decompiled C

```c
void FUN_8010a6c8(void)
{
    memset(0x80109c00, 0, 0x8010a000 - 0x80109c00);   // zero 0x400 = 1024 bytes
}
```

The literal pool encodes three constants:

| Symbol | Value | Meaning |
|--------|-------|---------|
| `PTR_DAT_8010a6e0` | `0x80109c00` | BSS region start |
| `PTR_FUN_8010a6e4` | `0x8010a000` | BSS region end (= patch entry point base) |
| `DAT_8010a6e8`     | `0x8000e98d` | ROM `memset` (MIPS16e, odd = MIPS16e bit) |

The call is: `(*0x8000e98d)(0x80109c00, 0, 0x400)` — ROM `memset` via function pointer.

### Patch RAM Layout (confirmed)

The end-pointer `0x8010a000` being the address of `FUN_8010a000` (patch entry) establishes the BSS boundary:

```
0x80100000 – 0x80109BFF   patch code section  (master installer, hooks, sub-installers)
0x80109C00 – 0x80109FFF   BSS / zero-init data  ← zeroed by this function (0x400 bytes)
0x8010A000 – 0x8010ADC3   patch code section 2 (entry point, late functions, dark firmware)
```

### Libre Firmware Implication

**Needed but trivial.** The libre master installer must zero `0x80109c00..0x80109fff` before installing any hooks. The simplest implementation is an identical call:

```asm
la   a0, 0x80109c00
li   a1, 0
li   a2, 0x400
la   t0, 0x8000e98c      ; ROM memset
jalr t0
nop
```

Alternatively, the libre patch binary can place zeros at file offsets `0x9c00–0x9fff` (BSS section), and the chip's ROM firmware-upload handler will copy those zeros verbatim — making the explicit `memset` call redundant (but still harmless to include).

---

## Notes on Methodology

- All disassembly and pseudo-C produced by Ghidra 12.1.2 headless from Kovah's annotated `.gzf`
- MIPS16e ISA (16-bit compressed MIPS); `ADJSP`/`addiu sp` prologues used for function detection
- PC-relative `lw` instructions (`lw v0, 0x23c(pc)`) are the MIPS16e way to load from a local literal pool — each `PTR_*` label in the decompile is one such pool entry
- The `_nop` after `jalr` is the MIPS branch-delay slot
- The function does NOT return a value (`undefined` return type) and takes no arguments

---

## Patch Entry Point — `FUN_8010a000` (2026-06-08)

**Runtime address**: `0x8010a000` (ROM calls it at `0x8010A001` — MIPS16e odd-address bit)  
**Size**: 578 bytes  
**Block**: data block (Kovah's runtime RAM snapshot), also present as patch block file offset `0xa000`

### What it is

`FUN_8010a000` is the **true patch entry point** — the first patch code the chip executes at
boot. The previous assumption that it was a thin wrapper delegating to `FUN_80103780` was
incorrect. It is itself a full 578-byte monolithic installer that:

1. Copies a 32-bit config word into two 16-bit halves
2. Calls BSS init `FUN_8010a6c8`
3. Clears bit 0 of `config_base+0xd8` (pre-install safety step)
4. Installs 35+ function-pointer hooks into `bos_base` and config structs
5. Calls all 6 sub-installers
6. Calls ROM `copies_config_bdaddr` (`0x8000fd38`) — reads BD_ADDR from config blob
7. Performs a chip-revision version check (table lookup at `PTR_DAT_8010a3a0`)
8. Conditionally calls additional hardware init based on revision
9. Installs a final batch of hooks
10. Writes `4` to a global flag (likely "patch active")

### ROM Caller

| Address | Function | Ref type |
|---------|----------|----------|
| `0x80010a18` | `calls_to_0x8010a001_as_fptr_to_install_patches` | COMPUTED_CALL |

Kovah named this ROM function explicitly for what it does: extracts `0x8010A001` from a stored
function pointer and makes a computed call into the patch. This is the bridge between ROM boot
and the patch firmware.

Other data references (not call sites): VSC FC20 download handler (`VSC_0xfc20__download_patch`
at `0x8002ff12`/`0x8003031c`), ROM `references_patch_download_mem2/4`, and a RAM word at
`0x80120050` — all hold `0x8010a000` as the patch load/entry address.

### Complete Boot Sequence

```
ROM power-on init
  └─ hardware / clock init
  └─ patch download via HCI VSC 0xFC20
       (VSC_0xfc20__download_patch__FUN_8002fee0 @ 0x8002fee0)
  └─ patch activation:
       calls_to_0x8010a001_as_fptr_to_install_patches @ 0x80010a18
         └─ COMPUTED_CALL to 0x8010a001 (MIPS16e)
              └─ FUN_8010a000 (patch entry, 578 bytes)
                   ├─ config data copy (32-bit → two 16-bit halves)
                   ├─ FUN_8010a6c8  — BSS zero init (0x80109c00..0x80109fff)
                   ├─ clear bit 0 @ config_base+0xd8
                   ├─ hook bos_base+0xd8 → 0x8010bba4 (LMP VSC)
                   ├─ hook bos_base+0x20 → 0x8010c1e8
                   ├─ hook bos_base+0x24 → 0x8010c224
                   ├─ hook bos_base+0x1c → 0x8010bc74 (via puVar5+0x1c)
                   ├─ hook bos_base+0x50 → 0x8010f884
                   ├─ hook bos_base+0x30 → 0x8010c088 (connection handler)
                   ├─ hook bos_base+0x14 → 0x8010b174
                   ├─ ... (25+ additional hook installs via literal pool)
                   ├─ FUN_8010af40   — sub-installer #1 (clears HW reg bit 6)
                   ├─ FUN_8011011c   — sub-installer #2 (19 fn-ptrs 0x80120600-0x80121100)
                   ├─ FUN_8010fc58   — sub-installer #3 (FUN_80110ca4 + 1 fn-ptr)
                   ├─ FUN_8010f370   — sub-installer #4 (10 data-ptrs, 0x28 bytes)
                   ├─ FUN_8010e81c   — sub-installer #5 (FUN_8010e82c fn-ptr)
                   ├─ FUN_8010eac0   — sub-installer #6 (writes 0xffffffff, calls via ptr)
                   ├─ copies_config_bdaddr (ROM 0x8000fd38) — load BD_ADDR
                   ├─ FUN_8003aea0   — ROM function (purpose TBD)
                   ├─ FUN_8010e214   — patch fn (purpose TBD)
                   ├─ FUN_8010a7b8   — patch fn (purpose TBD)
                   ├─ FUN_8010ad38   — patch fn (purpose TBD)
                   ├─ FUN_8010b04c   — patch fn (purpose TBD)
                   ├─ chip-rev check → conditional additional hw init
                   ├─ FUN_8010c278   — patch fn (purpose TBD, "late init")
                   └─ write 4 → global "patch active" flag
  └─ ROM resumes normal operation with patch hooks in place
```

### Hook Installs in `FUN_8010a000`

The decompile uses two main struct pointers:
- `puVar2 = PTR_config_base_8010a24c` — the firmware config struct
- `puVar3 = PTR_PTR_8010a258` — `bos_base` (BT operational state struct, ~`0x80121200`)
- `puVar5 = PTR_PTR_8010a268` — secondary struct (also part of BT state)

Selected installs (offsets into `puVar3` = `bos_base`):

| Offset | Target | Function installed |
|--------|--------|--------------------|
| `+0xd8` | `bos_base+0xd8` | `0x8010bba4` (LMP VSC dispatch) |
| `+0x20` | `bos_base+0x20` | `0x8010c1e8` |
| `+0x24` | `bos_base+0x24` | `0x8010c224` |
| `+0x30` | `bos_base+0x30` | `0x8010c088` (connection handler) |
| `+0x50` | `bos_base+0x50` | `0x8010f884` |
| `+0x1c` | `puVar5+0x1c`  | `0x8010bc74` |
| `+0x14` | `puVar5+0x14`  | `0x8010b174` |

Many more via generic `*(undefined **)PTR_PTR_8010aXXX = PTR_FUN_...` stores (see decompile).

The operation `*(uint *)(puVar2 + 0xd8) & 0xfffffffe` clears the LSBit of the **config struct's**
`+0xd8` word — separate from the `bos_base+0xd8` hook slot. Similarly, bit 14 of
`puVar2+0xe0` is cleared (`& 0xffffbfff`).

### Chip Revision Check

```c
uVar9 = (*DAT_8010a394)();          // read chip ID / revision register
*PTR_DAT_8010a39c = (char)uVar9;    // save it
bVar1 = *DAT_8010a398;              // read another byte (expected rev?)
puVar2[1] = (char)(bVar1 & 0x1f);   // store 5-bit rev field
cVar11 = false;
if ((bVar1 & 0x1f) == uVar9) {
    if (uVar9 < 0x10) {
        cVar11 = PTR_DAT_8010a3a0[uVar9];   // table lookup [0..15]
    }
    cVar11 = (cVar11 == 1);
}
// ...
if (cVar11 && iVar10 == 1) {
    (*DAT_8010a3bc)();              // extra hw init for this revision
}
```

The 16-entry byte table at `PTR_DAT_8010a3a0` maps chip revision IDs (0–15) to a boolean flag.
This gates additional hardware initialization for specific silicon revisions.

### Relationship to `FUN_80103780`

`FUN_80103780` (`thing_that_calls_thing_that_installs_LMP_Patch`) is **not** called by
`FUN_8010a000`. The callees list confirms zero overlap. They are parallel entries from the two
memory block variants in the GZF:

- **patch block** (`0x00000000+`): file offset `0x3780` = `FUN_80103780` — the master installer
  in the vanilla `rtl8761bu_fw.bin` structure; also calls BSS init (at offset +0x1c). The patch
  block entry at file offset `0xa000` has MIPS16e EXTEND decode errors in Ghidra and cannot be
  directly decompiled.
- **data block** (`0x80100000+`): Kovah's runtime RAM snapshot of a different firmware variant.
  `FUN_8010a000` here is the fully self-contained monolithic entry point we analyzed.

For the libre firmware, `FUN_8010a000` from the data block is the authoritative reference since
it is what the chip actually runs (matching address `0x8010a000` = file offset `0xa000`).

### Callees Summary

| Address | Name | Source | Purpose |
|---------|------|--------|---------|
| `0x8010a6c8` | `FUN_8010a6c8` | patch | BSS zero init |
| `0x8010af40` | `FUN_8010af40` | patch | sub-installer #1 |
| `0x8011011c` | `FUN_8011011c` | patch | sub-installer #2 |
| `0x8010fc58` | `FUN_8010fc58` | patch | sub-installer #3 |
| `0x8010f370` | `FUN_8010f370` | patch | sub-installer #4 |
| `0x8010e81c` | `FUN_8010e81c` | patch | sub-installer #5 |
| `0x8010eac0` | `FUN_8010eac0` | patch | sub-installer #6 |
| `0x8000fd38` | `copies_config_bdaddr` | ROM | read BD_ADDR from config blob |
| `0x8003aea0` | `FUN_8003aea0` | ROM | unknown |
| `0x8010e214` | `FUN_8010e214` | patch | unknown |
| `0x8010a7b8` | `FUN_8010a7b8` | patch | unknown |
| `0x8010ad38` | `FUN_8010ad38` | patch | unknown |
| `0x8010b04c` | `FUN_8010b04c` | patch | unknown |
| `0x8010c278` | `FUN_8010c278` | patch | unknown (late init) |

### Libre Firmware Implication

The libre patch entry at `0x8010a000` must:
1. **BSS init** — call ROM `memset(0x80109c00, 0, 0x400)` (or pre-zero the binary)
2. **Install LMP VSC hook** — `bos_base+0xd8 = 0x8010bba4` (or libre equivalent)
3. **Install all other required hooks** — from the table above; many are mandatory for
   basic BT operation; TBD which are optional
4. **Call `copies_config_bdaddr`** (`0x8000fd38`) — mandatory for BD_ADDR loading
5. **Call sub-installers** — all 6 confirmed necessary (from prior analysis)
6. **Write global flag** — `*ptr = 4` (may be checked by ROM for readiness)
7. **Unknown callees** (`FUN_8003aea0`, `FUN_8010e214`, `FUN_8010a7b8`, etc.) — must be
   analyzed before determining if libre stubs or ROM calls suffice
