# P4 Completeness Audit — 44 `FUN_8010a000` Hook Installs

**Date**: 2026-06-10  
**Status**: PASS — all 44 DRAM hook slots are IMPL or SHIM; zero `STUB_RET` among hook targets.  
**Script**: `rtl8761bu-libre/scripts/audit_hook_installs.py`  
**Sources**: Appendix D (`reverse_engineering_patch_installer.md`), tier table
(`reverse_engineering_mandatory_hooks.md` §4), `init.S` + `patch_entry_tail.S` install
sequence, `hook_stubs.S` stub scan.

---

## 1. Verdict

| Metric | Value |
|--------|-------|
| Direct DRAM hook installs (Appendix D) | **44** |
| Implemented (vendor byte-identical or libre transcription) | **43 IMPL** |
| ROM passthrough shim | **1 SHIM** (`fn_c088`) |
| `STUB_RET` among hook symbols | **0** |
| Remaining `hook_stubs.S` stub | **1** (`fn_eac0_callee` — sub-inst #6 optional callee, **not** a hook install) |

**P4 hook-install completeness: PASS.**

Build (`make docker`, profile `p2-libre`):

- `make compliance-ci`: LINUX-LIBRE PASS (single-patch 27,928 B)
- patch1 SHA256: `61d465d62bc55370789835163b8aa8e9be42f43d7cb8bb24b2f27babce397f31`
- fw SHA256: `ea7a57423983716cf61cceb7c278e8587f1542c5329428acdd63a6ca86898a56`
- PRAM code: 27,680 B + 124 B NOP pad (pad.py ELF overlay)

---

## 2. Install path

| Hooks | Installer location | Mechanism |
|-------|-------------------|-----------|
| #1–#40 | `init.S` `patch_entry` | `INSTALL_HOOK` / `INSTALL_HOOK_ABS` macros |
| #41–#44 | `patch_entry_tail.S` epilogue @ `[0x820,0xE4C)` | Vendor-transcribed MIPS16e; pool refs in `patch_entry_pool.S` |

Hook #42 (`fn_10ddc`) uses relocated runtime `0x8010AE4D` (PRAM+0x0E4C); pool entry
documents `fn_10ddc+1` at `0x8010a3f2`.

---

## 3. Per-hook table

| # | RAM slot | Vendor fn | Libre symbol | Kind | Source |
|---|----------|-----------|--------------|------|--------|
| 1 | `0x80121318` | `FUN_8010b118` | `fn_b118` | IMPL | `t1_hooks.S` |
| 2 | `0x80120844` | `LAB_8010b174` | `fn_b174` | IMPL | `t2_hooks.S` |
| 3 | `0x80120784` | `LAB_8010bba4` | `fn_bba4` | IMPL | `lmp_vsc.S` |
| 4 | `0x801286c0` | `LAB_8010be20` | `fn_be20` | IMPL | `t1_hooks.S` |
| 5 | `0x801206cc` | `LAB_8010c1e8` | `fn_c1e8` | IMPL | `t1_hooks.S` |
| 6 | `0x801206d0` | `LAB_8010c224` | `fn_c224` | IMPL | `t1_hooks.S` |
| 7 | `0x8012088c` | `FUN_8010b3d8` | `fn_b3d8` | IMPL | `t1_hooks.S` |
| 8 | `0x80121368` | `FUN_8010b0a4` | `fn_b0a4` | IMPL | `t1_hooks.S` |
| 9 | `0x8012136c` | `FUN_8010c198` | `fn_c198` | IMPL | `t1_hooks.S` |
| 10 | `0x80121360` | `FUN_8010d1f4` | `fn_d1f4` | IMPL | `t1_hooks.S` |
| 11 | `0x80121344` | `FUN_8010c780` | `fn_c780` | IMPL | `t1_hooks.S` |
| 12 | `0x80125550` | `FUN_8010c63c` | `fn_c63c` | IMPL | `t1_hooks.S` |
| 13 | `0x8012084c` | `LAB_8010b7f0` | `fn_b7f0` | IMPL | `t2_hooks.S` |
| 14 | `0x80120c9c` | `FUN_8010e27c` | `fn_e27c` | IMPL | `callees.S` |
| 15 | `0x80120de8` | `FUN_8010dd1c` | `fn_dd1c` | IMPL | `t1_hooks.S` |
| 16 | `0x80120f10` | `FUN_8010d890` | `fn_d890` | IMPL | `t1_hooks.S` |
| 17 | `0x80120dbc` | `FUN_8010d618` | `fn_d618` | IMPL | `t1_hooks.S` |
| 18 | `0x80120f3c` | `FUN_8010a594` | `fn_a594` | IMPL | `patch_entry_pool.S` |
| 19 | `0x80120cf8` | `FUN_8010c0f4` | `fn_c0f4` | IMPL | `t2_hooks.S` |
| 20 | `0x80121414` | `FUN_8010a4ac` | `fn_a4ac` | IMPL | `patch_entry_pool.S` |
| 21 | `0x801213dc` | `FUN_8010a49c` | `fn_a49c` | IMPL | `patch_entry_pool.S` |
| 22 | `0x80121348` | `FUN_8010bce0` | `fn_bce0` | IMPL | `t2_hooks.S` |
| 23 | `0x80120590` | `FUN_8010c49c` | `fn_c49c` | IMPL | `t1_hooks.S` |
| 24 | `0x8012067c` | `FUN_8010c43c` | `fn_c43c` | IMPL | `t1_hooks.S` |
| 25 | `0x80120f4c` | `FUN_8010d168` | `fn_d168` | IMPL | `t3_hooks.S` |
| 26 | `0x801213e8` | `FUN_8010fa34` | `fn_fa34` | IMPL | `t3_hooks.S` |
| 27 | `0x801213c8` | `FUN_8010f950` | `fn_f950` | IMPL | `t3_hooks.S` |
| 28 | `0x80121458` | `FUN_8010fb08` | `fn_fb08` | IMPL | `t3_hooks.S` |
| 29 | `0x80121410` | `FUN_8010abd0` | `fn_abd0` | IMPL | `t2_hooks.S` |
| 30 | `0x801206fc` | `LAB_8010f884` | `fn_f884` | IMPL | `t2_hooks.S` |
| 31 | `0x80120a0c` | `FUN_8010f85c` | `fn_f85c` | IMPL | `t3_hooks.S` |
| 32 | `0x80120cd4` | `FUN_8010a550` | `fn_a550` | IMPL | `patch_entry_pool.S` |
| 33 | `0x80120cf4` | `FUN_8010c160` | `fn_c160` | IMPL | `t2_hooks.S` |
| 34 | `0x80120824` | `FUN_8010c178` | `fn_c178` | IMPL | `t2_hooks.S` |
| 35 | `0x801206dc` | `LAB_8010c088` | `fn_c088` | **SHIM** | `shims.S` |
| 36 | `0x80120990` | `LAB_8010b4d0` | `fn_b4d0` | IMPL | `t2_hooks.S` |
| 37 | `0x80121370` | `FUN_8010a5ac` | `fn_a5ac` | IMPL | `patch_entry_pool.S` |
| 38 | `0x80120bfc` | `FUN_8010c854` | `fn_c854` | IMPL | `t2_hooks.S` |
| 39 | `0x80121334` | `FUN_8010c09c` | `fn_c09c` | IMPL | `t2_hooks.S` |
| 40 | `0x801206c8` | `LAB_8010bc74` | `fn_bc74` | IMPL | `lmp_vsc.S` |
| 41 | `0x80120cdc` | `FUN_8010ce0c` | `fn_ce0c` | IMPL | `t3_hooks.S` |
| 42 | `0x80121020` | `FUN_80110ddc` | `fn_10ddc` | IMPL | `t4_hooks.S` |
| 43 | `0x80121220` | `FUN_8010bda0` | `fn_bda0` | IMPL | `t2_hooks.S` |
| 44 | `0x8012167c` | `FUN_8010e350` | `fn_e350` | IMPL | `t3_hooks.S` |

---

## 4. Related surfaces (outside the 44)

### Protocol dispatch struct (`fn_e27c` installer, hook #14)

Installed by `fn_e27c` into `0x8012AE8C` — not counted in the 44 DRAM writes:

| Handler | Kind | Source |
|---------|------|--------|
| `fn_dfb0` | IMPL | `protocol_dispatch.S` |
| `fn_daa4` | IMPL | `protocol_dispatch.S` |
| `fn_cc94` | IMPL | `shims.S` |
| `fn_d154` | SHIM | `shims.S` |
| `fn_da70` | SHIM | `protocol_dispatch.S` |
| `fn_d9f4` | SHIM | `protocol_dispatch.S` |
| `fn_ca20` | IMPL | `protocol_dispatch.S` (534 B; was STUB-T1 through P2) |

### Sub-installer #2 (19 indirect targets)

All **IMPL** in `sub2_hooks.S` (548 B vendor-identical cluster @ PRAM `0xFE84`).

### Sub-installer side targets

| Target | Kind | Source |
|--------|------|--------|
| `fn_10ca4` (sub-inst #3) | IMPL | `t4_hooks.S` |
| `fn_e82c` (sub-inst #5) | IMPL | alias in `fn_e350_post_gap` |
| `fn_eac0_callee` | STUB | `hook_stubs.S` — optional `*0x80120c80`; ROM NULL-safe skip in `sub_installer_6` |

### Explicitly omitted (documented elsewhere)

- Address-pair table @ file `0xA0` — OMIT (`reverse_engineering_address_pair_table_omit.md`)
- Late-patch block `0x80109200`…`0x80109E83` — OMIT (`reverse_engineering_late_patch_block_omit.md`)
- Global RAM `0x801212e4` / `0x801212e0` — ROM-NULL (`reverse_engineering_hardware_layer.md` §12)

---

## 5. Tier roll-up (unchanged from mandatory_hooks §4)

| Tier | Count | P4 status |
|------|-------|-----------|
| T1 | 22 | All IMPL or SHIM |
| T2 | 14 | All IMPL |
| T3 | 7 | All IMPL |
| T4 | 1 (`fn_10ddc`) | IMPL |

---

## 6. Re-run

```bash
cd rtl8761bu-libre
docker run --rm -v "$(pwd)":/work rtl8761bu-libre make docker
python3 scripts/audit_hook_installs.py   # exit 0 = PASS
```

Expected: `PASS: all 44 hook installs are IMPL or SHIM`.
