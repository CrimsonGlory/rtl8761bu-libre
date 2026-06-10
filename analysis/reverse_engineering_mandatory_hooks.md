# Mandatory vs Optional ROM Hooks — Libre Design Decisions

**Date**: 2026-06-09  
**Status**: Phase 5 design (implements `[NEXT]` from `work-in-progress.txt`)  
**Sources**: `FUN_8010a000` hook map (Appendix D), hardware layer §12, protocol dispatch
layer, minimum feature set tiers, Ghidra decompiles (`DecompileRemainingNewFns.java`,
`DecompileAddr.java` on bos slots).

This document answers: *which hook slots must the libre patch populate, which can
stay NULL, and which need only a ROM passthrough shim — for each target feature tier?*

Companion docs: `reverse_engineering_minimum_feature_set.md` (function-level tiers),
`reverse_engineering_libre_patch_layout.md` (binary layout).

---

## 1. Decision vocabulary

| Verdict | Libre action | When ROM calls with NULL |
|---------|--------------|--------------------------|
| **ROM-NULL** | Do **not** install a pointer | ROM skips hook; fallback path runs |
| **ROM-CALL** | No patch code; `jalr` fixed `0x8000xxxx` | N/A — not a hook slot |
| **SHIM** | 16–44 B wrapper; tail-call ROM original | N/A — must install shim |
| **STUB** | Return 0 / accept; no side effects | Only if ROM NULL-safe (rare) |
| **IMPL-Tn** | Full patch implementation required for tier **Tn** | Undefined — reference installs always |

**Conservative rule (superseded where decompile proves otherwise):** Every slot written
by `FUN_8010a000` is **IMPL-T1** until shown ROM-NULL-safe or tier-gated.

**Practical libre profiles:**

| Profile | Goal | Hook policy |
|---------|------|-------------|
| **P0** | Patch loads, `*0x80120538 = 4` | Entry + sub-installers only; most hooks NULL |
| **P1** | `hciconfig up`, inquiry, scan, ACL | P0 + T1 IMPL hooks + SHIMs |
| **P2** | SCO/eSCO audio | P1 + T2 hooks |
| **P3** | AFH / Wi-Fi coexistence | P2 + T3 hooks |
| **P4** | Reference parity | All 44 installs + sub-installer #2 targets |

---

## 2. Global RAM hook variables (not in `bos_base`)

These are **not** installed by the patch entry; ROM or connection setup owns them.

| Runtime slot | ROM consumer | Patch installs? | Verdict | Notes |
|--------------|--------------|-----------------|---------|-------|
| `0x801212e4` | `FUN_8004f824` (HW write path) | **No** | **ROM-NULL** | GZF snapshot NULL; clamp-only fallback. Libre: leave NULL. |
| `0x801212e0` | `FUN_80050810` (conn-type dispatch) | **No** | **ROM-NULL** | Optional override; NULL → pure ROM type 0–3 chain. |
| `crypto_struct+0xe4` | Per-connection HW path | **No** | **ROM-managed** | `FUN_80025b68` at SSP; JIT MIPS16e in conn buffer. |
| `PTR_DAT_800508ec` | `FUN_80050810` prep hook | **No** | **ROM-NULL** | Double-indirect; reference leaves unset. |

**Conclusion:** Zero libre implementation for global HW hooks. Confirmed in
`reverse_engineering_hardware_layer.md` §12.

---

## 3. `bos_base` struct hooks (`puVar3` ≈ `0x801206AC`)

Six function-pointer fields in the BT operational state object. All are **written**
by `FUN_8010a000`; ROM dereferences them during HCI/LMP/connection processing.

| Offset | Handler | Size | Verdict | Rationale |
|--------|---------|------|---------|-----------|
| `+0xd8` | `LAB_8010bba4` | 176 B | **IMPL-T2** / NULL-T1 | LMP VSC gateway (`0x268`). Without it, ROM `FUN_80076110` timeout path — eSCO VSC fails; ACL-only may work. |
| `+0x30` | `LAB_8010c088` | 16 B | **SHIM-T1** | Passthrough: `(*DAT_8010c098)()` — ROM connection handler. Libre: 16 B tail-call shim. |
| `+0x20` | `LAB_8010c1e8` | 44 B | **IMPL-T1** | ROM pre-hook + conditional flag at `large2[0].+0x28` + secondary ROM call. Connection/ACL path. |
| `+0x24` | `LAB_8010c224` | ? | **IMPL-T1** | Paired HCI slot with `+0x20`; unanalyzed but same install phase. Treat as mandatory. |
| `+0x1c` | `LAB_8010bc74` | ? | **IMPL-T1** | Installed late (post-RF-init). VSC dispatcher data table neighbor (`0x8010bc54`); likely LMP/HCI glue. |
| `+0x50` | `LAB_8010f884` | ? | **IMPL-T2** | Installed mid-sequence with eSCO-adjacent slots (`a550`, `c160`). Defer to T2 unless hardware test proves optional. |

### `sec_base` and tertiary struct hooks

| Slot | Handler | Verdict | Rationale |
|------|---------|---------|-----------|
| `sec_base+0x14` (`0x80120844`) | `LAB_8010b174` | **IMPL-T2** | Installed before eSCO cluster; secondary struct hook. |
| `sec_base+0x1c` (`0x8012084c`) | `LAB_8010b7f0` | **IMPL-T2** | LMP eSCO packet processor (~772 B). |
| `secondary+0x30` (`0x80120990`) | `LAB_8010b4d0` | **IMPL-T2** | eSCO slot allocation trigger. |
| `0x801286c0` | `LAB_8010be20` | **IMPL-T1** | Global dispatch slot; installed early. Unanalyzed — conservative T1. |

---

## 4. All 44 `FUN_8010a000` DRAM hook installs

Complete table from Appendix D with libre verdict. **Tier column** = lowest tier
requiring a real implementation (SHIM counts as implemented).

| # | RAM slot | Function | ~Size | Tier | Verdict | Role / notes |
|---|----------|----------|-------|------|---------|--------------|
| 1 | `0x80121318` | `FUN_8010b118` | 82 B | T1 | **IMPL-T1** | Slot interval allocator |
| 2 | `sec+0x14` | `LAB_8010b174` | ? | T2 | **IMPL-T2** | Secondary struct hook |
| 3 | `bos+0xd8` | `LAB_8010bba4` | 176 B | T2 | **IMPL-T2** | LMP VSC gateway; optional for ACL-only |
| 4 | `0x801286c0` | `LAB_8010be20` | ? | T1 | **IMPL-T1** | Global dispatch; unanalyzed |
| 5 | `bos+0x20` | `LAB_8010c1e8` | 44 B | T1 | **IMPL-T1** | HCI handler; ROM wrapper + flag |
| 6 | `bos+0x24` | `LAB_8010c224` | ? | T1 | **IMPL-T1** | HCI handler pair |
| 7 | `0x8012088c` | `FUN_8010b3d8` | 206 B | T1 | **IMPL-T1** | ACL slot scheduler |
| 8 | `0x80121368` | `FUN_8010b0a4` | 108 B | T1 | **IMPL-T1** | ACL packet-type flag corrector |
| 9 | `0x8012136c` | `FUN_8010c198` | ? | T1 | **IMPL-T1** | Early install batch; unanalyzed |
| 10 | `0x80121360` | `FUN_8010d1f4` | ? | T1 | **IMPL-T1** | Early install batch; unanalyzed |
| 11 | `0x80121344` | `FUN_8010c780` | 34 B | T1 | **IMPL-T1** | Subsystem init (3 ROM calls) |
| 12 | `0x80125550` | `FUN_8010c63c` | 278 B | T1 | **IMPL-T1** | ACL retransmission handler |
| 13 | `sec+0x1c` | `LAB_8010b7f0` | ~772 B | T2 | **IMPL-T2** | LMP eSCO packet processor |
| 14 | `0x80120c9c` | `FUN_8010e27c` | 52 B | T1 | **IMPL-T1** | Protocol dispatch installer |
| 15 | `0x80120de8` | `FUN_8010dd1c` | ? | T1 | **IMPL-T1** | Near protocol-dispatch data |
| 16 | `0x80120f10` | `FUN_8010d890` | ? | T1 | **IMPL-T1** | Unanalyzed |
| 17 | `0x80120dbc` | `FUN_8010d618` | 422 B | T1 | **IMPL-T1** | HCI internal msg dispatcher (0x408/0x1002/0x1405/0xc03…) |
| 18 | `0x80120f3c` | `FUN_8010a594` | 14 B | T1 | **IMPL-T1** | Trivial MMIO `*(0xb6001080)=0x80` |
| 19 | `0x80120cf8` | `FUN_8010c0f4` | ? | T2 | **IMPL-T2** | eSCO-adjacent slot region |
| 20 | `0x80121414` | `FUN_8010a4ac` | 68 B | T2 | **IMPL-T2** | eSCO readiness gate |
| 21 | `0x801213dc` | `FUN_8010a49c` | 10 B | T1 | **IMPL-T1** | Trivial `*(0x8012b803)=0` |
| 22 | `0x80121348` | `FUN_8010bce0` | ? | T2 | **IMPL-T2** | Near eSCO slots |
| 23 | `0x80120590` | `FUN_8010c49c` | ? | T1 | **IMPL-T1** | Infrastructure ptr region |
| 24 | `0x8012067c` | `FUN_8010c43c` | ? | T1 | **IMPL-T1** | Infrastructure ptr region |
| 25 | `0x80120f4c` | `FUN_8010d168` | ? | T3 | **IMPL-T3** | Near AFH slots (#26–28) |
| 26 | `0x801213e8` | `FUN_8010fa34` | 184 B | T3 | **IMPL-T3** | AFH 79-ch map merger |
| 27 | `0x801213c8` | `FUN_8010f950` | 174 B | T3 | **IMPL-T3** | AFH quality + VSC FC95 trigger |
| 28 | `0x80121458` | `FUN_8010fb08` | 292 B | T3 | **IMPL-T3** | BLE 40-ch map aggregator |
| 29 | `0x80121410` | `FUN_8010abd0` | ? | T2 | **IMPL-T2** | Between eSCO and AFH groups |
| 30 | `bos+0x50` | `LAB_8010f884` | ? | T2 | **IMPL-T2** | bos_base hook |
| 31 | `0x80120a0c` | `FUN_8010f85c` | ? | T3 | **IMPL-T3** | AFH region |
| 32 | `0x80120cd4` | `FUN_8010a550` | 54 B | T2 | **IMPL-T2** | Set eSCO-active flag |
| 33 | `0x80120cf4` | `FUN_8010c160` | ? | T2 | **IMPL-T2** | eSCO slot region |
| 34 | `0x80120824` | `FUN_8010c178` | ? | T2 | **IMPL-T2** | eSCO slot region |
| 35 | `bos+0x30` | `LAB_8010c088` | 16 B | T1 | **SHIM-T1** | ROM passthrough connection handler |
| 36 | `secondary+0x30` | `LAB_8010b4d0` | 76 B | T2 | **IMPL-T2** | eSCO slot allocation trigger |
| 37 | `0x80121370` | `FUN_8010a5ac` | 36 B | T2 | **IMPL-T2** | Timing compensation |
| 38 | `0x80120bfc` | `FUN_8010c854` | ? | T2 | **IMPL-T2** | Installed with `a5ac` |
| 39 | `0x80121334` | `FUN_8010c09c` | 76 B | T2 | **IMPL-T2** | Per-link BB reg `0xf` gate |
| 40 | `bos+0x1c` | `LAB_8010bc74` | ? | T1 | **IMPL-T1** | bos_base hook |
| 41 | `0x80120cdc` | `FUN_8010ce0c` | 728 B | T3 | **IMPL-T3** | AFH capability mapper |
| 42 | `0x80121020` | `FUN_80110ddc` | 448 B | T4 | **IMPL-T4** | eSCO packet-type selector (relocated @ PRAM+0x0E4C) |
| 43 | `0x80121220` | `FUN_8010bda0` | 114 B | T2 | **IMPL-T2** | SCO/eSCO acceptance validator |
| 44 | `0x8012167c` | `FUN_8010e350` | 1174 B | T3 | **IMPL-T3** | AFH quality ranking engine |

### Tier roll-up (44 installs)

| Tier | Count | Policy |
|------|-------|--------|
| T1 | 22 | Required for profile **P1** |
| T2 | 14 | Add for profile **P2** (audio) |
| T3 | 7 | Add for profile **P3** (AFH) |
| T4 | 1 | Full parity only (`FUN_80110ddc`) |

---

## 5. Protocol dispatch struct (`0x8012AE8C`)

Installed by `FUN_8010e27c` (hook #14), not direct `bos_base` offsets.

| Handler | Size | Tier | Verdict |
|---------|------|------|---------|
| `FUN_8010e27c` | 52 B | T1 | **IMPL-T1** — installer + 3 BB reg writes |
| `FUN_8010dfb0` | 530 B | T1 | **IMPL-T1** — LMP intercept (SSP, eSCO activation) |
| `FUN_8010daa4` | 518 B | T1 | **IMPL-T1** — HCI events (inquiry/conn/LE meta) |
| `FUN_8010cc94` | 26 B | T1 | **IMPL-T1** — sequencer: ROM then patch |
| `FUN_8010d154` | 16 B | T1 | **SHIM-T1** — passthrough → ROM `0x80066e68` |
| `FUN_8010da70` | 44 B | T1 | **SHIM-T1** — LC TX pass-through → ROM `0x80042420` |
| `FUN_8010d9f4` | 98 B | T1 | **SHIM-T1** — LC RX pass-through → ROM `0x80042188` |
| `FUN_8010ca20` | 534 B | T3 | **IMPL-T3** — type `0x67` coexistence monitor (`protocol_dispatch.S`) |

---

## 6. Sub-installer side effects (not hook slots, but mandatory calls)

| # | Function | Tier | Verdict | Notes |
|---|----------|------|---------|-------|
| 1 | `FUN_8010af40` | T1 | **IMPL-T1** | Clear bit 6 of BB reg `0x108` |
| 2 | `FUN_8011011c` | T1 | **IMPL-T1** | Writes 19 fn-ptrs to `0x801205b0`–`0x80121100` |
| 3 | `FUN_8010fc58` | T1 | **IMPL-T1** | 1 fn-ptr + callee |
| 4 | `FUN_8010f370` | T1 | **IMPL-T1** | 10 data-ptrs into 28-byte struct |
| 5 | `FUN_8010e81c` | T1 | **IMPL-T1** | 1 fn-ptr |
| 6 | `FUN_8010eac0` | T1 | **IMPL-T1** | `0xffffffff` sentinel + call |

**Sub-installer #2 caveat:** The 19 installed functions are **unanalyzed** but the
installer itself is **IMPL-T1**. Libre must either (a) replicate the installer
verbatim with the same target addresses, or (b) decompile all 19 and classify
individually. Conservative: treat all 19 as **IMPL-T4** until decompiled; the
installer stub is still **IMPL-T1**.

---

## 7. Optional / omit entirely

| Item | Verdict | Notes |
|------|---------|-------|
| Address-pair table @ file `0xA0` | **OMIT** | Confirmed 2026-06-10: zero xrefs, no consumer; see `reverse_engineering_address_pair_table_omit.md` |
| `FUN_80103780` master installer | **OMIT** | Parallel variant; runtime entry is `FUN_8010a000` |
| Late-patch block `FUN_80109980`…`80109824` | **OMIT-T3** | In vanilla bin tail; GZF "dark firmware"; not in 27,808 B PRAM window |
| `bos+0xd8` NULL | **NULL-T1** | Test on hardware; ROM timeout fallback for LMP `0x268` |
| `FUN_8010ca20` empty stub | **STUB-T1** | Degrades Wi-Fi/BLE coexistence only |
| TLV loop active records | **SKIP** | Leave `remaining=0`; `FUN_8010a7b8` becomes no-op |

---

## 8. ROM surfaces that never need patch hooks

These are invoked by address from patch code but are **not** hook slots:

| Surface | ROM entry | Used from |
|---------|-----------|-----------|
| BD_ADDR copy | `0x8000fd38` | Entry boot |
| Register script | `0x8003aea0` | Entry via `0x801205b4` |
| HW reg read/write | `0x8001136c` / `0x8001139c` | Throughout patch |
| HCI OGF 0x3F (73 VSCs) | `0x80030f1c` | Never patched |
| LMP VSC leaf | `0x80047c50` | Via `FUN_8010bba4` pool |
| LMP `0x25B/25C/266/271` | ROM stubs | No patch fptr install |
| Protocol ROM originals | `0x800138cc`, `0x80066e68`, `0x80042188`, `0x80042420`, `0x80020bec`, `0x80071634` | Tail-called from patch handlers |
| Conn-type HW chain | `FUN_80050810` + 4 handlers | Types 0–3 |
| Codec templates | `FUN_80025b68` | SSP path only |

---

## 9. Recommended libre hook profiles

### P1 — Minimal Bluetooth (first hardware test)

**Install:** Entry boot + 6 sub-installers + 22 T1 DRAM hooks + 6 `bos_base`/struct
hooks (including SHIMs) + protocol dispatch core (5 IMPL + 3 SHIM + `ca20` STUB).

**May omit (test):** `bos+0xd8` (hook #3) if ACL-only.

**Leave NULL:** `0x801212e4`, `0x801212e0`, all per-conn `+0xe4`.

**Estimated:** ~22 mandatory implementations + ~3 shims + ~14 unanalyzed T1 functions
still needing decompile before sign-off.

### P2 — Audio / eSCO

**Add:** 14 T2 hooks (rows marked IMPL-T2 above) + `bos+0xd8` gateway + `FUN_8010bb54`.

### P3 — AFH / coexistence

**Add:** 7 T3 hooks + full `FUN_8010ca20`.

### P4 — Full parity

**Add:** Sub-installer #2's 19 targets + `FUN_80110ddc` + late-patch functions.
Address-pair table **omitted** (see `reverse_engineering_address_pair_table_omit.md`).

---

## 10. Implementation checklist (by install order)

For `patch_entry` replicating `FUN_8010a000`:

```
[ ] Phase 0: config copy, BSS zero, config bit clears
[ ] Phase 1: hooks #1–17 + sub-installers #1–2
[ ] Phase 2: sub-installer #3 + hooks #18–34 + sub-installers #4–6
[ ] Phase 3: ROM FUN_8003aea0, silicon rev, hooks #35–40
[ ] Phase 4: TLV + BD_ADDR + conditional BB init + RF init
[ ] Phase 5: hooks #41–44 + patch-active = 4
```

**NULL-safe skips:** Do not add stores for §2 global slots.

**SHIM pattern:** 16-byte MIPS16e prologue + `lw v0, pool(pc); jalr v0` + epilogue
(see `FUN_8010c088`, `FUN_8010d154`).

---

## 11. Open items

1. Decompile remaining **T1-unanalyzed** hooks: `c224`, `bc74`, `be20`, `c198`,
   `d1f4`, `dd1c`, `d890`, `c49c`, `c43c` — may downgrade some from IMPL-T1.
2. Decompile **sub-installer #2**'s 19 targets — required for P4; may affect P1
   if any slot is on the ACL critical path.
3. Hardware test: confirm **P1 works with `bos+0xd8` NULL** (ACL-only, no eSCO).
4. ~~Resolve **`FUN_80110ddc`** (hook #42)~~ — **DONE** (2026-06-10): eSCO packet-type selector; `t4_hooks.S`.
5. ~~**Address-pair table**~~ — **DONE** (2026-06-10): intentional OMIT; `reverse_engineering_address_pair_table_omit.md`.

---

## 12. Summary matrix

| Hook class | P1 Minimal | P2 eSCO | P3 AFH | Action |
|------------|------------|---------|--------|--------|
| Global `0x801212e4/e0` | NULL | NULL | NULL | ROM-NULL |
| `bos+0xd8` LMP VSC | NULL* | IMPL | IMPL | *ACL test |
| `bos+0x20/24/1c` | IMPL | IMPL | IMPL | ACL/HCI path |
| `bos+0x30` conn | SHIM | SHIM | SHIM | 16 B ROM tail-call |
| `bos+0x50` | — | IMPL | IMPL | T2 |
| 44 DRAM hooks (T1 subset) | 22 IMPL | 22 | 22 | See §4 |
| Protocol dispatch | 5+3 | +0 | +`ca20` full | §5 |
| Sub-installers 1–6 | all 6 | all 6 | all 6 | §6 |
| AFH cluster (#25–28,41,44) | omit | omit | IMPL | T3 |
| Address-pair table | omit | omit | omit | OMIT |

---

## Related documents

| File | Content |
|------|---------|
| `reverse_engineering_minimum_feature_set.md` | Function-level T0–T4 tiers |
| `reverse_engineering_libre_patch_layout.md` | PRAM layout, hook map excerpt |
| `reverse_engineering_patch_installer.md` | Appendix D full install map |
| `reverse_engineering_hardware_layer.md` | ROM-NULL global hooks §12 |
| `reverse_engineering_protocol_dispatch_layer.md` | Dispatch handlers + `ca20` |
| `reverse_engineering_sco_esco_layer.md` | T2 eSCO cluster decompiles |
