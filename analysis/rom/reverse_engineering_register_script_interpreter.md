# ROM Register-Script Interpreter (`FUN_8003aea0`)

Source: GZF process-mode decompile, 2026-06-21 (Phase 9 consolidation).
Originally flagged in Phases 1-8 as "register-script interpreter, 15+ call
sites" — confirmed here with full decompile and exactly 16 call sites found.

---

## Function signature and call sites

`FUN_8003aea0` @ `0x8003aea0`, 688 bytes — large for a ROM leaf function,
reflecting the breadth of its opcode dispatch.

```c
void FUN_8003aea0(int script_ptr, uint num_halfwords)
```

Callers (16 total): `FUN_8010a000` (**the patch entry point /
`thing_that_calls_thing_that_installs_LMP_Patch`'s installer region** — see
`firmware/reverse_engineering_patch_entry.md`), `FUN_8010ff08` (patch),
`FUN_8003aa20`, `FUN_800393f8`, `FUN_80039418` (×2), `invoke_register_script_from_global_context_0x60_0x64` (`0x800393d8`),
`run_bb_reg_init_hook_chain_and_program_0x1e_5bit_field` (`0x800395f0`, ×4), `FUN_80039174`, `FUN_8003b9a4`, `FUN_8003ab74`,
`FUN_8003b170`, and directly from the raw-data region at file offset `0x392c`
(inside `thing_that_calls_thing_that_installs_LMP_Patch` itself, per the GZF's
"data" block annotation). The patch-entry call site confirms this interpreter
is invoked as part of normal firmware bring-up, not just as a debug/test path.

---

## Decompiled logic

```c
void FUN_8003aea0(int param_1, uint param_2)
{
    if (param_1 == 0 || (param_2 & 0xffff) == 0) return;

    ushort write_mask = 0xffff;   // running write-mask, settable by opcode 0xF

    for (uint i = 0; i < (param_2 & 0xffff); i += 2) {
        ushort *entry = (ushort*)(param_1 + i*2);
        uint    opcode_word = entry[0];     // first halfword: opcode (top nibble) + operand (low 12 bits)
        ushort  data        = entry[1];     // second halfword: data/value/repeat-count

        uint opcode = opcode_word & 0xf000;
        uint operand = opcode_word & 0xfff;

        switch (opcode) {
        case 0x0000:   // RAW WRITE (no mask) to a "register-pair" function
            if (entry[0] has no high bits set) call PTR_DAT_8003b150(reg_hi, reg_lo, 1, data);
            break;

        case 0x1000:   // MASKED READ-MODIFY-WRITE via paired read/write function
            old = PTR_DAT_8003b154(reg_hi, reg_lo, 1);
            PTR_DAT_8003b150(reg_hi, reg_lo, 1, (old & ~write_mask) | (data & write_mask));
            break;

        case 0x2000:   // MASKED READ-MODIFY-WRITE via single byte-index function
            old = PTR_DAT_8003b158(operand);
            PTR_DAT_8003b158(operand, (old & ~write_mask) | (data & write_mask));
            break;

        case 0x3000:   // MASKED READ-MODIFY-WRITE via a different byte-index pair
            old = PTR_DAT_8003b15c(operand);
            PTR_DAT_8003b158(operand, (old & ~write_mask) | (data & write_mask));
            break;

        case 0x4000:   // alias of 0x2000/0x5000 path -> PTR_DAT_8003b160(operand, data)
        case 0x5000:   // MASKED write via PTR_DAT_8003b160, with old value sourced from DAT_8003b164[operand]
            PTR_DAT_8003b160(operand, (DAT_8003b164[operand] & ~write_mask) | (data & write_mask));
            break;

        case 0x6000:   // RAW WRITE to a direct memory table DAT_8003b168[operand]
        case 0x7000:   // MASKED write to the same table
            DAT_8003b168[operand] = (opcode==0x7000)
                ? (DAT_8003b168[operand] & ~write_mask) | (data & write_mask)
                : data;
            break;

        case 0x8000:   // RAW WRITE to a second direct memory table DAT_8003b16c[operand]
        case 0x9000:   // MASKED write to the same second table
            similar to 0x6000/0x7000 but using DAT_8003b16c base;
            break;

        case 0xA000:   // FUN_80009680(data)  -- direct ROM call, e.g. delay or HW strobe
            break;

        case 0xB000:   // FUN_80009694(data)  -- direct ROM call (also used as the "wait tick" below)
            break;

        case 0xC000:   // POLL-WAIT: repeatedly call PTR_DAT_8003b15c(operand) until result is negative,
                        // waiting via FUN_80009694(1) between tries, up to `data` iterations
            break;

        case 0xD000:   // POLL-WAIT: repeatedly call PTR_DAT_8003b154(reg_hi,reg_lo,1) until bit 0xf clears,
                        // waiting via FUN_80009694(1) between tries, up to `data` iterations
            break;

        case 0xE000:   // POLL-WAIT: repeatedly call PTR_DAT_8003b154(reg_hi,reg_lo,1) until bit 0x1 sets,
                        // waiting via FUN_80009694(1) between tries, up to `data` iterations
            break;

        case 0xF000:   // SET WRITE-MASK: write_mask = data; affects all subsequent masked ops this call
            break;
        }
    }
}
```

---

## Opcode summary table

| Opcode (top nibble) | Operation |
|---|---|
| `0x0` | Raw write via paired reg-write function (`PTR_DAT_8003b150`) |
| `0x1` | Masked read-modify-write via paired reg read/write functions (`...154`/`...150`) |
| `0x2` | Masked read-modify-write via single-index function (`...158`) |
| `0x3` | Masked read-modify-write, read via `...15c`, write via `...158` |
| `0x4`, `0x5` | Masked write via `...160`, prior value from table `DAT_8003b164` |
| `0x6`, `0x7` | Raw / masked write to direct table `DAT_8003b168` |
| `0x8`, `0x9` | Raw / masked write to direct table `DAT_8003b16c` |
| `0xA` | Direct call to `FUN_80009680(data)` |
| `0xB` | Direct call to `FUN_80009694(data)` |
| `0xC` | Poll-wait on `...15c(operand)` until result negative (timeout = `data` retries) |
| `0xD` | Poll-wait on `...154(...)` until bit `0xf` clears |
| `0xE` | Poll-wait on `...154(...)` until bit `0x1` sets |
| `0xF` | Set the running write-mask for subsequent masked ops |

Each script entry is a `(opcode_word, data)` halfword pair, i.e. the script
is a flat array of 4-byte records consumed 2 halfwords (`param_2` is a count
of **halfwords**, so each record is one iteration of `i += 2`).

---

## Purpose

`FUN_8003aea0` is a **generic byte-code interpreter for hardware
register-configuration scripts** — a small VM with 16 opcodes covering raw
writes, masked read-modify-writes (via several different underlying
register-access backends selected by opcode), direct function calls, polling
waits with timeout, and a settable write-mask that persists across script
entries. This pattern (mask-then-write, with the mask settable mid-script) is
exactly the kind of compact representation used to encode large, repetitive
hardware bring-up sequences (e.g. per-band RF calibration tables, baseband
init tables) without expanding them into full native code.

This matches and explains the existing finding in
`firmware/reverse_engineering_sco_esco_layer.md`/RF-table analysis scripts
(`ExtractRFTables.java`/`ExtractRFTables2.java`, see `GHIDRA_SCRIPTS.md`) that
pass `FUN_8003aea0` "reg-cmd arrays" — those arrays are exactly this
byte-code format. The fact that it's called both from ROM init
(`FUN_8003aa20` etc.) and from the patch entry point (`FUN_8010a000`) means
the **patch can and does supply its own register-configuration scripts** to
this same ROM interpreter, rather than needing to reimplement register access
itself — an important data point for the libre firmware design: hardware
init sequences can be expressed as register-script byte arrays consumed by
this existing ROM interpreter instead of native MIPS16e code.

### Underlying function-pointer table (literal pool, `0x8003b150`-`0x8003b16c`)

| Symbol | Used by opcodes | Role (inferred) |
|--------|-----------------|------|
| `PTR_DAT_8003b150` | `0x0`, `0x1` | Paired register write (3-4 args: hi, lo, width, value) |
| `PTR_DAT_8003b154` | `0x1`, `0xD`, `0xE` | Paired register read (3 args: hi, lo, width) |
| `PTR_DAT_8003b158` | `0x2`, `0x3` | Single-index register write/read |
| `PTR_DAT_8003b15c` | `0x3`, `0xC` | Single-index register read (status-poll source) |
| `PTR_DAT_8003b160` | `0x4`, `0x5` | Indexed write (table-backed) |
| `DAT_8003b164` | `0x5` | Shadow/cache table read by opcode `0x5` before masking |
| `DAT_8003b168` | `0x6`, `0x7` | Direct memory table 1 |
| `DAT_8003b16c` | `0x8`, `0x9` | Direct memory table 2 |

These addresses are very close to (but distinct from) the generic baseband
register helpers documented in `reverse_engineering_baseband_reg_helpers.md`
(`0x80011510`/`0x80011608` live at a different ROM region, `0x8001xxxx`) —
`FUN_8003aea0`'s backends are a separate, more specialized register-access
layer specific to whatever subsystem owns the `0x8003axxx`-`0x8003bxxx`
region (likely RF/PHY calibration, given the RF-table connection noted
above).

---

## Libre firmware implication

If the libre firmware needs to replicate any hardware bring-up sequence that
the original patch expressed as a register-script (rather than native code),
it can call `FUN_8003aea0` directly with an equivalent byte-code array,
rather than reimplementing register access from scratch. This is a strong
candidate primitive for keeping the libre patch small.
