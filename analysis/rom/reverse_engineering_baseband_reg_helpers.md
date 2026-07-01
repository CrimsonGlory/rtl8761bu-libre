# ROM Baseband Register Helper Functions

Source: GZF process-mode decompiles, 2026-06-21 (Phase 9 consolidation of
functions identified-but-not-written-up during Phases 1-8).

---

## Overview

These are low-level ROM helpers that sit directly on top of the documented
`FUN_8001136c`/`FUN_8001139c` HW register read/write protocol
(`rom/reverse_engineering_rom_regs.md`). They were originally noticed as
"new ROM fns" while tracing AFH (Adaptive Frequency Hopping) and other
firmware hook targets in Phases 1-8, but only ever got a one-line characterization.
This doc gives them a full write-up.

All functions are heavily call-fanned-out (dozens of callers each), confirming
they are generic low-level primitives rather than one-off helpers.

---

## 1. `read_baseband_register_masked_busywait` — Baseband register read (busy-wait, masked)

`0x80011510`, 98 bytes (renamed Pass 7b region `0x80010000`, 2026-07-01). Callers: 50+ (both ROM and patch/data block, e.g.
`VSC_0xfc61_write_to_relevant_data_FUN_80030dd8`, many `FUN_8010xxxx` patch
functions, AFH-related ROM functions below).

```c
undefined4 read_baseband_register_masked_busywait(uint param_1, uint param_2)
{
    // param_1 = register address/index, param_2 = width-selector (0/1/2)
    if (((1 << (param_2 & 0x1f)) - 1U & param_1 & 0xffff) == 0) {
        uVar7 = param_2 << 0x1b | param_1 & 0xffff;
        uVar5 = disable_interrupts__clear_LSBit_of_CP0_Status_Register_();
        *DAT_8001157c = DAT_80011578 | uVar7;   // set request word (with extra bits)
        *DAT_8001157c = uVar7;                  // then write clean request word
        // poll readiness bit in *DAT_8001157c, up to 20000 iterations
        if (ready) uVar8 = *DAT_80011580;       // read result register
        else       uVar8 = DAT_80011574;        // timeout fallback value
        enable_interrupts__set_CP0_Status_to_arg_(uVar5);
    } else {
        uVar8 = DAT_80011574;                   // misaligned access -> fallback
    }
    return uVar8;
}
```

This is a generic MMIO register-read primitive: it builds a request word
encoding the access width (`param_2`, used as a shift/mask selector — 0/1/2 for
byte/halfword/word-ish access) and the register address, writes it twice to a
control register, then busy-polls (20000-iteration timeout) a ready bit before
reading the result register. Interrupts are disabled for the duration —
consistent with the `FUN_8001136c` protocol documented in
`reverse_engineering_rom_regs.md`, but this is a **different, more general**
register window (its own literal-pool addresses `DAT_8001157c`/`...80`/`...74`/`...78`,
not the `0xb000a0bc` MMIO word used by `FUN_8001136c`). Alignment is validated
up-front: if the requested width's bit pattern doesn't cleanly divide the
address, it returns the fallback value without touching hardware.

**Purpose:** generic masked/width-aware baseband register read with hardware
busy-wait and a safe fallback on timeout or misalignment.

## 2. `write_baseband_register_masked_busywait` — Baseband register write (busy-wait, masked, with merge)

`0x80011608`, 110 bytes (renamed Pass 7 region `0x80010000`, 2026-07-01). Callers: 50+, same population as `read_baseband_register_masked_busywait`
(its write-side counterpart).

```c
void FUN_80011608(uint param_1, int param_2, uint param_3)
{
    if (((1 << (param_3 & 0x1f)) - 1U & param_1 & 0xffff) == 0) {
        uVar6 = DAT_80011678 | param_1 & 0xffff | param_3 << 0x1b;
        if ((param_3 & 0xff) == 0) {
            param_2 = param_2 << ((param_1 & 3) << 3);  // byte-lane shift for sub-word writes
        }
        uVar4 = disable_interrupts...();
        *DAT_8001167c = param_2;                 // write data register
        *DAT_80011680 = DAT_80011684 | uVar6;     // set request word (w/ extra bits)
        *DAT_80011680 = uVar6;                    // clean request word
        // poll ready bit, up to 20000 iterations
        enable_interrupts...(uVar4);
    }
    return;
}
```

Mirror image of `read_baseband_register_masked_busywait`: validates alignment, shifts sub-word data into
the correct byte lane when `param_3` (width selector) is 0 (byte access), writes
the data register then strobes the request register twice, and busy-waits for
completion. No return value — write completion isn't checked beyond the poll
timing out silently.

**Purpose:** generic masked/width-aware baseband register write, write-side of
the same low-level register access primitive as `read_baseband_register_masked_busywait`.

## 3. `FUN_800115c8` — Byte-extraction wrapper around `read_baseband_register_masked_busywait`

`0x800115c8`, 62 bytes. Callers: `FUN_8010c260` (patch), `FUN_801106bc`,
`pulse_bb_regs_0x00_0x10_with_cc_mode_bits_and_spin_delays`, `VSC_0xfc61_write_to_relevant_data_FUN_80030dd8`, `FUN_8003b170`.

```c
uint FUN_800115c8(uint param_1)
{
    if ((param_1 & 3) == 0)
        uVar1 = FUN_80011510(param_1 & 0xffff, 0);   // word-aligned: byte-width read
    else {
        uVar1 = FUN_80011510(param_1 & 0xfffc, 2);    // round down to word, word-width read
        uVar1 = uVar1 >> ((param_1 & 3) << 3);        // shift to extract target byte
    }
    return uVar1 & 0xff;
}
```

**Purpose:** byte-granular register read on top of `read_baseband_register_masked_busywait`, handling
the case where the requested byte address isn't word-aligned by reading the
containing word and shifting out the byte.

## 4. `FUN_80011584` — Halfword-extraction wrapper around `read_baseband_register_masked_busywait`

`0x80011584`, 66 bytes. Callers: `FUN_8010c278` (×7 call sites), `FUN_8010ce0c`,
`VSC_0xfc61_write_to_relevant_data_FUN_80030dd8`, `FUN_800122fc`, `FUN_8000eda0`,
2 anonymous call sites in the data block.

```c
uint FUN_80011584(ushort param_1)
{
    if ((param_1 & 3) == 2) {
        uVar1 = FUN_80011510(param_1 & 0xfffc, 2);  // word-aligned read
        uVar1 = uVar1 >> 0x10;                       // upper halfword
    } else if ((param_1 & 3) != 0) {
        return 0xdead;                               // misaligned: sentinel error value
    } else {
        uVar1 = FUN_80011510(param_1, 1);            // halfword-width read, already aligned
    }
    return uVar1 & 0xffff;
}
```

**Purpose:** halfword-granular register read; returns the literal sentinel
`0xdead` for addresses that are neither word- nor halfword-aligned (offset 1 or
3 mod 4) — a deliberate "this should never happen" marker rather than a
silently wrong value.

## 5. `afh_flag_gated_set_or_clear_bb_reg_0xfc_bit_0x1000` — AFH-related register bit toggle

`0x80011a74`, 56 bytes (renamed Pass 95 2026-07-01). Callers:
`unknown_referencing_default_name_6`, `FUN_8010ccb8` (patch), `FUN_80011ab0`,
plus 1 raw-data caller at `0x6532`.

```c
void afh_flag_gated_set_or_clear_bb_reg_0xfc_bit_0x1000(void)
{
    uVar2 = FUN_80011510(0xfc, 2);            // read register 0xfc (word width)
    if ((*DAT_80011aac >> 0x10 & 0x42) == 0)
        uVar2 = uVar2 | 0x1000;
    else
        uVar2 = uVar2 & 0xffffefff;
    FUN_80011608(0xfc, uVar2, 2);             // write back with bit 0x1000 toggled
}
```

Reads a global flags word (`*DAT_80011aac`, bits 0x42 in the upper halfword —
this is the same AFH capability/feature flag area referenced elsewhere in the
AFH init chain per `kovah_function_list.md`), and conditionally sets or clears
bit `0x1000` of baseband register `0xfc` based on it.

**Purpose:** conditionally enables/disables a single baseband-register control
bit (register `0xfc`, bit `0x1000`) based on AFH-related feature flags — part
of the AFH/channel-classification hardware bring-up sequence already partially
covered by `reverse_engineering_hardware_layer.md`'s call-graph context (this
function itself was not previously decompiled).

## 6. `FUN_800117a4` — Unconditional OR-mask register set

`0x800117a4`, 14 bytes. Callers: `FUN_8010ce0c` (patch), `FUN_80000d78`,
`FUN_80037460`.

```c
void FUN_800117a4(void)
{
    *PTR_DAT_800117b4 = *PTR_DAT_800117b4 | 0xfc00;
}
```

**Purpose:** trivial register/memory bit-set helper — ORs `0xfc00` into a
fixed global word. Likely sets a block of feature-enable or interrupt-mask
bits as part of init. (3 callers across very different subsystems — boot init
`FUN_80000d78`, an unrelated `FUN_80037460`, and the AFH init chain
`FUN_8010ce0c` — suggests this is a shared one-shot "enable this hardware
block" toggle rather than something with periodic re-application.)

## 7. `dispatch_afh_cap_param_to_bb_register_clear_loop` / `FUN_80012e38` — Config-driven AFH/feature register clear loop

`dispatch_afh_cap_param_to_bb_register_clear_loop` (was `FUN_800122b8`, `0x800122b8`, 64 bytes) reads a config byte pair
(`config_base->field64_0x46`/`field65_0x47`), and if bit `0x80` is set, extracts
a 4-bit nibble selected by `param_1` (1 or 2 → low or high nibble of byte+1)
and passes it to `FUN_80012e38`.

```c
void dispatch_afh_cap_param_to_bb_register_clear_loop(char param_1) {
    uVar2 = config_base->field64_0x46 | (config_base->field65_0x47 << 8);
    if ((uVar2 & 0x80) != 0) {
        if (param_1 == 1)      uVar2 = uVar2 >> 8 & 0xf;
        else if (param_1 == 2) uVar2 = uVar2 >> 0xc;
        else return;
        FUN_80012e38(uVar2);
    }
}

void FUN_80012e38(void) {  // note: Ghidra shows no formal params but uses an implicit arg
    bVar2 = 0;
    do {
        cVar3 = (bVar2 + 0x5c) * 4;                       // register indices 0x170,0x174,0x178,0x17c
        uVar1 = FUN_80011510(cVar3, 2);
        FUN_80011608(cVar3, uVar1 & 0xffffff3f, 2);        // clear bit 0x40 in each
        bVar2 = bVar2 + 1;
    } while (bVar2 < 4);
}
```

Callers of `dispatch_afh_cap_param_to_bb_register_clear_loop`: `unknown_referencing_default_name_6` (×3),
`FUN_8010ce0c` (patch AFH init chain), `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c`
(vendor HCI command dispatcher), `FUN_800122fc`. Only caller of `FUN_80012e38`
is `dispatch_afh_cap_param_to_bb_register_clear_loop`.

**Purpose:** config-blob-gated register maintenance — when config flag bit
`0x80` of byte `0x46/0x47` is set, clears bit `0x40` across 4 consecutive
baseband registers (`0x170`, `0x174`, `0x178`, `0x17c`). Reachable both from
ROM's own AFH/feature init path and from a vendor-specific HCI command,
suggesting this corresponds to a runtime-toggleable hardware feature (likely
AFH channel map or interference-mitigation related, matching the indices'
proximity to other AFH state offsets documented elsewhere).

## 8. `FUN_8000b820` — Fixed register/state triple-write + dispatch

`0x8000b820`, 56 bytes. Callers: `FUN_8010e214` (patch, ×2), `FUN_80110724`
(patch), `FUN_8003ada4`.

```c
void FUN_8000b820(uint param_1) {
    *DAT_8000b880 = (byte)param_1;                          // low byte of param
    *DAT_8000b884 = (*DAT_8000b884 & 0xfc) | ((param_1 >> 8) & 3);  // 2-bit field merge
    *DAT_8000b888 = 0xf;                                     // fixed control nibble
    FUN_8000b864();                                          // dispatch/commit
}
```

**Purpose:** packs a 10-bit value (`param_1`, split as 8 low bits + 2 high
bits) into two state bytes, sets a fixed control nibble (`0xf` — likely "all
fields valid / commit" flags), then calls `FUN_8000b864` to apply/dispatch the
change. Called from both ROM init (`FUN_8003ada4`) and patch-installed code
(`FUN_8010e214`, `FUN_80110724`), suggesting this is a small generic
"set + commit" state primitive reused by multiple subsystems (its narrow
10-bit value width is consistent with e.g. a clock divider, slot offset, or
similar small hardware parameter — exact semantics undetermined without
further tracing into `FUN_8000b864`).

---

## Summary Table

| Function | Size | Role |
|----------|------|------|
| `read_baseband_register_masked_busywait` | 98 B | Generic masked baseband register read (busy-wait) |
| `write_baseband_register_masked_busywait` | 110 B | Generic masked baseband register write (busy-wait) |
| `FUN_800115c8` | 62 B | Byte-granular read wrapper around `read_baseband_register_masked_busywait` |
| `FUN_80011584` | 66 B | Halfword-granular read wrapper around `read_baseband_register_masked_busywait` (returns `0xdead` on bad alignment) |
| `afh_flag_gated_set_or_clear_bb_reg_0xfc_bit_0x1000` | 56 B | AFH-flag-gated toggle of register `0xfc` bit `0x1000` |
| `FUN_800117a4` | 14 B | Unconditional OR `0xfc00` into a fixed register/global |
| `dispatch_afh_cap_param_to_bb_register_clear_loop` | 64 B | Config-flag-gated dispatcher to `FUN_80012e38` |
| `FUN_80012e38` | 70 B | Clears bit `0x40` across registers `0x170/0x174/0x178/0x17c` |
| `FUN_8000b820` | 56 B | Packs 10-bit value into 2 state bytes + commits via `FUN_8000b864` |

These are all internal ROM primitives with no patch-side reimplementation
needed — the libre firmware calls into them indirectly via the same hook
points already documented (`reverse_engineering_hardware_layer.md`,
`reverse_engineering_rom_regs.md`) and requires no new libre code for this
layer.
