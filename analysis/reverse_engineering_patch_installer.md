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

Reads one 32-bit word and splits it into two 16-bit hardware registers. Likely a clock or transport configuration that must be set before anything else. `FUN_8010a6c8` is the first patch function called; its role is not yet analyzed.

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

## Notes on Methodology

- All disassembly and pseudo-C produced by Ghidra 12.1.2 headless from Kovah's annotated `.gzf`
- MIPS16e ISA (16-bit compressed MIPS); `ADJSP`/`addiu sp` prologues used for function detection
- PC-relative `lw` instructions (`lw v0, 0x23c(pc)`) are the MIPS16e way to load from a local literal pool — each `PTR_*` label in the decompile is one such pool entry
- The `_nop` after `jalr` is the MIPS branch-delay slot
- The function does NOT return a value (`undefined` return type) and takes no arguments
