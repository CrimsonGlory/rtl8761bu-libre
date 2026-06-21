# ROM Hardware Register Read/Write Protocol

Source: GZF process mode analysis, 2026-06-08.

---

## Overview

The RTL8761BU baseband hardware registers are accessed through a memory-mapped
I/O (MMIO) interface at kseg1 physical address `0xb000a0bc`.  ROM functions
`FUN_8001136c` (read) and `FUN_8001139c` (write) are the sole access path used
by the patch and by `FUN_80009980` (HW register init).

---

## Register Access Protocol

### MMIO address

From the literal pools of both functions:

| Symbol | Value | Meaning |
|--------|-------|---------|
| `DAT_80011390` | `0xb000a0bc` (kseg1) | MMIO register control word address |
| `DAT_800113cc` | same | (write fn uses same address) |

Physical address: `0x1000a0bc` (kseg1 → physical: clear bit 29).

### Control word layout (32-bit MMIO register at `0xb000a0bc`)

```
Bit  31..22   21..16   15..0
      reserved  index   value
       (mask)  (6-bit) (16-bit)
```

- **bits[21:16]** — 6-bit register index (0–63)
- **bits[15:0]** — 16-bit register value (read result or write data)
- **mask constants** from literal pool control which bits are preserved during R-M-W

### Read protocol (`FUN_8001136c`, 34 bytes)

```c
uint FUN_8001136c(uint index) {
    uint *mmio = DAT_80011390;              // 0xb000a0bc
    // Read-modify-write: place index in bits[21:16], preserve other control bits
    *mmio = (*mmio & mask1 & mask2) | ((index & 0x3f) << 16);
    return *mmio & 0xffff;                  // return 16-bit value
}
```

`mask1` and `mask2` from the literal pool clear the index field and any status bits
before inserting the new index.  The hardware responds by updating `bits[15:0]` with
the register value.

### Write protocol (`FUN_8001139c`, 46 bytes)

```c
void FUN_8001139c(uint index, uint value) {
    uint *mmio = DAT_800113cc;              // 0xb000a0bc
    uint word = (*mmio & idx_mask & val_mask | ((index & 0x3f) << 16))
                & combined_mask
                | (value & 0xffff);
    *mmio = word;                           // write index + value
    *mmio = trigger_bit | word;             // set trigger bit to commit write
}
```

The second write (`*mmio = trigger_bit | word`) asserts a "write-enable" or "strobe"
bit (from `DAT_800113dc`) that causes the hardware to latch the new value.

**Two MMIO writes are required for every register write.**

---

## FUN_8000a180 — Register Read Wrapper / Retry Loop

At runtime `0x8000a180` (ROM), `FUN_80009980` calls this to read registers.
It is NOT a simple thunk — it's a polling/retry wrapper:

```c
void FUN_8000a180(param_1, param_2) {
    do {
        (*in_v0)(param_1, param_2, 4);   // call register read fn via v0 ptr
        // poll completion / status in a state struct via PTR_DAT_8000a1ac
        // retry loop up to 8 times; calls HCI cmd handler if needed
    } while (!done);
}
```

`in_v0` is the actual register read function (loaded from a function pointer, not a
hardcoded jal).  The loop iterates up to 8 times checking a status byte, updating
a counter, and potentially calling a HCI command-complete handler via
`called_at_end_of_every_HCI_CMD_via_fptr`.

---

## Seven Baseband Registers Programmed at Init (`FUN_80009980`)

From prior analysis of `FUN_80009980` (runtime `0x80109980`):

| Index | Operation | Source |
|-------|-----------|--------|
| 0 | write | computed from register 0 value |
| 2 | write | computed |
| 3 | write | computed |
| 5 | write | bos+0x168 (eSCO codec RX param) |
| 6 | write | bos+0x16a (eSCO codec TX param) |
| (2 others) | read first, then write | |

MMIO base from `FUN_80009980`'s literal pool: `0xb000a0bc` (same as above, confirmed).

---

## Implications for Libre Firmware

The libre firmware calls `FUN_80009980` which calls `FUN_8000a180` which calls the
register read function via a function pointer (`in_v0`).  That pointer must be
pre-loaded (by the ROM init chain) with the address of `FUN_8001136c`.

The libre firmware can call `FUN_80009980` and `FUN_8001139c` directly via their ROM
addresses since they are pure ROM functions with no patch dependencies.

Register indices are 0–63 (6-bit); values are 16-bit.  The trigger-bit write pattern
at `0xb000a0bc` is required for all writes.
