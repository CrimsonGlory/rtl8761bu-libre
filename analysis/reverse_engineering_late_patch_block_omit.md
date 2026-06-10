# Late-patch block `FUN_80109980`…`80109824` — intentional OMIT (2026-06-10)

## Summary

**Verdict: OMIT for all libre profiles (P1–P4).** Do not embed the seven
late-patch functions that live in GZF DATA-block **code section 1**
(`0x80109200`–`0x80109E83`, below BSS at `0x80109C00`). FC20 loads only
**27,808 B** at PRAM **`0x8010A000`**; this region is never present in the
single-patch image. Libre replacements for the two operationally relevant roles
(eSCO packet-type selection, HW register init) already exist inside the loaded
PRAM window.

---

## Functions in the block

| Runtime | Size | Role (decompiled 2026-06-08) |
|---------|------|------------------------------|
| `0x80109980` | 175 B | HW register init (7 BB regs; bos+0x168/0x16a) |
| `0x80109C08` | 448 B | eSCO packet-type negotiator |
| `0x80109DE0` | 164 B | Hamming-weight checksum (10-byte popcount) |
| `0x80109200` | 322 B | Bitfield encoder (PCM/I2S-style HW programming) |
| `0x80109550` | 226 B | Audio codec sampler (debug loop) |
| `0x801096D4` | 258 B | Audio circular buffer writer |
| `0x80109824` | 180 B | eSCO retransmission timer |

Span: `0x80109200` … `0x80109E83` (1,731 B). All addresses are **strictly
below** PRAM base `0x8010A000` → **outside** the FC20-loaded window
(`0x8010A000` … `0x80110C9F`).

---

## Patch RAM layout context

From `reverse_engineering_patch_installer.md`:

```
0x80100000 – 0x80109BFF   code section 1  ← late-patch block lives here
0x80109C00 – 0x80109FFF   BSS (zeroed by FUN_8010a6c8)
0x8010A000 – 0x8010ADC3   code section 2  ← FC20 entry + libre PRAM
```

Libre single-patch EPatch v2 copies **only** the 27,808-byte body to
`0x8010A000`. Code section 1 is **not** loaded at `0x80100000` in this layout
(entry-centric design in `rtl8761bu.ld` / `reverse_engineering_libre_patch_layout.md` §3).

---

## RE evidence (GZF process mode, 2026-06-10)

### `FindXrefsTo.java` — Ghidra xref scan

| Target | In PRAM? | Ghidra xrefs | Notes |
|--------|----------|--------------|-------|
| `0x80109980` | no | 0 | — |
| `0x80109C08` | no | 8 | Callers in `0x8010E940`–`0x8010F246` range (GZF DATA snapshot) |
| `0x80109DE0` | no | 0 | — |
| `0x80109200` | no | 1 | ROM pool `0x800145E0`; reader `FUN_80014524` |
| `0x80109550` | no | 0 | — |
| `0x801096D4` | no | 0 | — |
| `0x80109824` | no | 0 | — |

GZF xrefs to `0x80109C08` are **DATA-block co-resident artifacts**: the
runtime snapshot maps both section 1 and section 2 simultaneously. They do **not**
appear in the actual FC20 patch1 body.

### Literal-pool scan — vendor vs libre patch1 body

Full-word scan of the 27,808-byte patch1 image (file offset `0x30`):

| Image | Words == `0x80109C08/09`, `0x80109200/01`, `0x80109980/81` |
|-------|----------------------------------------------------------------|
| Non-free `rtl8761bu_fw.bin` patch1 | **0** |
| Libre `rtl8761bu_fw.bin` patch1 | **0** |

PRAM offsets where GZF reports `0x80109C08` xrefs (e.g. `+0x5226`) hold **different**
values in both vendor and libre shipped images — confirming the GZF layout ≠ FC20
execution layout for those sites.

### `FUN_8010a000` hook-install map

Appendix D enumerates 44 DRAM hook installs + 6 sub-installers. **None** target
any address in `0x80109200`–`0x80109FFF`. Entry boot never branches into code
section 1.

---

## Functional coverage in libre PRAM

| Late-patch role | Libre replacement |
|-----------------|-------------------|
| eSCO packet-type negotiator (`0x80109C08`) | `fn_10ddc` @ PRAM+`0x0E4C` (hook #42, T4) — 448 B byte-identical sibling; see `reverse_engineering_sco_esco_layer.md` Group AF |
| HW register init (`0x80109980`) | `fn_a5d8` / `fn_a6ec` in `patch_entry_helpers.S`; BB/RF init in entry callees |
| Hamming checksum, bitfield encoder, audio debug, retrans timer | No DRAM hook install; no patch1 pool reference; ROM-only or debug paths |

---

## Hardware / compliance evidence

| Profile | Result without late block |
|---------|---------------------------|
| `full-inject-t3-pe5-vendor-tail` (`7f051e64…`) | FC20 OK, ACL connect, A2DP |
| `p2-libre` (`addd6593…`) | `make compliance-ci` PASS; staged for UB500 |
| P4 current (`0220415a…`) | `make compliance-ci` PASS; sub2 + `fn_10ddc` transcribed |

---

## OMIT decision matrix

| Criterion | Result |
|-----------|--------|
| Loaded by FC20 single-patch | **No** (below `0x8010A000`) |
| Pool reference in shipped patch1 | **No** (vendor or libre) |
| `FUN_8010a000` hook install | **No** |
| Operationally replaced in PRAM | **Yes** (`fn_10ddc`, entry helpers) |
| Hardware regression without block | **None observed** |
| Fits 27,808 B budget if forced | Would displace relocated T2/T3/T4 code |
| Re-open if | Pool ref found in patch1, or HW bisect implicates section-1 callee |

---

## Libre implementation note

**Action: none.** No `.text.late` section, no `.incbin` of section-1 bytes.

The incorrect note in `reverse_engineering_phase7_hci_bringup.md` stating libre
"includes `FUN_80109c08`" is superseded by this document — libre ships
`fn_10ddc` at `0x80110DDC` (native) / PRAM+`0x0E4C` (EPatch-relative).

---

## Cross-references

- `reverse_engineering_mandatory_hooks.md` §7 — OMIT-T3 verdict
- `reverse_engineering_minimum_feature_set.md` §T4 — late-patch listed as non-ship
- `reverse_engineering_libre_patch_layout.md` §4.2 — `.text.late` optional, deferred
- `reverse_engineering_sco_esco_layer.md` Group AF — `fn_10ddc` negotiator
- `work-in-progress.txt` Phase 2 — per-function decompile summaries
