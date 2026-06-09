# Libre Patch Binary Layout — Design Specification

**Date**: 2026-06-09  
**Status**: Phase 5 design (implements `[NEXT]` from `work-in-progress.txt`)  
**Sources**: non-free `rtl8761bu_fw.bin` hex dump, GZF RE (`FUN_8010a000` hook map),
`rtl8761bu-libre/` build system (`pack.py`, `pad.py`, `rtl8761bu.ld`),  
`reverse_engineering_minimum_feature_set.md`, `reverse_engineering_patch_installer.md`.

This document defines the on-disk EPatch envelope and the in-RAM code layout the libre
firmware must produce. It is the authoritative reference for Phase 6 implementation.

---

## 1. Design goals

| Goal | Constraint |
|------|------------|
| Load via stock `btrtl` + HCI VSC `0xFC20` | Match envelope fields the driver validates |
| ROM entry at `0x8010A001` | `patch_entry` must land at runtime `0x8010A000` (MIPS16e odd = `0x8010A001`) |
| No binary blobs | All PRAM contents built from source |
| Tiered growth | Phase 1 = 27,808 B PRAM; T4 may need second code section |
| linux-libre policy | Source + constants only; ROM calls at fixed `0x8000xxxx` |

---

## 2. File envelope — EPatch v2

### 2.1 Verified non-free layout (`rtl8761bu_fw.bin`, 42,088 bytes)

```
File offset   Size        Field (all LE)
────────────────────────────────────────────────────────────────────
0x0000        9 B         magic = "Realtechk"          ← 9 bytes, not 8
0x0009        4 B         fw_version = 0x0209A98A
0x000D        2 B         num_patches = 2
0x000F        2×2 B       chip_id[n]     = 0x0001, 0x0002
0x0013        2×2 B       patch_length[n]= 0x36E0, 0x6CA0  (14,048 / 27,808)
0x0017        2×4 B       patch_offset[n]= 0x0030, 0x3780
0x001F        17 B        zero padding to 0x0030
────────────────────────────────────────────────────────────────────
0x0030        14,048 B    Patch 0  (chip_id=1, older silicon)
0x3780        27,808 B    Patch 1  (chip_id=2, UB500) ← production target
0xA420        64 B        extension: zeros
0xA460        4 B         chip-family constant = 0x00010EFF
0xA464        4 B         extension magic = 0x77FD0451  (btrtl checks this)
```

**Patch selection**: `chip_id = rom_version + 1`. UB500 reports `rom_version=1` → selects
`chip_id=2` (patch 1).

**fw_version**: Header stores `0x0209A98A`; `dmesg` prints `0x09a98a6b` (lower 24 bits +
encoding). Libre builds must use the same header word as the reference blob the chip expects
on target hardware (verify with `make verify` against a known-good dongle).

### 2.2 Recommended libre envelope (single patch)

Ship one patch per file — equivalent to reference patch 1, relocated to file offset `0x30`:

```
File offset   Size        Field
────────────────────────────────────────────────────────────────────
0x0000        9 B         magic = "Realtechk"
0x0009        4 B         fw_version = 0x0209A98A   (match non-free)
0x000D        2 B         num_patches = 1
0x000F        2 B         chip_id[0] = 2
0x0011        2 B         patch_length[0] = 0x6CA0  (27,808)
0x0013        4 B         patch_offset[0] = 0x0030
0x0017        25 B        zero padding to 0x0030
────────────────────────────────────────────────────────────────────
0x0030        27,808 B    Patch body (PRAM image, see §3)
0x6CD0        64 B        extension zeros
0x6D10        4 B         0x00010EFF
0x6D14        4 B         0x77FD0451
────────────────────────────────────────────────────────────────────
Total: 27,928 bytes (vs 42,088 non-free)
```

**Rationale**: UB500 never selects patch 0. A single `chip_id=2` patch avoids embedding
14 KiB of unused patch-0 code. The driver only requires valid magic, matching `chip_id`,
matching `fw_version`, and extension magic `0x77FD0451`.

### 2.3 Build pipeline mapping

```
src/*.[cS]  →  mipsel-linux-gnu-gcc  →  build/patch.elf
build/patch.elf  →  objcopy -O binary  →  build/patch.bin
build/patch.bin  →  pad.py (NOP fill + footer)  →  build/patch_padded.bin
build/patch_padded.bin  →  pack.py  →  rtl8761bu_fw.bin
gen_config.py  →  rtl8761bu_config.bin  (separate 6-byte file, appended by
                                         driver to last FC20 chunk — not in .fw.bin)
```

| Stage | Input size | Output size | Notes |
|-------|-----------|-------------|-------|
| `patch.bin` | variable | ≤ 27,804 B | Raw linked code + rodata |
| `patch_padded.bin` | — | **27,808 B** | `PATCH_SIZE` in Makefile |
| `rtl8761bu_fw.bin` | — | **27,928 B** | +48 B header + 72 B extension |

### 2.4 Patch body footer (inside PRAM image)

Last 4 bytes of the 27,808-byte patch body (file offset `0x6CCC` in single-patch layout):

| Offset (in patch body) | Value | Purpose |
|------------------------|-------|---------|
| `PATCH_SIZE − 4` | `0x09A95FD1` LE | Version footer; ROM reads at startup (`pad.py`) |

Fill between end of linked code and footer with MIPS16e NOP `0x6500` (bytes `00 65` LE).

### 2.5 pack.py corrections needed (Phase 6)

Current `pack.py` documents 8-byte `"Realtech"` magic and `fw_version` at offset 8.
Verified non-free uses **9-byte** `"Realtechk"` with `fw_version` at offset 9.
Update `pack.py` before release:

```python
MAGIC = b'Realtechk'          # 9 bytes
# header layout: magic(9) + fw_version(4) + num_patches(2) + chip_id(2) + length(2) + offset(4) + pad
HEADER = 0x30                 # unchanged
```

---

## 3. Runtime memory map

### 3.1 Address translation

When the ROM/driver loads a patch:

```
runtime_addr = patch_load_base + (file_offset − patch_file_offset)
```

For reference patch 1 (`file_offset=0x3780`, `length=0x6CA0`):

```
patch_load_base = 0x80103780
runtime = 0x80103780 + (file_off − 0x3780)
```

| File offset | Runtime address | Content |
|-------------|-----------------|---------|
| `0x3780` | `0x80103780` | `FUN_80103780` master installer (not called at boot) |
| `0xA000` | `0x8010A000` | **`FUN_8010a000` entry** (ROM calls `0x8010A001`) |
| `0xA41C` | `0x8010A41C` | Version footer `0x09A95FD1` |

For libre single-patch (`file_offset=0x30`, same length, same load base):

```
patch_load_base = 0x80103780   ← must match reference (chip_id=2 load address)
entry runtime   = 0x80103780 + (0xA000 − 0x3780) = 0x8010A000  ✓
```

**Critical**: Libre PRAM object file must place `patch_entry` at linked address
`0x8010A000`, **not** `0x80103780`. The 26,880-byte gap (`0x6880`) before entry is
NOP-filled or omitted from the file — the ROM never executes it because boot goes
directly to `0x8010A001`.

**Alternative (current `rtl8761bu.ld`)**: Link entire 27,808 B region at `0x8010A000`
with entry at offset 0. This is valid **if** the driver loads the blob at `0x8010A000`
(not `0x80103780`). Empirical test on UB500 required; if load fails, switch load base
to `0x80103780` and pad `0x6880` bytes of NOP before entry in the file.

### 3.2 Full chip RAM regions (context)

```
Region              Runtime range           Owner at boot
──────────────────────────────────────────────────────────────────
Patch code §1       0x80100000–0x80109BFF   Reference only (GZF patch block)
Patch BSS           0x80109C00–0x80109FFF   Zeroed by FUN_8010a6c8
Patch code §2       0x8010A000–0x8010ADC3   Entry + hooks (vanilla bin tail)
                    0x8010A000–0x8010A41F   Entire PRAM window (27,808 B load)
DRAM hook slots     0x80120000–0x80133FFF   ROM allocates; patch writes fn-ptrs
config_base         ≈0x80120070            ROM-init ("RRTK_BT_5.0" at +0x49)
bos_base            ≈0x801206AC            BT state; hook offsets +0x1c/+0xd8/…
ROM                 0x80000000–0x8007FFFF   Silicon; call only, never embed
```

Libre firmware **writes** hook pointers into DRAM slots; it does **not** embed the DRAM
image. ROM zero-fills or pre-populates those regions before `patch_entry` runs.

### 3.3 GZF vs vanilla size

| Image | Size | Notes |
|-------|------|-------|
| `rtl8761bu_fw.bin` (vanilla) | 42,088 B | Two patches; patch §1 ends at `0xA41C` |
| GZF patch block | 44,484 B | +2,396 B "dark firmware" at `0xA468–0xADC3` |
| Libre T1 target | 27,808 B PRAM | Entry-centric; no patch §1 below `0x8010A000` |
| Libre T4 target | ≤44,484 B | May require full GZF-equivalent layout |

---

## 4. PRAM code structure (reference patch 1)

### 4.1 Reference internal layout (informative)

From non-free static analysis of patch 1 loaded at `0x8010A000` (entry-relative view):

```
PRAM+0x0000   628 B     Init / entry cluster
PRAM+0x0274   344 B     Hook-install table (43 × 8 B {target, value})
PRAM+0x03CC   296 B     Address table (dense 32-bit pointers)
PRAM+0x04F4  ~26.5 KiB  Patch functions (47+ MIPS16e handlers)
PRAM+0x6C9C     4 B     Version footer 0x09A95FD1
```

GZF DATA-block analysis refines this: the **authoritative** entry is `FUN_8010a000`
(578 B installer body) at `0x8010A000`, not a thin init stub. The 43-entry table is
the `FUN_8010a000` literal-pool-driven hook map (Appendix D in patch installer doc).

### 4.2 Libre section model (linker)

Recommended ELF section layout for `rtl8761bu.ld`:

```ld
MEMORY {
    pram (rwx) : ORIGIN = 0x8010A000, LENGTH = 27808
}

SECTIONS {
    .text.entry 0x8010A000 : {
        KEEP(*(.text.entry))     /* patch_entry — MUST be first */
    } > pram

    .text.hooks : {
        *(.text.hooks)
        *(.text .text.*)
    } > pram

    .rodata : {
        *(.rodata .rodata.*)      /* literal pools, RF tables, chip-rev table */
    } > pram

    /* .data / .bss in PRAM only if explicitly initialized in patch_entry */
}
```

| Section | Contents | Tier |
|---------|----------|------|
| `.text.entry` | `patch_entry`: boot sequence, calls sub-installers, sets `*0x80120538=4` | T1 |
| `.text.hooks` | All `FUN_8010xxxx` hook implementations | T1–T4 |
| `.rodata` | Literal pools, 7 RF tables (`0x8011106c`), chip-rev table (`PTR_DAT_8010a3a0`) | T1 |
| `.text.late` (optional) | Late-patch fns (`FUN_80109980`…`80109824`) if not in §1 | T4 |

### 4.3 Entry boot order (code structure)

`patch_entry` at `0x8010A000` must execute in this order (from `FUN_8010a000` decompile):

```
1.  Config word split → config_base+0x60/+0x62
2.  FUN_8010a6c8      → ROM memset(0x80109C00, 0, 0x400)
3.  Sub-installers #1–#6 (FUN_8010af40, 8011011c, 8010fc58, 8010f370, 8010e81c, 8010eac0)
4.  44× hook pointer installs → DRAM slots (see hook map table below)
5.  FUN_8010e214       → silicon revision detect
6.  FUN_8010a7b8       → TLV applier (no-op when remaining=0)
7.  ROM copies_config_bdaddr (0x8000FD38)
8.  FUN_8010ad38       → HW variant probe
9.  FUN_8010b04c       → BB regs 0x114/0x154 (chip-rev gated)
10. FUN_8010c278       → RF channel init (7 tables from .rodata)
11. ROM FUN_8003AEA0   → register script via fn-ptr @ 0x801205B4
12. *0x80120538 = 4   → patch-active sentinel
13. return to ROM
```

### 4.4 Hook pointer install map (DRAM targets)

All installs are `*(uint32_t *)slot = (uint32_t)handler | 1` (MIPS16e interwork bit).
Handlers must reside in PRAM at their historical runtime addresses OR the install stores
the new libre address (libre may pack functions contiguously and install adjusted pointers).

**T1-mandatory slots** (from minimum feature set):

| DRAM slot | Handler | Size | Section |
|-----------|---------|------|---------|
| `0x80120C9C` | `FUN_8010e27c` | 52 B | `.text.hooks` — protocol dispatch installer |
| `0x80121318` | `FUN_8010b118` | 82 B | slot interval allocator |
| `0x8012088C` | `FUN_8010b3d8` | 206 B | ACL slot scheduler |
| `0x80121368` | `FUN_8010b0a4` | 108 B | ACL packet-type flags |
| `0x80121344` | `FUN_8010c780` | 34 B | subsystem init |
| `0x80125550` | `FUN_8010c63c` | 278 B | ACL retransmission |
| `0x801213DC` | `FUN_8010a49c` | 10 B | trivial flag clear |
| `0x80120F3C` | `FUN_8010a594` | 14 B | trivial MMIO write |
| `bos+0xD8` | `LAB_8010bba4` | 176 B | LMP VSC gateway (T2 for eSCO) |
| `bos+0x20…+0x50` | various | TBD | T1-conservative — decompile pending |

Protocol-dispatch struct @ `0x8012AE8C` is populated by `FUN_8010e27c`, not direct stores.

### 4.5 Literal pool placement

MIPS16e PC-relative `lw` loads require literal pools within ±32 KiB of consumers.
**Rule**: place each function's pool immediately after its code body (reference layout),
or group pools in `.rodata` with explicit `la`/address materialization.

Functions with large pools (entry, `FUN_8010b7f0`, `FUN_8010ca20`) need verified pool
offsets against GZF — use `DumpEntryLiteralPool.java` pattern for each.

### 4.6 ROM-only data (do not embed in PRAM)

| Item | Runtime address | Action |
|------|-----------------|--------|
| `config_base` | `0x80120070` | ROM-initialized; read only |
| TLV blob | `0x801115F8–0x80113FFF` | Leave `remaining=0`; skip loop |
| RAM fn-ptr slots pre-filled by ROM | `0x8012082C`, `0x80120958`, … | Overwrite only when installing hooks |
| Register-script array | `0x80120264` | ROM-populated; call `FUN_8003AEA0` via pool |
| `bos_base` struct | ≈`0x801206AC` | ROM-allocated; install hook fptrs only |

---

## 5. Tiered layout strategy

| Tier | PRAM budget | File size | Layout |
|------|-------------|-----------|--------|
| **T0** (current skeleton) | 27,808 B @ `0x8010A000` | 27,928 B | Entry + ROM calls only; no hook installs |
| **T1** (minimal BT) | 27,808 B | 27,928 B | Entry + sub-installers + T1 hooks; may exceed budget → see below |
| **T2** (+ eSCO) | 27,808 B or expand | TBD | Add `FUN_8010bba4` + eSCO cluster (~2 KiB) |
| **T3** (+ AFH) | likely **exceeds 27,808 B** | TBD | AFH engine alone ≈1.1 KiB analyzed |
| **T4** (full parity) | up to **44,484 B** | up to ~44.6 KiB | Two-section or full GZF layout |

### 5.1 Size pressure

| Category | Est. MIPS16e insns | Bytes (×~3) |
|----------|-------------------|-------------|
| T1 mandatory code | ~1,700–2,200 | ~5–7 KiB |
| T2 add-on | +~1,990 | +~6 KiB |
| T3 add-on | +~1,100 | +~3 KiB |
| T4 remainder | +~3,000+ | +~9 KiB+ |

T1 likely fits in 27,808 B. T3/T4 probably require either:

1. **PRAM expansion** — confirm with hardware whether load region can exceed `0x6CA0`;
2. **Two-patch envelope** — patch 0 at `0x30` (14,048 B) for §1 code (`0x80100000+`),
   patch 1 at `0x3780` for §2 (`0x80103780+`); or
3. **Compression** — not supported by FC20 loader.

**Decision point**: Defer two-patch layout until `make size` exceeds 27,804 B on a T2 build.
Document and prototype single-patch through T2.

---

## 6. Address-pair table (file `0xA0`)

Vanilla patch block has a 15×8-byte table at file offset `0xA0` (runtime `0x801000A0`):

```
{data_ptr, fn_ptr} × 15 — fn_ptrs aim into FUN_8010a000 body (not standalone entries)
```

Not processed by `FUN_8010e27c`. Purpose **unresolved**. Libre T1: **omit or zero-fill**
unless future RE proves it required. If needed, place at PRAM `0x801000A0` via expanded
two-section layout.

---

## 7. Config blob (separate file)

`rtl8761bu_config.bin` — 6 bytes, **not** part of `rtl8761bu_fw.bin`:

```
55 AB 23 87 00 00
│  │  └──┬──┘ └──┬──┘
│  │     │       value = 0x0000 (ROM defaults)
│  │     offset 0x8723 (system config bitmask)
│  marker bytes (literal, not LE uint16)
```

Driver appends this to the final FC20 download chunk. BD_ADDR override uses extended TLV
format — see `reverse_engineering_config_blob.md` §5.

---

## 8. Validation checklist

Before declaring layout correct on hardware:

- [ ] `hexdump -C rtl8761bu_fw.bin | head -3` shows `Realtechk` + `fw_version`
- [ ] `make verify` → extension magic `0x77fd0451` OK
- [ ] `make size` → code + rodata ≤ 27,804 B
- [ ] `objdump -d build/patch.elf | head` → `patch_entry` at `0x8010a000`
- [ ] Last 4 bytes of patch body = `d1 5f a9 09`
- [ ] `dmesg` after reload: `fw version 0x09a98a6b`, no FC20 timeout
- [ ] Ghidra load at `0x8010A000`, MIPS16e: entry decodes cleanly

---

## 9. Related documents

| File | Relevance |
|------|-----------|
| `reverse_engineering_minimum_feature_set.md` | Tier definitions, mandatory functions |
| `reverse_engineering_patch_installer.md` | Appendix D hook map, entry callees |
| `reverse_engineering_sub_installers.md` | Firmware header parse, sub-installer bodies |
| `rtl8761bu-libre/pack.py`, `pad.py`, `rtl8761bu.ld` | Current build implementation |
| `rtl8761bu-non-free/rtl8761bu_static_analysis_by_sonnet_4.6_high_thinking_enabled.md` | §5 EPatch format (note 8 vs 9 byte magic correction) |

---

## 10. Summary

| Layer | Libre choice |
|-------|--------------|
| **Envelope** | Single patch, `chip_id=2`, 27,808 B @ file `0x30`, 72 B extension |
| **PRAM base** | `0x8010A000` (entry); confirm driver load address on hardware |
| **Entry** | `patch_entry` = monolithic `FUN_8010a000` equivalent |
| **Padding** | MIPS16e NOP `0x6500`; footer `0x09A95FD1` at PRAM+`0x6C9C` |
| **DRAM** | ROM-owned; patch writes fn-ptr installs only |
| **Growth** | Single-patch through T2; revisit two-section for T3/T4 |
