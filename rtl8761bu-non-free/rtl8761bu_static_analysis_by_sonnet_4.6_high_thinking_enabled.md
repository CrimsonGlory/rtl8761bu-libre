# RTL8761BU Firmware Internals

**Device:** Realtek RTL8761BU — USB Bluetooth 5.0/5.1 controller  
**Reference dongle:** TP-Link UB500 (VID `0x2357`, PID `0x0604`)  
**Firmware file:** `/lib/firmware/rtl_bt/rtl8761bu_fw.bin`  
**Config file:** `/lib/firmware/rtl_bt/rtl8761bu_config.bin`  
**Primary research:** Xeno Kovah — *Reverse engineering Realtek RTL8761B Bluetooth chips to make better Bluetooth security tools & classes*, HardwearioNL 2025  

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)  
2. [CPU: MIPS16e](#2-cpu-mips16e)  
3. [Memory Map](#3-memory-map)  
4. [Linux Integration — the btrtl Driver](#4-linux-integration--the-btrtl-driver)  
5. [EPatch v2 File Format](#5-epatch-v2-file-format)  
6. [Config File Format](#6-config-file-format)  
7. [Vendor-Specific HCI Commands](#7-vendor-specific-hci-commands)  
8. [The Patching Mechanism — `if (fptr != NULL)`](#8-the-patching-mechanism--if-fptr--null)  
9. [Patch 1 Binary Structure](#9-patch-1-binary-structure)  
10. [The Hook Table — All 43 Entries](#10-the-hook-table--all-43-entries)  
11. [Kovah's Reverse Engineering Methodology](#11-kovaths-reverse-engineering-methodology)  
12. [Key Security-Relevant VSCs](#12-key-security-relevant-vscs)  
13. [Deblob Strategy for linux-libre](#13-deblob-strategy-for-linux-libre)  
14. [Open Questions](#14-open-questions)  
15. [References](#15-references)  

---

## 1. Architecture Overview

The RTL8761BU is a USB Bluetooth controller. Its internal CPU executes firmware that the Linux host uploads after USB enumeration. The host OS (Linux on x86 or ARM) does not execute any of this code — it only sends it as raw bytes via USB to the chip.

```
┌─────────────────────────────────────────────────────┐
│                   Linux host (x86/ARM)              │
│  btrtl driver → reads .bin → sends via VSC 0x20    │
└────────────────────────┬────────────────────────────┘
                         │ USB (HCI)
┌────────────────────────▼────────────────────────────┐
│               RTL8761BU chip                        │
│                                                     │
│  ┌──────────┐   ┌──────────┐   ┌────────────────┐  │
│  │ MIPS16e  │   │  PRAM    │   │  Silicon ROM   │  │
│  │  CPU     │◄──│0x8010A000│   │ 0x80000000+    │  │
│  │          │   │ (overlay)│   │ (immutable BT  │  │
│  └──────────┘   └──────────┘   │  stack)        │  │
│                                └────────────────┘  │
│  ┌──────────────────────────────────────────────┐  │
│  │  DRAM  0x80120000  (function-pointer table)  │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

The ROM contains a complete, working Bluetooth stack. The firmware blob's job is to *patch* specific functions in that stack by installing function pointers into a DRAM table. The ROM checks each slot before calling — `if (fptr != NULL) { fptr() }` — which is how the overlay works without modifying read-only silicon.

---

## 2. CPU: MIPS16e

The RTL8761B chip family uses a **MIPS16e** CPU. This was first identified by blog author "8051enthusiast" while reverse-engineering a co-located WiFi chip's firmware: a repeating padding pattern of bytes `00 65` (halfword `0x6500`) was identified as the MIPS16e NOP instruction. Kovah confirmed this finding.

**MIPS16e characteristics:**

| Property | Value |
|---|---|
| Base ISA | MIPS32 Release 2 |
| Extension | MIPS16e (16-bit compact encoding, similar to ARM Thumb) |
| Endianness | Little-endian |
| Instruction width | 16 bits (some instructions use 32-bit EXTEND prefix) |
| EXTEND prefix encoding | bits\[15:11\] = `0x1E` (identical bit pattern to ARM Thumb-2 T32 prefix — a common source of disassembly confusion) |
| Function-pointer interwork bit | LSB = 1 means MIPS16e target; LSB = 0 means MIPS32 target (identical convention to ARM Thumb bit) |
| NOP instruction | `0x6500` (bytes `00 65` in LE memory) |
| Return instruction | `JR ra` — encoded as `0xE820` in MIPS16e RR format |

**Confirmation in the RTL8761BU firmware binary:**

| Pattern | Count in `patch[1]` |
|---|---|
| `0x6500` MIPS16e NOP (`00 65`) | **185** |
| `0x4770` ARM BX LR (`70 47`) | **0** |
| `0xBF00` ARM NOP | **0** |
| EXTEND prefix (`0x1E` in bits\[15:11\]) | **887** |

All 28 PRAM function pointers in the hook table have LSB = 1 (MIPS16e interwork bit set), confirming the ISA uniformly.

**Toolchain for replacement firmware:**

```
mipsel-linux-gnu-gcc -mips32r2 -mips16 -EL -msoft-float -G 0 \
    -nostdlib -ffreestanding -fno-pic -Os
```

Available in Debian/Ubuntu as `gcc-mipsel-linux-gnu`.

---

## 3. Memory Map

Discovered via VSC `0x61` memory scan (Kovah). Invalid addresses return `0xDEADBEEF`.

```
Address range                Size       Region
─────────────────────────────────────────────────────────────────────
0x7FB0A000 – 0x7FB0BFFF     ?          Secondary ROM bank (alias?)
                                        Four init calls land here:
                                        0x7FB0A4C2, 0x7FB0A4DC,
                                        0x7FB0A572, 0x7FB0A58E
                                        (UNVERIFIED, below 0x80000000)

0x80000000 – 0x80DFFFFF     ~224 MB    Silicon ROM (immutable BT stack)
                                        PRAM overlays part of this

0x8010A000 – 0x80110C9F     27,808 B   PRAM — Patch RAM (standard window)
                                        Loaded by VSC 0x20 from firmware
                                        Overlays ROM at same addresses

0x80110CA0 – 0x80113FFF     13,151 B   Extended PRAM (available, unused
                                        by standard firmware)

0x80120000 – 0x80133FFF     ~80 KB     DRAM — Data RAM
                                        Contains the 43 function-pointer
                                        hook slots (all NULL before patch)

0x90000000 – 0x90000FFF     4 KB       MMIO region A (purpose unknown)
0x90003000 – 0x90004FFF     8 KB       MMIO region B
0x90006000 – 0x90006FFF     4 KB       MMIO region C
0xA0000000 – 0xA0133FFF     alias      Mirror of 0x80000000 range
0xB0000000 – 0xB0006FFF     alias      Mirror (partial)
```

**PRAM overlay mechanism:** The chip's memory controller maps PRAM at `0x8010A000`, shadowing the ROM code that would otherwise be there. Reads from that range return PRAM content; writes go to PRAM. The ROM code at other addresses (e.g., `0x805CA0D6`) remains directly accessible and is called by the patch code.

---

## 4. Linux Integration — the btrtl Driver

**Driver location:** `drivers/bluetooth/btrtl.c`  
**Firmware loading trigger:** USB enumeration of a Realtek-identified BT adapter

### Identification sequence

1. Driver sends `HCI_Read_Local_Version_Information` (standard HCI)
2. Chip returns `hci_ver`, `hci_rev`, `lmp_subver` (= `0x8761` for RTL8761B family)
3. Driver sends VSC `HCI_VENDOR_READ_ROM_VERSION` to get `rom_version`
4. Selects patch from EPatch file where `chip_id == rom_version + 1`

For UB500: `rom_version = 1`, so `chip_id = 2` is selected (Patch 1).

### dmesg trace (normal load)

```
Bluetooth: hci0: RTL: examining hci_ver=0a hci_rev=000b lmp_ver=0a lmp_subver=8761
Bluetooth: hci0: RTL: rom_version status=0 version=1
Bluetooth: hci0: RTL: loading rtl_bt/rtl8761bu_fw.bin
Bluetooth: hci0: RTL: loading rtl_bt/rtl8761bu_config.bin
Bluetooth: hci0: RTL: cfg_sz 6, total sz 27814
Bluetooth: hci0: RTL: fw version 0x09a98a6b
MGMT ver 1.22
```

`total sz 27814` = 27808 bytes patch + 6 bytes config, consistent with the binary.

### Download sequence

The driver sends the patch in 252-byte chunks via VSC `OCF 0x20` (see §7). The config file bytes are appended to the payload of the final chunk. The sequence number byte has bit 7 set on the last chunk. After all chunks are ACKed, the chip applies the patch and sends a final HCI event.

---

## 5. EPatch v2 File Format

**File:** `rtl8761bu_fw.bin`  
**Total size:** 42,088 bytes  
**Magic:** `Realtech` (8 ASCII bytes, no null terminator)

### File layout

```
Offset    Size   Content
──────────────────────────────────────────────────────────────────
0x0000    8 B    Magic: "Realtech"
0x0008    4 B    fw_version: 0x09A98A6B (LE uint32)
0x000C    2 B    num_patches: 2 (LE uint16)
0x000E    4 B    chip_id[0,1]: [1, 2] (2 × LE uint16)
0x0012    4 B    patch_length[0,1]: [14048, 27808] (2 × LE uint16)
0x0016    8 B    patch_offset[0,1]: [0x0030, 0x3780] (2 × LE uint32)
0x001E   18 B    Zero padding
──────────────────────────────────────────────────────────────────
0x0030 14048 B   Patch 0 — chip_id=1 (ROM version 0, older silicon)
0x3780 27808 B   Patch 1 — chip_id=2 (ROM version 1, UB500) ← selected
──────────────────────────────────────────────────────────────────
0xA3A0   64 B    Extension section: zero padding
0xA3E0    4 B    Chip-family constant: 0x00010EFF (LE uint32)
0xA3E4    4 B    Extension magic: 0x77FD0451 (LE uint32) ← driver checks this
──────────────────────────────────────────────────────────────────
```

### Patch selection

```
chip_id = rom_version + 1
```

The driver rejects the file if the extension magic (`0x77FD0451`) is absent or wrong. A firmware download fails silently if `fw_version` does not match what the chip reports.

### Patch 1 internal layout (27,808 bytes, base `0x8010A000`)

```
PRAM offset   Size      Content
─────────────────────────────────────────────────────────────────
0x0000        628 B     Init function — MIPS16e entry point
                        Called by ROM bootloader via JALX
                        Runs hardware init, applies hooks, returns
0x0274        344 B     Hook table — 43 × 8-byte entries
                        Each entry: {uint32 target, uint32 value}
0x03CC        296 B     Second address table — dense 32-bit addresses
                        ROM reads during boot (high entropy: 6.11 bits/B)
0x04F4      26,536 B    Patch functions — 47+ MIPS16e functions
0x6C9C          4 B     Version footer: 0x09A95FD1
─────────────────────────────────────────────────────────────────
```

---

## 6. Config File Format

**File:** `rtl8761bu_config.bin`  
**Size:** 6 bytes  
**Content:** `55 AB 23 87 00 00`

### Structure

```
Bytes 0–1:  0x55 0xAB  — fixed marker (NOT a LE uint16; stored as literal bytes)
Bytes 2–3:  LE uint16  — config entry offset within the chip's config table
Bytes 4–5:  LE uint16  — config entry value
```

The default config (`55 AB 23 87 00 00`) sets offset `0x8723` (system config bitmask) to `0x0000` — all bits at ROM default.

### BDADDR override

To set a custom Bluetooth Device Address, use offset `0x0030` with a 6-byte value (the address bytes, reversed):

```
55 AB 23 87 09 00 30 00 06 <b5> <b4> <b3> <b2> <b1> <b0>
│     │     │     │   │   │    └── BDADDR bytes LSB-first
│     │     │     │   │   └─────── length (6 bytes)
│     │     │     │   └─────────── total TLV length (9 bytes)
│     │     │     └─────────────── offset 0x0030 (LE uint16)
│     │     └───────────────────── secondary marker
│     └─────────────────────────── primary marker byte
└───────────────────────────────── marker byte
```

Kovah demonstrated this is the official mechanism for public address spoofing — there is no standard HCI command for changing a public BDADDR. The config file provides the only sanctioned path.

---

## 7. Vendor-Specific HCI Commands

All Realtek vendor-specific commands (VSCs) use **OGF = `0x3F`** (the reserved VSC group field, mandated by the Bluetooth specification). This makes the 16-bit opcode word always begin with `FC`, `FD`, `FE`, or `FF` (since `0x3F << 10 = 0xFC00`). Kovah used this fact as a disassembly landmark: searching for constants `0x3F`, `0xFC**`, and shifts-by-10 in the MIPS16e disassembly identified the VSC dispatcher.

### Known VSCs for RTL8761BU

| OCF | Opcode | Name | Description |
|-----|--------|------|-------------|
| `0x20` | `0xFC20` | `HCI_VENDOR_DOWNLOAD` | Firmware chunk download. Payload: 1-byte sequence number (bit 7 = last chunk), then up to 252 bytes of patch data. Config appended to last chunk's payload. |
| `0x61` | `0xFC61` | Read Memory | Read N bits from an absolute address. Payload: 4-byte address, 1-byte bit-count (`0x20` = 32 bits = 4 bytes). Returns `0xDEADBEEF` for invalid addresses. Kovah discovered this via UART logic-analyzer capture of `RFTestTool.exe`. |
| `0x62` | `0xFC62` | Write Memory | Write a value to an absolute address. Kovah used this for PoC firmware injection (writing custom MIPS16e code to a safe PRAM offset). |

Kovah used VSC `0x61` to scan all `0x1000`-aligned addresses and map the entire valid address space — the basis of the memory map in §3.

---

## 8. The Patching Mechanism — `if (fptr != NULL)`

This is Kovah's central finding. The ROM code is structured around a consistent pattern:

```c
/* ROM code (pseudo-C, inside the BT stack) */
void rom_some_bt_function(args...) {
    void (*fptr)(args...) = DRAM_SLOT_XYZ;   /* read from 0x8012xxxx */
    if (fptr != NULL) {
        fptr(args...);                         /* call the patch       */
        return;
    }
    /* ... ROM's own default implementation ... */
}
```

Before the firmware is loaded, every DRAM slot is `0x00000000` (NULL). The ROM always uses its own default implementations. After loading, the firmware's init function fills specific slots with MIPS16e function pointers. Subsequent ROM calls then divert to patch code.

Kovah identified this pattern by:
1. Noting a very large number of XREFs to `NULL` (`0x00000000`) in Ghidra
2. Seeing that those XREFs were all comparisons — `if (fptr != NULL)` guards
3. Observing that the firmware writes non-NULL values to those exact addresses

**Implications for a libre replacement:**

- If all DRAM slots remain NULL, the ROM falls back to its own defaults
- A minimal firmware only needs to run hardware initialisation and return
- Individual functions can be re-enabled by writing their DRAM slot — enabling incremental reimplementation

---

## 9. Patch 1 Binary Structure

### 9.1 Init function (`PRAM+0x000` – `PRAM+0x273`, 628 bytes)

The ROM calls this function via **JALX** (MIPS cross-ISA jump) immediately after the firmware download completes. This switches from whatever mode the ROM bootloader is running in to MIPS16e for the patch code.

The init function:

1. Allocates a stack frame (`ADJSP -5` = `SP -= 40`)
2. Saves the return address to the frame (`SWRASP 9` = `SW $ra, 36(SP)`)
3. Calls 25 ROM functions for hardware initialisation (detailed below)
4. Applies the 43-entry hook table — writes values to target addresses
5. Restores `$ra` and returns to the ROM bootloader

**25 ROM hardware-init calls (addresses UNVERIFIED — require MIPS16e disassembly):**

| Group | Addresses | Count | Probable purpose |
|-------|-----------|-------|-----------------|
| 1 | `0x80109F74` – `0x80109FB8` | 8 | Clock/PLL setup, power domains, reset control |
| 2 | `0x805CA0D6`, `0x805CA118` | 2 | BT core initialisation |
| 3 | `0x805EA15A`, `0x805EA19C` | 2 | BT subsystem initialisation |
| 4 | `0x809CA36A` | 1 | Unknown |
| 5 | `0x80A4A6D6` – `0x80A4A700` | 4 | RF/baseband configuration |
| 6 | `0x80DCA100` – `0x80DCA152` | 4 | Peripheral init A (USB/UART block) |
| 7 | `0x80DEA182` – `0x80DEA1D4` | 4 | Peripheral init B (DMA/interrupt controller) |
| ? | `0x7FB0A4C2`, `0x7FB0A4DC`, `0x7FB0A572`, `0x7FB0A58E` | 4 | Secondary ROM bank — purpose unknown |

These addresses were extracted from MIPS16e EXTEND sequences by an ARM Thumb-2 disassembler that happened to reconstruct the embedded constants correctly (the ARM BL decoder and the MIPS16e EXTEND decoder both extract 32-bit values from the same 4-byte sequences, producing the same result). Verification via MIPS16e disassembly in Ghidra is required before fully trusting them.

### 9.2 Hook table (`PRAM+0x274` – `PRAM+0x3CB`, 344 bytes)

43 entries of 8 bytes each: `{ uint32_t target_addr, uint32_t value }`.

The init function iterates this table and writes `value` to `target_addr`. The MIPS16e interwork bit (LSB = 1) is set in all function pointer values, telling the hardware to switch to MIPS16e mode when calling through them.

### 9.3 Second address table (`PRAM+0x3CC` – `PRAM+0x4F3`, 296 bytes)

A dense table of 32-bit addresses (entropy 6.11 bits/byte — consistent with packed address data). The first entry is `0x8010E159` (a PRAM Thumb/MIPS16e pointer). The ROM reads this table during startup; its exact purpose is unknown, but its presence is required for the firmware to load correctly.

### 9.4 Patch functions (`PRAM+0x4F4` – `PRAM+0x6C9B`, 26,536 bytes)

47+ MIPS16e functions implementing the patched BT stack behaviour. These are the functions whose addresses are installed into DRAM hook slots. For a deblob project these are the functions that need reimplementation if the ROM's default implementations are insufficient.

### 9.5 Version footer (`PRAM+0x6C9C`, 4 bytes)

`0x09A95FD1` — distinct from the EPatch header's `fw_version` (`0x09A98A6B`). The ROM bootloader appears to read this value; the hook table entry `[0]` rewrites it at runtime to point to a DRAM address (`0x80120062`).

---

## 10. The Hook Table — All 43 Entries

### Entry types

| Type | Count | Meaning |
|------|-------|---------|
| `DRAM←PRAM_fn` | **21** | Installs a MIPS16e patch function pointer into a DRAM slot. ROM checks this before calling (`if (fptr != NULL)`). These are the 21 actual patches. |
| `PRAM←DRAM_ptr` | **12** | Stores a DRAM data-structure address into a PRAM location. Gives patch functions access to ROM data structures in DRAM. |
| `PRAM←PRAM_fn` | **7** | Wires patch functions together internally. Only relevant if those patch functions are being reimplemented. |
| `DRAM←DRAM_ptr` | **3** | Stores a DRAM address into another DRAM location. Data-pointer setup. |

### Complete hook table

All function pointer values shown with their actual code address (LSB cleared):

```
[ 0]  0x80110C9C ← 0x80120062          PRAM←DRAM_ptr  footer slot → DRAM ptr
[ 1]  0x80120070 ← 0x80120060          DRAM←DRAM_ptr  data ptr
[ 2]  0x8010A754 ← PRAM+0x0FBC (fn)   PRAM←PRAM_fn   internal wiring
[ 3]  0x801206AC ← PRAM+0x1350 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[ 4]  0x80121318 ← 0x80120830          DRAM←DRAM_ptr  data ptr
[ 5]  0x8010B3AC ← 0x8012095C          PRAM←DRAM_ptr  data ptr → PRAM
[ 6]  0x8010C140 ← PRAM+0x2290 (fn)   PRAM←PRAM_fn   internal wiring
[ 7]  0x801286C0 ← PRAM+0x1254 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[ 8]  0x8010F7C8 ← PRAM+0x2748 (fn)   PRAM←PRAM_fn   internal wiring
[ 9]  0x8010C784 ← PRAM+0x16D0 (fn)   PRAM←PRAM_fn   internal wiring
[10]  0x8012088C ← PRAM+0x12DC (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[11]  0x80121368 ← PRAM+0x26F8 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[12]  0x8012136C ← PRAM+0x0668 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[13]  0x80121360 ← PRAM+0x2CF4 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[14]  0x80121344 ← PRAM+0x2BF0 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[15]  0x80125550 ← PRAM+0x1D20 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[16]  0x8010E1C0 ← 0x80120C9C          PRAM←DRAM_ptr  data ptr → PRAM
[17]  0x8010DE10 ← 0x80120DE8          PRAM←DRAM_ptr  data ptr → PRAM
[18]  0x8010DAF0 ← 0x80120F10          PRAM←DRAM_ptr  data ptr → PRAM
[19]  0x8010D888 ← 0x80120DBC          PRAM←DRAM_ptr  data ptr → PRAM
[20]  0x8010F304 ← PRAM+0x05A0 (fn)   PRAM←PRAM_fn   internal wiring
[21]  0x80120F3C ← PRAM+0x25F0 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[22]  0x80120CF8 ← PRAM+0x0504 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[23]  0x80121414 ← PRAM+0x04F4 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot (first installed)
[24]  0x801213DC ← PRAM+0x1610 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[25]  0x80121348 ← PRAM+0x29FC (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[26]  0x80120590 ← PRAM+0x299C (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[27]  0x8012067C ← PRAM+0x3534 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[28]  0x80120F4C ← PRAM+0x50E0 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[29]  0x801213E8 ← PRAM+0x4FFC (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[30]  0x801213C8 ← PRAM+0x51B4 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[31]  0x80121458 ← PRAM+0x1040 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[32]  0x80121410 ← PRAM+0x4F30 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[33]  0x8010EF08 ← 0x80120A0C          PRAM←DRAM_ptr  data ptr → PRAM
[34]  0x8010A55C ← 0x80120CD4          PRAM←DRAM_ptr  data ptr → PRAM
[35]  0x8010C65C ← 0x80120CF4          PRAM←DRAM_ptr  data ptr → PRAM
[36]  0x8010C674 ← 0x80120824          PRAM←DRAM_ptr  data ptr → PRAM
[37]  0x8010EA1C ← PRAM+0x48A8 (fn)   PRAM←PRAM_fn   internal wiring
[38]  0x8010C4F8 ← PRAM+0x2B9C (fn)   PRAM←PRAM_fn   internal wiring
[39]  0x80120960 ← PRAM+0x1A00 (fn)   DRAM←PRAM_fn   ★ DRAM hook slot
[40]  0x8010A5B8 ← 0x80121370          PRAM←DRAM_ptr  data ptr → PRAM
[41]  0x8010CD28 ← 0x80120BFC          PRAM←DRAM_ptr  data ptr → PRAM
[42]  0x801205B4 ← 0x80120264          DRAM←DRAM_ptr  data ptr
```

`★` marks the 21 DRAM hook slots that the ROM's `if (fptr != NULL)` guards check. These are the only entries that need reimplementation for a functional deblob.

---

## 11. Kovah's Reverse Engineering Methodology

Kovah's HardwearioNL 2025 talk describes the full process. Highlights relevant to understanding the binary:

### Motivation

1. **MITM attacks in class** — spoofing a Bluetooth public address requires a vendor-specific command; there is no standard HCI mechanism. CSR4.0 dongles (the only cheap option with known BDADDR-change VSCs) only support BT 4.0 (2010 spec). Kovah needed BT 5.x hardware.

2. **Blue2thprinting setup** — his active Bluetooth fingerprinting tool uses Braktooth (closed-source, x86-64 only) on expensive ESP32 devkits. He wanted to move to cheap USB dongles on Raspberry Pi Zero W.

3. **Leading by example** — documenting the RE process for his "BlueCrew" of volunteer BT security researchers.

### Key steps

**Identifying the chip:** All cheap Bluetooth dongles from Amazon were found to be Realtek-based via `hciconfig -a`. Dongles advertise BT 5.3/5.4 but Realtek confirmed in writing that RTL8761B only supports BT 5.1.

**Firmware location:** Standard Linux firmware path `/lib/firmware/rtl_bt/rtl8761bu_fw.bin`. Available uncompressed in Ubuntu 22.04 and earlier; zstandard-compressed in Ubuntu 24.04.

**Identifying MIPS16e:** Via 8051enthusiast's blog post on a co-located WiFi chip's firmware, which found `00 65` repeating as padding — identified as MIPS16e NOPs. This was the key breakthrough enabling disassembly.

**Dev board issues:** Kovah bought the wrong dev board (BTV = UART, not BUV = USB), then had to wait weeks for Realtek to send power-jumper documentation before the board would even power on.

**Memory mapping via UART:** Using `RFTestTool.exe` (a Realtek diagnostic tool), a USB-to-UART adapter, and a logic analyser, Kovah captured the UART traffic and reverse-engineered VSC `0x61` (read memory). He then wrote a Python script to scan all `0x1000`-aligned addresses and build the memory map.

**Ghidra disassembly:** Veronica Kovah wrote a Ghidra script to handle the MIPS16e disassembly pattern (EXTEND + instruction + data, which Ghidra's auto-analysis would stop at). MIPS16e is supported but poorly auto-analysed in Ghidra; pressing F12 instead of D is required to force MIPS16e decoding.

**Finding the VSC dispatcher:** The ROM contains very few strings. The useful strings found were: `"tHCI "`, `"tLMP "`, `"tLC "`, `"tEvent"`, `"MailBox"`. Cross-referencing the `"tHCI"` string led to an HCI command registration table, which contained code checking against `OGF = 0x3F` — the vendor-specific group. This pointed to the VSC dispatch function.

**BDADDR spoofing:** Leaked documentation for other Realtek chips showed that config files can set BDADDR. After confirming the mechanism for RTL8761BU by trial and error (offset `0x0030`), Kovah achieved Goal 1.

**Arbitrary LMP sending:** Kovah patched in custom MIPS16e code (initially written in hex — "the dumbest thing I've ever heard," per Veronica; later using a MIPS16e assembler). He found `send_LMP_pkt()` in the ROM and called it directly via a custom VSC handler. LMP packets can only be sent within an active connection.

**HCI logging:** He used `HCI_Remote_Name_Request_Complete` events (251 bytes payload) as a makeshift logging channel from controller to host, then later switched to `HCI_Vendor_Specific_Event (0xFF)` which Bumble ignored while still passing it to his handler.

**Realtek's response:** After contacting Realtek support, they offered to add official VSCs for arbitrary LMP/LLCP packet sending to the RTL8761C (the newer chip). They provided a dev board and confirmed the custom firmware worked. These VSCs will be a significant aid to future BT security researchers.

### Tools used by Kovah

| Tool | Purpose |
|------|---------|
| Ghidra + MIPS16e | Primary disassembly and decompilation |
| Veronica's Ghidra script | Automated MIPS16e function recovery |
| Saleae Logic 2 | UART traffic capture between `RFTestTool.exe` and dev board |
| Python (pyserial) | Memory scanner using VSC `0x61` |
| scapy-usbbluetooth | USB HCI communication for PoC (by Antonio Vasquez-Blanco) |
| Bumble | BT connection management for LMP testing |
| MIPS16e assembler | Writing patch code (after abandoning hex entry) |

---

## 12. Key Security-Relevant VSCs

Kovah documented these in his `BT_Security_VSC_DB` repository.

### Public BDADDR spoofing

No standard HCI mechanism exists. Required method: write a config TLV with offset `0x0030` and the desired 6-byte address (LSB-first). This is applied during firmware load. The address persists until the next USB reset.

### Arbitrary LMP packet sending

LMP (Link Manager Protocol) packets are the lowest-level Bluetooth Classic packets, sent before any pairing or authentication. Sending arbitrary LMP requires:

1. An active BT Classic connection
2. Calling `send_LMP_pkt()` (a ROM function) via a custom patch or VSC

There is no standard HCI command for raw LMP injection. Kovah achieved this by patching in a custom VSC handler that calls `send_LMP_pkt()` directly. The RTL8761C may gain official VSC support for this.

### Memory read/write

VSC `0x61` and `0x62` allow reading and writing any mapped address. This enables:
- Full ROM extraction
- DRAM inspection at runtime
- Live firmware patching

No authentication is required — any process with HCI access can use these commands.

---

## 13. Deblob Strategy for linux-libre

The objective for linux-libre is to replace `rtl8761bu_fw.bin` (a proprietary binary blob) with source-built MIPS16e code. The chip's silicon ROM is *not* a blob in linux-libre's terms — it is hardware. Only the filesystem-loaded `.bin` file is the blob.

### Phase 1 — Minimal firmware (test first)

A source-built firmware that only runs the 25 hardware-init ROM calls and returns — leaving all 21 DRAM hook slots NULL — may be sufficient. If the ROM's default `if (fptr != NULL)` implementations are functional, the device works without any patch code.

**Expected result if Phase 1 succeeds:** `hciconfig` shows `hci0` with a valid BD address and BT comes up normally.

**Expected result if Phase 1 fails:** Either `command 0xfc20 tx timeout` (wrong ROM call addresses) or partial BT functionality (some ROM defaults are broken or absent).

### Phase 2 — Incremental hook reimplementation

If specific features fail, identify which of the 21 DRAM slots the affected code paths use and reimplement those functions in C (compiled with `-mips16`). Start with hook `[23]` (slot `0x80121414`, patch function at `PRAM+0x04F4`) — this is the first slot the BT stack calls.

Priority order for reimplementation should be determined empirically: boot the device, observe what fails, trace to the responsible DRAM slot.

### Build requirements

```
Compiler:   mipsel-linux-gnu-gcc (Debian: gcc-mipsel-linux-gnu)
Flags:      -mips32r2 -mips16 -EL -msoft-float -G 0
            -nostdlib -ffreestanding -fno-pic -Os
Linker:     place patch_entry() at 0x8010A000 (first symbol)
Packer:     EPatch v2 wrapper (Python, ~40 lines)
Config gen: 6-byte TLV generator (Python, ~20 lines)
```

No proprietary tools, no binary blobs at any step of the build.

---

## 14. Open Questions

1. **ROM call address verification.** The 25 addresses in the init function were extracted indirectly. MIPS16e disassembly in Ghidra (load the binary at `0x8010A000`, select MIPS16e LE, press F12 to start) will give authoritative addresses.

2. **Secondary ROM bank at `0x7FB0xxxx`.** Four init calls target addresses below `0x80000000`. These may be a second ROM bank, a mirror of a higher address, or entirely optional. Needs verification.

3. **Phase 1 outcome.** Unknown whether the ROM's default implementations are functional or broken. The answer determines the scope of the deblob project.

4. **The 7 PRAM←PRAM_fn hooks.** These wire patch functions together. If any Phase 2 reimplementations call through these, they need compatible implementations or the PRAM slots need to be populated.

5. **The second address table (`PRAM+0x3CC`–`PRAM+0x4F3`).** The ROM reads this 296-byte table during startup. Its exact semantics are unknown. It must be preserved verbatim in any firmware that includes the original PRAM structure.

6. **Maximum TX power.** Kovah noted this as future work — the mechanism for adjusting TX power is unknown.

7. **RTL8761C official VSCs.** Realtek offered to add official arbitrary LMP/LLCP VSCs to RTL8761C. Status as of the talk (November 2025) was not final.

---

## 15. References

**Primary research:**
- Xeno Kovah. *Reverse engineering Realtek RTL8761B Bluetooth chips to make better Bluetooth security tools & classes*. HardwearioNL, November 2025. [Slides PDF]
- Xeno Kovah. *Reverse engineering Realtek RTL8761B Bluetooth chips to make better Bluetooth security tools & classes*. YouTube (auto-generated subtitles). [Video transcript]

**Kovah's code repositories:**
- `https://github.com/darkmentorllc/DarkFirmware_real_i` — custom firmware for arbitrary LMP sending
- `https://github.com/darkmentorllc/BT_Security_VSC_DB` — security-relevant VSC database
- `https://github.com/darkmentorllc/Blue2thprinting` — Bluetooth fingerprinting tool

**Related research cited by Kovah:**
- 8051enthusiast blog (2021). RTL8821AE WiFi firmware RE — identified MIPS16e NOP pattern `00 65`.  
  `https://8051enthusiast.github.io/2021/07/05/002-wifi_fun.html`
- InternalBlue — Broadcom BT firmware experimentation framework (patch-register approach).
- BrakTooth — Bluetooth LMP fuzzer (ESP32-based, closed-source x86-64/aarch64).
- scapy-usbbluetooth — USB HCI Python library (Antonio Vasquez-Blanco).  
  `https://github.com/usbbluetooth/scapy-usbbluetooth`
- Bumble — Google's Python BT stack. `https://github.com/google/bumble`

**Linux kernel driver:**
- `drivers/bluetooth/btrtl.c` — Realtek BT firmware loader in the mainline kernel

**Specification:**
- Bluetooth Core Specification 5.4, section p.1800 — HCI command opcode format (OGF/OCF)
- MIPS16e Application Specific Extension — MIPS Technologies

---

*This document was produced by analysis of `rtl8761bu_fw.bin` (sha1: see your copy) cross-referenced with Kovah's HardwearioNL 2025 research. All addresses marked UNVERIFIED require confirmation via MIPS16e disassembly.*
