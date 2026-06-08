# RTL8761BU — String-Associated Function Installer Analysis

Source: `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`
Analysis session: 2026-06-08

---

## Summary

Kovah's label `patch_that_installs_all_the_string_associated_function_patches__including_LMP`
refers to the **address-pair table at file offset 0xa0** (runtime `0x801000A0`). This
table IS the patch — it is the data the ROM uses when registering LMP/protocol handler
functions. The "installer" that processes this table is a ROM function
(`interesting_string_user_fptr_registration_function` at ROM `0x80009990`), which is NOT
accessible from the 42088-byte `rtl8761bu_fw.bin` binary.

No function in the accessible patch binary has any reference to `0x801000A0`. A full
scan of all 4-byte-aligned positions in the binary confirmed zero occurrences of
`0x801000A0` or any value in the `0x80100000–0x80100200` range in any literal pool.

---

## Architecture: What "Installs" the String-Associated Patches

```
[ROM: interesting_string_user_fptr_registration_function @ 0x80009990]
          │
          │ reads 15-entry address-pair table at runtime 0x801000A0
          │
          ▼
[Patch DATA: address-pair table @ file 0xa0, runtime 0x801000A0]
          │
          │ each entry: (data_struct_ptr, fn_ptr)
          │ data_struct_ptr → RAM LMP PDU descriptor (0x8012xxxx)
          │ fn_ptr → MIPS16e handler stub in patch (0x8010A174–0x8010A304)
          │
          ▼
[Patch CODE: MIPS16e handler stubs @ 0x8010A160–0x8010A2FF]
```

The address-pair table and the handler stubs are BOTH part of the patch binary.
The ROM function reads the table and wires up each (descriptor, handler) pair
into the BT stack's LMP dispatch machinery.

---

## Address-Pair Table (file 0xa0, runtime 0x801000A0)

15 entries of `(data_ptr, fn_ptr)` where:
- `data_ptr` → RAM struct describing an LMP PDU type (`0x8012xxxx`)
- `fn_ptr` → MIPS16e code stub in the patch (`0x8010xxxx`, even address = MIPS16e)

| Table addr (Ghidra) | data_ptr | fn_ptr | Status |
|---------------------|----------|--------|--------|
| `0x800000A0` | `0x8010A02C` | `0x8012005A` | header / sentinel |
| `0x800000A8` | `0x80120058` | `0x8010C960` | out-of-binary |
| `0x800000B0` | `0x801211A4` | `0x8010A34C` | **in binary** |
| `0x800000B8` | `0x8012141C` | `0x8010AC9C` | out-of-binary |
| `0x800000C0` | `0x80120D54` | `0x8010AA5C` | out-of-binary |
| `0x800000C8` | `0x80120E98` | `0x8010A5E8` | out-of-binary |
| `0x800000D0` | `0x80120FC0` | `0x8010A4C0` | out-of-binary |
| `0x800000D8` | `0x80120E70` | `0x8010A184` | **in binary** |
| `0x800000E0` | `0x801214D0` | `0x8010A174` | **in binary** |
| `0x800000E8` | `0x80121498` | `0x8010CA78` | out-of-binary |
| `0x800000F0` | `0x80121744` | `0x8010A1D8` | **in binary** |
| `0x800000F8` | `0x80120FFC` | `0x8010A208` | **in binary** |
| `0x80000100` | `0x8012506C` | `0x8010A304` | **in binary** |
| `0x80000108` | `0x80121184` | `0x8010A2B8` | **in binary** |
| `0x80000110` | `0x80121180` | `0x8010C9D0` | out-of-binary |

Seven fn_ptrs (0x8010A174, 0x8010A184, 0x8010A1D8, 0x8010A208, 0x8010A2B8,
0x8010A304, 0x8010A34C) map to Ghidra addresses 0x8000A174–0x8000A34C, which
fall within the 42088-byte patch binary. The remaining eight are beyond file end.

---

## Handler Stub Region (file 0xa160–0xa2FF, runtime 0x8010A160–0x8010A2FF)

### Raw bytes (Ghidra 0x8000A160–0x8000A280)

The region has a highly repetitive 8-byte structure:
```
8000a160: 12 20 b0 9e 11 20 23 00   → words: 0x2012 | 0x9eb0 | 0x2011 | 0x0023
8000a168: 12 20 30 9f 11 20 22 00   → words: 0x2012 | 0x9f30 | 0x2011 | 0x0022
8000a170: 12 20 20 9e 11 20 21 00   → words: 0x2012 | 0x9e20 | 0x2011 | 0x0021
8000a178: 12 20 a0 9e 11 20 20 00   → words: 0x2012 | 0x9ea0 | 0x2011 | 0x0020
8000a180: 12 20 20 9f 11 20 1f 00   → words: 0x2012 | 0x9f20 | 0x2011 | 0x001f
...  (pattern continues, index counting from 0x23 down to 0x00)
8000a280: 12 20 00 03 10 20 00 40   → end of repetitive region
```

**Ghidra cannot decode this region** — `FUN_8000a180` (the only function Ghidra
defines there) has no decoded instructions, only address-range ownership.

### Manual MIPS16e decoding of the 8-byte pattern

Each 8-byte block = four MIPS16e instructions:

| Offset | Word | Decoded |
|--------|------|---------|
| +0 | `0x2012` | `bteqz +36` (I8/BTEQZ, imm=0x12, branch if T=0) |
| +2 | `0x9eXX` / `0x9fXX` | `lw rX, off(rY)` (LW GR-relative, varies per entry) |
| +4 | `0x2011` | `bteqz +34` (I8/BTEQZ, imm=0x11) |
| +6 | `0x00ZZ` | `addiu s1, ZZ` (ADDIU RX, ZZ counts 0x23→0x00) |

This is a **case-chain dispatch** pattern: each 8-byte block tests a condition (T
bit from prior SLTI/SLTIU), conditionally loads from memory, and adds a case-
specific value to s1. The 36 entries (index 0x23 to 0x00) map to 36 LMP PDU types
or similar protocol discriminants.

### fn_ptr entry points within the region

The seven accessible fn_ptrs from the address-pair table enter this dispatch chain
at specific offsets (not necessarily at block boundaries):

| fn_ptr (runtime) | Ghidra | Bytes at entry |
|------------------|--------|----------------|
| `0x8010A174` | `0x8000A174` | `11 20` = `0x2011` = `bteqz +34` |
| `0x8010A184` | `0x8000A184` | `11 20` = `0x2011` = `bteqz +34` |
| `0x8010A1D8` | `0x8000A1D8` | `12 20` = `0x2012` = `bteqz +36` |
| `0x8010A208` | `0x8000A208` | `12 20` = `0x2012` = `bteqz +36` |
| `0x8010A2B8` | `0x8000A2B8` | varies (late region, less structured) |
| `0x8010A304` | `0x8000A304` | varies (after 0x8000A280, different structure) |
| `0x8010A34C` | `0x8000A34C` | file 0xa34c — post-dispatch handler code |

All seven fn_ptrs enter the dispatch chain at different points (corresponding to the
LMP PDU subtype handled by each installed handler). The BTEQZ-chain acts as a
multi-way branch selecting which offset to add to s1.

### Post-dispatch region (after 0x8000A280)

At 0x8000A280+, the byte pattern becomes varied (non-repetitive), suggesting this
is actual function code — likely the handler implementations that execute after the
dispatch table selects the correct case. Ghidra also fails to decode this region.

---

## Late-Patch Functions — What They Are

The seven functions in the 0x80009000–0x8000a000 range that CAN be decompiled
(using ADJSP prologue detection) are NOT related to the string-association installer.
They implement hardware and audio subsystem functionality:

### FUN_80009980 (runtime 0x80109980, size=175) — Hardware register configuration
```c
void FUN_80009980(void) {
    // Clears/sets control bits in registers at 0xb000a0bc, 0xb000a05c
    // (kseg1 MMIO: physical 0x1000a0bc, 0x1000a05c)
    pcVar1 = _thunk_FUN_8000a180;  // register-read  fn from ROM (via thunk)
    pcVar2 = pcRam80009aa8;         // register-write fn from ROM
    // Read-modify-write loop for 7 hardware registers (indices 0,3,5,6,2,0,...)
    // Reads eSCO codec params from struct at iRam80009ab4+0x168, +0x16a
}
```
- s0 = ROM register-read fn at runtime ~`0x8001136c` (accessed via thunk)
- s1 = ROM register-write fn at runtime ~`0x8001139c`
- Targets: 7 hardware baseband registers (indices 0,2,3,5,6)
- Reads codec params from `bos_struct+0x168` and `bos_struct+0x16a`

### FUN_80009c08 (runtime 0x80109c08, size=448) — eSCO connection type negotiator
```c
undefined4 FUN_80009c08(param_1, char connected, undefined1 param_3, int conn_rec) {
    // Selects eSCO packet type based on connection type at conn_rec+0x28:
    //   0xA000 / 0xB000 → HV3/EV3 class
    //   0xE000 / 0xF000 → EV4/EV5 class (with extended table at +0x250)
    // Checks capability flags at conn_rec+0x250 (bits: 0x10, 0x8, 0x4, 0x2)
    // Checks negotiated bandwidth at conn_rec+0x26
    // Returns packet type code via call to pcRam80009dd4(connected, param_3)
}
```
- Connection type field (`conn_rec+0x28`): distinguishes SCO/eSCO variants
- Capability word (`conn_rec+0x250`): indicates supported packet types
- Return code path: calls two function pointers (primary/alternate)
- Packet type codes: `0x3`, `0x4`, `0x8`, `0xa`, `0xb`, `0x3a`–`0x4b` (eSCO subtypes)

### FUN_80009de0 (runtime 0x80109de0, size=164) — Bit-count checksum
```c
uint FUN_80009de0(int param_1) {
    // Hamming-weight (popcount) checksum over first 10 bytes of param_1:
    //   - popcount of 4-byte words at param_1[0..7] (LE, via 4 sb-loads each)
    //   - popcount of 2-byte value at param_1[8..9]
    // Returns 8-bit sum of all set bits
    // Uses standard popcount constants: 0x55555555, 0x33333333, 0x0f0f0f0f, 0x01010101
}
```
Likely used for **LMP PDU header validation** or firmware integrity checking.

### FUN_80009200 (runtime 0x80109200, size=322) — Bitfield-to-register encoder
```c
void FUN_80009200(byte *packed_cfg, uint count, uint reg_base) {
    // bit_positions[] = {7, 11, 15}  (groups of 2 bits at those bit offsets)
    // Loop `count` times:
    //   - Reads 2 bits from packed_cfg at each of 3 bit positions
    //   - Constructs 16-bit value `local_14` from the 3 groups
    //   - Writes to hardware register via pcRam80009344(reg_addr, 0, value)
    //   - Reads back via pcRam80009348(alt_reg_addr, 0)
    //   - Clears/sets bit groups via pcRam8000934c
}
```
Decodes a packed bitfield configuration array and programs hardware registers.
Likely a PCM/I2S codec or RF parameter initializer.

### FUN_80009550 (runtime 0x80109550, size=226) — Audio codec sample reader
```c
void FUN_80009550(void) {
    // Loops 64 times: calls pcRam8000963c(channel, local_20) to get samples
    // Builds packed result buffer
    // Calls logging/debug fn pcRam80009648(3, 0xfd, 0x217, 0x4e53, 8, ...)
    // 0x4e53 = 'NS' and 0x4e54 = 'NT' — likely codec debug tags
}
```

### FUN_800096d4 (runtime 0x801096d4, size=258) — Audio circular buffer writer
```c
void FUN_800096d4(void) {
    // Writes to an 8-element circular buffer (index at iRam+0x80, count at +0x82)
    // Each element: 12 bytes of audio/connection parameters
    // Two modes (parity bit in flag):
    //   - Mode 0: fills offsets 0x6, 0x8, 0xa, 0xc, 0xe
    //   - Mode 1: fills offsets 0xc, 0xe (offset 10-byte audio frame)
    // Tracks sequence number at iRam+0x83 and index at iRam+0x80 (mod 8)
}
```

### FUN_80009824 (runtime 0x80109824, size=180) — eSCO retransmission timer
```c
void FUN_80009824(void) {
    // Checks eSCO packet counter vs threshold uRam80009950
    // If exceeded: handles retry (calls pcRam80009954 for type 3, pcRam80009958 otherwise)
    // Checks hardware register mask uRam8000995c
    // If set: calls connection setup/teardown via pcRam80009960(1/0, ...)
    // Bit manipulation at conn_rec+0x44, +0x45 (packet type flags)
}
```

---

## Table Processor: Confirmed NOT in Accessible Binary

A complete scan found:
- **Zero** instances of `0x801000A0` in any 4-byte-aligned position (whole binary)
- **Zero** instances of any value in `0x80100000–0x80100200` range in literal pools
- **Zero** Ghidra xrefs to `0x800000A0`
- Only one instruction matching the pattern (`FUN_800009c0:0x800009c2 beqz 0x80000a08`) — a branch target, unrelated to the table

The table processor is confirmed to be a ROM function at `0x80009990`
(`interesting_string_user_fptr_registration_function` per Kovah's annotation).
This ROM function is not accessible from wairz (the raw binary covers only the
patch at `0x80000000–0x8000A467`; ROM functions at ≥ `0x80008000` are beyond the
patch file).

---

## Libre Replacement Implications

### What must be provided

| Item | Status | Notes |
|------|--------|-------|
| Address-pair table at runtime `0x801000A0` | Required | 15 entries, ROM reads it |
| Handler stubs at `0x8010A160–0x8010A2FF` | Required (partial) | 7 of 15 fn_ptrs in binary; 8 are beyond binary |
| Hardware register init (`FUN_80009980`) | Required | Must program 7 baseband registers using ROM register r/w fns |
| eSCO negotiator (`FUN_80009c08`) | Required | Selects eSCO packet type for each connection |
| Checksum fn (`FUN_80009de0`) | Likely required | Called for LMP PDU validation |
| Audio buffer fns (`FUN_800096d4`, `FUN_80009824`) | Required for audio | eSCO audio path |

### What can be stubbed

The 8 out-of-binary fn_ptrs can likely be replaced with minimal stubs (return 0 or
no-op) if only basic HCI bring-up (not audio) is required for linux-libre compliance.

### Handler stub template (8-byte pattern)

Each MIPS16e handler stub in the dispatch chain is 8 bytes. A stub for a new entry
can follow the same `bteqz / lw / bteqz / addiu` pattern with the appropriate index
and register loads. The exact semantics require dynamic analysis (not available from
static analysis alone).

---

## Vanilla vs Kovah Firmware

The GZF (`2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`) differs from the
redistributed `rtl8761bu_fw.bin` in a significant way: Kovah appended his own
analysis code ("dark firmware") to the patch binary.

| Item | Vanilla `rtl8761bu_fw.bin` | GZF patch block |
|------|---------------------------|-----------------|
| File size | 42088 bytes (`0xa468`) | 44484 bytes (`0xadc4`) |
| Runtime end | `0x8010A467` | `0x8010ADC3` |
| Extra region | — | `0x8010A468–0x8010ADC3` (2396 bytes, `0x95c`) |
| Entry modified | No | Likely: jump to `0x8010A468` hooked at `0x8010A001` |

The 2396-byte extra region at `0x8010A468–0x8010ADC3` is Kovah's analysis code,
not part of the Realtek firmware. It does not exist in any Realtek release.

The GZF additionally contains two memory blocks not in the vanilla binary:
- **`data` block** (`0x80100000–0x8013FFFF`): runtime RAM snapshot taken from
  Kovah's running chip — this is where the sub-installer functions exist
  (e.g., `0x8010B254`, `0x8010F7C8`); these addresses live in RAM, initialized
  by ROM before the patch boots.
- **`rom` block** (`0x80000000–0x8007FFFF`): the ROM dump Kovah extracted via
  HCI VSC `0xFC61` — contains all ROM functions including
  `interesting_string_user_fptr_registration_function` @ `0x80009990` and the
  register r/w fns at `0x8001136c` / `0x8001139c`.

**wairz limitation**: `run_ghidra_headless` always resolves the GZF to its
associated raw binary (vanilla 42088 bytes) and re-imports it fresh at
`0x80000000`. The GZF's `data` and `rom` blocks are encoded inside the GZF
(which is a ZIP/Ghidra project archive) but are never loaded. The ROM and DATA
content is present in the GZF file; it just cannot be accessed through wairz's
current import path. Interactive Ghidra GUI opening the GZF directly is the
only known way to access those blocks.

---

## Blockers Remaining

| Blocker | Root cause |
|---------|-----------|
| 9 of 15 fn_ptrs beyond binary | Require ROM dump or GZF DATA block |
| MIPS16e handler stubs undecoded | Ghidra 12.1.2 EXTEND decoder fails in this region |
| ROM table processor not accessible | wairz always loads raw binary, not GZF ROM block |
| Full LMP PDU type mapping unknown | Requires tracing ROM dispatch chain |
