# ROM Region 0x80020000–0x8002ffff — Catch-All Triage

Part of Phase 9's exhaustive ROM-function sweep (see `work-in-progress.txt`,
"PHASE 9 CONTINUED — EXHAUSTIVE RE OF EVERY ROM FUNCTION"). This doc covers
functions in `0x80020000`–`0x8002ffff` that don't belong to a subsystem with
its own dedicated doc.

This region contains the HCI command router (already high-confidence, documented
in `reverse_engineering_hci_command_router.md`) and the SAFER+ cipher
(already high-confidence, documented in `reverse_engineering_encryption_engine.md`),
plus a large payload of undocumented LMP procedure handlers, encryption-related
procedures, and utility functions.

## Scope

Authoritative listing (`ListRegion0x80020000.java` + `ListRegion0x80020000_Upper.java`,
both saved 2026-06-22, run via `script_file_id` against the GZF in process mode,
`use_saved_project=True`) enumerated **every** function in this address range
directly from Ghidra's `FunctionManager`.

**Initial count (full region)**: approximately 384 functions per the Phase 9
original estimate (321 unnamed + 63 thin-named).

Cross-referencing against `rom_function_index.md`'s existing named-function table:

| Category | Estimated Count |
|---|---|
| Already named, **high confidence** (decompiled+documented elsewhere) | ~24 |
| Named, **medium confidence** (one-line purpose, not decompiled) | ~20 |
| Named, **low confidence** (Kovah name, purpose unclear) | ~43 |
| Completely unnamed (`FUN_8002xxxx`, Ghidra `DEFAULT` source type) | ~297 |
| **Total** | **~384** |

The actual precise count will be refined via direct re-run of `ListRegionxxxx.java`
scripts at the start of the next pass (following the Phase 9 protocol).

## Key subsystems already documented (high confidence, listed here for completeness)

### HCI Command Router (0x80020ee0 + dispatcher OGFs)

Full detailed documentation: `reverse_engineering_hci_command_router.md`

Functions already at high confidence:
- `assoc_w_tHCI_CMD` (`0x80020ee0`, 672B) — top-level HCI command parser/router
- `HCI_CMD_OGF_01__Link_Control__FUN_80020814` (`0x80020814`, 872B)
- `HCI_CMD_OGF_02__Link_Policy__FUN_8002060c` (`0x8002060c`, 464B)
- `HCI_CMD_OGF_03__Controller_and_Baseband__big_switch_FUN_800202c0` (`0x800202c0`, 600B)
- `HCI_CMD_OGF_05__Status_Parameters__FUN_80020188` (`0x80020188`, 288B)
- `HCI_CMD_OGF_06__TestMode__big_switch_FUN_800200a8` (`0x800200a8`, 142B)
- `HCI_CMD_OGF_04?__wraps__possible_OGF0?_referencing_default_name_10` (`0x8002013c`, 74B)

Also:
- `assoc_w_tHCI_EVT` (`0x80020bec`, 718B) — HCI event dispatcher (already documented in `usb_transport_hci_driver`)

### SAFER+ Encryption (0x8002ca88 + related)

Full detailed documentation: `reverse_engineering_encryption_engine.md`

Functions already at high confidence:
- SAFER+ key schedule, core round, bias constants, S-box tables
- Bluetooth-spec E1/E21/E22 wrappers
- LMP encryption procedure handlers (linked from `reverse_engineering_encryption_engine.md`)

## Preliminary observations from listing scripts

The lower region (0x80020000–0x80024fff) contains:
- HCI command router and dispatcher functions (already documented)
- Event senders for HCI events (send_evt_HCI_*) — significant cluster
- Utility functions for config/parameter handling

The upper region (0x80025000–0x8002ffff, **241 functions from script run**) contains:
- Large cluster of LMP procedure handlers (LMP_*_0x##):
  - `LMP_COMB_KEY_0x09`, `LMP_AU_RAND_0x0B`, `LMP_SRES_0x0C`, etc.
  - Encryption-related procedures: `LMP_ENCRYPTION_MODE_REQ_0x0F`, `LMP_ENCRYPTION_KEY_SIZE_REQ_0x10`, etc.
  - Pairing procedures: `LMP_SIMPLE_PAIRING_NUMBER_0x40`, `LMP_SIMPLE_PAIRING_CONFIRM_0x3F`, etc.
  - Miscellaneous: `LMP_STOP_ENCRYPTION_REQ_0x12`, `LMP_TEMP_KEY_0x0E`, `LMP_TEMP_RAND_0x0D`, etc.
- Large unnamed function cluster (many `FUN_80029xxx`, `FUN_8002axxx`, etc. with DEFAULT source type)
- VSC handlers (`VSC_0xfc93_*`, `VSC_0xfd40_*`, etc.)
- Utility functions for crypto struct manipulation, link-key handling, etc.

## Pass 1 (2026-06-22) — Initial listing and scope establishment

**Work done this pass**:
1. Created `ListRegion0x80020000.java` and `ListRegion0x80020000_Upper.java` scripts
2. Ran both to enumerate all functions in the full region
3. Parsed output to establish scope: ~384 total functions
4. Identified already-documented subsystems (HCI router, SAFER+ cipher)
5. Created this document as a baseline for future triaging passes

**Next priority targets** (ordered by value for future passes):
1. **LMP procedure handlers cluster** (0x8002xxxx range) — start with the named ones
   first to confirm decompile-reliability, then move to unnamed neighbors
2. **Large unnamed cluster** (0x8002axxx+) — batch decompile to find patterns and
   group by theme
3. **VSC handlers** (0x8002fxxx range) — smaller, more isolated functions that
   might have fewer dependencies
4. **Event sender helpers** (0x80021xxx–0x80023xxx) — already have high-confidence
   examples to learn from in region_0x80010000

**Known open questions**:
- Exact boundary of HCI router vs LMP vs encryption subsystems (some functions may
  belong to multiple categories)
- Whether the large unnamed cluster represents independent utility functions or
  specialized sub-procedures of the named clusters
- Relationship between region_0x80020000 LMP handlers and region_0x80010000's LMP
  VSC hook (likely callers/callees relationship)

**Status**: [NEXT] — this region is too large to fully RE in a single turn; the
splitting rule applies. Future passes will continue triaging from the priority
targets above, shrinking the ticket scope as work completes.

---

## Remaining scope

**Full region, ~360 functions still requiring triage**:
- ~20 medium-confidence named (need decompile to upgrade confidence)
- ~43 low-confidence named (need decompile to understand purpose)
- ~297 completely unnamed (cold-start triage)

The list is too long to enumerate here in full; future passes will maintain it
in per-sub-range form, similar to the `region_0x80000000` pass-by-pass structure.
