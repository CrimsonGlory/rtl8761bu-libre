# Stub Implementations for Non-Critical Functions — Design Specification

**Date**: 2026-06-09  
**Status**: Phase 5 design (implements `[NEXT]` from `work-in-progress.txt`)  
**Sources**: `reverse_engineering_mandatory_hooks.md`, `reverse_engineering_minimum_feature_set.md`,
`reverse_engineering_protocol_dispatch_layer.md`, `reverse_engineering_libre_patch_layout.md`,
GZF decompiles (`FUN_8010c088`, `FUN_8010d154`, `FUN_8010ca20`, `FUN_8010a49c`, `FUN_8010a594`).

This document specifies **concrete stub and shim patterns** for every patch function that
does not need a full reimplementation at a given libre profile (P0–P4). It is the
implementation guide for Phase 6 assembly/C authors.

Companion docs: `reverse_engineering_mandatory_hooks.md` (verdict per hook),
`reverse_engineering_minimum_feature_set.md` (tier definitions).

---

## 1. Stub taxonomy

| Pattern | Code size | When to use | ROM NULL-safe? |
|---------|-----------|-------------|----------------|
| **ROM-NULL** | 0 B | Global/per-conn hooks ROM manages | Yes — ROM skips |
| **OMIT-INSTALL** | 0 B | Tier-gated hook; slot left at ROM default | Depends on slot |
| **SHIM-DIRECT** | 12–20 B | Unconditional tail-call to fixed ROM addr | N/A — must install |
| **SHIM-POOL** | 16–44 B | Tail-call via PC-relative literal pool | N/A — must install |
| **SHIM-ARGS** | 44–98 B | Forward `a0` (and saved regs), then ROM tail-call | N/A |
| **EMPTY-RET** | 4–8 B | Patch-only side path; no ROM call | Caller must tolerate |
| **ACCEPT-RET** | 8–12 B | Validator that always accepts (`return 0`) | Caller-specific |
| **TRIVIAL-FX** | 8–14 B | Single store or MMIO write | N/A |
| **IMPL-Tn** | variable | Full implementation required at tier **Tn** | — |

**MIPS16e conventions** (all stubs):

- Function entry addresses are **odd** (`addr | 1`) when installed as fn-ptrs.
- Tail-call targets use the same odd convention (`0x80042421` → ROM `FUN_80042420`).
- PC-relative literal pools: `lw rx, imm(pc)` immediately after the load insn.
- Standard epilogue when saving `ra`: `lw a3, offset(sp); jr a3` + delay-slot `addiu sp`.

---

## 2. Shared assembly macros

Recommended `src/stub_macros.S` (included by hook files):

```asm
    .set mips16

/* Load 32-bit constant into $a0 using 3-insn sequence (see init.S). */
.macro LOAD_ADDR reg, addr
    .set push
    .set noat
    li      \reg, (((\addr) + 0x8000) >> 16) - (((\addr) & 0x8000) >> 15)
    sll     \reg, 16
    addiu   \reg, ((\addr) & 0xffff) - (((\addr) & 0x80000000) >> 15)
    .set pop
.endm

/* SHIM-DIRECT: unconditional ROM tail-call, no stack frame.
 * reg must hold full runtime address (odd for MIPS16e). */
.macro SHIM_DIRECT rom_addr
    LOAD_ADDR $a0, \rom_addr
    jr      $a0
    nop
.endm

/* SHIM-POOL: 16-byte pattern matching FUN_8010d154 / FUN_8010c088.
 * Pool word follows the jalr (4-byte aligned). */
.macro SHIM_POOL rom_addr
    lw      $v0, 1f
    jalr    $v0
    nop
1:  .word   \rom_addr
.endm
```

### 2.1 Reference SHIM sizes (from GZF DATA block)

| Function | Pattern | Bytes | ROM target |
|----------|---------|-------|------------|
| `FUN_8010d154` | SHIM-POOL | 16 | `0x80066e69` → `FUN_80066e68` |
| `FUN_8010c088` | SHIM-POOL | 16 | `(*0x8010c098)` → ROM conn handler |
| `FUN_8010cc94` | SHIM-ARGS ×2 | 26 | ROM `0x800138cd` then patch `ca20` |
| `FUN_8010da70` (T1) | SHIM-ARGS | 20 | `0x80042421` → `FUN_80042420` |
| `FUN_8010d9f4` (T1) | SHIM-ARGS | 20 | `0x80042189` → `FUN_80042188` |

---

## 3. ROM-NULL — no code, no install

These never receive libre patch code. The entry installer **must not** write them.

| Slot | Consumer | Libre action |
|------|----------|--------------|
| `0x801212e4` | `FUN_8004f824` HW-write path | Leave NULL — clamp-only fallback |
| `0x801212e0` | `FUN_80050810` conn-type override | Leave NULL — ROM types 0–3 |
| `crypto_struct+0xe4` | Per-connection HW JIT | ROM `FUN_80025b68` at SSP |
| `PTR_DAT_800508ec` | `FUN_80050810` prep hook | Leave NULL |
| Address-pair table @ file `0xA0` | Unresolved | **OMIT** — not processed by installer |

**Profile**: P0–P4 identical (always NULL).

---

## 4. SHIM stubs — protocol dispatch layer

### 4.1 `FUN_8010d154` — LMP_CH passthrough (SHIM-T1, all profiles)

```c
void FUN_8010d154(void) {
    (*(void (*)(void))0x80066e69)();
}
```

Assembly: **SHIM-POOL** to `0x80066e69`. No arguments. **16 bytes**.

### 4.2 `FUN_8010da70` — LC TX (SHIM-T1 for P1; full for P2+)

**P1 stub** — skip eSCO opcode `0x32e` intercept:

```c
void FUN_8010da70(int *msg) {
    (*(void (*)(int *))0x80042421)(msg);
}
```

**P2+ implementation** — add `if (msg[2] == 0x32e) { ... codec ...; return; }` before ROM call.

| Profile | Pattern | Size est. |
|---------|---------|-----------|
| P1 | SHIM-ARGS | ~20 B |
| P2+ | IMPL | 44 B ref |

### 4.3 `FUN_8010d9f4` — LC RX (SHIM-T1 for P1; full for P2+)

**P1 stub** — skip eSCO state save/restore around opcode `0x2cd`:

```c
void FUN_8010d9f4(uint msg) {
    (*(void (*)(uint))0x80042189)(msg);
}
```

| Profile | Pattern | Size est. |
|---------|---------|-----------|
| P1 | SHIM-ARGS | ~20 B |
| P2+ | IMPL | 98 B ref |

### 4.4 `FUN_8010cc94` — index-0 sequencer (IMPL-T1, not a stub)

Must call **both** ROM and patch handler with `param_1` in `a0`:

```c
void FUN_8010cc94(void *msg) {
    unknown_fptr_index0(msg);           /* 0x800138cd */
    new_func_for_unknown_fptr_index0(msg); /* patch ca20 */
}
```

**26 bytes** — cannot be shortened; both calls are mandatory for P1.

### 4.5 `FUN_8010ca20` — type-0x67 monitor (IMPL-T3, transcribed 2026-06-10)

**P1/P2 stub** (EMPTY-RET with optional fast-path check):

```c
void FUN_8010ca20(int *msg) {
    if (*(uint16_t *)(msg + 8) != 0x67)
        return;
    /* P1: no further action — coexistence/AFH tuning disabled */
}
```

Minimal assembly (~12 B):

```asm
    lhu     $v0, 8($a0)      /* msg[8] type */
    li      $v1, 0x67
    bne     $v0, $v1, 1f
    nop
1:  jr      $ra
    nop
```

**P3+**: Full 534 B implementation — 7 BSS counters @ `0x8012b80c`–`0x8012b944`,
ROM sub-calls (`0x80074fa8`, `0x80009a30`, `0x8003ce50`, `0x80043038`), patch
`FUN_8010a5d8` for reg `0x2d`.

| Profile | Pattern | BSS | Degradation if stubbed |
|---------|---------|-----|------------------------|
| P1 | EMPTY-RET | none | Wi-Fi/BLE coexistence monitors inactive |
| P2 | EMPTY-RET | none | Same |
| P3 | IMPL | 7 counters | — |

---

## 5. SHIM stubs — `bos_base` and DRAM hooks

### 5.1 `LAB_8010c088` — connection handler @ `bos+0x30` (SHIM-T1)

Reference is a 16 B indirect tail-call through literal pool `DAT_8010c098`
(ROM connection handler installed at runtime by ROM, not a fixed address).

**Libre approach**:

1. **Preferred**: Replicate reference — `lw v0, pool; jalr v0` with pool word filled
   at install time from ROM-populated `0x8010c098` value (read-before-overwrite in
   `patch_entry`, or hardcode if ROM always writes the same ROM addr).
2. **Fallback SHIM-DIRECT**: If hardware test confirms a stable ROM target, tail-call
   it directly (same pattern as `d154`).

| Profile | Pattern |
|---------|---------|
| P1–P4 | SHIM-POOL (16 B) |

### 5.2 `bos+0xd8` — LMP VSC gateway `LAB_8010bba4` (OMIT-T1 / IMPL-T2)

| Profile | Action |
|---------|--------|
| P1 ACL-only test | **OMIT-INSTALL** — leave slot NULL; ROM `FUN_80076110` timeout path |
| P2+ | **IMPL** — 176 B gateway (required for LMP `0x268` / eSCO VSC) |

**P1 stub strategy**: Do not write `bos+0xd8` during hook-install phase. Document
hardware test matrix (ACL connect OK? HCI `FC67` fails as expected?).

### 5.3 Tier-gated DRAM hooks — OMIT-INSTALL matrix

For profiles below the function's tier, **skip the store** in `patch_entry` rather
than installing a dummy pointer. ROM leaves prior NULL or default.

| Function | Tier | P1 | P2 | P3 |
|----------|------|----|----|-----|
| `LAB_8010b7f0` | T2 | omit | IMPL | IMPL |
| `LAB_8010b4d0` | T2 | omit | IMPL | IMPL |
| `FUN_8010fa34/f950/fb08` | T3 | omit | omit | IMPL |
| `FUN_8010ce0c/e350` | T3 | omit | omit | IMPL |
| `FUN_80110ddc` | T4 | omit | omit | omit |

**Exception**: Sub-installer #2 writes 19 fn-ptrs unconditionally (IMPL-T1 for the
installer). Replicate verbatim — individual targets are ROM or patch; omitting the
installer breaks infrastructure.

---

## 6. TRIVIAL-FX — minimal real code (not stubs)

These are small but **not** replaceable by empty returns; install at all tiers.

| Function | Bytes | Code |
|----------|-------|------|
| `FUN_8010a49c` | 10 | `*(uint8_t *)0x8012b803 = 0` |
| `FUN_8010a594` | 14 | `*(uint32_t *)0xb6001080 = 0x80` |

Assembly sketch (`a49c`):

```asm
    li      $v0, -30585          /* 0x8012 → hi for 0x8012b803 */
    sll     $v0, 16
    addiu   $v0, -18429          /* lo → 0x8012b803 */
    sb      $zero, 3($v0)        /* offset 3 from computed base */
    jr      $ra
    nop
```

---

## 7. Validator stubs (use with caution)

### 7.1 `FUN_8010bb54` — eSCO packet-type validator (T2)

Reference returns `0` (accept) or `0x12` (LMP unsupported feature). **Do not stub
with unconditional accept** until P2 hardware test — wrong acceptance may corrupt
eSCO negotiation.

| Profile | Pattern |
|---------|---------|
| P1 | OMIT (gateway not installed) |
| P2+ | **IMPL** (74 B) |

### 7.2 `FUN_8010bda0` — SCO/eSCO acceptance validator (T2)

Returns `0x12` to reject or `0` to accept. **IMPL required** for P2 — rejecting
all eSCO breaks audio.

---

## 8. `FUN_8010a7b8` — TLV applier no-op (SKIP body)

Not a stub in the hook sense: the function must exist but the loop is a no-op when
`remaining == 0` (DATA snapshot). Libre: ensure TLV header at `0x801115fc` has
`remaining = 0` (ROM default) or replicate the 114 B function with an immediate
`return` when count is zero.

---

## 9. Source tree layout (Phase 6)

```
rtl8761bu-libre/src/
  init.S                 # patch_entry boot sequence
  stub_macros.S          # §2 macros
  shims/
    shim_d154.S          # LMP_CH passthrough
    shim_c088.S          # bos+0x30 conn handler
    shim_da70_t1.S       # LC TX P1 passthrough
    shim_d9f4_t1.S       # LC RX P1 passthrough
    stub_ca20_t1.S       # type-0x67 empty handler
  trivial/
    fn_a49c.S
    fn_a594.S
  hooks/                 # full IMPL-T1+ (one file per function or group)
    ...
```

**Build profiles** via Makefile `PROFILE=P1|P2|P3` selecting object lists:

| `PROFILE` | Extra objects beyond P0 entry |
|-----------|-------------------------------|
| `P0` | Entry + sub-installers only; most hook installs skipped |
| `P1` | + shims + trivial + T1 IMPL set + `stub_ca20_t1.o` |
| `P2` | + replace da70/d9f4 stubs with full; add T2 IMPL set |
| `P3` | + replace `stub_ca20` with full `ca20`; add T3 AFH cluster |

---

## 10. Size budget (stub contribution)

| Component | Count | Bytes each | Total |
|-----------|-------|------------|-------|
| SHIM-POOL (`d154`, `c088`) | 2 | 16 | 32 |
| SHIM-ARGS (`da70`, `d9f4` P1) | 2 | 20 | 40 |
| EMPTY-RET (`ca20` P1) | 1 | 12 | 12 |
| TRIVIAL-FX (`a49c`, `a594`) | 2 | 12 | 24 |
| `cc94` sequencer | 1 | 26 | 26 |
| Literal pools / alignment | — | — | ~16 |
| **Stub subtotal** | **8** | — | **~150 B** |

Stubs are negligible vs T1 IMPL estimate (~1700–2200 insns). The size win is
**OMIT-INSTALL** for T2/T3 hooks (~21 functions, ~4 KiB reference) when building
profiled images.

---

## 11. Install-time checklist (per profile)

### P1 minimal Bluetooth

```
[ ] ROM-NULL: skip 0x801212e4, 0x801212e0
[ ] OMIT: bos+0xd8 (test NULL), all T2/T3/T4 DRAM hooks
[ ] SHIM: d154, c088, da70_t1, d9f4_t1
[ ] STUB: ca20_t1 (after cc94 sequencer)
[ ] TRIVIAL: a49c, a594
[ ] IMPL: e27c, dfb0, daa4, cc94 + 22 T1 hooks + 6 sub-installers
[ ] SKIP: TLV active records (remaining=0)
[ ] OMIT: address-pair table
```

### P2 eSCO audio

```
[ ] IMPL: bos+0xd8 (bba4), bb54, T2 hook cluster (14 functions)
[ ] REPLACE: da70_t1 → da70 full, d9f4_t1 → d9f4 full
[ ] KEEP: ca20_t1 stub (until P3)
```

### P3 AFH

```
[ ] REPLACE: ca20_t1 → ca20 full (534 B + 7 BSS counters)
[ ] IMPL: ce0c, ccb8, fa34, f950, fb08, e350 (7 T3 hooks)
```

---

## 12. Verification plan

| Test | Profile | Pass criterion |
|------|---------|----------------|
| Patch load | P0 | `dmesg`: firmware loaded; `*0x80120538 == 4` |
| HCI up | P1 | `hciconfig hci0 up` succeeds |
| Inquiry/scan | P1 | `hcitool scan` finds peers |
| ACL connect | P1 | L2CAP ping to phone/peer |
| ACL without `bos+0xd8` | P1 | Connect works; `FC67`/eSCO VSC returns error |
| SCO/eSCO | P2 | Headset audio bidirectional |
| AFH under Wi-Fi | P3 | No elevated disconnect vs reference firmware |

---

## 13. Summary

| Category | Functions | Libre action |
|----------|-----------|--------------|
| ROM-NULL globals | 4 slots | Never install |
| SHIM (unconditional ROM) | `d154`, `c088`, `da70` P1, `d9f4` P1 | 16–20 B each |
| EMPTY-RET | `ca20` P1/P2 | ~12 B; upgrade at P3 |
| OMIT-INSTALL | T2/T3/T4 hooks, `bos+0xd8` P1 | Skip store in installer |
| TRIVIAL-FX | `a49c`, `a594` | Real 10–14 B code |
| Sequencer | `cc94` | 26 B; not reducible |
| Full IMPL | All others per tier | See mandatory_hooks §4 |

**Key design principle**: Prefer **OMIT-INSTALL** over **EMPTY fn-ptr** for tier-gated
hooks — a NULL slot lets ROM fall back; a stub pointer to `jr ra` may not match the
expected calling convention and can crash the BT stack.

---

## Related documents

| File | Content |
|------|---------|
| `reverse_engineering_mandatory_hooks.md` | Per-hook verdict and profiles P0–P4 |
| `reverse_engineering_minimum_feature_set.md` | T0–T4 tier definitions |
| `reverse_engineering_protocol_dispatch_layer.md` | Dispatch handlers + `ca20` decompile |
| `reverse_engineering_libre_patch_layout.md` | PRAM layout, section model |
| `reverse_engineering_hardware_layer.md` | ROM-NULL global hooks §12 |
