# Phase 9: Exhaustive RE — ROM Region 0x80040000-0x8004ffff

**Status**: PASS 1-6 COMPLETE (2026-06-23); PASS 7 COMPLETE (2026-06-24) — 151-600B tier fully exhausted; >150B tier exhausted Pass 52fr (2026-06-30); 1-150B tier cold-triage resumed Pass 52fs (2026-06-30): 82 unnamed remain in-region. Formal park unaffected by opportunistic cross-region passes since (Pass 33/47/51 addenda) — see bottom of file for the latest (PASS 52ge, 2026-06-30).

## Overview

Region 0x80040000-0x8004ffff (64 KiB address range):
- **Total functions**: 319 (307 unnamed + 12 thin-named)
- **Already documented**: ~40+ functions (eSCO/SCO and LE Meta Event clusters)
- **Remaining triage**: 269+ unnamed functions requiring decompile + purpose classification

## Already High-Confidence Functions

These functions have been fully documented in prior Phase 9 thematic passes and are anchors for understanding the region's structure:

### eSCO/SCO Connection Type Dispatch & Feature Management (~10 functions)

| Address | Function | Size | Purpose | Ref |
|---------|----------|------|---------|-----|
| 0x80050810 | `FUN_80050810` | ~200B | Connection type dispatcher (0-3 types, eSCO/SCO capability negotiation) | rom/reverse_engineering_conn_type_dispatch_and_esco.md |
| 0x800506ac | `FUN_800506ac` | ~180B | Type 0 handler (new SCO/eSCO initiation, slot allocation) | rom/reverse_engineering_conn_type_dispatch_and_esco.md |
| 0x8004e670 | `FUN_8004e670` | ~80B | Type 1 handler (accept/mirror connection, copy caps) | rom/reverse_engineering_conn_type_dispatch_and_esco.md |
| 0x8004e6f4 | `FUN_8004e6f4` | ~120B | Type 2 handler (renegotiate, adjust caps) | rom/reverse_engineering_conn_type_dispatch_and_esco.md |
| 0x8004e76c | `FUN_8004e76c` | ~90B | Type 3 handler (restore/reject, force eSCO-only) | rom/reverse_engineering_conn_type_dispatch_and_esco.md |
| 0x80044730 | `FUN_80044730` | ~100B | eSCO packet-type table processor (codec selection, validity check) | rom/reverse_engineering_conn_type_dispatch_and_esco.md |
| 0x80052c64 | `FUN_80052c64` | ~600B | Feature page hash-bucket refcounting + capability commit | rom/reverse_engineering_conn_feature_dispatch.md |

### LE Meta Event Sender Cluster (~30+ functions, 0x80044730–0x80046620+)

Confirmed in rom/reverse_engineering_ble_link_layer.md:
- `send_evt_HCI_Connection_Complete` and Enhanced variant (full spec, 1032B decompile)
- `send_evt_Meta_subevent_01_LE_Advertising_Report` (332B)
- `send_evt_Meta_subevent_0x0A_LE_PHY_Update_Complete`
- `send_evt_Meta_subevent_LE_Secure_Connections_P256_*`
- Additional 10+ Meta-event senders covering BLE advertisement, channel selection, DHKey generation

**Implication**: ~40 functions in this region already at high confidence (decompiled + documented). Remaining ~269 functions require triage.

## Thin-Named Functions (12 entries, PASS 2 TRIAGE TARGETS)

All 12 confirmed from `rom_function_index.md` (0x80040000-0x8004ffff range, low/medium confidence):

| Address | Size | Name | Category |
|---------|------|------|----------|
| `0x80041c18` | 64B | `fHCI_Exit_Periodic_Inquiry_Mode_0x04` | HCI command handler |
| `0x80042188` | 634B | `assoc_w_tLC_RX` | Link Controller RX handler (protocol dispatch) |
| `0x80042420` | 418B | `assoc_w_tLC_TX` | Link Controller TX handler (protocol dispatch) |
| `0x80042a14` | 18B | `check_new_power_val!=0` | Power control checker |
| `0x80042a28` | 16B | `check_if_at_max_power_(6)` | Power control limit check |
| `0x80042a3c` | 22B | `increment_new_power_val_if_<_6` | Power control incrementer |
| `0x80042a58` | 16B | `increment_new_power_val_if_!=_0` | Power control incrementer (alt) |
| `0x80042b38` | 62B | `return_RSSI` | RSSI value getter |
| `0x80043810` | 102B | `called_by_fHCI_Remote_Name_Request_3` | Remote name request helper |
| `0x80044430` | 90B | `OGC_3_default_func_3` | OGF=3 command default handler |
| `0x8004a71c` | 16B | `VSC_0xfc95_called1` | VSC 0xFC95 helper |
| `0x8004c0f4` | 472B | `LMP__26F__sends_LE_HCI_Events` | LMP opcode 0x26F with LE HCI event sender |

**Pass 2 action**: Decompile all 12 via `BatchDecompileList.java` (arguments: comma-separated addresses above). Rename via `RenameBatch1.java` once purposes confirmed.

## Unnamed Functions (307 entries)

Cold-triage candidates identified from region structure:
- **Connection-type dispatch lower half** (0x80040000–0x80045000): allocation, slot scheduling, timing
- **LE Meta Event upper half** (0x80046000–0x8004ffff): higher-level link-layer event routing, secondary handlers

## Enumeration Strategy (Pass 1)

**Step 1**: Run `ListRegion0x80040000.java` (GZF process mode) to enumerate all 319 functions by size/name/type.

**Step 2**: Cross-reference output against:
- rom_function_index.md (confidence flags, existing names)
- rom/reverse_engineering_ble_link_layer.md (Meta-event cluster boundaries)
- rom/reverse_engineering_conn_feature_dispatch.md (feature dispatch cluster)

**Step 3**: Identify next priority batch for decompile:
- 12 thin-named functions (highest-ROI, already named by Kovah)
- Largest cold-named functions in each sub-region (lowest addresses, connection-setup path)

## Next Steps (Self-Chaining)

If enumeration script completes successfully:

1. **Pass 2 (Decompile Batch)**: Run `BatchDecompileList.java` on 12 thin-named + top 5 largest unnamed
2. **Pass 2 (Rename)**: Use `RenameBatch1.java` to upgrade thin-named → medium confidence + give names to top unnamed
3. **Pass 3+ (Triage Loop)**: Continue region sweep until all 269 unnamed reach medium+ confidence
4. **Final**: Update rom_function_index.md + analysis/INDEX.md; mark region [DONE] when all functions are high-confidence or explicitly marked non-real

---

## Tool Notes

- `ListRegion0x80040000.java`: Generic template (reuse of `ListRegion0x80020000.java` logic) — no stdout truncation expected for 319 functions (fits under ~30KB limit)
- GZF process mode: Renames made in prior passes (0x80010000, 0x80020000, 0x80030000, 0x80000000) persist; confirm persistence here too
- Timeline: FAST enumeration pass target = 8–10 minutes (script only, no decompiles)

## Coverage Progress

| Metric | Current | Target |
|--------|---------|--------|
| Region total functions | 336 (recount at Pass 3) | 336 |
| Already high-confidence | ~55 (16.4%, +3 from Pass 3/3-continuation) | 336 (100%) |
| Thin-named (decompile pending) | 0 (Pass 2 complete) | 0 (all high/medium) |
| Unnamed (cold triage) | ~281 (83.6%; 30 functions triaged across Pass 3 + continuation: 3 renamed HIGH, 27 at MEDIUM/MEDIUM-HIGH and left as FUN_*) | 0 (all named) |

---

## Pass 2 — Batch Decompile (2026-06-22)

**Prepared batch for execution** (awaiting supervisor/wairz MCP invocation):

### Step 1: Enumerate Functions (confirm 319 count)
```
script_file_id: ListRegion0x80040000
binary_path: 2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
use_saved_project: True
```

### Step 2: Decompile 12 thin-named (HIGH-ROI batch)
```
script_file_id: BatchDecompileList
script_args[0]: 0x80041c18,0x80042188,0x80042420,0x80042a14,0x80042a28,0x80042a3c,0x80042a58,0x80042b38,0x80043810,0x80044430,0x8004a71c,0x8004c0f4
binary_path: 2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
use_saved_project: True
timeout: 120
```
**Expected output**: Full decompilation + callee analysis for each function; saves to GZF project (renames persist).

### Step 3: Rename Functions (upgrade to high confidence)
Once decompiles confirm purposes, execute:
```
script_file_id: RenameBatch1
script_args[0]: (See "Rename mapping" section below after decompile)
binary_path: 2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
use_saved_project: True
```

### Rename Mapping (post-decompile, TBD)
To be filled in after `BatchDecompileList.java` output is reviewed.

---

## Pass 2 — Batch Decompile EXECUTED (2026-06-23)

All 12 thin-named functions decompiled successfully via 3 split batches
(`BatchDecompile80040000_B1/B2/B3.java`, 4 functions each — split up-front per
the 0x80050000 Pass 3c log-truncation lesson; each batch's log stayed well
under the 100KB single-window limit). All 12 reached **HIGH confidence** and
were renamed via an updated `RenameBatch1Region80040000.java`.

### Findings summary

| Address | Old name | New name | Confidence basis |
|---|---|---|---|
| `0x80041c18` | `fHCI_Exit_Periodic_Inquiry_Mode_0x04` | *(unchanged)* | Confirmed: clears 4 state fields + EIR ptr/len, calls 2 helper fns — matches HCI Exit Periodic Inquiry Mode semantics |
| `0x80042188` | `assoc_w_tLC_RX` | `LC_event_RX_dispatcher` | Confirmed: ~15-case switch on Link-Controller RX opcode field, calls many already-known helpers (`send_evt_Meta_subevent_0x11`, etc.) |
| `0x80042420` | `assoc_w_tLC_TX` | `LC_event_TX_dispatcher` | Confirmed: ~14-case switch on LC TX opcode field, incl. interrupt-disable + free-list teardown loop |
| `0x80042a14` | `check_new_power_val!=0` | `check_new_power_val_nonzero` | Trivial nonzero check, cosmetic rename only |
| `0x80042a28` | `check_if_at_max_power_(6)` | `check_power_val_below_max_limit_6` | Confirmed comparison against configured max-limit byte |
| `0x80042a3c` | `increment_new_power_val_if_<_6` | `increment_power_val_if_less_than_6` | Confirmed clamp-then-increment |
| `0x80042a58` | `increment_new_power_val_if_!=_0` | `decrement_power_val_if_nonzero` | **Correction**: decompile shows `param - 1`, i.e. decrement, not increment as Kovah's original guess had it |
| `0x80042b38` | `return_RSSI` | `return_RSSI_value` | Confirmed: optional HW-hook fn-pointer override, else config-struct-derived RSSI formula |
| `0x80043810` | `called_by_fHCI_Remote_Name_Request_3` | `remote_name_request_feature_index_selector` | Confirmed: selects feature/connection index (0-0xff, 0xff = none) based on config flags + feature-page state |
| `0x80044430` | `OGC_3_default_func_3` | `LMP_PDU_0xc6d_feature_page_bit_toggle` | Confirmed: handles only LMP opcode 0xc6d (toggles 2 feature-page bits); 0xc6c returns success, anything else returns error 0x12 — not a generic "default" stub |
| `0x8004a71c` | `VSC_0xfc95_called1` | `VSC_0xfc95_clear_bit_helper` | Confirmed: single-bit-clear helper on a 16-bit mask |
| `0x8004c0f4` | `LMP__26F__sends_LE_HCI_Events` | `LMP_opcode_0x26F_LE_event_router` | Confirmed: 11-case switch routing to the already-documented LE Meta Event sender cluster — this is the central dispatcher feeding that cluster |

**Net effect**: all 12 thin-named functions upgraded low/medium → **HIGH
confidence**. One factual correction (`0x80042a58`: increment → decrement).
Two functions (`0x80042188`, `0x80042420`) confirmed as genuine LC-layer
RX/TX dispatchers, tying the eSCO/SCO connection-type cluster together with
the broader Link Controller event-handling machinery. `0x8004c0f4` confirmed
as the dispatcher feeding the already-documented LE Meta Event cluster.

Full decompiled C for all 12 functions is preserved in the Ghidra run logs
(see `mcp__wairz__list_ghidra_logs` for `BatchDecompile80040000_B1/B2/B3.java`
and `RenameBatch1Region80040000.java` runs, 2026-06-23).

## Pass 3 — Cold-Triage + Batch Decompile EXECUTED (2026-06-23)

`ColdTriageRegion80040000Pass3.java` was run in GZF process mode against the
live project (336 total functions counted at this point, 305 unnamed —
counts drift slightly run-to-run as earlier passes' renames land). Size-tier
buckets: 1-50B:89, 51-150B:117, 151-300B:49, 301-600B:32, 601+B:18. Half
split: lower (0x80040000-0x80044fff) 90 unnamed, upper (0x80045000-0x8004ffff)
215 unnamed.

**Top-15 candidates per half** (size, descending):

Lower half: `0x80043e04`(1168B), `0x80040a24`(988B), `0x8004147c`(934B),
`0x80041dac`(876B), `0x80040594`(566B), `0x80041230`(560B), `0x80042640`(530B),
`0x800401c4`(426B), `0x80040e60`(386B), `0x80041900`(376B), `0x80043c7c`(372B),
`0x80043a60`(358B, xrefs:15 — notably high), `0x80041a94`(352B),
`0x800435a8`(338B), `0x80041028`(336B).

Upper half: `0x8004d8b8`(1898B), `0x80049d20`(1476B), `0x8004d294`(1280B),
`0x8004ce70`(908B), `0x8004c4a8`(894B), `0x800483c0`(866B), `0x80047628`(832B),
`0x80047304`(780B), `0x8004cb48`(722B), `0x80047c50`(700B), `0x8004966c`(696B),
`0x80046900`(682B), `0x800480b0`(682B), `0x8004b468`(624B), `0x80045964`(560B).

**Decompiled this pass** (top-4 from each half, 8 functions total, via
`BatchDecompile80040000Pass3Lower.java`/`Lower4.java` (split after the 4th
function hit the log-truncation issue again — same lesson as 0x80050000
Pass 3c) and `BatchDecompile80040000Pass3UpperA.java`/`UpperB.java` (2+2,
pre-split to avoid the same issue)). All 8/8 decompiled successfully.

### Findings summary

| Address | Size | Behavior | Confidence |
|---|---|---|---|
| `0x8004d8b8` | 1898B | Global BT-state/connection-table initializer: `memset`s the entire `PTR_base_of_0x1ac_struct_array_0xA_large2` array (the project's established 11-entry `big_ol_struct` connection-record array), then loops `uVar14 < 0xb` setting default LST `0xa0a` (same constant `init_connection_record`/0x8005b9d8 uses), default poll intervals, BD_ADDR/feature fields from `config_struct`, and calls sub-initializers `init_0x58_stride_conn_record_ptr_table_11_slots`, `FUN_80058a34`, `FUN_80009774`, `FUN_8005c988`. Structurally the top-level/global counterpart to the already-named per-record `init_connection_record`. | **HIGH** — renamed `init_global_connection_table_and_bt_state` |
| `0x80049d20` | 1476B | Validates a packed parameter block (bandwidth/packet-type/retransmission-window fields with explicit range checks matching SCO/eSCO bounds, e.g. `0x3ffd`/`0xc7b`/`0xc77` window checks), writes results into `big_ol_struct` SCO/eSCO fields at offsets `+0x1a6..+0x1ce`, and terminates with `send_evt_HCI_Command_Status(*param_1, status)` — the canonical "this is an HCI command handler" signature. Parameter shape matches HCI Setup/Accept Synchronous Connection. | **HIGH** — renamed `HCI_Setup_Synchronous_Connection_handler` |
| `0x80043e04` | 1168B | `program_dual_slot_lmp25c_packet_credits_by_conn_index` — IRQ-off dual-slot LMP-25C packet-credit programmer on 0x84-stride role records; see Pass 52da | **HIGH** |
| `0x80040a24` | 988B | `process_dual_slot_lmp25c_role_record_packet_completion` — dual-slot role-record LMP-25C packet completion handler; see Pass 52db | **HIGH** |
| `0x8004147c` | 934B | `program_inquiry_or_esco_baseband_from_hci_command` — HCI inquiry/cancel/SCO-setup baseband programmer (fptr `fptr_DAT_80036f5c` for opcodes `0x401`/`0x419`/`0x43f`); programs BD_ADDR halves, access-code sync word, clock offset, role/AM_ADDR at BB reg `0xaa`, channel-table entries, clears role-switch hook; optional veto callback before arming. Renamed Pass 52dc. | HIGH |
| `0x80041dac` | 876B | `teardown_inquiry_lap_slot_baseband_cleanup_and_release` — inquiry/LAP slot teardown orchestrator on `param_1`-indexed `big_ol_struct`; IRQ-off baseband register teardown, clears role-switch hook, releases inquiry LAP pending bitmask, clears AFH channel map, bitmask cleanup; callers `fHCI_conn_req_cancel` + `connection_teardown_HCI_event_finalizer`. Renamed Pass 52dd. | HIGH |
| `0x8004d294` | 1280B | `init_or_reset_sco_esco_hw_registers_and_link_slots` — SCO/eSCO HW register init/reset blob; dual-mode on `param_1` (full config-driven programming + 64+64 link-register-B clears + 11 eSCO slot clears when zero); see Pass 52dw | HIGH |
| `0x8004ce70` | 908B | `dispatch_conn_tx_by_packet_type_nibble_with_reassembly` — conn TX packet-type dispatcher; type-0 multi-chunk reassembly via `walk_tx_reassembly_buffer_*` + `walk_conn_tx_segments_dispatch_by_packet_type_nibble`; type-1 single-chunk + clock-offset check; type-4 LE accumulate via `walk_le_tx_segments_validate_slot10_clock_offset_and_return_count`; see Pass 52dx/52ea/52eb | HIGH |
| `0x8004a730` | 456B | `walk_le_tx_segments_validate_slot10_clock_offset_and_return_count` — type-4 LE TX segment consumer; walks length-prefixed segments, validates slot-10 clock-offset fields, returns segment count; callee of `dispatch_conn_tx_by_packet_type_nibble_with_reassembly`; see Pass 52ea | HIGH |
| `0x8004ae74` | 452B | `walk_conn_tx_segments_dispatch_by_packet_type_nibble` — type-0 multi-chunk TX segment walker; dispatches each segment by packet-type nibble via `PTR_PTR_8004b044` table, fallback to `compute_length_prefixed_segment_advance_clamped_to_remainder`; callee of `dispatch_conn_tx_by_packet_type_nibble_with_reassembly`; see Pass 52eb | HIGH |
| `0x8004ab64` | 414B | `gate_ext_adv_param_bitfields_and_apply_via_vsc_0xfc97` — validates ext-adv param dword bitfields against per-link status bytes, commits power-state via `commit_link_power_state_bits_to_hw_register_with_retry`, applies via `VSC_0xfc97_Set_Extended_Advertising_Parameters_variant_2` sub-id `0xe`; see Pass 52ec | HIGH |
| `0x80045000` | 358B | `hci_finalize_conn_type_timing_delta_send_cmd_complete` — conn-type timing-counter finalize + 6-byte HCI Command Complete; computes `field241`−`field245` delta (type≠2) or overflow check (type 2); clears `_x02_byte_0x1ac_index` + global HW status bitfields; see Pass 52ef | HIGH |

**Net effect this pass**: 2 of 8 decompiled candidates renamed to HIGH
confidence (`init_global_connection_table_and_bt_state`,
`HCI_Setup_Synchronous_Connection_handler`); the remaining 6 documented above
at MEDIUM/MEDIUM-HIGH and left as `FUN_*` for a future pass to pursue
(xrefs_to/find_callers tooling against this GZF's process-mode project was
attempted but the binary path used by those tools does not resolve against
the GZF process-mode cache — see project tooling notes; confidence here was
established via decompile content + struct/constant cross-confirmation
against already-named functions instead, consistent with the 0x80050000
Pass 3b/3c precedent).

Full decompiled C for all 8 functions is preserved in the Ghidra run logs
(`BatchDecompile80040000Pass3Lower.java`, `...Lower4.java`,
`...UpperA.java`, `...UpperB.java`, plus `RenameBatch1Region80040000Pass3.java`,
all 2026-06-23).

### Remaining top-15 candidates not yet decompiled (future pass)

Lower half: `0x80040594`, `0x80041230`, `0x80042640`, `0x800401c4`,
`0x80040e60`, `0x80041900`, `0x80043c7c`, `0x80043a60` (xrefs:15, worth
prioritizing next), `0x80041a94`, `0x800435a8`, `0x80041028`.

Upper half: `0x8004c4a8`, `0x800483c0`, `0x80047628`, `0x80047304`,
`0x8004cb48`, `0x80047c50`, `0x8004966c`, `0x80046900`, `0x800480b0`,
`0x8004b468`, `0x80045964`.

---

## Pass 3 continuation — Remaining 22 top-15 candidates EXECUTED (2026-06-23)

All 22 remaining top-15-per-half candidates (11 lower + 11 upper) decompiled
successfully via 9 small batches (`BatchDecompile80040000Pass3RemA`
through `...RemI.java`, 2-3 functions each, per the established
log-truncation-avoidance practice). `0x80043a60` (xrefs:15) was prioritized
first as instructed.

### Findings summary — lower half (11 functions, none reached HIGH)

| Address | Size | Behavior | Confidence |
|---|---|---|---|
| `0x80043a60` | 358B | High xref count (15) for its size tier but decompile shows a fairly generic field-update/state-transition helper; no single confirmed call site distinguishes its purpose precisely. | MEDIUM |
| `0x80040594` | 566B | `build_and_submit_sco_esco_lmp_pdu_for_conn_type_1_or_2` — SCO/eSCO LMP PDU builder/submitter for conn-type nibble 1–2; see Pass 52de | HIGH |
| `0x80041230` | 560B | `dequeue_conn_event_ring_and_dispatch_lmp_by_conn_type_nibble` — IRQ-masked 16-slot conn-event ring dequeue + type-nibble LMP dispatch; see Pass 52df | HIGH |
| `0x80042640` | 530B | `select_sco_esco_packet_type_and_cap_window_by_conn_index` — SCO/eSCO packet-type selector from `field273_0x250` feature flags + window threshold; see Pass 52dg | HIGH |
| `0x800401c4` | 426B | `commit_dual_slot_lmp25c_role_record_state_and_chain_credits` — IRQ-off dual-slot LMP-25C role-record state commit; see Pass 52dh | HIGH |
| `0x80040e60` | 386B | `accept_dual_slot_lmp_role_connection_and_program_baseband_regs` — IRQ-off dual-slot LMP role connection accept + baseband programmer; see Pass 52di | **HIGH** |
| `0x80041900` | 376B | `program_page_train_baseband_regs_and_start_paging` — HCI Create Connection page-train BB programmer; see Pass 52dj | **HIGH** |
| `0x80043c7c` | 372B | `compute_automatic_flush_timeout_ticks_by_connection_handle` — ACL automatic-flush-timeout tick calculator; see Pass 52dk | **HIGH** |
| `0x80041a94` | 352B | `configure_periodic_inquiry_lap_delays_baseband_and_arm_lmp` — HCI Periodic Inquiry Mode configure handler; programs LAP/min/max delays, access-code sync word, baseband regs, LMP 0x25B/0x268; see Pass 52eg | HIGH |
| `0x800435a8` | 338B | `reassign_inquiry_lap_slot_refcount_pending_and_program_channel` — IRQ-off LAP slot transition; decrements old-slot `_x142_LAP[slot+0x45]` refcount, releases pending bitmask, optional HW teardown; increments new-slot refcount, arms pending bitmask per `bdaddr_random_`, programs HW channel table; see Pass 52eh | HIGH |
| `0x80041028` | 336B | `accept_lmp_conn_setup_and_program_baseband_from_unpacked_pdu` — IRQ-masked LMP connection-setup BB programmer; unpacks PDU 6-byte field, programs BB regs + access-code sync word; see Pass 52ei | HIGH |

**Net effect (lower half)**: no renames this pass — all 11 stay at MEDIUM,
documented above for a future targeted pass (likely needs working
`xrefs_to`/`find_callers` against this GZF to push further, since the
decompiled shapes are individually plausible but not distinguishable from
each other on content alone).

### Findings summary — upper half (11 functions, 1 reached HIGH)

| Address | Size | Behavior | Confidence |
|---|---|---|---|
| `0x8004c4a8` | 894B | Large handler, shape and field-touches not yet isolated to one specific purpose. | MEDIUM |
| `0x800483c0` | 866B | `program_baseband_link_setup_slot10_and_send_hci_cmd_complete` — validates eSCO-style params (`param_2≤0x27`, `param_4≤7`), programs conn-record slot 10 fields `+0xf4..+0x106`, dispatches on packet-mode 1–4 to program HW regs, calls `compute_indexed_table_addr_by_category_and_bank`, derives slot count via `0x271` divisor, optional veto hook; terminates via `hci_event_sender(0xe,…)` Command Complete. See Pass 52dz | HIGH |
| `0x80047628` | 832B | `reassemble_lmp_pdu_fragments_into_subrecord_chain_with_role_bit_set` — HCI/LMP fragment-reassembly handler (role-bit-set variant); conn `+0x2c` length accumulator; `find_tail_of_payload_subrecord_chain_at_field0x50` + `setup_type3_esco_sco_conn_record_with_role_bit_set`; sibling of `reassemble_lmp_pdu_fragments_into_subrecord_chain_with_role_bit_clear`. See Pass 52do | HIGH |
| `0x80047304` | 780B | `reassemble_lmp_pdu_fragments_into_subrecord_chain_with_role_bit_clear` — HCI/LMP fragment-reassembly handler (role-bit-clear variant); conn `+0x2c` accumulator; `find_tail_of_payload_subrecord_chain_at_field0x50_0x24` + `setup_type3_esco_sco_conn_record_with_role_bit_clear`; completion walks `+0x20` chain via `prepend_payload_subrecord_to_pending_lists_if_low3bits_set`; sibling of `reassemble_lmp_pdu_fragments_into_subrecord_chain_with_role_bit_set`. See Pass 52dp | HIGH |
| `0x8004cb48` | 722B | `program_esco_slot_from_lmp0x22_negotiation_pdu_and_emit_0x26f` — eSCO slot programmer for LMP PDU type `0x22`; `esco_sco_param_negotiate_and_stage` + 0x1ac-stride field writes; LMP `0x26f` emit + VSC fc95 trigger. See Pass 52dq | HIGH |
| `0x80047c50` | 700B | `parse_validate_and_commit_esco_sco_config_pdu_to_conn_record` — 28-byte eSCO/SCO config PDU validator+committer; LMP VSC 0x268 pool slot `0x8010bc64`; `alloc_link_record_and_register_by_index` + `dispatch_hw_hook_for_main_and_substates`. See Pass 52dr | HIGH |
| `0x8004966c` | 696B | `undefined1 FUN_8004966c(...)`: validates SCO/eSCO bandwidth/packet-type/retransmission-window params using nearly identical bounds checks to the already-confirmed `HCI_Setup_Synchronous_Connection_handler` (0x80049d20), writes into `get_0x1ac_struct_ptr_by_index`-addressed connection-record fields, and terminates via `send_evt_HCI_Command_Status` on every path — the same "this is an HCI command handler" signature as its sibling. Parameter shape and termination pattern match HCI Accept Synchronous Connection Request. | **HIGH** — renamed `HCI_Accept_Synchronous_Connection_Request_handler` |
| `0x80046900` | 682B | `validate_and_stage_sco_packet_type_table_from_hci_params` — multi-entry SCO packet-type table HCI param validator; 3-bit packet-type mask; `lazy_alloc_tag9_singleton_and_encode_lowbit_index` + `align_sco_slots_and_derive_retx_buffer_dims` per entry. See Pass 52ds | HIGH |
| `0x800480b0` | 682B | `validate_and_stage_sco_air_mode_change_from_hci_command` — HCI SCO air-mode change validator+stager; see Pass 52dt | HIGH |
| `0x8004b468` | 624B | `fragment_conn_tx_overflow_chain_into_hw_descriptor_slots_by_budget` — IRQ-off snapshot of overflow queue at `conn+0x128..0x131`, walks `+0x100` chain, allocates HW descriptor slots, fragments TX bytes within budget, recycles or enqueues via list-A; see Pass 52du | HIGH |
| `0x80045964` | 560B | `validate_and_commit_hci_scan_activity_params_from_command` — HCI scan-activity interval/window validator+committer; bounds `0x1f..0x4000`, bit-packs into global BT-state `+0x28..0x2f`, `hci_event_sender(0xe,...)` Command Complete; see Pass 52dv | HIGH |
| `0x8004d294` | 1280B | `init_or_reset_sco_esco_hw_registers_and_link_slots` — SCO/eSCO HW register init/reset blob; programs ~40 HW globals, calls `set_masked_nibble_field_on_sco_esco_hw_register(7,7)` + `set_hw_control_flag_bit6(1)`; full-reset path (`param_1==0`) copies config into BT-state, clears 128 link-register-B slots + 11 eSCO nibbles; callee of HCI-Reset teardown chain; see Pass 52dw | HIGH |

**Net effect (upper half)**: 1 of 11 renamed to HIGH confidence
(`HCI_Accept_Synchronous_Connection_Request_handler`, sibling to the
already-named `HCI_Setup_Synchronous_Connection_handler`); 5 documented at
MEDIUM-HIGH (strong structural evidence — cluster membership or near-identical
sibling shape — but lacking a distinguishing confirmation), 1 at MEDIUM
(internal scheduler/queue infra, no HCI-handler signature), and 4 more at
MEDIUM/MEDIUM-HIGH per the table above.

**Tooling note (re-confirmed, not re-investigated)**: `xrefs_to` and
`find_callers` continue to fail with "Binary not found" against this GZF in
process mode, consistent with the previously-flagged wairz tooling gap. All
confidence assessments this pass were made via decompile-content +
struct/constant cross-confirmation only.

Full decompiled C for all 22 functions is preserved in the Ghidra run logs
(`BatchDecompile80040000Pass3RemA.java` through `...RemI.java`, plus
`RenameBatch80040000Pass3Cont.java`, all 2026-06-23).

### Region 0x80040000 Pass 3 status: top-15-per-half lists exhausted

Both halves' top-15 candidate lists (30 total, 8 from the initial Pass 3 batch
+ 22 from this continuation) are now fully decompiled and triaged. 3 functions
total reached HIGH confidence across the two batches
(`init_global_connection_table_and_bt_state`,
`HCI_Setup_Synchronous_Connection_handler`,
`HCI_Accept_Synchronous_Connection_Request_handler`); the remainder sit at
MEDIUM/MEDIUM-HIGH, which is an expected outcome given the known
`xrefs_to`/`find_callers` tooling gap against this GZF — those tools would
likely be needed to push several MEDIUM-HIGH candidates (especially the
near-identical sibling pairs `0x80047628`/`0x80047304` and the scan-activity
candidate `0x80045964`) over the HIGH bar. Pass 3 (top-15-per-half sweep) is
considered **complete**; a future pass should either (a) retry once the
xrefs tooling gap is resolved, or (b) continue cold-triaging the remaining
~280 unnamed functions in this region outside the top-15-per-half lists.

---

## Pass 4 — Cold-triage of remaining ~281 unnamed (outside top-15 lists) EXECUTED (2026-06-23)

`ColdTriageRegion80040000Pass4.java` re-ran the cold-triage, explicitly
excluding the 30 addresses already triaged in Pass 3 + continuation, and
added in-script xref counts (using `ReferenceManager.getReferencesTo`, which
works fine from inside a Ghidra script context — distinct from the MCP
`xrefs_to`/`find_callers` wrapper tools, which still fail with "Binary not
found" against this GZF in process mode; this script-side query sidesteps
that specific gap for ranking purposes only, not as a general fix).

Result: 275 unnamed functions remain outside the top-15 lists (size tiers:
89 in 1-50B, 117 in 51-150B, 49 in 151-300B, 20 in 301-600B, 0 above 600B —
the >600B tier is now fully exhausted by Pass 3's top-15 sweep). Ranking the
151-600B tier (69 functions) by xref count then size produced a short list
of high-value candidates topped by `0x8004090c` (246B, 11 xrefs),
`0x8004ca7c` (192B, 8 xrefs), `0x8004f580` (314B, 7 xrefs), `0x8004bde8`
(354B, 6 xrefs), `0x800431a0` (184B, 5 xrefs).

**Decompiled this pass** (top-3 by xref count, via `BatchDecompile80040000Pass4A.java`):

| Address | Size | xrefs | Behavior | Confidence |
|---|---|---|---|---|
| `0x8004090c` | 246B | 11 | `void FUN_8004090c(void)`: disables interrupts, calls `look_for_non_matching_bdaddr_bos_index_i_e__free_connection_slot` to find a free/mismatched connection slot, reads a per-slot byte field from `big_ol_struct`, conditionally calls `FUN_80043158` (lookup-table indexed by that byte) and `LMP__259__FUN_800702e4`, then conditionally calls `possible_logger_called_if_no_patch3` (event-ish call with const `600`) before re-enabling interrupts. Looks like a connection-slot mismatch/cleanup handler tied into the LMP_259 event path, but no single confirmed purpose distinguishes it from a generic "BD_ADDR slot reconciliation" helper. | MEDIUM-HIGH |
| `0x8004ca7c` | 192B | 8 | `void FUN_8004ca7c(uint param_1, uint param_2)`: indexes the established `big_ol_struct`-family connection-record array by `param_1` (conn index), gated on flag bit 0 of `field3_0x3`. When `param_2==0`, clears the 4 already-confirmed RSSI/link-quality rolling counters (`field70_0x46`/`field76_0x4c`/`field74_0x4a`/`field68_0x44` — "good"/"bad-class-A"/"bad-class-B"/"marginal", per the already-documented `conn_rssi_quality_history_update`), copies the RSSI field (`field40_0x28`) into `field72_0x48`, then calls `VSC_0xfc95_called2`. The `param_2!=0` branch does the analogous reset on a sibling pair of fields and, for `param_2==2`, sets a flag byte (`field135_0x8e`). Both branches converge on the established VSC_0xfc95 triad (`LMP__25B__most_common_for_VSCs1`/`VSC_0xfc95_called2`/`LMP__268__most_common_for_VSCs2_checks_fptr_patch`) — the exact same 3-function call pattern documented gating the `link_state6_afh_or_channel_feature_toggle1/2/3` siblings in region 0x80000000. The combination of (a) touching all 4 independently-confirmed quality-history fields together and (b) the confirmed VSC_0xfc95 triad gives two independent cross-confirmations. | **HIGH** — renamed `conn_link_quality_history_reset_and_vsc_0xfc95_trigger` |
| `0x8004f580` | 314B | 7 | `undefined4 FUN_8004f580(int *param_1)`: classic intrusive doubly-linked-list insert, sorted by a wraparound-masked delta key (`DAT_8004f6c4`/`c8` masks), inserting into one of two lists selected by a bit in `*(byte*)(param_1+2)` (with a third early-return path logging via `possible_logging_function__var_args` when `param_1[1]!=0`). No HCI-handler signature, no VSC/LMP call — pure internal data-structure helper (looks like the generic timer/event-queue insertion primitive used elsewhere in the firmware). | MEDIUM (clear generic-infra purpose, not specific enough for a precise verb-noun name without knowing which timer/queue subsystem calls it) |

**Net effect this pass**: 1 of 3 decompiled candidates renamed to HIGH
confidence (`conn_link_quality_history_reset_and_vsc_0xfc95_trigger`); the
other 2 documented above at MEDIUM/MEDIUM-HIGH and left as `FUN_*`.

**Tooling note (re-confirmed, not re-investigated)**: the MCP
`xrefs_to`/`find_callers` tools still fail with "Binary not found" against
this GZF in process mode. This pass's xref counts came from an in-script
`ReferenceManager` query inside `ColdTriageRegion80040000Pass4.java` itself
(which runs fine, since it executes inside the live Ghidra script context
rather than going through the separate MCP wrapper) — this is a usable
workaround for ranking purposes within a single script run, not a fix for
the underlying MCP tool gap, which remains open.

Full decompiled C for all 3 functions is preserved in the Ghidra run log for
`BatchDecompile80040000Pass4A.java` (2026-06-23).

### Region 0x80040000 Pass 4 status: 272 unnamed functions remain untriaged

After this pass, 272 of the original ~281 "outside top-15" unnamed functions
remain completely untouched (3 were decompiled this pass: 1 renamed, 2
parked at MEDIUM/MEDIUM-HIGH). The remaining 151-600B tier still has 66
ranked-but-undecompiled candidates (next up: `0x8004bde8` at 354B/6 xrefs,
`0x800431a0` at 184B/5 xrefs, `0x8004ef08` at 526B/4 xrefs, `0x8004e5ac` at
188B/4 xrefs, `0x80040060` at 184B/4 xrefs, `0x8004326c` at 166B/4 xrefs,
`0x8004b3c0` at 162B/4 xrefs — see `ColdTriageRegion80040000Pass4.java`'s
full ranked output for the complete list). The 1-150B tiers (206 functions)
remain essentially unexplored — likely diminishing returns for HIGH-confidence
renames given their size (less code = less distinguishing behavior to
cross-confirm), but a future pass could still spot-check the highest-xref
ones in that tier if the mid-tier list gets exhausted first.

---

## Pass 5 — Continue 151-600B tier cold-triage EXECUTED (2026-06-23)

Re-ran `ColdTriageRegion80040000Pass4.java` for a fresh ranked list (now 274
unnamed outside top-15 lists; counts shifted slightly from Pass 4's run due
to the 3 renames already applied: 89/117/48/20/0 across tiers 1-5). Decompiled
the next 7 ranked 151-600B candidates via two small batches
(`BatchDecompile80040000Pass5A.java`, 3 functions; `...Pass5B.java`, 4
functions). Two candidates from the originally-planned priority list
(`0x8004e5ac`, `0x800431a0`) were skipped because they already have
substantial prior analysis elsewhere — `0x8004e5ac` is fully documented in
`reverse_engineering_conn_record_subsystem.md` §11 ("hardware sub-object
hierarchy teardown" / handle-table deregister), and `0x800431a0` is named as
a callee of `apply_codec_type_and_role_switch_hook_dispatch` in
`reverse_engineering_region_0x80000000.md` — both still show as `FUN_*` in
this GZF (not yet formally renamed in Ghidra) but are not "untriaged."

| Address | Size | xrefs | Behavior | Confidence |
|---|---|---|---|---|
| `0x8004bde8` | 354B | 6 | `uint FUN_8004bde8(byte*,uint,uint,char,u4,u4,u1,char)`: validates a buffer-size/header budget (`*pbVar7 * 2 + 6 + header_len`) with a logging fallback on underflow, checks a minimum-length gate against `param_3`, then calls `FUN_80056988` (payload extraction), an optional callback via `PTR_PTR_8004bf54`/`FUN_8005001c`, a function-pointer-table dispatch indexed by `*param_1 & 0xf` (opcode-style nibble), an optional `FUN_800530a0` forwarding call, and finally branches between `FUN_8004bc74`+`FUN_8004ba34` or `FUN_80050f7c`+`FUN_80052f38` depending on a "got an answer" flag and a state byte. Generic packet/PDU-dispatch-and-forward shape (header parse → opcode-table callback → conditional response/forward), but no single HCI/LMP-specific terminator confirms a precise name. | MEDIUM |
| `0x8004ef08` | 526B | 4 | `void FUN_8004ef08(void)`: complex doubly-linked-list management over two lists rooted at `PTR_DAT_8004f118` (`+4`/`+8`/`+0x10` head pointers), with insert/unlink helpers (`FUN_8004ee94`, `FUN_8004ee50`) and a deadline/budget calculation (`uVar4 >> 1`, masked delta `DAT_8004f12c`). Structurally the **complement of `0x8004f580`** (Pass 4's "generic timer/event-queue insertion primitive," sorted by the same wraparound-masked delta key) — this looks like that queue's **dequeue/dispatch/requeue** routine: it processes one "in-flight" entry, then drains a pending list into an active list while budget remains, finally moving the next entry to in-flight. No HCI/LMP signature; pure internal scheduler infra. | MEDIUM-HIGH (cluster match with `0x8004f580`, but internal infra so no command-name target) |
| `0x80040060` | 184B | 4 | `bool FUN_80040060(int,int)`: calls the named `LMP__25C_called2()` (documented elsewhere as `lmp_25c_procedure_completion_waiter`, a busy-wait barrier for an in-flight LMP procedure) inside an interrupt-disabled critical section that computes a credit/window comparison (`(ushort << 1) < (uVar11 - iVar10)`) against a per-connection deadline (`*piVar9`), setting a state byte at `param_2+0x247` to 1 on success. Reads/clears a "coexistence" sentinel (`*piVar9 = -1`) and a 2-bit mode field at `param_1+1`. This is a **scheduling-readiness check gating the LMP-25C procedure**, consistent with the busy-wait barrier's caller-side credit/deadline test, but isn't distinguishable to a specific named HCI/LMP procedure without more context. | MEDIUM-HIGH |
| `0x8004326c` | 166B | 4 | `void FUN_8004326c(uint param_1)`: sweeps the `big_ol_struct` connection-record array (10 entries, skipping `param_1`), and for entries with the valid-entry flag set, reads a role-switch-hook-style byte (`byte_0xCC`) and a connection-index field, checks bit 0 of `PTR_DAT_80043318[idx+4]` (role-switch-hook bit pattern matching the documented `set/clear_bos_e4_role_switch_hook_bit` family), remaps the index `+8` into the eSCO range if a second gate table entry is `1`, then calls `FUN_80014450()` followed by `FUN_80034c5c(0x1c00, 0xc000, codec_table[idx])` — programming the connection's packet type to eSCO (`0x1c00`)/max-rate-SCO (`0xc000`), the exact same packet-type-constant pair used throughout the already-documented codec-type/role-switch cluster in `reverse_engineering_region_0x80000000.md` (`apply_codec_type_and_role_switch_hook_dispatch`, `role_switch_packet_type_reset_and_log`, etc.), then clears two per-index gate-table bytes to `0xff` (disabled sentinel) under interrupt-disable. This is a **connection-table-wide SCO/eSCO packet-type-and-role-switch-hook reset/teardown sweep**, structurally part of that same cluster, but the single confirming signal (an xref from a specific named caller) is unavailable due to the open `xrefs_to` tooling gap, so it's held at MEDIUM-HIGH rather than HIGH. | MEDIUM-HIGH |
| `0x8004b3c0` | 162B | 4 | `void FUN_8004b3c0(int*,uint,char,char)`: gated on a nonzero byte-count field (`param_1[2]`), under interrupt-disable, selects one of two `0x1ac`-strided connection-record field pairs (`field289_0x128`/`field403_0x1a0`, chosen by `param_4`) and performs a linked-list-style splice: depending on `param_3`, either appends `param_1`'s 3-word record onto the selected list's tail (unaligned 4-byte store sequence into `+0x100..0x103`) or prepends it, then accumulates the byte-count field. Generic intrusive-list append/prepend helper over per-connection field-pair lists — no HCI/LMP-specific signature, looks like shared list-splice infra (akin to the `0x8004f580`/`0x8004ef08` queue pair but operating on per-connection lists instead of a global one). | MEDIUM |
| `0x8004fd6c` | 226B | 3 | `void FUN_8004fd6c(byte,char,int)`: gated by `FUN_8004fcb8(param_3)` (an existence/validity check), then for `param_1<4` switches on `param_2-1` over a small per-index struct array (`PTR_DAT_8004fe60`, 7-byte stride): case 0 sets a 2-bit mode field and a bit in the struct, cases 1/3 clear a bit, case 4 sets a different bit and increments a shared counter byte, case 5 clears that bit and decrements the counter. Classic **enable/ref-count/disable state-machine setter** over a small fixed-size resource table (≤4 entries — plausibly SCO/eSCO channel slots given the `<4` bound seen elsewhere in this region), but no command-name-level confirmation available. | MEDIUM |
| `0x8004b898` | 194B | 3 | `undefined4 FUN_8004b898(uint,undefined1,char)`: bounds-checks `param_1-0x10 < 0xb` (an 11-value enum/index range), then under interrupt-disable checks a per-connection valid-entry bit and a 16-bit feature-capability mask (`field453_0x1d2`/`field454_0x1d3`, bit-indexed by `param_1-0x10`) before writing `param_2` into `field138_0x91`, setting a flag bit in `field136_0x8f`, and setting/clearing bit 0 of `field139_0x92` based on `param_3`; on success calls a function-pointer hook (`PTR_DAT_8004b960`) with `(index, param_2)`, then always logs via `possible_logging_function__var_args`. Shape matches a **feature-bit-gated per-connection parameter setter with HW-callback notification** (e.g. toggling a per-connection mode/parameter that requires both software state and a hardware-hook side effect), but the specific feature/parameter isn't identifiable without naming the bit positions in the capability mask. | MEDIUM-HIGH |

**Net effect this pass**: 0 of 7 decompiled candidates renamed to HIGH
confidence — all sit at MEDIUM/MEDIUM-HIGH. `0x8004326c` has the strongest
structural case (direct match to the already-documented packet-type/role-
switch-hook cluster's constant pair `0x1c00`/`0xc000`) but, consistent with
project policy, is held below HIGH without a definitive single-purpose
confirmation (a caller-side xref or a uniquely distinguishing constant). This
continues the pattern observed since Pass 3: the 151-600B tier yields mostly
structural/cluster-membership evidence rather than clean single-purpose
confirmations, which is expected given the open `xrefs_to`/`find_callers`
tooling gap against this GZF (still not re-investigated this pass; flagged
repeatedly in Pass 3/4, no change in status).

Full decompiled C for all 7 functions is preserved in the Ghidra run logs for
`BatchDecompile80040000Pass5A.java` and `...Pass5B.java` (2026-06-23).

### Region 0x80040000 Pass 5 status: 267 unnamed functions remain untriaged

After this pass, 267 of the 274 "outside top-15" unnamed functions identified
at the start of Pass 5 remain completely untouched. The 151-600B tier now has
59 ranked-but-undecompiled candidates remaining (next up by xref count:
`0x8004fd6c` and `0x8004b898` already covered this pass: the rest of the
xrefs:2 tier — `0x8004eb18` (404B), `0x8004f374` (368B), `0x8004f730` (230B),
`0x800442bc` (222B), `0x8004ea2c` (220B), `0x80043884` (210B), `0x8004c940`
(194B), `0x8004f25c` (186B), `0x80043984` (178B) — then the xrefs:1 tier
topped by `0x8004ba34` (534B), `0x8004a730` (456B), `0x8004ae74` (452B). The
1-150B tiers (206 functions) remain unexplored. Given 5 consecutive passes
(Pass 3/3-cont/4/5) have now consistently produced MEDIUM/MEDIUM-HIGH outcomes
in the absence of working `xrefs_to`/`find_callers`, a future pass should
either (a) continue the xrefs:2/1 tier for a few more easy-win attempts, or
(b) pivot to a different, less-explored region (e.g. 0x80060000 or
0x80070000) where fresh top-15-by-size sweeps may yield a better HIGH-rename
hit rate than this region's now-thoroughly-picked-over remainder.

## Pass 6 — Finish xrefs:2 tier EXECUTED (2026-06-23)

Batch-decompiled the remaining 9 xrefs:2-tier candidates in 3 small batches
(`BatchDecompile80040000Pass6A/B/C.java`, 3 each): `0x8004eb18` (404B),
`0x8004f374` (368B), `0x8004f730` (230B), `0x800442bc` (222B), `0x8004ea2c`
(220B), `0x80043884` (210B), `0x8004c940` (194B), `0x8004f25c` (186B),
`0x80043984` (178B).

| Address | Size | Behavior | Confidence |
|---|---|---|---|
| `0x80043884` | 210B | `uint FUN_80043884(void)`: gates on the exact same struct-field pattern already confirmed HIGH in `remote_name_request_feature_index_selector` (`0x80043810`, 102B) — `field208_0xd8`, `byte_0x16a`/`int_0x10` on `the_0x300` struct, `field_0x173`/`field_0x171`, `DAT_*` config flags — but where `0x80043810` only *selects* an index, this function goes further: after the gate passes (mask `field208_0xd8 & 8`), it computes an offset from a feature-page ushort field and calls `FUN_8003ca28()` to commit/apply it, writing the result into a struct field at `+0x11c`. This is the **apply-side counterpart** to the already-confirmed selector, sharing every gating field 1:1 — sufficient direct structural identity with an existing HIGH function to clear the bar without a caller xref. | **HIGH** — renamed `remote_name_request_feature_apply_8` |
| `0x80043984` | 178B | `void FUN_80043984(void)`: near-byte-identical twin of `0x80043884` — same gate fields, same `the_0x300`/`config_struct` layout, differing only in the bitmask tested on `field208_0xd8` (`4` instead of `8`) and the commit function called (`FUN_8003c94c` instead of `FUN_8003ca28`). Same reasoning as above: direct structural sibling of an existing HIGH function, clears the bar. | **HIGH** — renamed `remote_name_request_feature_apply_4` |
| `0x8004eb18` | 404B | `void FUN_8004eb18(undefined4,int,ushort)`: a long sequence of unaligned bitfield writes into ~14 different `DAT_*`-pointed hardware/config registers, packing source values from a `puVar3` struct (offsets 5/8/0xc/0xe/0x10/0x11/0x12/0x14/0x16) and from `param_2` (offsets 4/0xa/0xc/0xe/0x10/0x12) — classic "apply negotiated connection parameters to baseband/HW registers" shape (akin to other per-connection register-programming routines elsewhere in this region), but no command-name-level signature distinguishes which specific parameter set this is. | MEDIUM |
| `0x8004f374` | 368B | `void FUN_8004f374(int,undefined4*,int)`: pure diagnostic/logging dump — 5 separate `possible_logging_function__var_args` calls covering different sub-structs reachable from `param_2[0..4]`, gated by a byte-field bitmask (`bVar1 & 0x3f`). No control-flow side effects beyond logging; a "dump connection/LMP state for debug" helper. | MEDIUM |
| `0x8004f730` | 230B | `void FUN_8004f730(int)`: optional fn-pointer-hook short-circuit (`PTR_DAT_8004f818`), then a bit-scan loop (5 iterations) over a byte field, conditionally patching 2-3 byte fields from an "active" struct into a "current" struct (cases differ by `param_1+8 & 7`), with a logging call on one path. Generic per-connection state-field copy/sync helper; pattern resembles a feature/parameter sync routine but no unique terminator. | MEDIUM |
| `0x800442bc` | 222B | `void FUN_800442bc(void)`: a large reset/zero-out routine — zeroes ~20 `DAT_*` globals and struct fields, loops clearing a 0xc-entry array and a 10-entry array (interrupt-disabled for the second), and conditionally calls `LMP__25B__most_common_for_VSCs1()` if a sentinel field is not `-1`. Shape strongly resembles a **subsystem-wide state-reset/teardown** routine (clearing connection counters, feature flags, and an outstanding-VSC-request sentinel), parallel in spirit to the codec/role-switch reset sweep already documented at `0x8004326c`, but the breadth of unrelated-looking fields touched (no single named struct family) keeps this below HIGH absent a caller xref. | MEDIUM-HIGH |
| `0x8004ea2c` | 220B | `void FUN_8004ea2c(undefined4,byte*,ushort)`: short, dense bitfield-pack routine writing into 3 `DAT_*`-pointed HW config registers from `param_2`/`param_3`/a struct field, with 2 follow-up OR-flag sets gated on 2-bit sub-fields exceeding 1. Same general "apply negotiated parameter to baseband register" shape as `0x8004eb18` (smaller variant, fewer registers) — likely a sibling/simplified-path version of that function, but no unique signature to confirm which parameter. | MEDIUM |
| `0x8004c940` | 194B | `void FUN_8004c940(uint param_1)`: indexes the `big_ol_struct` connection-record array (`0x1ac` stride) by `param_1`, under interrupt-disable swaps out and clears 3 fields (`field403_0x1a0`/`field407_0x1a4`/`field411_0x1a8` — an inline embedded list head), then walks the extracted list unlinking each node (clearing a 4-byte field at node+0x100) and calling a function-pointer hook with `(node, 5)` — logging on nonzero return — before incrementing a byte counter (`field89_0x59`) by the saved count and finally calling the already-named `send_evt_HCI_Number_Of_Completed_Packets()`. This is a **per-connection pending-packet list drain that feeds the HCI_Number_Of_Completed_Packets event**, a clear and specific purpose confirmed by the call terminus into an already-HIGH-confidence named function — but the exact list (TX queue vs ACL fragment queue) isn't independently confirmable without the struct field's name, so held at MEDIUM-HIGH rather than HIGH. | MEDIUM-HIGH |
| `0x8004f25c` | 186B | `int* FUN_8004f25c(int,ushort*)`: a 2-list linked-list search (2 list heads at `local_10[0]/[1]`) with wraparound-masked delta-key comparison (matching the `0x8004f580`/`0x8004ef08` queue-key pattern from Pass 4/5) and an optional secondary match on a packed connection-handle-like ushort derived from list-node fields `0xb`/`0xc`. On match, unlinks the node from its list and returns it. This is the **search/remove counterpart** to the Pass 4/5 queue-insert/dequeue pair (`0x8004f580` insert, `0x8004ef08` dequeue, this one a keyed lookup-and-remove) — strengthens that cluster's identification as a generic timer/event queue, but still no command-specific name available. | MEDIUM-HIGH |

**Net effect this pass**: 2 of 9 decompiled candidates renamed to HIGH
confidence (`remote_name_request_feature_apply_8`,
`remote_name_request_feature_apply_4`) — both confirmed via exact structural
identity (same gating struct fields) with the already-HIGH
`remote_name_request_feature_index_selector` sibling. The other 7 sit at
MEDIUM/MEDIUM-HIGH; two pairs of structural siblings were identified
(`0x8004eb18`/`0x8004ea2c` register-programming pair, and `0x8004f25c`
joining the Pass 4/5 queue-insert/dequeue cluster as its search/remove
counterpart) but none independently confirm a command-specific name. This
breaks the 5-pass HIGH-rename drought, though the yield (2 of 9, both from
one tight family) confirms the region's broader remainder is still
low-density for HIGH-confidence work absent the `xrefs_to`/`find_callers`
tooling fix.

Full decompiled C for all 9 functions is preserved in the Ghidra run logs for
`BatchDecompile80040000Pass6A/B/C.java` (2026-06-23).

### Region 0x80040000 Pass 6 status: pivoting to region 0x80070000

The xrefs:2 tier is now fully exhausted (9/9 decompiled). 58 unnamed
functions remain in the 151-600B tier (the xrefs:1 tier, next topped by
`0x8004ba34` (534B), `0x8004a730` (456B), `0x8004ae74` (452B)), plus the
fully-unexplored 1-150B tiers (206 functions). Per the project's standing
pivot policy (6 consecutive passes — 3/3-cont/4/5/6 — with thin HIGH-rename
yield relative to effort spent, this pass's 2-of-9 notwithstanding since both
came from one already-adjacent family rather than fresh discovery), this
region is parked here. Per `analysis/INDEX.md`, region `0x80060000` is
**already complete** (zero unclassified), so it is not a candidate. Region
`0x80070000` has the most remaining unexplored work of any ROM region: 245
total functions, 191 unnamed, only Pass 1 (enumeration) and Pass 2 (6
functions decompiled) done — Pass 2 already staged concrete next targets
(`0x8007095c` 568B, `0x800754c4` 402B, `0x80073348` 362B, `0x80071d98` 306B,
`0x80074c8c` 232B). The next ticket pivots there.

## Pass 7 — 151-600B Tier Exhaustion Verification (2026-06-24)

`ColdTriageRegion80040000Pass7.java` was run in GZF process mode against the
live project to verify the 151-600B tier status (next tier after xrefs:2
exhaustion in Pass 6).

**Finding**: ZERO untouched functions remain in the 151-600B tier. All have been
either HIGH-renamed (Passes 2-6) or already decompiled and documented at
MEDIUM/MEDIUM-HIGH (Passes 3-6). The 151-600B tier is **fully exhausted**.

### Region 0x80040000 status: PARKED after Pass 7

Remaining work: 1-150B tiers (206 functions per Pass 6's breakdown). Per the
project's standing pivot policy (7 consecutive passes on this region with
thin HIGH-confidence yield), and Pass 6's conclusion that "six consecutive
passes yield mostly MEDIUM/MEDIUM-HIGH outcomes... pivot to a different
region for better HIGH-rename hit rate," this region is **formally parked**.

The 1-150B tiers remain as lower-priority future work: smaller code size = less
behavioral distinction per function, historically lower HIGH-confidence yield.
No structural signal suggests sudden improvement in that tier (consistent with
observations across other 64KiB regions). Estimated effort-to-reward ratio for
the 206 remaining functions is unfavorable relative to fresher regions
(0x80050000 with 345 unnamed, 0x80070000 with 191 unnamed) where prior cluster
work has established clearer architectural landmarks and naming conventions.

### Addendum (2026-06-27) — one cross-region rename from region 0x80050000 Pass 33

`0x8004a318` → `process_link_feature_toggle_command_and_send_status_event` (296B)
was renamed as a carryover from region `0x80050000`'s Pass 32/33 work (its two
callees, `FUN_8005c80c`/`FUN_8005c86c`, are in-region for `0x80050000` and were
named there too: `allocate_free_link_slot_if_enabled`/`dispatch_link_slot_state_op`).

### Addendum (2026-06-28) — three cross-region renames from region 0x80050000 Pass 47

Region `0x80050000`'s Pass 47 (deferred-callee sweep resolving that region's `FUN_8005c640`/
`FUN_80057ce8` holdovers) decompiled and renamed 3 functions that live in this region:

- `0x8004b064` → `insert_byte_into_per_connection_singly_linked_list_head_or_tail` (a tag-1
  callee of region `0x80050000`'s `drain_n_records_from_connection_event_queue`)
- `0x8004a5f4` → `dispatch_slot_timing_reprogram_if_pending_and_ready` (90B; confirmed direct
  callee of the already-named `ring_buffer_event_drain_dispatch_loop`, region `0x80000000`)
- `0x8004a660` → `dispatch_slot_timing_reprogram_if_feature_enabled_and_ready` (74B; structural
  twin of the above, narrower gating, no static callers found)

Both `0x8004a5f4`/`0x8004a660` dispatch (via a literal-pool function pointer) into region
`0x80050000`'s `recompute_and_commit_esco_sco_slot_timing_window` (`0x80057ce8`) — i.e. these are
this region's half of a cross-region "is this connection's eSCO/SCO timing dirty? if so,
reprogram it" dispatch pair. Full evidence is in `reverse_engineering_region_0x80050000.md`'s
"Pass 47" section. Per the precedent set by the 2026-06-27 addendum above, this does not reopen
this region's formal park (Pass 7) — it's 3 opportunistic renames driven by another region's
callee-resolution need, not a resumption of the 1-150B tier sweep.
Full evidence is in `reverse_engineering_region_0x80050000.md`'s "Pass 33" section.
This does not reopen this region's formal park — it's a single opportunistic
rename, not a resumption of the 1-150B tier sweep described above.

## Pass 51 — eSCO/SCO LMP-PDU dispatcher cluster, 12 HIGH renames (2026-06-28)

Region `0x80050000`'s Pass 50 confirmed `FUN_8004bde8` (region `0x80040000`, 354B) as the
**sole caller** of `finalize_and_emit_negotiation_complete_hci_event_from_lmp_pdu`. This pass
fully resolved that function and its dispatcher cluster — a self-contained, opportunistic
cross-region pass driven by another region's caller-resolution need (same pattern as the
2026-06-27/2026-06-28 addenda above), not a resumption of the 1-150B tier sweep this region
formally parked at after Pass 7.

**`0x8004bde8` → `esco_sco_lmp_pdu_validate_negotiate_and_dispatch`** (354B): the master per-PDU
eSCO/SCO LMP dispatcher. Validates a buffer/header-length budget (`*pbVar7*2+6+header_len`),
stages the PDU's negotiation parameters via the already-named `esco_sco_param_negotiate_and_stage`,
dispatches through a 16-entry opcode-nibble function-pointer table (`PTR_DAT_8004bf58`),
optionally emits `dispatch_meta_subevent_0x13_with_addr_resolve`, then forks on a subsystem-mode
flag (`PTR_PTR_8004bf54[4]`) into one of two paths:
- **link-slot-allocation path** (flag clear): `resolve_or_alloc_link_slot_and_program_hw_register_for_lmp_pdu`,
  and on success `append_rssi_entry_to_pending_batch_and_flush_if_full`.
- **negotiation-finalize path** (flag set): `negotiation_lru_promote_and_gate_completion_bit`, and
  on success `finalize_and_emit_negotiation_complete_hci_event_from_lmp_pdu` (the already-named
  Pass 50 function — confirming the bidirectional caller/callee relationship Pass 50 flagged).

Called by thin per-opcode wrapper functions — `FUN_8004bf60` (46B, confirmed via `find_callers`)
passes fixed args `(min_header_len=6, default_answer=1)`. `find_callers`/`xrefs_to` reported 5
more call sites (`0x8004bfb0`/`0x8004c046`/`0x8004c084`/`0x8004c0b4`/`0x8004c0e4`), but a
dedicated `FindContainingFunctionsPass51.java` run (written directly to
`/root/wairz/ghidra/scripts/`, hardcoded addresses — see Tooling Note below) confirmed all 5 sit
in `NO_CONTAINING_FUNCTION` territory: orphaned/mis-disassembled code, not resolvable to a named
sibling wrapper without a force-disassembly pass (the project's known MIPS16e
code-after-data gap). These are presumably more per-opcode wrapper thunks analogous to
`FUN_8004bf60`, immediately preceding the already-named `LMP_opcode_0x26F_LE_event_router`
(`0x8004c0f4`) — left unresolved as a future force-disassembly candidate, not blocking this
pass's HIGH renames.

**Link-slot-allocation / RSSI-batch fork** (3 renames):
- **`0x8004bc74` → `resolve_or_alloc_link_slot_and_program_hw_register_for_lmp_pdu`** (364B):
  resolves an existing link-table slot by bdaddr/index (already-named
  `find_link_record_by_bdaddr_and_flag`) or allocates a new one (already-named
  `conn_slot_alloc_type01_and_store_bdaddr`/`conn_slot_alloc_and_commit_dispatch`), then programs
  the corresponding HW link register via the already-named
  `write_indexed_link_register_b_with_slot_check`. All 4 callees pre-named — HIGH on direct
  structural grounds.
- **`0x8004ba34` → `append_rssi_entry_to_pending_batch_and_flush_if_full`** (534B): computes an
  RSSI value (`return_RSSI_value`) plus a per-connection adjustment, gates on a config flag via
  `FUN_80074940`, then appends bdaddr+opcode+RSSI into one of two mode-selected per-instance
  pending-batch arrays, incrementing a counter byte; once the counter reaches a config-derived
  max (`cVar13`), flushes both batches via `FUN_8004574c` and resets the counter to 0.
- **`0x8004574c` → `flush_rssi_batch_arrays_via_meta_subevent_0x2_or_0xb`**: a 2-call body —
  calls the already-named `send_evt_Meta_subevent_0x2_or_0x0b` once on each of the exact two
  per-instance array families `append_rssi_entry_to_pending_batch_and_flush_if_full` populates.
  This is the confirming anchor that clears the batch-append function to HIGH: the "flush" was
  otherwise just inferred from the counter-threshold shape.

**Correction: 4 of the 12 renamed addresses are region `0x80050000`, not this region.**
`esco_sco_lmp_pdu_validate_negotiate_and_dispatch` calls `negotiation_lru_promote_and_gate_completion_bit`
(was `FUN_80050f7c`) and `classify_and_commit_lmp_pdu_negotiation_category` (was `FUN_8005001c`) —
both `0x80050xxx` addresses, i.e. region `0x80050000`. Likewise the link-record-alloc-chain's entry
point `alloc_link_record_and_register_by_index` (was `FUN_8005058c`) and the structural-twin
`alloc_event_record_and_log_tag_0xb` (was `FUN_8005e40c`) are also region-`0x80050000` addresses.
Full evidence for all 4 is documented in `reverse_engineering_region_0x80050000.md`'s own "Pass 51"
section, not here — only the genuinely-in-region functions are detailed below.

**Category-classify's in-region callee** (1 rename): the cross-region
`classify_and_commit_lmp_pdu_negotiation_category` (region `0x80050000`) delegates its field commit
to:
- **`0x8004fee4` → `commit_bdaddr_role_fields_to_negotiation_state`**: fetches/allocates the
  connection's negotiation-state record via the already-named `get_or_alloc_conn_negotiation_state`,
  then commits the bdaddr/role fields (`+4`/`+0xa`/`+0xe`/`+0x10`/`+0xb`) from either a
  secondary/peer link record or the incoming PDU's own fields — matches the field layout the
  Pass 49/50 negotiation-state cluster already established.

**Link-record-alloc-and-index chain's in-region helpers** (3 renames — supporting the cross-region
entry point `alloc_link_record_and_register_by_index`, which itself resolves region `0x80050000`'s
Pass 48 holdover `FUN_8005058c` via its sole caller `FUN_80047c50`, the large eSCO/SCO LMP
parameter validator/committer):
- **`0x8004e298` → `set_link_record_index_and_register_in_table`** (26B): writes the record's
  connection-index field (`+0x10`) — the identical field `FUN_80047c50` itself writes directly on
  its "already had a record" branch — then registers the record into a sorted lookup table via
  `FUN_8004e100`.
- **`0x8004e100` → `insert_record_into_sorted_index_table_if_absent`** (84B): binary-searches via
  `FUN_8004e0b0`; if absent and there's room, shifts entries to make space and inserts the new
  record pointer at the returned position, incrementing the table's count.
- **`0x8004e0b0` → `binary_search_sorted_table_by_index_byte`** (80B): binary search over a
  `{array,capacity,count}` table struct keyed on a record's index-byte field (`+0x10`) — the
  structural twin of the already-named `lookup_record_ptr_by_key_via_bsearch`'s underlying bsearch
  helper (region `0x80050000` Pass 31), distinguished only by a 1-byte index key instead of an
  8-byte key.

**Pass 48's 3rd holdover, `0x80058638` (region `0x80050000`), stays unrenamed**: sets a HW-config
bit (`0x200` on `DAT_80058678`), then conditionally clears a bit on a parent-context record via
the already-named `resolve_parent_context_by_role`. No callers found (`find_callers` returns none
— likely reached only via an indirect/table call, the project's standing tooling gap for indirect
xrefs). Generic utility shape with no command-specific anchor; left at MEDIUM.

**This region's share of Pass 51: 8 of the pass's 12 HIGH renames** — the dispatcher itself, its
link-slot-alloc/RSSI-batch fork, the `0x8004e2xx`/`0x8004e1xx`/`0x8004e0xx` index-table helper
chain, and the negotiation-state field-commit callee. The other 4 are region-`0x80050000`
addresses, applied by the same `RenamePass51Region80040000.java` script (cross-region rename
scripts spanning both regions are an established pattern in this project — see the Pass 47/48
addenda above) but documented in that region's own file. Full script result: `renamed=12
alreadyOk=0 missing=0 failed=0`, `Save succeeded`. Live-verified via a fresh `decompile_function`
call on `esco_sco_lmp_pdu_validate_negotiate_and_dispatch` — all 4 renamed callee references (2
in-region, 2 cross-region) resolve correctly under their new names.

**Tooling note (script_args regression, 2026-06-28):** `run_ghidra_headless`'s `script_args`
parameter is currently broken for **every** invocation shape tested this pass —
`script_name`+`script_args` (no `use_saved_project`), `script_file_id`+`script_args` (with and
without `use_saved_project=true`) — all fail identically with
`ghidra.util.exception.InvalidInputException: Bad argument: <value>` thrown from
`AnalyzeHeadless.parseOptions`, even for a single hex address with no comma. This blocks the
documented `BatchDecompileList.java`/parameterized-script workflow entirely; confirmed not
specific to any one script (reproduced with both the pre-existing `BatchDecompileList.java` and a
brand-new `FindContainingFunctionsPass51.java`). **Workaround used this pass (and the one
documented in CLAUDE.md/the WIP ticket text already, now re-confirmed necessary in practice, not
just in principle): write the script directly to `/root/wairz/ghidra/scripts/` with hardcoded
addresses/names baked into the script body, and invoke via `script_name` +
`use_saved_project=true` with no `script_args`.** This worked cleanly for both the lookup script
and the 12-rename script in this pass. Logged as a `[TODO]` in `wairz_requested_changes.txt`.

### Region 0x80040000 status after Pass 51

Still formally parked (Pass 7) for the bulk 1-150B-tier sweep — this was a fully opportunistic
cross-region pass. 12 more functions named (cluster total now includes
`esco_sco_lmp_pdu_validate_negotiate_and_dispatch` and its full dispatch fork). The 5 orphaned
call-site addresses identified above are a concrete future force-disassembly target if anyone
revisits the MIPS16e code-after-data gap tooling.

### Addendum (2026-06-28, region `0x80050000` Pass 53): 3 opportunistic cross-region renames

Paired with region `0x80050000`'s own Pass 53 (general cold-triage re-rank, carrying forward the
cross-region leads `FUN_8004faa4`/`FUN_8004faf4` plus a documentation-staleness check on
`FUN_8004fa64`). 3 renames via `RenamePass53Region80050000.java` — does not reopen this region's
formal park:

- **`0x8004faa4` → `find_tail_of_payload_subrecord_chain_at_field0x50`** (74B, HIGH): tail-of-
  singly-linked-list walker (`+0x20` next-pointer chain rooted at `param_1+0x50`, capped at `0x65`
  hops with an overrun log). Decompiling its sole caller, `0x80047628` (the cold-triage entry
  above), confirmed both `0x80047628` and `0x80047304` are LMP-PDU/HCI-command fragment-
  reassembly handlers that call this function to find where to append received payload bytes,
  and that it is the exact lookup the already-named (region `0x80050000`, Pass 52)
  `setup_type3_esco_sco_conn_record_with_role_bit_set` uses to link a freshly-allocated sub-record
  onto the chain's tail. 2 independent caller contexts.
- **`0x8004faf4` → `find_tail_of_payload_subrecord_chain_at_field0x50_0x24`** (74B, HIGH): exact
  twin of the above, walk rooted one level deeper at `(*(param_1+0x50))+0x24`. Sole caller
  `0x80047304` (the cold-triage entry's sibling); also the lookup
  `setup_type3_esco_sco_conn_record_with_role_bit_clear` uses. Same evidentiary class as its
  sibling.
- **`0x8004fa64` → `resolve_peer_record_ptr_by_conn_type`** (60B, HIGH): selects between 2 peer/
  secondary-record pointer fields by a 3-bit type code at `param_1+8`. Closes a documentation-
  staleness gap: this function had been called "the already-named `FUN_8004fa64` 'peer lookup'"
  in region `0x80050000`'s own docs across 3+ prior passes (Pass 34, 52) without ever actually
  being renamed in Ghidra — confirmed via a direct `decompile_function` check this pass. 1
  previously-documented named caller (`propagate_timing_offset_to_peer_record_by_type`) plus 2
  more named callers from Pass 52.

`0x80047628`/`0x80047304` themselves remain unrenamed (mechanism confirmed, exact HCI/LMP opcode
identity not pinned down — see the updated cold-triage table entries above); a future pass
wanting to push them to HIGH would need to identify the specific command/PDU type from the BT
spec (handle + role-byte + variable-length-payload shape, `Command_Complete` event-code `0xe`
terminus).

Full evidence is in `reverse_engineering_region_0x80050000.md`'s own Pass 53 section.

## Pass 54 addendum (2026-06-28) — `atomically_take_conn_list_b_and_apply_quota_overflow` (`0x8004ca10`)

Pass 54's planned full cold-triage re-rank for region `0x80050000` is still deferred
(`ColdTriageRegion80050000Pass54.java` not run this iteration). This pass closed the
standing Pass 54a lead: decompiled and renamed `FUN_8004ca10` →
`atomically_take_conn_list_b_and_apply_quota_overflow` (102B, HIGH) via
`RenamePass54bFun8004ca10.java` (`renamed=1 alreadyOk=0 missing=0 failed=0`, live-verified).

**Confirmed callers** (all from already-HIGH documented parents in prior passes):

| Caller | Region | Call context |
|--------|--------|--------------|
| `ring_buffer_event_drain_loop_variant2` (`0x800083ec`) | `0x80000000` | After computing a per-slot quota delta and calling `drain_n_records_from_connection_event_queue`, calls this function then `apply_per_slot_quota_delta_and_validate_link_register`; may also call `atomically_drain_conn_pending_queue` first if a config flag is set |
| `conn_field_increment_and_cleanup_dispatch` (`0x80008328`) | `0x80000000` | Single-shot counterpart of the ring drain above: after incrementing `field92_0x5c` and calling `drain_n_records_from_connection_event_queue`, conditionally calls this function when a per-connection flag is set, then `apply_per_slot_quota_delta_and_validate_link_register` |
| `drain_and_dispatch_conn_event_ring_by_kind_then_reinit` (`0x8005c720`) | `0x80050000` | After draining the per-connection pending-event ring and optionally calling `atomically_drain_conn_pending_queue`, conditionally calls this function before reinitializing the ring |

**Mechanism (decompile-confirmed, HIGH confidence):** keyed by connection index
(`param_1 & 0xff`) into the established `0x1ac` struct array
(`PTR_base_of_0x1ac_struct_array_0xA_large2_1__field0_0x0`). Under IRQ disable, snapshots
the list-B head index byte at `conn+0x144` and immediately resets that field to the empty-list
sentinel `0xa0a` — the same default constant `init_connection_record` /
`release_connection_record` write at `+0x140/+0x144`, and the same `'\n'` (`0x0a`) empty check
used by the already-HIGH `insert_byte_into_per_connection_singly_linked_list_head_or_tail`.
After re-enabling interrupts, walks the saved list head via `FUN_8004b1d0`, which traverses
the shared `0xc`-byte node table until the head index reaches `0x0a`, incrementing each linked
record's 16-bit counter at `+0x104` and collecting records whose counter exceeds their limit
at `+0x2`. If any overflow records were collected (`local_20 != 0`), calls `FUN_8004b3c0`; if
`field411_0x1a8` is set on the connection, calls `FUN_8004c940`. This is the shared post-drain
"take ownership of list B and apply quota overflow" step in the quota / pending-event
reconciliation pipeline — sequenced after `drain_n_records_from_connection_event_queue` (or the
Pass 53 ring-drain equivalent) and before the pipeline finalizer
`apply_per_slot_quota_delta_and_validate_link_register` (`0x8002b6f4`, region `0x80020000`,
renamed Pass 54c).

Adjacent at `0x8004ca7c` is the already-HIGH `conn_link_quality_history_reset_and_vsc_0xfc95_trigger`
(Pass 4); same address cluster, unrelated purpose.

**Next:** run `ColdTriageRegion80050000Pass54.java` for the deferred Pass 54 general cold-triage
re-rank (region `0x80050000`).

## Pass 52 (2026-06-30) — 1-150B tier cold-triage + rank-1 rename

`ColdTriageRegion80040000Pass52.java` ranked all unnamed `FUN_*` in the 1-150B tier by xref
count then size. Live ground truth: **267 unnamed** in-region (336 total functions); **181** in
the 1-150B combined tier (83 in 1-50B, 98 in 51-150B) — down from Pass 7's parked count of 206
due to opportunistic renames since then.

**Top-5 staged candidates:**

| Rank | Address | xrefs | Size | Tier |
|------|---------|-------|------|------|
| 1 | `0x8004310c` | 30 | 68B | 51-150B |
| 2 | `0x80043158` | 27 | 64B | 51-150B |
| 3 | `0x8004ce44` | 16 | 38B | 1-50B |
| 4 | `0x8004e500` | 15 | 118B | 51-150B |
| 5 | `0x8004287c` | 10 | 88B | 51-150B |

**Rank-1 decompiled and renamed (HIGH):** `FUN_8004310c` →
`or_merge_hw_channel_table_entry_and_indexed_dispatch` (68B) via
`RenamePass52Region80040000Fun8004310c.java` (`renamed=1`, live-verified).

```c
void or_merge_hw_channel_table_entry_and_indexed_dispatch(uint index, ushort mask)
{
  irq = disable_interrupts();
  table = DAT_80043150;
  fptr_table = PTR_DAT_80043154;
  (*fptr_table)(index & 0xffff, table[index] | mask);
  enable_interrupts(irq);
}
```

IRQ-disabled indexed dispatch: OR-merges `mask` onto the per-index ushort HW-channel parameter
table entry at `DAT_80043150`, then calls through the function-pointer table at `PTR_DAT_80043154`.
Structural OR-variant twin of the already-documented `FUN_800430ac` (mask-merge:
`(table & ~mask3) | (mask3 & value)`) and Pass 52b's
`and_mask_hw_channel_table_entry_and_indexed_dispatch` (AND-mask: `value & table[index]`)
in the SCO/eSCO HW channel parameter-commit cluster (`init_or_clear_sco_hw_channel_subsystem` in
region `0x80030000` iterates channel slots via `FUN_800430ac`, finishes with
`and_mask_hw_channel_table_entry_and_indexed_dispatch`).

## Pass 52b (2026-06-30) — rank-2 AND-mask twin rename

**Rank-2 decompiled and renamed (HIGH):** `FUN_80043158` →
`and_mask_hw_channel_table_entry_and_indexed_dispatch` (64B) via
`RenamePass52bRegion80040000Fun80043158.java` (`renamed=1`, live-verified).

```c
void and_mask_hw_channel_table_entry_and_indexed_dispatch(uint index, ushort mask)
{
  irq = disable_interrupts();
  table = DAT_80043198;
  fptr_table = PTR_DAT_8004319c;
  (*fptr_table)(index & 0xffff, mask & table[index]);
  enable_interrupts(irq);
}
```

IRQ-disabled indexed dispatch: AND-masks `mask` with the per-index ushort HW-channel parameter
table entry at `DAT_80043198`, then calls through the function-pointer table at `PTR_DAT_8004319c`.
Structural AND-variant twin of Pass 52's `or_merge_hw_channel_table_entry_and_indexed_dispatch`
(OR-merge on `DAT_80043150`/`PTR_DAT_80043154`) and `FUN_800430ac` (mask-merge:
`(table & ~mask3) | (mask3 & value)`) in the SCO/eSCO HW channel parameter-commit cluster
(`init_or_clear_sco_hw_channel_subsystem` in region `0x80030000` iterates channel slots via
`FUN_800430ac`, finishes with `and_mask_hw_channel_table_entry_and_indexed_dispatch`).

## Pass 52c (2026-06-30) — rank-3 eSCO type-byte writer rename

**Rank-3 decompiled and renamed (HIGH):** `FUN_8004ce44` →
`write_conn_record_esco_type_byte_and_link_quality_reset` (38B) via
`RenamePass52cRegion80040000Fun8004ce44.java` (`renamed=1`, live-verified).

```c
void write_conn_record_esco_type_byte_and_link_quality_reset(uint conn_idx, byte type_byte)
{
  conn_rec_array[(conn_idx & 0xff) * 0x1ac].field135_0x8e = type_byte;
  conn_link_quality_history_reset_and_vsc_0xfc95_trigger(conn_idx & 0xff, 1);
}
```

Writes the eSCO/connection-type byte to `conn_rec+0x8e` (`field135_0x8e`), then
triggers the already-HIGH `conn_link_quality_history_reset_and_vsc_0xfc95_trigger`
with `mode==1`. Invoked from patch eSCO setup via RAM fn-ptr slot `0x8012082c`
(`(**0x8012082c)(conn_idx, 6)` from `FUN_8010b5d8` / `FUN_8010b4d0`).

## Pass 52d (2026-06-30) — rank-4 LMP procedure busy-probe rename

**Rank-4 decompiled and renamed (HIGH):** `FUN_8004e500` →
`is_any_conn_lmp_procedure_busy_by_index` (118B) via
`RenamePass52dRegion80040000Fun8004e500.java` (`renamed=1`, live-verified).

```c
byte is_any_conn_lmp_procedure_busy_by_index(byte index)
{
  // optional hook override at PTR_DAT_8004e578
  nibble = (index < 5) ? lookup_table[index] : 0;  // PTR_DAT_8004e57c
  for (conn_idx = 0; conn_idx < conn_table_count; conn_idx++) {
    conn_rec = conn_table[conn_idx];  // PTR_PTR_8004e580
    if (conn_rec != 0
        && (conn_rec+0x08 & 7) == 0      // link-state low 3 bits clear
        && (conn_rec+0x1d & 2) != 0      // procedure-active flag
        && (conn_rec+0x20 & 0xf) == nibble)  // procedure-type nibble match
      return 1;
  }
  return 0;
}
```

Boolean busy-procedure probe: maps `index` 0-4 to a conn_rec+0x20 low-nibble value via
lookup table, then scans the global connection table for any active link with matching
procedure state. Used 15× as stage-(b) of the 4-stage permission gate in
`check_feature_permission_by_category_and_index` (`0x8005af8c`, region `0x80050000`);
sibling of still-unnamed `FUN_8004e9e0` (global busy-bit scan without index).

## Pass 52e (2026-06-30) — rank-5 access-code sync-word writer rename

**Rank-5 decompiled and renamed (HIGH):** `FUN_8004287c` →
`compute_access_code_sync_word_from_bdaddr` (88B) via
`RenamePass52eRegion80040000Fun8004287c.java` (`renamed=1`, live-verified).

```c
void compute_access_code_sync_word_from_bdaddr(uint bdaddr_low24, byte *out_buf)
{
  // Select CRC polynomial seed based on (seed & bdaddr_low24)
  seed = ((DAT_800428d4 & bdaddr_low24) == 0) ? DAT_800428dc : DAT_800428d8;
  state = ((seed | bdaddr_low24) ^ DAT_800428e0) << 2;
  // 30-iteration (0x1e) LFSR-style bit walk with feedback constants
  // DAT_800428e4 / DAT_800428e8 when MSB set
  for (i = 0; i < 0x1e; i++) { ... }
  // Pack 5 output bytes into caller buffer (sync-word halves for HW regs 0x10/0x12)
  out_buf[0..4] = packed_bits;
}
```

CRC/LFSR-style 30-iteration bit walk derives the 5-byte Bluetooth access-code
sync word from the BD_ADDR low-24 bits (`param_1`). Output is written to the
caller buffer and programmed into baseband HW registers `0x10`/`0x12` by
`FUN_80041900` (page-train baseband programming) when the peer BD_ADDR differs
from the cached local address — otherwise the ROM uses fixed default sync-word
constants (`0x6e1e`/`0x88d6`). 10 xrefs; sibling cluster to the
SCO/eSCO HW-channel indexed-dispatch trio from Passes 52–52b.

## Pass 52f (2026-06-30) — refreshed rank-1 LCG PRNG rename

**Refreshed rank-1 decompiled and renamed (HIGH):** `FUN_80042934` →
`lcg_prng_bounded_modulo` (62B) via `RenamePass52fRegion80040000Fun80042934.java`
(`renamed=1`, live-verified).

```c
uint lcg_prng_bounded_modulo(uint max_inclusive)
{
  bound = min(max_inclusive, *PTR_DAT_80042974);
  state = *PTR_DAT_80042978;
  state = state * DAT_8004297c + DAT_80042980;  // LCG advance
  *PTR_DAT_80042978 = state;
  return (state >> 0x16) % (bound + 1);
}
```

Linear-congruential PRNG: advances global seed at `PTR_DAT_80042978`, returns
`(state >> 22) % (min(param, max_bound)+1)`. Shared utility called from PSM/QoS
bitpair-eligibility walkers in region `0x80070000` (e.g.
`expand_psm_qos_state_0x16_retry1_random_bitpair_eligibility` passes `0x4e`;
`finalize_psm_qos_eligibility_bitpair_expand_or_fail_channel` passes `0x27`).
Sibling of `advance_lcg_prng_state_and_return_high_byte` (`0x80071948`) in the
AFH/LAP cluster — same LCG pattern, different output extraction. 10 xrefs.

Post-rename cold-triage refresh: **261 unnamed** in-region (175 in 1-150B tier).
Refreshed rank-1 is `0x8004a2e4` (10 xrefs, 1B — likely artifact); rank-3
`0x800430ac` (8 xrefs, 88B) is the mask-merge SCO/eSCO HW-channel sibling
already referenced in Pass 52.

## Pass 52g (2026-06-30) — rank-3 mask-merge HW-channel dispatch rename

**Rank-3 decompiled and renamed (HIGH):** `FUN_800430ac` →
`mask_merge_hw_channel_table_entry_and_indexed_dispatch` (88B) via
`RenamePass52gRegion80040000Fun800430ac.java` (`renamed=1`, live-verified).

```c
void mask_merge_hw_channel_table_entry_and_indexed_dispatch(uint index, ushort value, ushort mask)
{
  irq = disable_interrupts();
  table = DAT_80043104;
  fptr_table = PTR_DAT_80043108;
  (*fptr_table)(index & 0xffff,
                table[index] & ~mask | mask & value);
  enable_interrupts(irq);
}
```

IRQ-disabled indexed dispatch: mask-merges `value`/`mask` onto the per-index
ushort HW-channel parameter table entry at `DAT_80043104` via
`(table & ~mask) | (mask & value)`, then calls through the function-pointer
table at `PTR_DAT_80043108`. Primary channel-commit helper in the SCO/eSCO HW
cluster — iterated by `init_or_clear_sco_hw_channel_subsystem` (region
`0x80030000`) before the Pass 52b AND-mask finisher. Completes the OR/AND/mask
trio with Pass 52's `or_merge_hw_channel_table_entry_and_indexed_dispatch` and
Pass 52b's `and_mask_hw_channel_table_entry_and_indexed_dispatch`. 8 xrefs.

Post-rename: **260 unnamed** in-region. Refreshed rank-1 remains `0x8004a2e4`
(10 xrefs, 1B — likely artifact).

## Pass 52h (2026-06-30) — rank-1 artifact triage + rank-3 link-mask probe rename

**Rank-1 triaged (non-function artifact):** `0x8004a2e4` (10 xrefs, 1B) — decompile
returns `halt_baddata()` / bad-instruction truncation; confirmed 1-byte mis-disassembly
artifact, not a substantive function. Cold-triage rank-1 remains this address until
Ghidra boundary cleanup; substantive work skips to rank-2+.

**Refreshed rank-3 decompiled and renamed (HIGH):** `FUN_8004e9e0` →
`scan_active_link_mask_for_slot_status_flag` (70B) via
`RenamePass52hRegion80040000Fun8004e9e0.java` (`renamed=1`, live-verified).

```c
undefined4 scan_active_link_mask_for_slot_status_flag(void)
{
  mask = *(byte *)(*PTR_PTR_8004ea28 + 0x24);
  while (true) {
    bit = -mask & mask;              // isolate lowest set bit
    if (bit == 0) return 0;
    if (*(char *)(*PTR_PTR_8004ea28 + (bit == 4) + 0x22) == 1) break;
    mask = ~bit & mask;              // clear bit, continue scan
  }
  return 1;
}
```

Bit-scan over active-link bitmask byte at global context `+0x24`: for each set bit,
checks status byte at `+0x22` (default) or `+0x23` (when isolated bit == 4); returns
1 on first match, 0 when mask exhausted. Isolate-lowest-set-bit idiom matches the
bit-scan family in region `0x80050000` (`find_and_clear_pending_bit_for_index_and_dispatch`).
8 xrefs.

Post-rename: **259 unnamed** in-region. Refreshed substantive rank-2 was `0x80042da0`
(9 xrefs, 18B — sum-of-two-byte threshold probe) — completed Pass 52i.

## Pass 52i (2026-06-30) — rank-2 two-byte counter threshold probe rename

**Refreshed rank-2 decompiled and renamed (HIGH):** `FUN_80042da0` →
`is_two_byte_counter_sum_above_one` (18B) via
`RenamePass52iRegion80040000Fun80042da0.java` (`renamed=1`, live-verified).

```c
bool is_two_byte_counter_sum_above_one(void)
{
  byte *pbVar1;
  pbVar1 = PTR_DAT_80042db4;
  return 1 < (uint)pbVar1[1] + (uint)*pbVar1;
}
```

Reads two bytes via `PTR_DAT_80042db4`; returns true when `byte[0]+byte[1] > 1`.
Threshold probe used by packet-type narrowing in
`recompute_and_store_field_0x250_packet_type_on_conn_slot` (region `0x80070000`,
Pass 12cd): when false and `ushort_0x24==0x80`, narrows mask to `0xfff`. 5
confirmed callers (`FUN_8006b5f4`, `FUN_8006bdf8`, `FUN_8006c470`, `FUN_8006d33c`,
`FUN_8006d8f8`).

Post-rename: **258 unnamed** in-region.

## Pass 52j (2026-06-30) — rank-2 LMP response header packer rename

**Refreshed rank-2 decompiled and renamed (HIGH):** `FUN_80044564` →
`pack_lmp_response_header_status_and_handle` (32B) via
`RenamePass52jRegion80040000Fun80044564.java` (`renamed=1`, live-verified).

```c
void pack_lmp_response_header_status_and_handle(uint handle, char *out)
{
  handle = handle & 0xffff;
  if (handle == 0) {
    out[0] = '\0';
  } else {
    status = PTR_struct_of_at_least_0x300_size_80044584->field_0x165;
    if (status == '\0') status = '\x01';
    out[0] = status;
  }
  out[1] = (char)handle;
  out[2] = (char)(handle >> 8);
}
```

Packs a 3-byte LMP response header: byte 0 is status from per-connection
`field_0x165` (default `1` when zero, `0` when handle==0); bytes 1–2 are the
uint16 handle little-endian. Installed in the LMP VSC 0x268 dispatch pool at
patch slot `0x8010bc6c` (documented as "LMP response header builder" in
`reverse_engineering_lmp_vsc_opcode_map.md`). LE Meta Event cluster neighbor
(`0x80044564` sits just below `FUN_800454a8` HCI_Command_Status stub). 8
xrefs.

Post-rename: **257 unnamed** in-region.

## Pass 52k (2026-06-30) — rank-4 AFH channel index selector rename

**Refreshed rank-4 decompiled and renamed (HIGH):** `FUN_8004e3ec` →
`select_available_afh_channel_index_for_conn_record` (124B) via
`RenamePass52kRegion80040000Fun8004e3ec.java` (`renamed=1`, live-verified).

```c
void select_available_afh_channel_index_for_conn_record(conn_rec *rec)
{
  if (hook == NULL || hook() == 0) {
    for (i = 0; i < 0x65; i++) {
      *DAT_8004e46c = 5;
      index = *DAT_8004e470 % 0x25;   // 0-36 AFH channel range
      table = (config->field285_0x129 & 1)
            ? PTR_base_of_0x1ac_struct_array_..._field456_0x1d5
            : PTR_DAT_8004e478;
      if (table[index >> 3] >> (index & 7) & 1) break;
    }
    rec->field_at_0x10 = rec->field_at_0x10 & 0xc0 | (byte)index;
  }
}
```

Optional hook gate at `PTR_DAT_8004e468`; when absent or returns zero, loops up
to 101 iterations: writes trigger `5` to `DAT_8004e46c`, reads pseudo-random
index `ushort % 37`, probes AFH channel availability bitmask (config bit
`field285_0x129` selects between two tables), breaks on first set bit, stores
selected channel index in low 6 bits of conn record `+0x10` (preserving top 2
bits `0xc0`). LMP/AFH cluster neighbor of Pass 52d's
`is_any_conn_lmp_procedure_busy_by_index`. 6 xrefs.

Post-rename: **256 unnamed** in-region.

## Pass 52l (2026-06-30) — rank-3 credit-scheduler context accessor rename

**Rank-3 triaged (substantive, not artifact):** `0x8004f240` (6B) — decompile
confirms a real global accessor, not a mis-disassembly stub like `0x8004a2e4`.

**Rank-3 decompiled and renamed (HIGH):** `FUN_8004f240` →
`get_credit_scheduler_context_active_entry_ptr` (6B) via
`RenamePass52lRegion80040000Fun8004f240.java` (`renamed=1`, live-verified).

```c
undefined4 get_credit_scheduler_context_active_entry_ptr(void)
{
  return *(undefined4 *)(PTR_DAT_8004f248 + 4);
}
```

Returns the active credit-scheduler slot descriptor pointer from global context
at `PTR_DAT_8004f248+4`. Callers (`sco_esco_slot_timing_offset_calc_variant1`/
`variant2`, `LMP_power_and_clk_adj_procedure_orchestrator`) dereference the
returned pointer at `+8` (type byte), `+0x10`/`+0x12`/`+4` (timing ushorts),
and `+0x20` (nested descriptor when type==0x0a). Sibling of
`lookup_sco_esco_slot_timing_byte_by_opcode_index` (indexed byte table lookup) in the SCO/eSCO slot-timing cluster
documented in region `0x80000000`. 3+ xrefs.

Post-rename cold-triage refresh: **255 unnamed** in-region (169 in 1-150B tier).
Refreshed rank-1 remains `0x8004a2e4` (10 xrefs, 1B — artifact); rank-4
substantive lead is `0x8004fcb8` (46B, 5 xrefs).

**Next:** continue refreshed 1-150B cold-triage — decompile rank-5
substantive candidate or skip rank-1/2/3 artifacts.

## Pass 52m (2026-06-30) — rank-4 conn-record free precondition rename

**Refreshed rank-4 decompiled and renamed (HIGH):** `FUN_8004fcb8` →
`is_conn_record_pkt_modes_cleared_for_free` (46B) via
`RenamePass52mRegion80040000Fun8004fcb8.java` (`renamed=1`, live-verified).

```c
bool is_conn_record_pkt_modes_cleared_for_free(conn_rec *rec)
{
  bool ok = (rec->field_at_0x08 & 7) == 0;
  if (!ok)
    possible_logging_function__var_args(2, 0xd2, 0x1ed, 0xd39, 1,
                                        PTR_unknown_dat_ref_by_logger_8004fce8,
                                        rec->field_at_0x08 & 7);
  return ok;
}
```

Returns true only when conn record `+0x08` low 3 bits (`pkt_modes`) are zero —
the free precondition gate before `FUN_8004fcec` releases the slot back to the
free pool. Logs error code `0xd2` on failure. Gated callers include
`FUN_8004fd6c` (SCO/eSCO channel-slot state-machine setter) and
`conditional_dispatch_LE_channel_selection_algorithm` (region `0x80050000`).
Documented in `reverse_engineering_conn_record_subsystem.md` §10. 5 xrefs.

Post-rename: **254 unnamed** in-region.

## Pass 52n (2026-06-30) — rank-5 inquiry/LAP slot bitmask release rename

**Refreshed cold-triage (rank-1–4 skipped as artifacts or already done):** rank-5
`0x80042c94` (150B, 4 xrefs) — substantive inquiry/LAP slot-state manager.

**Rank-5 decompiled and renamed (HIGH):** `FUN_80042c94` →
`release_inquiry_lap_slot_pending_bitmask` (150B) via
`RenamePass52nRegion80040000Fun80042c94.java` (`renamed=1`, live-verified).

```c
void release_inquiry_lap_slot_pending_bitmask(uint slot_index)
{
  ctx = PTR_DAT_80042d2c;  // inquiry/LAP slot state: +0 active, +1 refcount,
                           // +2 active_slot, +3 4-bit mask, +4[slot] status
  if ((ctx[slot_index + 4] >> 1 & 1) == 0) return;  // pending bit not set
  if ((ctx[slot_index + 4] & 1) == 0) {
    // non-primary slot: find next set bit in mask, clear pending + mask bit,
    // decrement refcount at +1
    ...
  } else if (*ctx != 0) {
    // primary slot: if LAP[slot+0x45] in the_0x300 struct is zero,
    // clear active flag and slot status bits
    ...
  }
}
```

Releases inquiry/LAP slot pending bitmask state on link teardown or HCI cancel.
Global context at `PTR_DAT_80042d2c` tracks per-slot status bytes (`+4[slot]`:
bit0 = primary, bit1 = pending), a 4-bit active-slot mask (`+3`), refcount (`+1`),
and active-slot index (`+2`). When the pending bit is set on the given slot index,
either clears the next active slot from the mask (non-primary path) or clears the
primary active flag when the corresponding `_x142_LAP[slot+0x45]` entry is zero.
Setter sibling is `set_inquiry_lap_slot_pending_bitmask` (`0x80042c28`, Pass 52o).
Callers include `fHCI_conn_req_cancel` (`0x80036bd0`, region `0x80030000`) and
connection teardown `FUN_80041dac`. 4 xrefs.

Post-rename: **253 unnamed** in-region (167 in 1-150B tier).

## Pass 52o (2026-06-30) — rank-6 inquiry/LAP slot bitmask setter rename

**Refreshed cold-triage (rank-1–5 skipped as artifacts or already done):** rank-6
`0x80042c28` (100B, 4 xrefs) — substantive inquiry/LAP slot-state setter, sibling
of Pass 52n's `release_inquiry_lap_slot_pending_bitmask`.

**Rank-6 decompiled and renamed (HIGH):** `FUN_80042c28` →
`set_inquiry_lap_slot_pending_bitmask` (100B) via
`RenamePass52oRegion80040000Fun80042c28.java` (`renamed=1`, live-verified).

```c
uint set_inquiry_lap_slot_pending_bitmask(int is_primary, byte *slot_index_ptr)
{
  hook = PTR_DAT_80042c8c;
  if (hook == NULL || hook(&is_primary) == 0) {
    slot = *slot_index_ptr;
    ctx = PTR_DAT_80042c90;  // same layout as PTR_DAT_80042d2c in release fn
    if (is_primary == 1) {
      ctx[slot + 4] |= 1;   // primary flag
      *ctx = 1;             // active
      ctx[2] = slot;        // active_slot index
    } else {
      ctx[slot + 4] &= 0xfe; // clear primary flag
      ctx[1]++;              // refcount
      ctx[3] |= (1 << slot); // set bit in 4-bit active mask
    }
    ctx[slot + 4] |= 2;     // pending flag (always set)
  }
  return slot;
}
```

Acquires inquiry/LAP slot pending bitmask state — setter counterpart to
`release_inquiry_lap_slot_pending_bitmask`. Optional veto hook at `PTR_DAT_80042c8c`.
Global context at `PTR_DAT_80042c90` (same struct layout as `PTR_DAT_80042d2c` in
the release function): `+0` active, `+1` refcount, `+2` active_slot, `+3` 4-bit
mask, `+4[slot]` status (bit0 primary, bit1 pending). When `is_primary==1`, marks
slot as primary and sets active; otherwise clears primary bit, bumps refcount, and
ORs slot into the active mask. Always sets the pending bit before return. 2
confirmed callers via `xrefs_to`:
`reassign_inquiry_lap_slot_refcount_pending_and_program_channel` and
`role_switch_completion_or_abort_handler`.

Post-rename: **252 unnamed** in-region (166 in 1-150B tier).

## Pass 52p (2026-06-30) — rank-7 LMP slot-offset relation classifier rename

**Refreshed cold-triage (rank-1–6 skipped as artifacts or already done):** rank-7
`0x80042c0c` (24B, 4 xrefs) — substantive tri-state masked slot-offset comparator
in the inquiry/LAP / role-switch cluster (sibling of Pass 52n/o bitmask setters).

**Rank-7 decompiled and renamed (HIGH):** `FUN_80042c0c` →
`classify_lmp_slot_offset_relation_masked` (24B) via
`RenamePass52pRegion80040000Fun80042c0c.java` (`renamed=1`, live-verified).

```c
int classify_lmp_slot_offset_relation_masked(uint current_offset, uint proposed_offset)
{
  int result = 1;
  if (proposed_offset <= current_offset) {
    mask = DAT_80042c24;  // literal-pool mask at +0x18
    result = 3 - (uint)(((current_offset ^ proposed_offset) & mask) == 0);
  }
  return result;
}
```

Tri-state masked LMP slot-offset relation classifier for role-switch handling:
returns **1** when `proposed > current` (accept path in `LMP_SWITCH_REQ_0x13`);
**2** when `proposed <= current` and masked bits differ (reject →
`LMP_NOT_ACCEPTED` error `0x28`); **3** when `proposed <= current` but equal in
masked bits (special adjust loop in `FUN_8006f994` role-switch completion).
Current offset from `FUN_80034a24`; proposed from conn record `field_0x18` or
LMP switch PDU bytes. 4 confirmed callers via `find_callers`: `LMP_SWITCH_REQ_0x13`,
`FUN_8006f994`, `FUN_8006fd20`, `LMP_SET_AFH_0x3C`.

Post-rename: **251 unnamed** in-region (165 in 1-150B tier).

## Pass 52q (2026-06-30) — rank-8 per-slot IIR RSSI sample updater rename

**Refreshed cold-triage (rank-1–7 skipped as artifacts or already done):** rank-8
`0x80042b80` (132B, 3 xrefs) — substantive per-connection-slot IIR RSSI accumulator
in the `return_RSSI_value` / `big_ol_struct` cluster (sibling of region `0x80050000`'s
`apply_iir_filter_to_rssi_and_store`).

**Rank-8 decompiled and renamed (HIGH):** `FUN_80042b80` →
`apply_iir_filter_to_rssi_on_big_ol_struct_slot` (132B) via
`RenamePass52qRegion80040000Fun80042b80.java` (`renamed=1`, live-verified).

```c
void apply_iir_filter_to_rssi_on_big_ol_struct_slot(ushort sample, uint slot)
{
  if ((slot & 0xffff) == 0xff) return;
  big_ol_struct[slot].field_0x86 = sample;
  big_ol_struct[slot].field_0x84++;          // sample counter
  rssi = return_RSSI_value();
  if (big_ol_struct[slot].unknown2_0x88 == 0x7fff)
    big_ol_struct[slot].unknown2_0x88 = (short)(rssi * 0x10);
  else {
    coef = (*(code *)PTR_DAT_80042c08)(slot, 0);
    big_ol_struct[slot].unknown2_0x88 =
         (short)(((0x10 - coef) * old + coef * rssi * 0x10) / 0x10);
  }
}
```

Per-slot IIR-filtered RSSI sample updater on `big_ol_struct[slot]`: stores caller
`sample` at `+0x86`, increments counter at `+0x84`, reads live RSSI via named
`return_RSSI_value`, initializes filtered value at `+0x88` from `rssi*16` when
sentinel `0x7fff`, else blends with coefficient from optional hook at
`PTR_DAT_80042c08(slot,0)` — same IIR arithmetic as
`apply_iir_filter_to_rssi_and_store` (`0x80059de4`). Sole caller via `find_callers`:
`lmp_pdu_received_top_level_processor`.

Post-rename: **250 unnamed** in-region (164 in 1-150B tier).

## Pass 52r (2026-06-30) — rank-9 HW slot-counter poll into conn field0x3e rename

**Refreshed cold-triage (rank-1–8 skipped as artifacts or already done):** rank-9
`0x8004fb44` (110B, 2 callers) — substantive HW slot-counter poll/merge into
conn_record `+0x3e` before LMP PDU fragment reassembly starts.

**Rank-9 decompiled and renamed (HIGH):** `FUN_8004fb44` →
`poll_hw_slot_counter_into_conn_record_field0x3e` (110B) via
`RenamePass52rRegion80040000Fun8004fb44.java` (`renamed=1`, live-verified).

```c
void poll_hw_slot_counter_into_conn_record_field0x3e(conn_record_t *conn)
{
  ushort current = conn->field_0x3e & 0xfff;
  ushort new_val = current;
  for (int retry = 0; retry < 0xb; retry++) {
    if (current != new_val) break;
    *DAT_8004fbb4 = 5;              // poke HW refresh trigger
    new_val = *DAT_8004fbb8;          // read global slot counter
  }
  if (retry == 0xb && current == new_val)
    possible_logging_function__var_args(2, 0xd2, 0x4e7, 0xd45, ...);
  conn->field_0x3e = (conn->field_0x3e & 0xf000) | (new_val & 0xfff);
}
```

HW slot-counter poll before LMP PDU fragment collection: reads current 12-bit
value at conn_record `+0x3e`, pokes global `DAT_8004fbb4` with `5` and polls
`DAT_8004fbb8` up to 11 times until the counter changes (logs overrun if
unchanged), then merges the new 12-bit value back preserving upper nibble
(`0xf000`). Called from the twin LMP-PDU fragment-reassembly handlers
`FUN_80047304`/`FUN_80047628` when starting a new fragment sequence
(`bVar1==1` or `3`, and `bVar1==4` in the `0x80047628` variant) — immediately
before writing the expected fragment length to `+0x2e`/`+0x2c`. Sits in the
`0x8004f8xx` hardware-hook neighborhood adjacent to `FUN_8004f824`.

Post-rename: **249 unnamed** in-region (163 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-11+
substantive candidate; skip rank-1–10 artifacts.

## Pass 52s (2026-06-30) — rank-10 LMP procedure busy-probe rename

**Refreshed cold-triage (rank-1–9 skipped as artifacts or already done):** rank-10
`0x8004e4bc` (62B, 3 callers) — substantive global conn-table LMP procedure
busy probe with link-mode mask.

**Rank-10 decompiled and renamed (HIGH):** `FUN_8004e4bc` →
`is_any_conn_lmp_procedure_busy_with_link_mode_mask_0x180` (62B) via
`RenamePass52sRegion80040000Fun8004e4bc.java` (`renamed=1`, live-verified).

```c
int is_any_conn_lmp_procedure_busy_with_link_mode_mask_0x180(void)
{
  for (conn_idx = 0; conn_idx < conn_table_count; conn_idx++) {
    conn_rec = conn_table[conn_idx];  // PTR_PTR_8004e4fc
    if (conn_rec != 0
        && (conn_rec+0x08 & 7) == 0      // pkt_modes low 3 bits clear
        && (conn_rec+0x1d & 2) != 0      // procedure-active flag
        && (conn_rec+0x1c & 0x180) != 0) // link-mode bits 0x80/0x100 set
      return 1;
  }
  return 0;
}
```

Boolean busy-procedure probe without index parameter: scans the global
connection table (`PTR_PTR_8004e4fc`) for any active link with pkt_modes clear,
procedure-active (`+0x1d` bit 2), and link-mode bits `0x180` set at `+0x1c`.
Sibling of `is_any_conn_lmp_procedure_busy_by_index` (`0x8004e500`, which
matches on `+0x20` low nibble instead). Used as stage-(b) busy gate in three
HCI command handlers (`FUN_8004996a`, `FUN_80049a2c`, `FUN_80049b40`) —
when non-zero, handlers return HCI status `0x0c` (Command Disallowed).

Post-rename: **248 unnamed** in-region (162 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-12+
substantive candidate; skip rank-1–11 artifacts.

## Pass 52t (2026-06-30) — rank-11 SCO slot-alignment helper rename

**Refreshed cold-triage (rank-1–10 skipped as artifacts or already done):** rank-11
`0x800445b8` (58B, 3 xrefs) — substantive SCO/eSCO slot-count alignment helper
in the HCI synchronous-connection setup cluster (sibling of
`pack_lmp_response_header_status_and_handle`).

**Rank-11 decompiled and renamed (HIGH):** `FUN_800445b8` →
`align_sco_packet_slots_to_max_interval_mod6_or_mod3` (58B) via
`RenamePass52tRegion80040000Fun800445b8.java` (`renamed=1`, live-verified).

```c
uint align_sco_packet_slots_to_max_interval_mod6_or_mod3(uint max_slots, uint packet_slots)
{
  max_slots &= 0xffff;
  rem = max_slots % 6;
  packet_slots &= 0xffff;
  delta = max_slots - packet_slots;
  if ((rem <= delta) || (rem = max_slots % 3, rem <= delta))
    packet_slots = max_slots - rem;
  return packet_slots;
}
```

Aligns SCO/eSCO packet-slot count to max-interval modulus boundaries: when the
gap between max slots and packet slots exceeds `max_slots % 6` (or else
`max_slots % 3`), snaps packet slots to `max_slots - remainder`. Invoked via
function pointer `PTR_DAT_8004a2fc` from
`HCI_Setup_Synchronous_Connection_handler` and
`HCI_Accept_Synchronous_Connection_Request_handler`; result stored at
conn_record `+0x1cc` and used to derive retransmission windows
(`(slot_count - 1) * 2`).

Post-rename: **247 unnamed** in-region (161 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-13+
substantive candidate; skip rank-1–12 artifacts.

## Pass 52u (2026-06-30) — rank-12 noirq AND-mask HW-channel dispatch rename

**Refreshed cold-triage (rank-1–11 skipped as artifacts or already done):** rank-12
`0x80042984` (3 xrefs, 32B) — substantive AND-mask indexed HW-channel table dispatch
without IRQ disable; lightweight twin of Pass 52b's irq-guarded variant.

**Rank-12 decompiled and renamed (HIGH):** `FUN_80042984` →
`and_mask_hw_channel_table_entry_indexed_dispatch_noirq` (32B) via
`RenamePass52uRegion80040000Fun80042984.java` (`renamed=1`, live-verified).

```c
void and_mask_hw_channel_table_entry_indexed_dispatch_noirq(uint index, ushort mask)
{
  table = DAT_800429a4;
  fptr_table = PTR_DAT_800429a8;
  (*fptr_table)(index & 0xffff, mask & table[index]);
}
```

AND-mask indexed dispatch without IRQ protection: AND-masks `mask` with the
per-index ushort HW-channel parameter table entry at `DAT_800429a4`, then calls
through the function-pointer table at `PTR_DAT_800429a8`. Structural noirq twin
of `and_mask_hw_channel_table_entry_and_indexed_dispatch` (`0x80043158`, 64B,
IRQ-disabled) in the SCO/eSCO HW channel parameter-commit cluster alongside
`or_merge_hw_channel_table_entry_and_indexed_dispatch` and
`mask_merge_hw_channel_table_entry_and_indexed_dispatch`. 3 xrefs.

Post-rename: **246 unnamed** in-region (160 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-13+
substantive candidate; skip rank-1–12 artifacts.

## Pass 52v (2026-06-30) — rank-16 conn-slot timing-window sweep rename

**Refreshed cold-triage (rank-1–12 skipped as artifacts or already done; ranks
13–15 are 1B artifacts):** rank-16 `0x8004012c` (144B, 2 xrefs) — substantive
linked conn-slot timing-window sweep/reschedule walker.

**Rank-16 decompiled and renamed (HIGH):** `FUN_8004012c` →
`sweep_linked_conn_slots_reschedule_timing_window_by_index_and_type` (144B) via
`RenamePass52vRegion80040000Fun8004012c.java` (`renamed=1`, live-verified).

```c
void sweep_linked_conn_slots_reschedule_timing_window_by_index_and_type(
     char type_byte, uint conn_index)
{
  // Walk singly-linked list at PTR_DAT_800401c0 (next at +0x408)
  // Match subrecord at node+0x40c: +0xd == big_ol_struct[conn_index].bos_connection__array_index
  //                                    +0xf == type_byte
  // Gate on FUN_80040060 (LMP-25C scheduling-readiness check)
  // If +0x10 == 0 and (+6 + +4) <= +10: set +0xe pending, call FUN_8001840c; restart on success
  // If +0x10 == 1: clear +0x11 and +0x10
}
```

Walks the per-connection linked slot list, matching entries by connection-array
index and type byte, gating on the LMP-25C scheduling-readiness probe
(`FUN_80040060`). When the accumulated timing window (`ushort+6` + `ushort+4`)
has elapsed relative to the deadline at `ushort+10`, sets a pending flag at
`+0xe` and invokes `FUN_8001840c` to reschedule; on success restarts from list
head. Alternate path clears state bytes `+0x11`/`+0x10` when mode byte `+0x10`
is set. Sibling cluster to `dispatch_slot_timing_reprogram_if_pending_and_ready`
and the LMP-25C busy-wait gate family.

Post-rename: **245 unnamed** in-region (159 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-17+
substantive candidate; skip rank-1–16 artifacts.

## Pass 52w (2026-06-30) — rank-17 HCI link-policy setup handler rename

**Refreshed cold-triage (rank-1–16 skipped as artifacts or already done):** rank-17
`0x80047174` (122B, 2 xrefs) — substantive HCI command handler in the
link-policy/feature-toggle cluster (sibling of
`process_link_feature_toggle_command_and_send_status_event`).

**Rank-17 decompiled and renamed (HIGH):** `FUN_80047174` →
`hci_link_policy_param_setup_handler_send_cmd_complete` (122B) via
`RenamePass52wRegion80040000Fun80047174.java` (`renamed=1`, live-verified).

```c
uint hci_link_policy_param_setup_handler_send_cmd_complete(
     short *hci_cmd, undefined4 unused, int payload_len)
{
  ctrl = PTR_base_of_0x1ac_struct_array[0xb];
  state = ctrl.field96_0x60;
  if (state == 0) {
    ctrl.field96_0x60 = 2;
    if (PTR_PTR_800471f8[4] == 0)
      status = FUN_8004704c(hci_cmd, unused, payload_len - 0x7c);
    else
      status = 0xc;
  } else if ((*PTR_DAT_800471f4 & 0x10) == 0 || state == 2)
    status = FUN_8004704c(hci_cmd, unused, payload_len - 0x7c);
  else
    status = 0xc;  // Command Disallowed
  // Pack Command Complete (0xe) response: handle bytes + status
  hci_event_sender(0xe, &response, 4);
  return status;
}
```

Re-entrancy-gated HCI command handler on global control record `bos[0xb]`:
uses the same `field96_0x60` state byte as
`process_link_feature_toggle_command_and_send_status_event` (0→2 transition,
`0xc` when disallowed). Delegates parameter parsing/commit to still-unnamed
`FUN_8004704c` (bitmask iteration over link slots, per-link interval pairs,
tag-5 subrecord alloc via `lazy_alloc_tag5_singleton_and_encode_lowbit_index`).
Always terminates via `hci_event_sender(0xe, …)` (Command Complete). No direct
callers found (function-pointer registration).

Post-rename: **244 unnamed** in-region (158 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-18+
substantive candidate; skip rank-1–17 artifacts.

## Pass 52x (2026-06-30) — rank-18 LAP-slot triple inverted-mask HW dispatch rename

**Refreshed cold-triage (rank-1–17 skipped as artifacts or already done):** rank-18
`0x80042f74` (118B, 2 xrefs) — substantive LAP-slot gated triple inverted-mask
HW dispatch in the AFH/LAP cluster.

**Rank-18 decompiled and renamed (HIGH):** `FUN_80042f74` →
`triple_inverted_mask_hw_dispatch_for_lap_slot_if_0x45_clear` (118B) via
`RenamePass52xRegion80040000Fun80042f74.java` (`renamed=1`, live-verified).

```c
void triple_inverted_mask_hw_dispatch_for_lap_slot_if_0x45_clear(uint lap_slot)
{
  lap_slot = lap_slot & 0xff;
  if ((lap_slot < 4) &&
      (struct_of_at_least_0x300_size->_x142_LAP[lap_slot + 0x45] == 0))
  {
    idx = lap_slot * 2;
    // Read three per-slot ushort table entries
    val_a = table_a[idx];
    val_b = table_b[idx];
    val_c = table_c[idx];
    // Triple fptr dispatch: (value, ~lookup_table[value])
    fptr(val_a, ~lookup[val_a]);
    fptr(val_c, ~lookup[val_c]);
    fptr(val_b, ~lookup[val_b]);
  }
}
```

When LAP slot index 0–3 has its `_x142_LAP[slot+0x45]` byte clear (inactive slot),
reads three per-slot ushort parameter-table entries and calls the shared HW-write
function pointer at `PTR_DAT_80043000` three times with the inverted-mask idiom
`(value, ~lookup_table[value])` used throughout the SCO/eSCO HW-channel commit
cluster. AFH/LAP sibling of `set_inquiry_lap_slot_pending_bitmask` /
`release_inquiry_lap_slot_pending_bitmask` and the OR/AND/mask HW-channel dispatch
trio. 2 xrefs.

Post-rename: **243 unnamed** in-region (157 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-19+
substantive candidate; skip rank-1–18 artifacts.

## Pass 52y (2026-06-30) — rank-19 per-connection HW-state word init rename

**Refreshed cold-triage (rank-1–18 skipped as artifacts or already done):** rank-19
`0x8004f898` (98B, 2 xrefs) — substantive per-connection HW-state word initializer
in the SCO/eSCO slot-register programming cluster.

**Rank-19 decompiled and renamed (HIGH):** `FUN_8004f898` →
`init_per_connection_hw_state_word` (98B) via
`RenamePass52yRegion80040000Fun8004f898.java` (`renamed=1`, live-verified).

```c
void init_per_connection_hw_state_word(uint conn_idx)
{
  word_ptr = per_conn_table[(conn_idx & 0xff) * 4];
  old_val = *word_ptr;
  if ((old_val >> 0x18 & 0xffffff80) != 0) {
    possible_logging_function__var_args(..., conn_idx, old_val);
  }
  *word_ptr = (old_val & mask)
            | ((config_struct[0xa8] & 3) << 0x16)
            | (uint)exception_handler_fptr;
}
```

Per-connection indexed `uint` word init: reads current value at
`base + (conn_idx * 4)`, logs if high-byte guard bits set, writes back masked
value packing a 2-bit type code from struct `+0xa8` at bits [23:22] and an
exception-handler function pointer in the low bits. Called from
`program_esco_hw_slot_registers_with_secondary_record` and
`program_sco_hw_slot_registers_from_conn_record` — SCO/eSCO HW slot register
programming path. Sibling of the queue-scheduler helper trio in region
`0x80050000`.

Post-rename: **242 unnamed** in-region (156 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-21+
substantive candidate; skip rank-1–20 artifacts.

## Pass 52z (2026-06-30) — rank-20 inquiry/LAP slot pending-clear selector rename

**Refreshed cold-triage (rank-1–19 skipped as artifacts or already done):** rank-20
`0x80042f2c` (68B, 2 xrefs) — substantive inquiry/LAP slot selector in the
AFH/LAP bitmask cluster (sibling of `set_inquiry_lap_slot_pending_bitmask` /
`release_inquiry_lap_slot_pending_bitmask` / `triple_inverted_mask_hw_dispatch_for_lap_slot_if_0x45_clear`).

**Rank-20 decompiled and renamed (HIGH):** `FUN_80042f2c` →
`find_first_inquiry_lap_slot_with_pending_clear` (68B) via
`RenamePass52zRegion80040000Fun80042f2c.java` (`renamed=1`, live-verified).

```c
uint find_first_inquiry_lap_slot_with_pending_clear(void)
{
  ctx = PTR_DAT_80042f70;  // same layout as PTR_DAT_80042d2c / PTR_DAT_80042c90
  if (ctx[1] + ctx[0] != 1) return 0xff;
  if ((ctx[4] >> 1 & 1) == 0) return 0;
  if ((ctx[5] >> 1 & 1) == 0) return 1;
  if ((ctx[6] >> 1 & 1) == 0) return 2;
  if ((ctx[7] >> 1 & 1) == 0) return 3;
  return 0xff;
}
```

When exactly one inquiry/LAP slot is active (`active + refcount == 1`), scans
per-slot status bytes at `+4[slot]` (bit0 primary, bit1 pending per Pass 52n/o)
and returns the first slot index 0–3 whose pending bit is clear, or `0xff` if
none qualify. No direct callers found (likely function-pointer registration);
2 data xrefs. Selector sibling of the setter/release pair and the LAP-slot HW
dispatch helpers in the same `0x80042c00–0x80042f00` cluster.

Post-rename: **241 unnamed** in-region (155 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-21+
substantive candidate; skip rank-1–20 artifacts.

## Pass 52aa (2026-06-30) — rank-21 0x54-record free-list linker rename

**Refreshed cold-triage (rank-1–20 skipped as artifacts or already done):** rank-21
`0x8004e220` (64B, 2 xrefs) — substantive record-pool free-list linker in the
config-driven allocator cluster (sibling of `alloc_0x54_record_pool_and_ptr_table` /
`init_0xfc_record_pool_and_send_LMP_25B` in region `0x80050000`).

**Rank-21 decompiled and renamed (HIGH):** `FUN_8004e220` →
`link_0x54_record_pool_into_free_list_by_config_count` (64B) via
`RenamePass52aaRegion80040000Fun8004e220.java` (`renamed=1`, live-verified).

```c
void link_0x54_record_pool_into_free_list_by_config_count(void)
{
  head = PTR_PTR_8004e260;
  *head = 0;
  n = config.field468_0x1e0 & 0x1f;
  for (i = 0; i < n; i++) {
    rec = PTR_PTR_8004e264 + i * 0x54;
    *rec = *head;   // next pointer
    *head = rec;
  }
  meta = PTR_PTR_8004e270;
  meta[1] = n;
  meta[0] = PTR_PTR_8004e26c;
  meta[2] = 0;
}
```

Builds a singly-linked free list through the first dword of each pre-allocated
0x54-byte record; count from config `field468_0x1e0` low 5 bits. Head at
`PTR_PTR_8004e260`, pool base at `PTR_PTR_8004e264`, metadata triple at
`PTR_PTR_8004e270`. Called by `alloc_0x54_record_pool_and_ptr_table`
(`0x800524b8`) and `init_0xfc_record_pool_and_send_LMP_25B` (`0x80052344`)
after struct-pool allocation — shared linking step in the record-pool init family.

Post-rename: **240 unnamed** in-region (154 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-22+
substantive candidate; skip rank-1–21 artifacts.

## Pass 52ab (2026-06-30) — rank-22 timer-queue deadline reschedule rename

**Refreshed cold-triage (rank-1–21 skipped as artifacts or already done):** rank-22
`0x8004ee50` (60B, 2 xrefs) — substantive budget-gated deadline reschedule helper in
the timer/event-queue cluster (sibling of `sorted_event_list_insert_by_relative_key`
`0x8004ee94` and dispatch routine `FUN_8004ef08`).

**Rank-22 decompiled and renamed (HIGH):** `FUN_8004ee50` →
`try_reschedule_timer_queue_entry_deadline_within_budget` (60B) via
`RenamePass52abRegion80040000Fun8004ee50.java` (`renamed=1`, live-verified).

```c
uint try_reschedule_timer_queue_entry_deadline_within_budget(int *entry, uint now_half)
{
  anchor = (now_half + 2) & wrap_mask;
  budget = ((entry->deadline + entry->delta) & wrap_mask) - anchor & wrap_mask;
  if (((budget & threshold_mask) == 0) && (budget != 0)) {
    if ((entry->deadline - anchor & threshold_mask) != 0) {
      entry->deadline = anchor;
      entry->delta = (short)budget;
    }
    return 0;  // in-budget: keep in queue
  }
  FUN_8004ee0c(entry, now_half);  // snap deadline forward
  return 1;  // out-of-budget: caller unlinks/requeues
}
```

Wraparound-masked deadline arithmetic on queue entry fields `+0xc` (deadline) and
`+0x1c` (short delta); masks `DAT_8004ee8c`/`DAT_8004ee90`. Returns 0 when the
entry can stay in-place, 1 when `FUN_8004ee0c` must snap the deadline and the caller
(`FUN_8004ef08`) unlinks/reinserts via `sorted_event_list_insert_by_relative_key`.
Pure internal scheduler infra — same evidentiary class as the already-HIGH
`0x8004ee94` insert primitive.

Post-rename: **239 unnamed** in-region (153 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-23+
substantive candidate; skip rank-1–22 artifacts.

## Pass 52ac (2026-06-30) — rank-23 global LMP procedure busy probe rename

**Refreshed cold-triage (rank-1–22 skipped as artifacts or already done):** rank-23
`0x8004e480` (56B, 2 xrefs) — substantive global conn-table LMP procedure busy
probe in the permission-gate cluster (sibling of `is_any_conn_lmp_procedure_busy_by_index`
and `is_any_conn_lmp_procedure_busy_with_link_mode_mask_0x180`).

**Rank-23 decompiled and renamed (HIGH):** `FUN_8004e480` →
`is_any_conn_lmp_procedure_busy` (56B) via
`RenamePass52acRegion80040000Fun8004e480.java` (`renamed=1`, live-verified).

```c
byte is_any_conn_lmp_procedure_busy(void)
{
  for (conn_idx = 0; conn_idx < conn_table_count; conn_idx++) {
    conn_rec = conn_table[conn_idx];  // PTR_PTR_8004e4b8
    if (conn_rec != 0
        && (conn_rec+0x08 & 7) == 0      // pkt_modes low 3 bits clear
        && (conn_rec+0x1d & 2) != 0)    // procedure-active flag
      return 1;
  }
  return 0;
}
```

Unfiltered global busy-procedure probe: scans the connection table for any active
link with pkt_modes clear and procedure-active bit set — same `+0x08`/`+0x1d`
criteria as `is_any_conn_lmp_procedure_busy_by_index` but without the index/nibble
filter at `+0x20`, and without the link-mode mask `0x180` check at `+0x1c` that
Pass 52s's sibling applies. Single caller:
`le_channel_selection_algorithm_event_dispatch` (`0x80055204`, region `0x80050000`)
uses the result to gate status-byte update after LE Channel Selection Algorithm
meta-subevent dispatch.

Post-rename: **238 unnamed** in-region (152 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-25+
substantive candidate; skip rank-1–24 artifacts.

## Pass 52ad (2026-06-30) — rank-24 0xfc record-pool free-list push rename

**Refreshed cold-triage (rank-1–23 skipped as artifacts or already done):** rank-24
`0x8004e204` (22B, 2 xrefs) — substantive singly-linked-list LIFO prepend primitive
in the 0xfc-record-pool allocator cluster (sibling of `free_list_lifo_push` and
`link_0x54_record_pool_into_free_list_by_config_count`).

**Rank-24 decompiled and renamed (HIGH):** `FUN_8004e204` →
`free_list_lifo_push_0xfc_record_pool` (22B) via
`RenamePass52adRegion80040000Fun8004e204.java` (`renamed=1`, live-verified).

```c
void free_list_lifo_push_0xfc_record_pool(undefined4 *node)
{
  head = PTR_PTR_8004e21c;
  *node = *head;    // node->next = current head
  *head = node;     // head = node
}
```

Trivial singly-linked-list LIFO push onto the 0xfc-record-pool free-list head at
`PTR_PTR_8004e21c` — same idiom as `free_list_lifo_push` (`0x8004e808`, head at
`PTR_PTR_8004e818`) but without a NULL guard. Consumed by `link_0xfc_record_pool_into_free_list_by_config_count` (`0x8004e1c4`, 64B),
which loops over config-counted `0xfc`-byte records and prepends each via this
helper — structural parallel to `link_0x54_record_pool_into_free_list_by_config_count`
(`0x8004e220`) and region-`0x80050000`'s `alloc_and_link_0xfc_record_pool`.

Post-rename: **237 unnamed** in-region (151 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-25+
substantive candidate; skip rank-1–24 artifacts.

## Pass 52ae (2026-06-30) — rank-16 conn-diagnostic batch logger rename

**Refreshed cold-triage (ranks 1-24 skipped as artifacts or already done; ranks
25-30 are 1-4B stubs):** rank-16 `0x8004f910` (126B, 2 xrefs) — substantive
conn-diagnostic batch logging helper in the diagnostic-dump cluster (callee of
`collect_and_dispatch_conn_diagnostic_batches` in region `0x80050000`).

**Rank-16 decompiled and renamed (HIGH):** `FUN_8004f910` →
`log_conn_diagnostic_batch_up_to_five_entries` (126B) via
`RenamePass52aeRegion80040000Fun8004f910.java` (`renamed=1`, live-verified).

```c
void log_conn_diagnostic_batch_up_to_five_entries(
    byte batch_idx, byte count, byte mode,
    byte *field0x10_arr, ushort *field0x20_arr, byte *field0x2b_arr)
{
  possible_logging_function__var_args(
      6, 0xd2, 0x594, 0xd2a, 0x13, PTR_unknown_dat_ref_by_logger_8004f990,
      batch_idx, *PTR_DAT_8004f994 & 1, count, mode,
      field0x10_arr[0..4], field0x20_arr[0..4], field0x2b_arr[0..4]);
}
```

Formats up to five per-connection diagnostic triples (byte at conn_rec `+0x10`,
ushort at `+0x20`, nibble at `+0x2b`) into a single varargs logger call.
Called once per full batch of five and once for any partial trailing batch by
`collect_and_dispatch_conn_diagnostic_batches` (`0x80050194`, region
`0x80050000`); sibling of the already-HIGH `conn_diagnostic_batch_dump`
(`0x80050304`) and `diagnostic_batch_entry_log_emit` (`0x8004fe64`).

Post-rename: **236 unnamed** in-region (150 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-17+
substantive candidate; skip rank-1–16 artifacts and already-done ranks.

## Pass 52af (2026-06-30) — rank-17 dual record-pool free-list linker rename

**Refreshed cold-triage (ranks 1-16 skipped as artifacts or already done):** rank-17
`0x8004e878` (114B, 2 xrefs) — substantive config-driven free-list wiring for
0x60- and 0x108-byte record pools (callee of
`alloc_0x60_and_0x108_record_pools_from_config_and_wire` in region `0x80050000`).

**Rank-17 decompiled and renamed (HIGH):** `FUN_8004e878` →
`link_0x60_and_0x108_record_pools_into_free_lists_by_config` (114B) via
`RenamePass52afRegion80040000Fun8004e878.java` (`renamed=1`, live-verified).

```c
void link_0x60_and_0x108_record_pools_into_free_lists_by_config(void)
{
  *PTR_PTR_8004e8ec = 0;  // 0x60-pool free-list head
  *PTR_PTR_8004e8f0 = 0;  // 0x108-pool free-list head
  for (i = 0; i < (config+0x1e1 >> 2 & 0x1f); i++) {
    record = PTR_PTR_8004e8f4 + i * 0x60;
    *record = *PTR_PTR_8004e8ec;  // record->next = head
    *PTR_PTR_8004e8ec = record;   // head = record
  }
  for (i = 0; i < (((config+0x1e2 & 0xf) << 1) | (config+0x1e1 >> 7)); i++) {
    record = PTR_PTR_8004e8fc + i * 0x108;
    *record = *PTR_PTR_8004e8f0;
    *PTR_PTR_8004e8f0 = record;
  }
}
```

Clears two free-list heads, then LIFO-prepends config-counted arrays of
`0x60`-byte and `0x108`-byte records into singly-linked chains. Counts derive
from `PTR_config_base` fields `0x1e1`/`0x1e2` — same config fields consumed by
caller `alloc_0x60_and_0x108_record_pools_from_config_and_wire` (`0x80052458`).
Structural sibling of `link_0x54_record_pool_into_free_list_by_config_count` and
`alloc_and_link_0xfc_record_pool` in the record-pool init cluster invoked from
`init_record_pools_and_related_substates`.

Post-rename: **235 unnamed** in-region (149 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-19+
substantive candidate; skip rank-1–18 artifacts and already-done ranks.

## Pass 52ag (2026-06-30) — rank-18 TX reassembly buffer segment walker rename

**Refreshed cold-triage (ranks 1-17 skipped as artifacts or already done):** rank-18
`0x8004a5ac` (68B, 2 xrefs) — substantive TX reassembly buffer walker in the
connection/feature dispatch cluster (`FUN_8004ce70`, 908B).

**Rank-18 decompiled and renamed (HIGH):** `FUN_8004a5ac` →
`walk_tx_reassembly_buffer_consuming_length_prefixed_segments` (68B) via
`RenamePass52agRegion80040000Fun8004a5ac.java` (`renamed=1`, live-verified).

```c
/* returns total bytes consumed (v0) */
int walk_tx_reassembly_buffer_consuming_length_prefixed_segments(byte *buf, uint budget)
{
  uint offset = 0;
  while (budget != 0) {
    byte len_byte = buf[offset + 1];
    byte adjusted = len_byte + 2;
    if ((len_byte + 2U & 1) != 0) adjusted = len_byte + 3;  /* align to even */
    uint seg_len = *PTR_DAT_8004a5f0 * 2 + 6 + adjusted;
    if (budget < seg_len) break;
    budget -= seg_len;
    offset += seg_len;
  }
  return offset;
}
```

Walks a TX reassembly staging buffer (`PTR_DAT_8004d20c` in caller), consuming
variable-length segments whose size derives from each segment's length byte at
`buf[offset+1]`, a global scale factor at `PTR_DAT_8004a5f0`, and a fixed +6
overhead. Called from `FUN_8004ce70` during type-0 multi-chunk TX reassembly
after each `FUN_8002b558` HW-TX submit — return value subtracted from remaining
buffer budget before `FUN_8004ae74` finalizes the fragment. Connection/feature
dispatch cluster sibling of `walk_le_tx_segments_validate_slot10_clock_offset_and_return_count` (adjacent segment parser).

Post-rename: **234 unnamed** in-region (148 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-20+
substantive candidate; skip rank-1–19 artifacts and already-done ranks.

## Pass 52ah (2026-06-30) — rank-19 BOS-slot pending-queue purge wrapper rename

**Refreshed cold-triage (ranks 1-18 skipped as artifacts or already done):** rank-19
`0x800443fc` (46B, 2 xrefs) — substantive thin wrapper indexing `big_ol_struct[conn_idx]`
and dispatching pending-queue node purge via `FUN_8006aee4`.

**Rank-19 decompiled and renamed (HIGH):** `FUN_800443fc` →
`purge_pending_queue_nodes_for_bos_slot` (46B) via
`RenamePass52ahRegion80040000Fun800443fc.java` (`renamed=1`, live-verified).

```c
void purge_pending_queue_nodes_for_bos_slot(uint conn_idx, byte force_purge)
{
  slot = big_ol_struct[conn_idx & 0xffff];
  FUN_8006aee4(slot.bos_connection__array_index, slot.byte_0xCC, force_purge);
}
```

Indexes `big_ol_struct` by connection index, passes `bos_connection__array_index`,
`byte_0xCC` (slot/sub-index), and `force_purge` to `FUN_8006aee4` — which walks the
linked pending-queue at `PTR_DAT_8006af60`, matching nodes on `+0x19`/`+0x1a` and
optionally forcing purge when `force_purge==1` even if `+0x1b` is set. Callers:
`dual_slot_buffer_reassignment_on_role_switch` (force_purge=1 during IRQ-masked role-
switch buffer reassignment) and `possible_LMP_DETACH_handler` (force_purge=0 on LMP
detach teardown when pending counter hits zero). Connection-teardown cluster sibling
in the `0x800443xx`/`0x800445xx` SCO/eSCO slot path.

Post-rename: **233 unnamed** in-region (147 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-28+
substantive candidate; skip rank-1–27 artifacts and already-done ranks.

## Pass 52ai (2026-06-30) — rank-27 BOS subindex byte resolver rename

**Refreshed cold-triage (ranks 1-19 skipped as artifacts or already done; ranks
20-26 are 1-4B stubs):** rank-27 `0x80042e38` (150B, 1 xref) — substantive
BOS subindex (`byte_0xCC`) resolver for connection index in the inquiry/LAP
cluster (sibling of `set_inquiry_lap_slot_pending_bitmask` /
`release_inquiry_lap_slot_pending_bitmask` /
`find_first_inquiry_lap_slot_with_pending_clear`).

**Rank-27 decompiled and renamed (HIGH):** `FUN_80042e38` →
`resolve_bos_subindex_byte_for_connection_index` (150B) via
`RenamePass52aiRegion80040000Fun80042e38.java` (`renamed=1`, live-verified).

```c
char resolve_bos_subindex_byte_for_connection_index(uint conn_idx)
{
  conn_idx = conn_idx & 0xffff;
  if (hook_at_PTR_DAT_80042ed0 && hook(conn_idx, &out) != 0)
    return out;
  slot = big_ol_struct[conn_idx];
  out = slot.byte_0xCC;
  ctx = PTR_DAT_80042ed8;
  if (slot.bdaddr_random_ == 0) {
    if (ctx[0] != 0) out = ctx[2];
  } else {
    lap = PTR_struct_of_at_least_0x300_size_80042edc;
    if (1 < lap->_x142_LAP[(byte)ctx[2] + 0x45]) {
      /* scan inquiry/LAP slot pending bits at ctx+4..+7 (bit1) */
      out = highest_consecutive_pending_slot_index_or_0xff;
    }
  }
  return out;
}
```

Optional hook at `PTR_DAT_80042ed0` may override the default path. Otherwise
indexes `big_ol_struct[conn_idx]` and returns `byte_0xCC` (BOS subindex/slot
byte). Public BD_ADDR path (`bdaddr_random_==0`): when global gate byte at
`PTR_DAT_80042ed8[0]` is set, substitutes `ctx[2]`. Random BD_ADDR path: when
the LAP table entry `_x142_LAP[slot+0x45]` count exceeds 1, scans per-slot
status bytes at `ctx+4..+7` (bit1 pending pattern matching the inquiry/LAP
bitmask cluster) and returns the highest consecutive pending-slot index (0-3)
or `-1` when all four slots are pending. No direct callers found (likely
function-pointer registration). Consumer sibling of
`purge_pending_queue_nodes_for_bos_slot` which passes the resolved `byte_0xCC`
to `FUN_8006aee4`.

Post-rename: **232 unnamed** in-region (146 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-29+
substantive candidate; skip rank-1–28 artifacts and already-done ranks.

## Pass 52aj (2026-06-30) — rank-28 record-pool free-list diagnostic logger rename

**Refreshed cold-triage (ranks 1-27 skipped as artifacts or already done):** rank-28
`0x8004f4e8` (138B, 1 xref) — substantive record-pool diagnostic logger in the
`0x8004f5xx` cluster (sibling of `log_conn_diagnostic_batch_up_to_five_entries`).

**Rank-28 decompiled and renamed (HIGH):** `FUN_8004f4e8` →
`log_record_pool_four_free_list_counts_and_head_state` (138B) via
`RenamePass52ajRegion80040000Fun8004f4e8.java` (`renamed=1`, live-verified).

```c
void log_record_pool_four_free_list_counts_and_head_state(void)
{
  ctx = PTR_DAT_8004f574;
  head = *(void **)(ctx + 4);
  if (head == 0) { head_byte = 0; head_dword = 0; }
  else { head_byte = *(byte *)(head + 8); head_dword = *(dword *)(head + 0xc); }
  count0 = walk_singly_linked_list(*(void **)(ctx + 8));
  count1 = walk_singly_linked_list(*(void **)(ctx + 0x10));
  count2 = walk_singly_linked_list(*(void **)(ctx + 0x18));
  count3 = walk_singly_linked_list(*PTR_PTR_8004f578);
  possible_logging_function__var_args(
      6, 0xd2, 0x2263, 0xd4c, 9, PTR_unknown_dat_ref_by_logger_8004f57c,
      head, head_byte, head_dword, count0, count1, count2, count3,
      *(dword *)(ctx + 0x24), *(dword *)(ctx + 0x28));
}
```

Walks four singly-linked free lists rooted at `PTR_DAT_8004f574+8/+0x10/+0x18` and
`PTR_PTR_8004f578`, counts elements in each, reads head-record byte/dword fields
from the pointer at `ctx+4`, and emits a single varargs diagnostic log via
`possible_logging_function__var_args`. Record-pool allocator diagnostic sibling of
`log_conn_diagnostic_batch_up_to_five_entries` and the `0x8004e2xx`/`0x8004e8xx`
free-list init cluster (`link_0x54_record_pool_into_free_list_by_config_count`,
`link_0x60_and_0x108_record_pools_into_free_lists_by_config`,
`free_list_lifo_push_0xfc_record_pool`). No direct callers found (likely
function-pointer registration).

Post-rename: **231 unnamed** in-region (145 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-30+
substantive candidate; skip rank-1–29 artifacts and already-done ranks.

## Pass 52ak (2026-06-30) — rank-29 HW-crypto digest HCI handler rename

**Refreshed cold-triage (ranks 1-28 skipped as artifacts or already done):** rank-29
`0x800492d8` (132B, 1 xref) — substantive HCI command handler in the upper-half
`0x80049xxx` cluster (direct caller of `hw_crypto_compute_8word_in_8word_out`).

**Rank-29 decompiled and renamed (HIGH):** `FUN_800492d8` →
`hci_hw_crypto_dual_block_digest_handler_send_cmd_complete` (132B) via
`RenamePass52akRegion80040000Fun800492d8.java` (`renamed=1`, live-verified).

```c
uint hci_hw_crypto_dual_block_digest_handler_send_cmd_complete(short *hci_cmd)
{
  optimized_memcpy(buf_a, hci_cmd + 3, 0x10);
  optimized_memcpy(buf_b, hci_cmd + 0x13, 0x10);
  hw_crypto_compute_8word_in_8word_out(buf_a, buf_b, digest_out);
  status_byte = (*hci_cmd == 0) ? 0 : global.field_0x165 ? global.field_0x165 : 1;
  pack_cmd_complete_response(status_byte, *hci_cmd, digest_out);
  hci_event_sender(0xe, &response, 0x14);
  return 0;
}
```

HCI command handler: copies two 16-byte input blocks from the command payload at
`+3` and `+0x13`, runs `hw_crypto_compute_8word_in_8word_out`, packs a 20-byte
Command Complete (event `0xe`) response with status bytes derived from the first
command word and `PTR_struct_of_at_least_0x300_size_8004935c.field_0x165`, plus
the 16-byte HW-crypto digest. Direct caller of `hw_crypto_compute_8word_in_8word_out`
(documented in region `0x80050000` Pass 35) — not a caller of
`match_feature_page_slot_by_hw_crypto_prefix_update_tlv` (Pass 12ga doc error
corrected). Sibling pattern of `hci_link_policy_param_setup_handler_send_cmd_complete`
(Pass 52w). No direct callers found (function-pointer registration).

Post-rename: **230 unnamed** in-region (144 in 1-150B tier).

## Pass 52al (2026-06-30) — rank-30 dual-slot ushort probe rename

**Refreshed cold-triage (ranks 1-29 skipped as artifacts or already done):** rank-30
`0x80043508` (128B, 1 xref) — substantive dual-slot buffer ushort probe in the
`0x800435xx` inquiry/LAP / role-switch cluster (sibling of
`reassign_inquiry_lap_slot_refcount_pending_and_program_channel` and
`dual_slot_buffer_reassignment_on_role_switch` in region `0x80030000`).

**Rank-30 decompiled and renamed (HIGH):** `FUN_80043508` →
`is_indexed_dual_slot_ushort_match_context_and_log` (128B) via
`RenamePass52alRegion80040000Fun80043508.java` (`renamed=1`, live-verified).

```c
bool is_indexed_dual_slot_ushort_match_context_and_log(uint index)
{
  table = PTR_DAT_80043588;
  expected = *(ushort *)(PTR_DAT_8004358c + 0xc);
  entry = table + (index & 0xff) * 0x84;
  active_slot = *entry & 1;
  if (entry[1] == 0) {
    if ((entry[active_slot * 0x40 + 0x30] != expected) &&
        (expected != entry[(!active_slot) * 0x40 + 0x30]))
      return false;
  } else {
    if (entry[1] != 1) return false;
    if (expected != entry[active_slot * 0x40 + 0x30]) return false;
  }
  possible_logging_function__var_args(6, 0x2c, 0xcfa, 0x667, 0, PTR_unknown_dat_ref_by_logger_80043590, 0);
  return true;
}
```

Indexed probe on the stride-0x84 per-role dual-slot buffer table at `PTR_DAT_80043588`:
bit 0 of `entry[0]` selects the active 0x40-byte slot; compares the ushort at
`entry[slot*0x40+0x30]` against the global expected value at `PTR_DAT_8004358c+0xc`.
When `entry[1]==0`, accepts a match on either active or alternate slot; when
`entry[1]==1`, requires the active slot only. On success emits a varargs diagnostic
log via `possible_logging_function__var_args`. Thin wrapper caller `FUN_80043594`
(6B thunk). Same dual-slot layout family as `dual_slot_buffer_reassignment_on_role_switch`
(region `0x80030000`) and LMP switch slot checks at `+0x30`/`+0x70` documented in
region `0x80070000`. 1 xref.

Post-rename: **229 unnamed** in-region (143 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-32+
substantive candidate; skip rank-1–31 artifacts and already-done ranks.

## Pass 52am (2026-06-30) — rank-31 list-A overflow collect + TX scheduler rename

**Refreshed cold-triage (ranks 1-30 skipped as artifacts or already done):** rank-31
`0x8004b6ec` (122B, 1 xref) — substantive per-connection TX queue orchestrator in the
`0x8004b4xx` cluster (list-A counterpart to `atomically_take_conn_list_b_and_apply_quota_overflow`
at `0x8004ca10`; sibling of `FUN_8004b468` TX fragmentation scheduler).

**Rank-31 decompiled and renamed (HIGH):** `FUN_8004b6ec` →
`atomically_take_conn_list_a_collect_overflow_and_schedule_tx` (122B) via
`RenamePass52amRegion80040000Fun8004b6ec.java` (`renamed=1`, live-verified).

```c
void atomically_take_conn_list_a_collect_overflow_and_schedule_tx(uint conn_idx, uint budget)
{
  conn_idx = conn_idx & 0xff;
  budget = budget & 0xff;
  ctx = PTR_base_of_0x1ac_struct_array;
  irq = disable_interrupts();
  pending = ctx[conn_idx].field264_0x10f;
  saved_head = ctx[conn_idx].field307_0x140;
  if (pending != 0) {
    if (saved_head != 10) ctx[conn_idx].field307_0x140 = 0xa0a;
    pending = 1;
    ctx[conn_idx].field264_0x10f = 0;
  }
  enable_interrupts(irq);
  if (pending == 1) {
    FUN_8004b29c(&local_list, saved_head);
    if (local_list.count != 0)
      FUN_8004b3c0(&local_list, conn_idx, 0, 0);
  }
  FUN_8004b468(conn_idx, budget);
}
```

IRQ-off snapshot of list-A head index at `conn+0x140` (`field307_0x140`), gated on
`field264_0x10f`; when set, resets the head field to empty-list sentinel `0xa0a` (same
`init_connection_record` / `release_connection_record` default as list-B at `+0x144`).
After re-enabling interrupts, walks the saved head via `FUN_8004b29c` (slot-table chain
collecting overflow records into a local 3-word list), conditionally splices collected
records back via `FUN_8004b3c0` (list-A / `param_4==0` path at `field289_0x128`), then
always invokes `FUN_8004b468` (624B TX fragmentation scheduler over the `0x1ac` conn array).
List-A twin of `atomically_take_conn_list_b_and_apply_quota_overflow` (which uses `+0x144`,
`FUN_8004b1d0`, and optional `FUN_8004c940`). 1 xref (function-pointer registration).

Post-rename: **228 unnamed** in-region (142 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-33+
substantive candidate; skip rank-1–32 artifacts and already-done ranks.

## Pass 52an (2026-06-30) — rank-32 LMP/VSC eSCO recovery dispatch rename

**Refreshed cold-triage (ranks 1-31 skipped as artifacts or already done):** rank-32
`0x80048b80` (118B, 1 xref) — substantive VSC 0xFC95 / LMP 0x268 eSCO recovery
orchestrator in the `0x80048bxx` cluster (sibling of `conn_link_quality_history_reset_and_vsc_0xfc95_trigger`
and HCI VSC 0xFC67 bridge documented in `reverse_engineering_lmp_vsc_opcode_map.md`).

**Rank-32 decompiled and renamed (HIGH):** `FUN_80048b80` →
`dispatch_lmp_25c_25b_and_optional_vsc_fc95_lmp_268_recovery` (118B) via
`RenamePass52anRegion80040000Fun80048b80.java` (`renamed=1`, live-verified).

```c
void dispatch_lmp_25c_25b_and_optional_vsc_fc95_lmp_268_recovery(byte enable_fc95_268)
{
  conn = PTR_base_of_0x1ac_struct_array;  // index-0 conn record
  if (*(int *)&conn->field24_0x18 != -1) {
    LMP__25C_called1(*(int *)&conn->field24_0x18, 0);
    LMP__25B__most_common_for_VSCs1(conn->field24_0x18);
  }
  if (enable_fc95_268 != 0) {
    delay = *(ushort *)&conn->field22_0x16;
    if (delay < 0x65) {
      VSC_0xfc95_called2(1, conn->field24_0x18, PTR_LAB_80048cf4_1, 0, 0);
      LMP__268__most_common_for_VSCs2_checks_fptr_patch(
          **(int **)&conn->field24_0x18, (uint)delay * 1000);
    } else {
      *PTR_DAT_80048bfc = (uint)delay;
      VSC_0xfc95_called2(0, conn->field24_0x18, PTR_LAB_80048d04_1, 0, 0);
      LMP__268__most_common_for_VSCs2_checks_fptr_patch(
          **(int **)&conn->field24_0x18, DAT_80048c04);
    }
  }
}
```

When conn handle at `+0x18` is valid (`!= -1`), always fires `LMP__25C_called1` then
`LMP__25B__most_common_for_VSCs1`. When `enable_fc95_268` is set, branches on ushort
delay/count at `conn+0x16`: if `< 0x65` (101), calls `VSC_0xfc95_called2(1, …)` then
`LMP__268` with timeout `field22*1000` ms; else stores delay to `PTR_DAT_80048bfc`,
calls `VSC_0xfc95_called2(0, …)`, then `LMP__268` with constant `DAT_80048c04`. Same
established VSC 0xFC95 triad idiom as `conn_link_quality_history_reset_and_vsc_0xfc95_trigger`
and HCI VSC 0xFC67 (`LMP__25B` + `LMP__268(conn, delay×100)`). Operates on conn-array
index 0 only. 1 xref (function-pointer registration).

Post-rename: **227 unnamed** in-region (141 in 1-150B tier).

## Pass 52ao (2026-06-30) — rank-33 list-A tail append rename

**Refreshed cold-triage (ranks 1-32 skipped as artifacts or already done):** rank-33
`0x8004b0f8` (110B, 1 xref) — substantive IRQ-masked list-A byte enqueue in the
`0x8004b4xx` TX fragmentation scheduler cluster (callee of `FUN_8004b468`; sibling of
`insert_byte_into_per_connection_singly_linked_list_head_or_tail` and Pass 52am's
`atomically_take_conn_list_a_collect_overflow_and_schedule_tx`).

**Rank-33 decompiled and renamed (HIGH):** `FUN_8004b0f8` →
`irq_masked_append_byte_to_conn_list_a_tail` (110B) via
`RenamePass52aoRegion80040000Fun8004b0f8.java` (`renamed=1`, live-verified).

```c
void irq_masked_append_byte_to_conn_list_a_tail(byte value, uint conn_idx)
{
  conn_idx = conn_idx & 0xff;
  if (value != '\n') {  // 0x0a empty-list sentinel
    saved = disable_interrupts__clear_LSBit_of_CP0_Status_Register_();
    ctx = PTR_base_of_0x1ac_struct_array;
    if (ctx[conn_idx].field307_0x140 == '\n')
      ctx[conn_idx].field307_0x140 = value;           // init head
    else
      node_pool[(byte)ctx[conn_idx].field308_0x141 * 0xc + 0xb] = value;  // append via tail index
    ctx[conn_idx].field308_0x141 = value;             // update tail
    ctx[conn_idx].field309_0x142++;                   // increment count
    enable_interrupts__set_CP0_Status_to_arg_(saved);
  }
}
```

When `value` is not the empty-list sentinel `0x0a`, IRQ-disabled critical section appends
`value` to per-connection list-A at `conn+0x140` (head index), `+0x141` (tail index),
`+0x142` (count): if head is empty (`== 0x0a`), sets head directly; else chains through
the shared `0xc`-byte node pool at `PTR_PTR_8004b16c` indexed by current tail. Same
sentinel and field layout as `insert_byte_into_per_connection_singly_linked_list_head_or_tail`
and `atomically_take_conn_list_a_collect_overflow_and_schedule_tx`. Callee of the 624B TX
fragmentation scheduler `FUN_8004b468`. 1 xref.

Post-rename: **226 unnamed** in-region (140 in 1-150B tier).

## Pass 52ap (2026-06-30) — rank-34 SCO slot offset delta rename

**Refreshed cold-triage (ranks 1-33 skipped as artifacts or already done):** rank-34
`0x80043438` (106B, 1 xref) — substantive SCO slot timing offset calculator in the
`0x800434xx` cluster (callee of `apply_SCO_connection_params_to_hw` alongside
still-unnamed `FUN_80043400` clock-phase spin-wait).

**Rank-34 decompiled and renamed (HIGH):** `FUN_80043438` →
`compute_sco_slot_offset_delta_from_hw_clock` (106B) via
`RenamePass52apRegion80040000Fun80043438.java` (`renamed=1`, live-verified).

```c
uint compute_sco_slot_offset_delta_from_hw_clock(
    ushort slot_period, ushort target_offset, uint flags, byte conn_idx, ushort *out_delta)
{
  FUN_80034a24(&clock_raw, conn_idx);
  if ((flags & 2) != 0) clock_raw ^= DAT_800434a8;
  phase = (clock_raw >> 1) & DAT_800434a4;
  remainder = phase % slot_period;
  if (remainder == target_offset) *out_delta = slot_period;
  else if (remainder < target_offset) *out_delta = target_offset - remainder;
  else *out_delta = (target_offset + slot_period) - remainder;
  return phase;
}
```

Reads HW clock via `FUN_80034a24` (conn-indexed BOS subindex reader), optionally XOR-adjusts
when `flags&2`, masks to slot phase, computes modulo `slot_period`, then writes wrapped
delta slots until `target_offset` into `*out_delta`. Paired with `FUN_80043400` (spin until
clock bit-2 toggles) inside interrupt-bracketed `apply_SCO_connection_params_to_hw`.
1 xref (direct call from `0x8003d7bc`).

Post-rename: **225 unnamed** in-region (139 in 1-150B tier).

## Pass 52aq (2026-06-30) — rank-35 LAP/role-record reset rename

**Refreshed cold-triage (ranks 1-34 skipped as artifacts or already done):** rank-35
`0x80043bfc` (104B, 1 xref) — substantive 4-connection LAP + dual-slot role-record
initializer in the inquiry/LAP/role-switch cluster (`0x800435xx` siblings).

**Rank-35 decompiled and renamed (HIGH):** `FUN_80043bfc` →
`reset_four_conn_lap_and_dual_slot_role_records` (104B) via
`RenamePass52aqRegion80040000Fun80043bfc.java` (`renamed=1`, live-verified).

```c
void reset_four_conn_lap_and_dual_slot_role_records(void)
{
  *global_flag_dword = 0;
  *global_mode_byte = 0xff;
  for (conn_idx = 0; conn_idx < 4; conn_idx++) {
    big_ol_struct->_x142_LAP[conn_idx + 0x45] = 0;
    entry = role_table + conn_idx * 0x84;
    entry[0] &= 0xfc;          // keep slot-selector bits 0-1
    entry[1] = 2;              // default role/type byte
    entry[2] = entry[3] = 0;
    entry[0x32] = entry[0x72] = 0;
    per_conn_flag[conn_idx] = 0;
  }
  memset(aux_8byte_buf, 0, 8);
}
```

Clears two globals, zeroes four LAP entries at `big_ol_struct+0x142`, resets each
0x84-stride dual-slot role record (slot selector in `entry[0]&3`, type byte `+1`=2,
clears `+0x32`/`+0x72` per-slot fields matching `is_indexed_dual_slot_ushort_match_context_and_log`
layout), clears per-connection flag bytes, then zeroes an 8-byte auxiliary buffer.
1 xref.

Post-rename: **224 unnamed** in-region (138 in 1-150B tier).

## Pass 52ar (2026-06-30) — rank-36 HCI Reset teardown chain rename

**Refreshed cold-triage (ranks 1-35 skipped as artifacts or already done):** rank-36
`0x8004d220` (90B, 1 xref) — HCI Reset body releasing all connection records and
dispatching teardown function-pointer chain.

**Rank-36 decompiled and renamed (HIGH):** `FUN_8004d220` →
`release_all_conn_records_and_invoke_teardown_chain` (90B) via
`RenamePass52arRegion80040000Fun8004d220.java` (`renamed=1`, live-verified).

```c
void release_all_conn_records_and_invoke_teardown_chain(void)
{
  for (conn_idx = 0; conn_idx < 0xb; conn_idx++)
    release_connection_record(conn_idx, 1);
  if (*PTR_DAT_8004d27c != -1)
    LMP__25B__most_common_for_VSCs1();
  conn_array->field24_0x18 = PTR_DAT_8004d27c;
  (*(code *)*PTR_PTR_8004d284)();
  if ((config_base->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ & 0x10) != 0)
    (*(code *)*PTR_DAT_8004d28c)();
  (*(code *)*PTR_DAT_8004d290)(0);
}
```

Releases all 11 connection records (indices 0–10) via the already-HIGH
`release_connection_record`, optionally triggers `LMP__25B__most_common_for_VSCs1`
when a global flag dword is not `0xffffffff`, rebinds the `0x1ac` struct-array
`+0x18` pointer, then invokes three registered teardown hooks (middle hook
config-gated on `_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ & 0x10`). Sole caller
`fHCI_Reset_0x03_full_subsystem_teardown` (`0x8001f408`); downstream callee
`FUN_8004d294` (1280B SCO/eSCO HW init blob) documented in region `0x80050000`
Pass 49.

Post-rename: **223 unnamed** in-region (137 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-37+
substantive candidate; skip rank-1–36 artifacts and already-done ranks.

## Pass 52as (2026-06-30) — rank-37 LMP procedure slot arming rename

**Refreshed cold-triage (ranks 1-36 skipped as artifacts or already done):** rank-37
`0x8004e340` (86B, 1 xref) — substantive LMP procedure slot allocator in the
`0x8004e2xx` permission-gate cluster (sibling of `is_any_conn_lmp_procedure_busy_by_index`).

**Rank-37 decompiled and renamed (HIGH):** `FUN_8004e340` →
`arm_lmp_procedure_slot_pending_by_active_link_count` (86B) via
`RenamePass52asRegion80040000Fun8004e340.java` (`renamed=1`, live-verified).

```c
int arm_lmp_procedure_slot_pending_by_active_link_count(void)
{
  active_count = FUN_8004e2f4();  // counts links: pkt_modes clear, +0x20 bit0, +0x1d procedure-active
  ctx = PTR_DAT_8004e398;
  if ((active_count + (byte)ctx[1]) < 4) {
    slot_idx = 1;
    if ((ctx[10] >> 1 & 1) && (slot_idx = 2, (ctx[0x11] >> 1 & 1))) {
      if (ctx[0x18] >> 1 & 1) goto fail;
      slot_idx = 3;
    }
    ctx[slot_idx * 7 + 3] |= 2;   // arm pending bit on 7-byte stride slot record
    return slot_idx;
  }
fail:
  return 0xff;
}
```

Counts active procedure links via still-unnamed `FUN_8004e2f4` (same pkt_modes/
procedure-active criteria as the busy-probe family, plus `+0x20` bit0). When
`active_count + ctx[1] < 4`, cascades through three enable flags at `ctx+0x0a`,
`+0x11`, `+0x18` (bit1 each) to pick slot index 1–3, then sets bit2 on the
corresponding 7-byte stride record at `ctx[slot*7+3]`. Returns `0xff` when at
capacity. Sole caller `validate_unique_handles_and_commit_sync_conn_params`
(380B HCI synchronous-connection param commit handler): when `conn_rec+0x20`
bit0 set and procedure not yet active, calls this function — on `0xff` returns
HCI error `9`, else passes slot index to
`set_channel_slot_enable_refcount_and_conn_record_mode` before arming procedure
bit `+0x1d` and scheduling
`build_linked_conn_param_buffers_and_schedule_link_timing_setup`.

Post-rename: **222 unnamed** in-region (136 in 1-150B tier).

## Pass 52at (2026-06-30) — rank-38 LMP transaction-mode mapper rename

**Refreshed cold-triage (ranks 1-37 skipped as artifacts or already done):** rank-38
`0x80043718` (82B, 1 xref) — substantive BOS-subindex→LMP transaction-mode byte mapper
in the remote-name/role-switch cluster (`0x800437xx` siblings of
`remote_name_request_feature_index_selector`).

**Rank-38 decompiled and renamed (HIGH):** `FUN_80043718` →
`map_bos_subindex_to_lmp_txn_mode_when_bdaddr_random` (82B) via
`RenamePass52atRegion80040000Fun80043718.java` (`renamed=1`, live-verified).

```c
char map_bos_subindex_to_lmp_txn_mode_when_bdaddr_random(
    uint conn_idx, byte bos_subindex, char default_mode)
{
  if (big_ol_struct[conn_idx].bdaddr_random_ != '\0') {
    if (bos_subindex < 4)
      return bos_subindex + 8;   // map 0-3 → 8-11 for LMP PDU transaction mode
    possible_logging_function__var_args(2, 0x2c, 0xb79, 0xc34, 1, logger, bos_subindex);
    return '\0';
  }
  return default_mode;
}
```

When `bdaddr_random_` is set on the connection (post role-switch / random-address
state), remaps BOS subindex bytes 0–3 to LMP transaction-mode values 8–11; logs and
returns 0 on out-of-range subindex. When `bdaddr_random_` is clear, passes through
`default_mode` unchanged (typically the output of sibling
`resolve_bos_connection_index_for_subindex_when_bdaddr_public`). Sole caller
`FUN_8006fd20` (454B role-switch/LMP slot-offset completion handler in region
`0x80070000`): after `resolve_bos_subindex_byte_for_connection_index` and
`resolve_bos_connection_index_for_subindex_when_bdaddr_public`, stores mapped mode into pending LMP PDU staging struct at `+0x11`
before `FUN_80042ee0` + `FUN_8006f994` chain. Pairs with Pass 52ai's
`resolve_bos_subindex_byte_for_connection_index` and Pass 52p's
`classify_lmp_slot_offset_relation_masked` in the same role-switch path.

Post-rename: **221 unnamed** in-region (135 in 1-150B tier).

## Pass 52au (2026-06-30) — rank-39 SCO slot-align + retx-dims filler rename

**Refreshed cold-triage (ranks 1-38 skipped as artifacts or already done):** rank-39
`0x800445f4` (78B, 1 xref) — substantive SCO/eSCO packet-parameter struct filler in
the HCI synchronous-connection validator cluster (sibling of
`align_sco_packet_slots_to_max_interval_mod6_or_mod3`).

**Rank-39 decompiled and renamed (HIGH):** `FUN_800445f4` →
`align_sco_slots_and_derive_retx_buffer_dims` (78B) via
`RenamePass52auRegion80040000Fun800445f4.java` (`renamed=1`, live-verified).

```c
void align_sco_slots_and_derive_retx_buffer_dims(ushort *param_1)
{
  aligned = (*(code *)*PTR_DAT_80044644)(param_1[1], param_1[0]);
  param_1[2] = aligned;
  if (param_1[4] == 0)
    param_1[4] = (aligned - 1) * 2;
  if (param_1[3] == 0)
    param_1[3] = param_1[4];
  uVar = (aligned - 1) * 2;
  if ((int)(uint)param_1[3] < (int)uVar)
    uVar = (uint)param_1[3];
  param_1[5] = (short)uVar;
  param_1[8] = aligned >> 1;
  iVar = aligned - 1;
  if (8 < iVar)
    iVar = 8;
  param_1[9] = (short)iVar;
}
```

Invokes slot-align hook at `PTR_DAT_80044644` (runtime `0x801206a8` — patch override
of `PTR_DAT_8004a2fc` / `align_sco_packet_slots_to_max_interval_mod6_or_mod3`) with
`(max_interval, packet_slots)` from the 0x14-byte per-entry struct at offsets `+0x22`/
`+0x24`, stores aligned slot count at `+0x26`, then fills derived retransmission-buffer
dimensions: `+0x28` default `(slots-1)*2`, `+0x2a` cap copy, `+0x2c` min of the two,
`+0x32` slots/2, `+0x34` min(slots-1, 8). Sole caller
`validate_and_stage_sco_packet_type_table_from_hci_params` (`0x80046900`, Pass 52ds):
called once per enabled packet-type entry after
copying interval/latency fields into the 0x14-stride allocation block.

Post-rename: **220 unnamed** in-region (134 in 1-150B tier).

## Pass 52av (2026-06-30) — rank-40 HCI-reset LMP-check buffer + BB-reg programmer rename

**Refreshed cold-triage (ranks 1-39 skipped as artifacts or already done):** rank-40
`0x8004ab0c` (70B, 1 xref) — substantive HCI-reset sub-step in the config-flag/BB-register
programming cluster (callee of region `0x80070000` Pass 12cw
`hci_reset_apply_bdaddr_scramble_and_patch_hooks` when `field_0xe & 0x20`).

**Rank-40 decompiled and renamed (HIGH):** `FUN_8004ab0c` →
`hci_reset_clear_lmp_check_buf_set_flag_and_program_bb_regs` (70B) via
`RenamePass52avRegion80040000Fun8004ab0c.java` (`renamed=1`, live-verified).

```c
void hci_reset_clear_lmp_check_buf_set_flag_and_program_bb_regs(void)
{
  buf = PTR_check_before_call_LMP_func_8004ab54;
  memset(buf, 0, 0x20);
  *(uint *)(buf + 0x1c) = 0xffffffff;
  *(uint *)(PTR_DAT_8004ab58 + 0x44) |= 1;
  write_bb_regs_0x212_quad_toggle_0x4000_bit_via_patch_hook();
  (*(code *)*PTR_DAT_8004ab60)(0x21a, *DAT_8004ab5c | 1);
}
```

Clears the 32-byte LMP pre-call check buffer at `PTR_check_before_call_LMP_func_8004ab54`,
seeds sentinel `0xffffffff` at `+0x1c`, sets config flag bit0 at `PTR_DAT_8004ab58+0x44`,
invokes Pass 12cy's `write_bb_regs_0x212_quad_toggle_0x4000_bit_via_patch_hook` (BB-reg
`0x212..0x218` quad toggle on config-flag state), then writes BB register `0x21a` with
`(*DAT_8004ab5c | 1)` via patch hook at `PTR_DAT_8004ab60`. Sole caller
`hci_reset_apply_bdaddr_scramble_and_patch_hooks` (`0x80079934`, region `0x80070000` Pass
12cw) when global config `field_0xe & 0x20` — HCI-reset continuation sibling of Passes
12cm–12cx.

Post-rename: **219 unnamed** in-region (133 in 1-150B tier).

## Pass 52aw (2026-06-30) — rank-41 LE chan-sel pending-list purge rename

**Refreshed cold-triage (ranks 1-40 skipped as artifacts or already done):** rank-41
`0x8004f9d8` (70B, 1 xref) — substantive LE channel-selection pending-list purge in
the `resolve_parent_context_by_role` / conn-index-byte cluster (sibling of Pass 52as's
`arm_lmp_procedure_slot_pending_by_active_link_count` callee `FUN_8004e2f4`).

**Rank-41 decompiled and renamed (HIGH):** `FUN_8004f9d8` →
`purge_le_chan_sel_pending_list_by_conn_index_byte` (70B) via
`RenamePass52awRegion80040000Fun8004f9d8.java` (`renamed=1`, live-verified).

```c
void purge_le_chan_sel_pending_list_by_conn_index_byte(int conn_rec)
{
  idx_byte = *(char *)(conn_rec + 0x10);
  list = *(int **)(PTR_DAT_8004fa20 + 0xc);
  while (node = list, node != NULL) {
    list = (int *)*node;
    parent = resolve_parent_context_by_role(node);
    if (*(char *)(parent + 0x10) == idx_byte) {
      if (*node == 0)
        *(int *)(PTR_DAT_8004fa20 + 0x10) = node[1];
      else
        *(int *)(*node + 4) = node[1];
      *(int *)node[1] = *node;
    }
  }
}
```

Walks the doubly-linked pending list at `PTR_DAT_8004fa20+0xc`, resolves each node's
effective connection context via the already-named `resolve_parent_context_by_role`,
and unlinks nodes whose parent `+0x10` conn-index byte matches `conn_rec+0x10`. Sole
caller `conditional_dispatch_LE_channel_selection_algorithm` (`0x80055320`, region
`0x80050000` Pass 24): when the active connection at `puVar2+8` differs from `param_1`,
purges stale pending entries before dispatching
`le_channel_selection_algorithm_event_dispatch(0x44, param_1, 0)`.

Post-rename: **218 unnamed** in-region (132 in 1-150B tier).

## Pass 52ax (2026-06-30) — rank-42 conn channel/param mismatch predicate rename

**Refreshed cold-triage (ranks 1-41 skipped as artifacts or already done):** rank-42
`0x8004f328` (66B, 1 xref) — substantive connection channel/param mismatch predicate
in the `conn_param_revalidate_if_dirty` cluster (sibling of
`conn_record_get_4byte_field_by_handle` per `reverse_engineering_conn_feature_dispatch.md`).

**Rank-42 decompiled and renamed (HIGH):** `FUN_8004f328` →
`conn_channel_param_mismatch_predicate` (66B) via
`RenamePass52axRegion80040000Fun8004f328.java` (`renamed=1`, live-verified).

```c
bool conn_channel_param_mismatch_predicate(int lmp_buf, int *param_2, int sub_rec)
{
  if (*(int *)(param_2 + 0x10) != 0) {
    return false;
  }
  flags = *(byte *)(sub_rec + 8);
  if ((flags & 1) == 0) {
    if ((flags & 2) == 0) {
      return false;
    }
    if (((flags == 6) && ((*(byte *)(lmp_buf + 2) & 0xc0) == 0x80)) &&
       (**(char **)(sub_rec + 0x20) == '\x01')) {
      return *(char *)(sub_rec + 0x1d) == '\x02';
    }
  }
  return true;
}
```

Returns true when channel/param flags indicate a mismatch needing re-sync (caller
`conn_param_revalidate_if_dirty` at `0x80050ff8` sets sub-record `+0x11` bit4 on
non-zero result). Short-circuits false when `param_2+0x10` is non-zero. Flag byte at
`sub_rec+8`: bit0 alone → true; bit1-only path has a special eSCO case (flags==6,
LMP opcode `0x80` class, codec ptr `+0x20`==1) comparing `sub_rec+0x1d` against 2.
Sole caller `conn_param_revalidate_if_dirty` (region `0x80050000`, also reached via
`lmp_esco_sco_negotiation_packet_handler` / `FUN_80052c64`).

Post-rename: **217 unnamed** in-region (131 in 1-150B tier).

## Pass 52ay (2026-06-30) — rank-43 timer-queue deadline snap rename

**Refreshed cold-triage (ranks 1-42 skipped as artifacts or already done):** rank-43
`0x8004ee0c` (60B, 1 xref) — substantive out-of-budget deadline snap helper in the
timer/event-queue cluster (callee of `try_reschedule_timer_queue_entry_deadline_within_budget`
`0x8004ee50`, sibling of `sorted_event_list_insert_by_relative_key` `0x8004ee94`).

**Rank-43 decompiled and renamed (HIGH):** `FUN_8004ee0c` →
`snap_timer_queue_entry_deadline_to_next_period` (60B) via
`RenamePass52ayRegion80040000Fun8004ee0c.java` (`renamed=1`, live-verified).

```c
void snap_timer_queue_entry_deadline_to_next_period(int entry, int now_half)
{
  period = *(ushort *)(entry + 0x18);
  wrap_mask = DAT_8004ee4c;
  threshold_mask = DAT_8004ee48;
  deadline = (period + *(ushort *)(entry + 0x1c) + *(uint *)(entry + 0xc))
             - *(ushort *)(entry + 0x1a) & wrap_mask;
  delta_from_now = deadline - now_half & wrap_mask;
  if ((threshold_mask & delta_from_now) != 0) {
    if (period == 0) trap(7);
    slots = ((period - 1) + (-delta_from_now & wrap_mask)) / period;
    deadline = slots * period + deadline & wrap_mask;
  }
  *(uint *)(entry + 0xc) = deadline;
  *(ushort *)(entry + 0x1c) = *(ushort *)(entry + 0x1a);
}
```

Wraparound-masked deadline snap on queue entry fields `+0xc` (deadline uint),
`+0x18` (period ushort), `+0x1a` (anchor ushort copied to `+0x1c` delta), and
`+0x1c` (short delta). When `delta_from_now` crosses the threshold mask relative
to `now_half`, rounds the deadline forward to the next `period` boundary via integer
division; traps if period is zero. Sole caller
`try_reschedule_timer_queue_entry_deadline_within_budget` (`0x8004ee50`, Pass 52ab):
invoked on the out-of-budget path before returning 1 so the caller (`FUN_8004ef08`)
can unlink/reinsert via `sorted_event_list_insert_by_relative_key`.

Post-rename: **216 unnamed** in-region (130 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-44+
substantive candidate; skip rank-1–43 artifacts and already-done ranks.

## Pass 52az (2026-06-30) — rank-44 LMP PDU staging slot-param filler rename

**Refreshed cold-triage (ranks 1-43 skipped as artifacts or already done):** rank-44
`0x80042ee0` (56B, 1 xref) — substantive LMP PDU staging ushort-pair loader in the
role-switch / transaction-mode cluster (callee chain after Pass 52at's
`map_bos_subindex_to_lmp_txn_mode_when_bdaddr_random` in `FUN_8006fd20`).

**Rank-44 decompiled and renamed (HIGH):** `FUN_80042ee0` →
`fill_lmp_pdu_staging_slot_params_from_txn_mode` (56B) via
`RenamePass52azRegion80040000Fun80042ee0.java` (`renamed=1`, live-verified).

```c
void fill_lmp_pdu_staging_slot_params_from_txn_mode(void)
{
  staging = PTR_DAT_80042f18;
  txn_mode = (uint)(byte)staging[0x11];
  mode_idx = txn_mode - 8 & 0xff;
  if (mode_idx < 4) {
    idx = mode_idx * 2;
    val_a = *(ushort *)(PTR_DAT_80042f1c + idx);
    val_b = *(ushort *)(PTR_DAT_80042f20 + idx);
  }
  else {
    if (0xb < txn_mode) return;
    val_a = (ushort)(byte)PTR_DAT_80042f24[txn_mode];
    val_b = (ushort)(byte)PTR_DAT_80042f28[txn_mode];
  }
  *(ushort *)(staging + 8) = val_a;
  *(ushort *)(staging + 10) = val_b;
}
```

Reads the LMP transaction-mode byte at staging struct `+0x11` (written by
`map_bos_subindex_to_lmp_txn_mode_when_bdaddr_random` on the role-switch path).
For modes 8–11 (`txn_mode-8 < 4`), loads a ushort pair from ushort lookup tables
`PTR_DAT_80042f1c`/`PTR_DAT_80042f20`; for modes 0–11, loads from byte tables
`PTR_DAT_80042f24`/`PTR_DAT_80042f28` expanded to ushort. Writes the pair to
staging `+8` and `+10` before the `FUN_8006f994` continuation in
`FUN_8006fd20`. Sole caller `FUN_8006fd20` (role-switch/LMP slot-offset
completion handler, region `0x80070000`). Pairs with Pass 52at's txn-mode mapper
and Pass 52ai's `resolve_bos_subindex_byte_for_connection_index`.

Post-rename: **215 unnamed** in-region (129 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-45+
substantive candidate; skip rank-1–44 artifacts and already-done ranks.

## Pass 52ba (2026-06-30) — rank-45 IRQ-guarded HW-channel dual-dispatch rename

**Refreshed cold-triage (ranks 1-44 skipped as artifacts or already done):** rank-45
`0x80043070` (54B, 1 xref) — substantive IRQ-guarded raw dual indexed dispatch in the
SCO/eSCO HW-channel cluster (sibling of Pass 52g's mask-merge and Pass 52b's AND-mask
variants; uses fptr table `PTR_DAT_800430a8` without table read-modify-write).

**Rank-45 decompiled and renamed (HIGH):** `FUN_80043070` →
`irq_guarded_hw_channel_raw_dual_indexed_dispatch` (54B) via
`RenamePass52baRegion80040000Fun80043070.java` (`renamed=1`, live-verified).

```c
void irq_guarded_hw_channel_raw_dual_indexed_dispatch(uint slot_index)
{
  irq = disable_interrupts();
  fptr_table = PTR_DAT_800430a8;
  (*fptr_table)(10, (slot_index & 0xff) << 4);
  (*fptr_table)(0, 0xc);
  enable_interrupts(irq);
}
```

IRQ-disabled raw dual indexed dispatch through `PTR_DAT_800430a8`: first call commits
index 10 with `(slot_index << 4)`, second call commits index 0 with constant `0xc`.
No per-index ushort table merge — direct (index, value) pairs unlike the OR/AND/mask
trio (`or_merge_hw_channel_table_entry_and_indexed_dispatch`,
`mask_merge_hw_channel_table_entry_and_indexed_dispatch`,
`and_mask_hw_channel_table_entry_and_indexed_dispatch`). Sole caller
`fHCI_Disconnect_0x06` (region `0x80010000`): invoked on the `field310_0x278 == 3`
SCO-active disconnect path after clearing teardown globals and dispatching hook arg 3 —
commits HW-channel teardown slot params before LMP detach/unsniff continuation.

Post-rename: **214 unnamed** in-region (128 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-46+
substantive candidate; skip rank-1–45 artifacts and already-done ranks.

## Pass 52bb (2026-06-30) — rank-46 sorted-index-table lookup rename

**Refreshed cold-triage (ranks 1-45 skipped as artifacts or already done):** rank-46
`0x8004e190` (52B, 1 xref) — substantive lookup companion of Pass 51's
`insert_record_into_sorted_index_table_if_absent` / `binary_search_sorted_table_by_index_byte`
handle→conn_rec sorted-pointer-table cluster (documented in
`reverse_engineering_conn_record_subsystem.md` §8).

**Rank-46 decompiled and renamed (HIGH):** `FUN_8004e190` →
`lookup_record_ptr_by_index_byte_via_bsearch` (52B) via
`RenamePass52bbRegion80040000Fun8004e190.java` (`renamed=1`, live-verified).

```c
bool lookup_record_ptr_by_index_byte_via_bsearch(int *table, byte index_byte, undefined4 *out)
{
  int found_idx;
  bool found = binary_search_sorted_table_by_index_byte(table, index_byte, &found_idx);
  if (found) {
    *out = *(undefined4 *)(*table + found_idx * 4);
  }
  return found;
}
```

Binary-searches the sorted conn_rec-pointer table via
`binary_search_sorted_table_by_index_byte` (sort key = `conn_rec[+0x10]` LMP handle);
on hit, dereferences `table->array_base[found_idx]` and writes the record pointer to
`*out`. Structural twin of region `0x80050000`'s `lookup_record_ptr_by_key_via_bsearch`
(8-byte key variant). Insert/remove siblings:
`insert_record_into_sorted_index_table_if_absent` / `remove_record_from_sorted_index_table`
(not yet renamed). Widely reused by conn-record accessors (e.g.
`conn_record_get_4byte_field_by_handle`, LMP VSC hook entry, LE connection-complete
event senders — see conn-record subsystem docs).

Post-rename: **213 unnamed** in-region (127 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-47+
substantive candidate; skip rank-1–46 artifacts and already-done ranks.

## Pass 52bc (2026-06-30) — rank-47 teardown hook-triplet rename

**Refreshed cold-triage (ranks 1-46 skipped as artifacts or already done):** rank-47
`0x8004a4bc` (46B, 1 xref) — substantive config-gated teardown fptr-hook triplet in the
HCI Reset / SCO/eSCO HW-reset cluster (hook-only slice of Pass 52ar's
`release_all_conn_records_and_invoke_teardown_chain` tail; one of three callers of
unnamed `FUN_8004d294` per region `0x80050000` Pass 54 cross-region lead).

**Rank-47 decompiled and renamed (HIGH):** `FUN_8004a4bc` →
`invoke_teardown_hook_triplet_with_lmp_power_gate` (46B) via
`RenamePass52bcRegion80040000Fun8004a4bc.java` (`renamed=1`, live-verified).

```c
void invoke_teardown_hook_triplet_with_lmp_power_gate(void)
{
  puVar1 = (undefined4 *)PTR_PTR_8004a4ec;
  (*(code *)*puVar1)();
  pcVar2 = PTR_config_base_8004a4f0;
  if ((pcVar2->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ & 0x10) != 0) {
    puVar1 = (undefined4 *)PTR_DAT_8004a4f4;
    (*(code *)*puVar1)();
  }
  puVar1 = (undefined4 *)PTR_DAT_8004a4f8;
  (*(code *)*puVar1)(0);
}
```

Invokes three registered teardown fptr hooks in sequence: hook-1 always (via
`PTR_PTR_8004a4ec`); hook-2 gated on config bit `0x10` of
`_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` (via `PTR_DAT_8004a4f4`); hook-3 always
with literal arg `0` (via `PTR_DAT_8004a4f8`, likely `FUN_8004d294(0)` SCO/eSCO HW
subsystem init). Structurally identical to the tail of
`release_all_conn_records_and_invoke_teardown_chain` (`0x8004d220`, Pass 52ar) minus
the conn-record release preamble — intended for indirect fptr-table registration when
records are already released elsewhere. No static callers (consistent with
fptr-registration pattern); `find_callers` on downstream `FUN_8004d294` lists this
address as a caller (region `0x80050000` Pass 54 lead).

Post-rename: **212 unnamed** in-region (126 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-48+
substantive candidate; skip rank-1–47 artifacts and already-done ranks.

## Pass 52bd (2026-06-30) — rank-48 LMP pre-check entry clear + bitmask arm rename

**Refreshed cold-triage (ranks 1-47 skipped as artifacts or already done):** rank-48
`0x8004a6ec` (42B, 1 xref) — substantive per-connection LMP pre-check table entry
clear + active/inactive bitmask update in the conn-index status cluster (callee of
`conn_index_status_bit_apply_and_log` in region `0x80000000`; sibling of Pass 52av's
`hci_reset_clear_lmp_check_buf_set_flag_and_program_bb_regs` and Pass 52bc's teardown
hook triplet in the `0x8004a4xx`/`0x8004a6xx` LMP-check buffer neighborhood).

**Rank-48 decompiled and renamed (HIGH):** `FUN_8004a6ec` →
`clear_lmp_precheck_entry_and_arm_connection_active_bitmask` (42B) via
`RenamePass52bdRegion80040000Fun8004a6ec.java` (`renamed=1`, live-verified).

```c
void clear_lmp_precheck_entry_and_arm_connection_active_bitmask(uint conn_index)
{
  buf = PTR_check_before_call_LMP_func_8004a718;
  *(ushort *)(buf + (conn_index & 0xff) * 2) = 0;
  bit = (ushort)(1 << (conn_index & 0x1f));
  *(ushort *)(buf + 0x16) = *(ushort *)(buf + 0x16) | bit;
  *(ushort *)(buf + 0x18) = ~bit & *(ushort *)(buf + 0x18);
}
```

Clears the per-connection ushort entry in the LMP pre-call check buffer at
`PTR_check_before_call_LMP_func_8004a718`, then arms the connection in the dual
bitmask pair at `+0x16` (set) / `+0x18` (clear complement). Invoked from
`conn_index_status_bit_apply_and_log` (`0x80007330`) on the validated-connection
commit path after LMP-procedure-pending and slot-budget checks pass — marks the
connection index active in the global check-buffer state before downstream
`lmp_packet_completion_event_drain_dispatch` work.

Post-rename: **211 unnamed** in-region (125 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-49+
substantive candidate; skip rank-1–48 artifacts and already-done ranks.

## Pass 52be (2026-06-30) — rank-49 armed conn-slot status LSB query rename

**Refreshed cold-triage (ranks 1-48 skipped as artifacts or already done):** rank-49
`0x80042e10` (36B, 1 xref) — substantive armed-connection-slot status query in the
`0x80042exx` inquiry/LAP/role-switch cluster (sibling of Pass 52ai's
`resolve_bos_subindex_byte_for_connection_index` and Pass 52az's
`fill_lmp_pdu_staging_slot_params_from_txn_mode`).

**Rank-49 decompiled and renamed (HIGH):** `FUN_80042e10` →
`is_armed_conn_slot_status_lsb_clear` (36B) via
`RenamePass52beRegion80040000Fun80042e10.java` (`renamed=1`, live-verified).

```c
byte is_armed_conn_slot_status_lsb_clear(uint conn_index)
{
  if (((conn_index & 0xff) < 4) &&
     (slot_byte = PTR_DAT_80042e34[(conn_index & 0xff) + 4],
      ((slot_byte >> 1) & 1) != 0)) {
    return (slot_byte & 1) ^ 1;
  }
  return 0;
}
```

For connection indices 0–3, probes per-slot byte at `PTR_DAT_80042e34 + index + 4`.
When bit 1 is set (armed slot), returns inverted bit 0 (1 when LSB clear, 0 when set);
otherwise returns 0. Called from `role_switch_confirmation_matcher` (`0x80002b60`) when
the incoming parameter block's header field at `+3` bits 3–5 are zero — return value 1
selects the alternate per-connection table offset (`conn_index*8+2`) instead of the
default (`conn_index*8+1`) on the role-switch confirmation commit path.

Post-rename: **210 unnamed** in-region (124 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-50+
substantive candidate; skip rank-1–49 artifacts and already-done ranks.

## Pass 52bf (2026-06-30) — rank-50 one-shot armed callback dispatch rename

**Refreshed cold-triage (ranks 1-49 skipped as artifacts or already done):** rank-50
`0x8004f214` (36B, 1 xref) — substantive one-shot armed callback dispatcher in the
`0x8004f2xx` credit-scheduler / cmd-type-0xb dispatch cluster (sibling of Pass 52l's
`get_credit_scheduler_context_active_entry_ptr` and Pass 52bc's teardown-hook triplet).

**Rank-50 decompiled and renamed (HIGH):** `FUN_8004f214` →
`invoke_armed_one_shot_callback_if_byte_0x20_set` (36B) via
`RenamePass52bfRegion80040000Fun8004f214.java` (`renamed=1`, live-verified).

```c
void invoke_armed_one_shot_callback_if_byte_0x20_set(void)
{
  puVar1 = PTR_DAT_8004f238;
  if (puVar1[0x20] != '\0') {
    puVar2 = (undefined4 *)PTR_DAT_8004f23c;
    (*(code *)*puVar2)();
    puVar1[0x20] = 0;
  }
}
```

When byte `+0x20` of global state at `PTR_DAT_8004f238` is armed, invokes the registered
one-shot callback fptr at `PTR_DAT_8004f23c` and clears the flag. Sole caller:
`unknown_fptr_index0` case `0xb` (cmd type 111) — runs immediately before that case
fans out pending callback bits in `PTR_PTR_80013bac[7]` (low 2 bits → `PTR_DAT_80013bbc`,
bit 2 → `PTR_DAT_80013bb0`). Deferred-callback prelude in the fptr-index-0 dispatch table.

Post-rename: **209 unnamed** in-region (123 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-51+
substantive candidate; skip rank-1–50 artifacts and already-done ranks.

## Pass 52bg (2026-06-30) — rank-51 payload subrecord dual-list prepend rename

**Refreshed cold-triage (ranks 1-50 skipped as artifacts or already done):** rank-51
`0x8004e274` (28B, 1 xref) — substantive payload-subrecord list-registration helper in the
`0x8004e2xx` record-pool / fragment-reassembly cluster (sibling of
`set_link_record_index_and_register_in_table` and `free_list_lifo_push_0xfc_record_pool`).

**Rank-51 decompiled and renamed (HIGH):** `FUN_8004e274` →
`prepend_payload_subrecord_to_pending_lists_if_low3bits_set` (28B) via
`RenamePass52bgRegion80040000Fun8004e274.java` (`renamed=1`, live-verified).

```c
void prepend_payload_subrecord_to_pending_lists_if_low3bits_set(undefined4 *node)
{
  if ((*(byte *)(node + 2) & 7) != 0) {
    alt = (undefined4 *)node[5];           // +0x14
    head = (undefined4 *)PTR_PTR_8004e290;
    *alt = *head;
    *head = alt;
  }
  head = (undefined4 *)PTR_PTR_8004e294;
  *node = *head;
  *head = node;
}
```

When the subrecord's byte at `+0x08` has any of its low 3 bits set, LIFO-prepends the
pointer stored at `+0x14` onto global list head `PTR_PTR_8004e290`; always LIFO-prepends
`node` itself onto `PTR_PTR_8004e294`. Sole caller: `FUN_80047304` (780B fragment-reassembly
handler) — when the per-connection expected-length field `+0x2e` reaches zero, walks the
payload subrecord chain at `+0x20` and invokes this on each node to register completed
fragments into the pending-completion queues.

Post-rename: **208 unnamed** in-region (122 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-52+
substantive candidate; skip rank-1–51 artifacts and already-done ranks.

## Pass 52bh (2026-06-30) — rank-52 conditional LMP 0x25B dispatch on HCI reset rename

**Refreshed cold-triage (ranks 1-51 skipped as artifacts or already done):** rank-52
`0x8004aae8` (26B, 1 xref) — substantive conditional LMP 0x25B dispatch gated on the
LMP pre-check buffer sentinel at `+0x1c` in the `0x8004aaxx`/`0x8004abxx` HCI-reset
LMP-check cluster (sibling of Pass 52av's
`hci_reset_clear_lmp_check_buf_set_flag_and_program_bb_regs` which seeds `+0x1c` to
`0xffffffff`, and Pass 52bd's `clear_lmp_precheck_entry_and_arm_connection_active_bitmask`
on a related check-buffer pointer).

**Rank-52 decompiled and renamed (HIGH):** `FUN_8004aae8` →
`invoke_lmp_25b_if_precheck_sentinel_not_minus_one` (26B) via
`RenamePass52bhRegion80040000Fun8004aae8.java` (`renamed=1`, live-verified).

```c
void invoke_lmp_25b_if_precheck_sentinel_not_minus_one(void)
{
  buf = PTR_check_before_call_LMP_func_8004ab04;
  if (*(int *)(buf + 0x1c) != -1) {
    LMP__25B__most_common_for_VSCs1(PTR_DAT_8004ab08);
  }
}
```

When the dword sentinel at `+0x1c` of `PTR_check_before_call_LMP_func_8004ab04` is not
`0xffffffff` (i.e. pre-check state is active/pending rather than idle), invokes the
established `LMP__25B__most_common_for_VSCs1` pending-flag scheduler wrapper (vendor LMP
opcode 0x25B, documented in `reverse_engineering_lmp_vsc_opcode_map.md`). Sole caller:
`fHCI_Reset_0x03_full_subsystem_teardown` — fires during full HCI Reset teardown before
Pass 52ar's `release_all_conn_records_and_invoke_teardown_chain` work, ensuring any
in-flight LMP 0x25B pending state is flushed when the pre-check sentinel indicates
activity.

Post-rename: **207 unnamed** in-region (121 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-54+
substantive candidate; skip rank-1–53 artifacts and already-done ranks.

## Pass 52bi (2026-06-30) — rank-53 conn-record handle deregistration rename

**Refreshed cold-triage (ranks 1-52 skipped as artifacts or already done):** rank-53
`0x8004e2b8` (20B, 1 xref) — substantive sorted-index-table handle deregistration in the
`0x8004e2xx` conn-record handle-table cluster (direct inverse of
`set_link_record_index_and_register_in_table` at `0x8004e298`).

**Rank-53 decompiled and renamed (HIGH):** `FUN_8004e2b8` →
`deregister_link_record_handle_from_index_table` (20B) via
`RenamePass52biRegion80040000Fun8004e2b8.java` (`renamed=1`, live-verified).

```c
void deregister_link_record_handle_from_index_table(int conn_rec)
{
  FUN_8004e154(PTR_PTR_8004e2cc, *(undefined1 *)(conn_rec + 0x10));
}
```

Reads the LMP handle byte at `conn_rec[+0x10]` and removes it from the sorted
conn_rec-pointer table at `PTR_PTR_8004e2cc` via sorted-array delete
(`FUN_8004e154` → `binary_search_sorted_table_by_index_byte` + shift-left).
Sole caller: `FUN_8004fcec` — conn-record free path after
`is_conn_record_pkt_modes_cleared_for_free` gate and `FUN_8004e5ac` sub-object
teardown, immediately before SCO sub-resource and main free-list push.

Post-rename: **206 unnamed** in-region (120 in 1-150B tier).

**Next (at Pass 52bi):** rank-54 — completed Pass 52bj below.

## Pass 52bj (2026-06-30) — rank-54 active-link mask bit-four predicate rename

**Refreshed cold-triage (ranks 1-53 skipped as artifacts or already done):** rank-54
`0x8004e96c` (14B, 1 xref) — substantive byte-equality predicate in the
`0x8004e9xx` active-link-mask cluster (sibling of Pass 52h's
`scan_active_link_mask_for_slot_status_flag` and still-unnamed `FUN_8004e9a8`
lookup-table accessor).

**Rank-54 decompiled and renamed (HIGH):** `FUN_8004e96c` →
`is_active_link_mask_bit_four` (14B) via
`RenamePass52bjRegion80040000Fun8004e96c.java` (`renamed=1`, live-verified).

```c
bool is_active_link_mask_bit_four(char param_1)
{
  return param_1 == '\x04';
}
```

Returns true when the isolated active-link bitmask bit equals 4. Sole caller
`FUN_8004704c` (HCI link policy param setup body, delegate of
`hci_link_policy_param_setup_handler_send_cmd_complete`) uses the return value as
an offset bias: `status_byte_addr = global_ctx + is_active_link_mask_bit_four(bit) + 0x22`,
selecting `+0x22` (default) vs `+0x23` (when bit==4) — the same `(bit == 4) + 0x22`
idiom documented in Pass 52h's mask scanner.

Post-rename: **205 unnamed** in-region (119 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-55+
substantive candidate; skip rank-1–54 artifacts and already-done ranks.

## Pass 52bk (2026-06-30) — rank-55/56 artifacts skipped; rank-58 active-link slot status lookup rename

**Refreshed cold-triage (ranks 1-54 skipped as artifacts or already done):**
- rank-55 `0x8004a2e8` (8B, 1 xref) — **artifact** (`halt_baddata`, bad instruction data)
- rank-56 `0x8004e3e0` (8B, 1 xref) — **artifact** (`halt_baddata`, bad instruction data)
- rank-57 `0x8004a444` (4B listed, 1 xref) — deferred (Ghidra body undersized; decompile
  shows substantive HCI Command Complete sender — revisit when boundary fixed)
- rank-58 `0x8004e9a4` (4B, 1 xref) — substantive 3-entry lookup in the
  `0x8004e9xx` active-link-mask cluster (sibling of Pass 52bj's
  `is_active_link_mask_bit_four` and still-unnamed `FUN_8004e9a8`)

**Rank-58 decompiled and renamed (HIGH):** `FUN_8004e9a4` →
`lookup_active_link_slot_status_by_index` (4B) via
`RenamePass52bkRegion80040000Fun8004e9a4.java` (`renamed=1`, live-verified).

```c
undefined1 lookup_active_link_slot_status_by_index(uint index)
{
  uint slot = (index & 0xff) - 2 & 0xff;
  if (slot < 3) {
    return PTR_DAT_8004e9c0[slot];
  }
  return 0;
}
```

Maps conn-slot indices 2–4 to a 3-byte status table at `PTR_DAT_8004e9c0`
(indices 0–2); returns 0 for out-of-range. Same cluster as
`scan_active_link_mask_for_slot_status_flag` / `is_active_link_mask_bit_four`.

Post-rename: **204 unnamed** in-region (118 in 1-150B tier).

**Next (at Pass 52bk):** rank-59+ — completed Pass 52bl below.

## Pass 52bl (2026-06-30) — ranks 59–65 artifacts skipped; rank-66 LMP power/clk-adj HCI handler rename

**Refreshed cold-triage (ranks 1-58 skipped as artifacts, deferred, or already done):**
- ranks 59–65 (`0x80047270`, `0x80047610`, `0x80047968`, `0x80048cd4`,
  `0x80048fa4`, `0x80049154`, `0x80049548`) — **artifacts** (1B bodies,
  `halt_baddata` bad-instruction data)
- rank-66 `0x800494b0` (150B, 0 xrefs in triage) — substantive HCI command
  handler in the `0x800494xx`/`0x800496xx` SCO/eSCO handler cluster (sibling
  of `HCI_Accept_Synchronous_Connection_Request_handler` at `0x8004966c`)

**Rank-66 decompiled and renamed (HIGH):** `FUN_800494b0` →
`validate_conn_and_start_lmp_power_clk_adj_if_enabled` (150B) via
`RenamePass52blRegion80040000Fun800494b0.java` (`renamed=1`, live-verified).

```c
undefined4 validate_conn_and_start_lmp_power_clk_adj_if_enabled(undefined2 *param_1)
{
  if (*(ushort *)((int)param_1 + 3) >= 0x1000) {
    send_evt_HCI_Command_Status(*param_1, 0x12);
    return 0x12;
  }
  conn = query_config_struct_0x1ac_by_index();
  if (conn == 0) {
    send_evt_HCI_Command_Status(*param_1, 2);
    return 2;
  }
  if ((config->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ & 0xc) != 0
      && (bos[+0x1d0] & 1) && (conn[+3] & 4)) {
    send_evt_HCI_Command_Status(*param_1, 0);
    conn[+0x60] |= 1;
    if (conn[+0x7c] == 0 && conn[+0x78] == 0)
      dispatch_and_commit_pending_proc_bit0x10_reason5(conn);
    else
      conn[+0x8f] |= 8;
    return 0;
  }
  send_evt_HCI_Command_Status(*param_1, 0xc);
  return 0xc;
}
```

Validates conn handle (`+3` < `0x1000`), looks up `0x1ac` conn record via
`query_config_struct_0x1ac_by_index`, gates on
`config->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` bits 2–3 plus BOS
`+0x1d0` bit0 and conn `+3` bit2. On success: HCI Command Status 0 (pending),
sets conn `+0x60` bit0, and either calls
`dispatch_and_commit_pending_proc_bit0x10_reason5` or defers via `+0x8f` bit8.
Errors: `0x12` invalid params, `0x2` unknown connection, `0xc` command
disallowed. Canonical HCI-handler signature (`send_evt_HCI_Command_Status` on
every path); exact HCI opcode not pinned (indirect router dispatch).

Post-rename: **203 unnamed** in-region (117 in 1-150B tier).

**Next (at Pass 52bl):** rank-67+ — completed Pass 52bm below.

## Pass 52bm (2026-06-30) — rank-67 sync-params HCI handler rename

**Refreshed cold-triage (ranks 1-66 skipped as artifacts, deferred, or already done):**
rank-67 `0x80049420` (140B, 0 xrefs in triage) — substantive HCI command
handler in the `0x800494xx` SCO/eSCO handler cluster (sibling of
`validate_conn_and_start_lmp_power_clk_adj_if_enabled` at `0x800494b0`).

**Rank-67 decompiled and renamed (HIGH):** `FUN_80049420` →
`validate_conn_copy_sync_params_and_alloc_tag3_dispatch` (140B) via
`RenamePass52bmRegion80040000Fun80049420.java` (`renamed=1`, live-verified).

```c
undefined4 validate_conn_copy_sync_params_and_alloc_tag3_dispatch(undefined2 *param_1)
{
  if (*(ushort *)((int)param_1 + 3) >= 0x1000) {
    send_evt_HCI_Command_Status(*param_1, 0x12);
    return 0x12;
  }
  conn = query_config_struct_0x1ac_by_index();
  if (conn == 0) {
    send_evt_HCI_Command_Status(*param_1, 2);
    return 2;
  }
  if ((bos[+0x1d0] & 1) && (conn[+3] & 4)) {
    send_evt_HCI_Command_Status(*param_1, 0);
    optimized_memcpy(conn + 0xac, (int)param_1 + 5, 0x1a);
    if ((conn[+0x7c] == 0) && (conn[+0x78] == 0))
      alloc_tag3_or_tag0xa_record_and_dispatch_by_flag_bit1(conn);
    else
      conn[+0x8f] |= 4;
    return 0;
  }
  send_evt_HCI_Command_Status(*param_1, 0xc);
  return 0xc;
}
```

Validates conn handle (`+3` < `0x1000`), looks up `0x1ac` conn record,
gates on BOS `+0x1d0` bit0 and conn `+3` bit2 (same gate as sibling
`validate_conn_and_start_lmp_power_clk_adj_if_enabled` but without the
`config->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` check). On success:
HCI Command Status 0 (pending), copies 26-byte sync-param block from cmd
payload `+5` into conn `+0xac`, and either calls
`alloc_tag3_or_tag0xa_record_and_dispatch_by_flag_bit1` or defers via
`+0x8f` bit4. Errors: `0x12` invalid params, `0x2` unknown connection,
`0xc` command disallowed.

Post-rename: **202 unnamed** in-region (116 in 1-150B tier).

**Next (at Pass 52bm):** rank-68+ — completed Pass 52bn below.

## Pass 52bn (2026-06-30) — rank-68 link-policy readback HCI handler rename

**Refreshed cold-triage (ranks 1-67 skipped as artifacts, deferred, or already done):**
rank-68 `0x8004537c` (124B, 0 xrefs in triage) — substantive HCI Command Complete
sender in the link-policy/feature-toggle cluster (sibling of
`process_link_feature_toggle_command_and_send_status_event` and
`hci_link_policy_param_setup_handler_send_cmd_complete`).

**Rank-68 decompiled and renamed (HIGH):** `FUN_8004537c` →
`hci_link_policy_settings_read_send_cmd_complete` (124B) via
`RenamePass52bnRegion80040000Fun8004537c.java` (`renamed=1`, live-verified).

```c
undefined1 hci_link_policy_settings_read_send_cmd_complete(short *hci_cmd)
{
  ctrl = PTR_base_of_0x1ac_struct_array[0xb];
  state = ctrl.field96_0x60;
  active_count = ctrl.field3_0x3;
  if (state == 0)
    ctrl.field96_0x60 = 1;
  else if ((*PTR_DAT_800453fc & 0x10) && state != 1)
    status = 0xc;  // Command Disallowed
  else
    status = 0;
  // Derive link-index offset byte from active-count vs threshold
  offset_byte = 0;
  if (active_count < *PTR_DAT_80045400)
    offset_byte = (active_count - threshold) * 3;
  // Pack 5-byte Command Complete (0xe): num_pkts + opcode(2) + status + offset_byte
  hci_event_sender(0xe, &response, 5);
  return status;
}
```

Re-entrancy-gated HCI command handler on global control record `bos[0xb]`:
uses the same `field96_0x60` state byte as
`process_link_feature_toggle_command_and_send_status_event` (0→1 transition,
`0xc` when disallowed) but does not mutate link-policy fields — readback-only
path that packs an extra link-index offset byte into the 5-byte Command Complete
response (vs the 4-byte response of the param-setup sibling). No direct callers
found (function-pointer registration).

Post-rename: **201 unnamed** in-region (115 in 1-150B tier).

**Next (at Pass 52bn):** rank-69+ — completed Pass 52bo below.

## Pass 52bo (2026-06-30) — rank-69 LE-cluster HCI status-echo handler rename

**Refreshed cold-triage (ranks 1-68 skipped as artifacts, deferred, or already done):**
rank-69 `0x800457cc` (118B, 0 xrefs in triage) — substantive HCI Command Complete
sender in the LE Meta Event cluster (neighbor of `flush_rssi_batch_arrays_via_meta_subevent_0x2_or_0xb` at `0x8004574c`).

**Rank-69 decompiled and renamed (HIGH):** `FUN_800457cc` →
`hci_global_field_0x165_status_echo_send_cmd_complete` (118B) via
`RenamePass52boRegion80040000Fun800457cc.java` (`renamed=1`, live-verified).

```c
undefined4 hci_global_field_0x165_status_echo_send_cmd_complete(short *hci_cmd)
{
  cmd_word = *hci_cmd;
  if (cmd_word == 0)
    status = 0;
  else {
    status = PTR_struct_of_at_least_0x300_size_80045850->field_0x165;
    if (status == '\0') status = '\x01';
  }
  // Pack 12-byte Command Complete (0xe): status + echoed cmd-word bytes + opcode template
  response[0] = status;
  response[1] = (byte)cmd_word;
  response[2] = (byte)(cmd_word >> 8);
  response[3] = 0;
  hci_event_sender(0xe, &response, 0xc);
  return 0;
}
```

HCI command handler: derives status from global `the_0x300` struct
`field_0x165` (0 when cmd word==0, else field value defaulting to 1) — same
idiom as `hci_hw_crypto_dual_block_digest_handler_send_cmd_complete` and
`pack_lmp_response_header_status_and_handle`; echoes the first cmd-word bytes in
the 12-byte Command Complete response. No direct callers found
(function-pointer registration).

Post-rename: **200 unnamed** in-region (114 in 1-150B tier).

**Next (at Pass 52bo):** rank-70+ — completed Pass 52bp below.

## Pass 52bp (2026-06-30) — rank-70 conn-diag-batch-gated HCI handler rename

**Refreshed cold-triage (ranks 1-69 skipped as artifacts, deferred, or already done):**
rank-70 `0x80047200` (110B, 0 xrefs in triage) — substantive HCI Command Complete
sender with conn-diagnostic-batch prelude and re-entrancy gate; sibling cluster of
`hci_link_policy_settings_read_send_cmd_complete` and
`hci_global_field_0x165_status_echo_send_cmd_complete`.

**Rank-70 decompiled and renamed (HIGH):** `FUN_80047200` →
`hci_conn_diag_batch_gate_field_0x165_send_cmd_complete` (110B) via
`RenamePass52bpRegion80040000Fun80047200.java` (`renamed=1`, live-verified).

```c
undefined1 hci_conn_diag_batch_gate_field_0x165_send_cmd_complete(short *hci_cmd)
{
  // Re-entrancy gate on global state byte at +0x15dc (0→2, or return 0xc if bit 0x10 set)
  if (gate_byte == 0) gate_byte = 2;
  else if ((flag_byte & 0x10) && gate_byte != 2) return 0xc;

  FUN_8004fd30();
  conn_diagnostic_batch_dump();

  cmd_word = *hci_cmd;
  status = (cmd_word == 0) ? 0 : field_0x165 defaulting to 1;
  hci_event_sender(0xe, {status, cmd_lo, cmd_hi, gate_result}, 4);
  return gate_result;
}
```

HCI command handler: re-entrancy-gated prelude calls `FUN_8004fd30` +
`conn_diagnostic_batch_dump` (same diagnostic family as
`schedule_conn_diagnostic_dump_if_idle`); derives status from global `the_0x300`
struct `field_0x165` (0 when cmd word==0, else field value defaulting to 1);
packs 4-byte Command Complete (`hci_event_sender(0xe,…)`) with status + echoed
cmd-word bytes + gate-result byte. No direct callers found (function-pointer
registration).

Post-rename: **199 unnamed** in-region (113 in 1-150B tier).

**Next (at Pass 52bp):** rank-71+ — completed Pass 52bq below.

## Pass 52bq (2026-06-30) — rank-71 reentry-gated HCI status-echo handler rename

**Refreshed cold-triage (ranks 1-70 skipped as artifacts, deferred, or already done):**
rank-71 `0x80044c04` (104B, 0 xrefs in triage) — substantive HCI Command Complete
sender in the LE Meta Event cluster (`0x80044730`–`0x80046620` neighborhood);
re-entrancy-gated sibling of `hci_link_policy_param_setup_handler_send_cmd_complete`
and `hci_global_field_0x165_status_echo_send_cmd_complete`.

**Rank-71 decompiled and renamed (HIGH):** `FUN_80044c04` →
`hci_reentry_gate_field_0x165_cmd_echo_u16_send_cmd_complete` (104B) via
`RenamePass52bqRegion80040000Fun80044c04.java` (`renamed=1`, live-verified).

```c
undefined1 hci_reentry_gate_field_0x165_cmd_echo_u16_send_cmd_complete(short *hci_cmd)
{
  // Re-entrancy gate on bos[0xb].field96_0x60 (0→2, or return 0xc if bit 0x10 set and state != 2)
  if (gate_byte == 0) gate_byte = 2;
  else if ((flag_byte & 0x10) && gate_byte != 2) return 0xc;

  cmd_word = *hci_cmd;
  status = (cmd_word == 0) ? 0 : field_0x165 defaulting to 1;
  global_u16 = *PTR_DAT_80044c78;
  hci_event_sender(0xe, {status, cmd_lo, cmd_hi, gate_result, u16_lo, u16_hi}, 6);
  return gate_result;
}
```

HCI command handler: re-entrancy-gated on global control record `bos[0xb]`
(`field96_0x60` state byte 0→2, returns `0xc` when disallowed) — same gate
idiom as `hci_link_policy_settings_read_send_cmd_complete` and
`hci_conn_diag_batch_gate_field_0x165_send_cmd_complete`. Derives status from
global `the_0x300` struct `field_0x165` (0 when cmd word==0, else field value
defaulting to 1); packs 6-byte Command Complete (`hci_event_sender(0xe,…)`) with
status + echoed cmd-word bytes + gate-result byte + global ushort template at
`PTR_DAT_80044c78`. No direct callers found (function-pointer registration).

Post-rename: **198 unnamed** in-region (112 in 1-150B tier).

**Next (at Pass 52bq):** rank-72+ — completed Pass 52br below.

## Pass 52br (2026-06-30) — rank-72 conn[10] diagnostic dword-pair stager rename

**Refreshed cold-triage (ranks 1-71 skipped as artifacts, deferred, or already done):**
rank-72 `0x8004b7d8` (96B, 0 xrefs in triage) — substantive IRQ-masked
conn-record slot-10 diagnostic staging helper; sibling neighborhood of
`set_hw_ctrl_bits_and_update_conn10_0x154` and `0x8004b898` per-connection
parameter setter.

**Rank-72 decompiled and renamed (HIGH):** `FUN_8004b7d8` →
`irq_safe_stage_conn10_diagnostic_dword_pair_and_incr_count` (96B) via
`RenamePass52brRegion80040000Fun8004b7d8.java` (`renamed=1`, live-verified).

```c
void irq_safe_stage_conn10_diagnostic_dword_pair_and_incr_count(uint *entry)
{
  if ((char)entry[2] == 0) return;

  irq = disable_interrupts();
  rec = PTR_base_of_0x1ac_struct_array[10];
  if (rec.field_0xd0_dword == 0)
    rec.field_0xd0_dword = entry[0];
  else {
    ptr = rec.field_0xd4_ptr;
    *(uint *)(ptr + 0x104) = entry[0];
  }
  rec.field_0xd4_dword = entry[1];
  rec.field_0xd8_count += (byte)entry[2];
  enable_interrupts(irq);
}
```

IRQ-masked diagnostic accumulator on fixed connection-record slot 10
(local-device / special slot): gated on nonzero count byte at `entry+8`;
when inline dword at `+0xd0` is zero stores first dword inline, else writes
to `*ptr_at_0xd4 + 0x104`; stores second dword at `+0xd4` and accumulates
count byte into `+0xd8`. No direct callers found (function-pointer
registration). Sibling of conn-slot-10 maintenance functions in region
`0x80050000` (`0x8005d154` linked-list drain at `+0xdc`).

Post-rename: **197 unnamed** in-region (111 in 1-150B tier).

**Next (at Pass 52br):** rank-73+ — completed Pass 52bs below.

## Pass 52bs (2026-06-30) — rank-73 conn10-flag HCI status-echo handler rename

**Refreshed cold-triage (ranks 1-72 skipped as artifacts, deferred, or already done):**
rank-73 `0x80044f9c` (88B, 0 xrefs in triage) — substantive HCI Command Complete
sender in the LE Meta Event cluster; conn-slot-10 `+0x154` bit-2 gate byte sibling of
`hci_reentry_gate_field_0x165_cmd_echo_u16_send_cmd_complete` and
`hci_global_field_0x165_status_echo_send_cmd_complete`.

**Rank-73 decompiled and renamed (HIGH):** `FUN_80044f9c` →
`hci_conn10_0x154_bit2_flag_0x1a_global_byte_send_cmd_complete` (88B) via
`RenamePass52bsRegion80040000Fun80044f9c.java` (`renamed=1`, live-verified).

```c
undefined4 hci_conn10_0x154_bit2_flag_0x1a_global_byte_send_cmd_complete(short *hci_cmd)
{
  cmd_word = *hci_cmd;
  status = (cmd_word == 0) ? 0 : field_0x165 defaulting to 1;
  flag_byte = (conn_table[10].field_0x154 & 2) ? 0 : 0x1a;
  global_byte = *PTR_DAT_80044ffc;
  hci_event_sender(0xe, {status, cmd_lo, cmd_hi, flag_byte, global_byte}, 5);
  return 0;
}
```

HCI command handler: derives status from global `the_0x300` struct `field_0x165`
(0 when cmd word==0, else field value defaulting to 1) — same idiom as
`hci_global_field_0x165_status_echo_send_cmd_complete`. Echoes cmd-word bytes;
packs 5-byte Command Complete (`hci_event_sender(0xe,…)`) with status + echoed
cmd-word bytes + conn-slot-10 `+0x154` bit-2 gate byte (`0x1a` when bit 2 clear,
0 when set) + global byte at `PTR_DAT_80044ffc`. No re-entrancy gate (unlike
`hci_reentry_gate_field_0x165_cmd_echo_u16_send_cmd_complete`). No direct callers
found (function-pointer registration).

Post-rename: **196 unnamed** in-region (110 in 1-150B tier).

**Next (at Pass 52bs):** rank-74+ — completed Pass 52bt below.

## Pass 52bt (2026-06-30) — rank-74 conn-struct HW-reg HCI status-echo handler rename

**Refreshed cold-triage (ranks 1-73 skipped as artifacts, deferred, or already done):**
rank-74 `0x8004a464` (80B, 0 xrefs in triage) — substantive HCI Command Complete
sender in the `0x8004a4xx` LMP-check/teardown neighborhood; copies 6 bytes from
HCI params into conn-struct `+0x1c` staging area, programs baseband via
`write_connection_struct_fields_1c_1e_20_to_hw_regs`, then emits status echo.

**Rank-74 decompiled and renamed (HIGH):** `FUN_8004a464` →
`hci_copy_conn_struct_1c_6byte_hw_regs_field_0x165_send_cmd_complete` (80B) via
`RenamePass52btRegion80040000Fun8004a464.java` (`renamed=1`, live-verified).

```c
undefined4 hci_copy_conn_struct_1c_6byte_hw_regs_field_0x165_send_cmd_complete(short *hci_cmd)
{
  cmd_word = *hci_cmd;
  optimized_memcpy(conn_struct_1c_staging, hci_cmd + 3, 6);
  write_connection_struct_fields_1c_1e_20_to_hw_regs();
  status = (cmd_word == 0) ? 0 : field_0x165 defaulting to 1;
  hci_event_sender(0xe, {status, cmd_lo, cmd_hi, 0}, 4);
  return 0;
}
```

HCI command handler: copies 6 bytes from HCI command params at offset +3 into
the per-connection `0x1ac` struct staging area at `+0x1c` (via
`PTR_base_of_0x1ac_struct_array_0xA_large2_0__field28_0x1c_8004a4b4`), then calls
`write_connection_struct_fields_1c_1e_20_to_hw_regs` to program MMIO from struct
fields `+0x1c`/`+0x1e`/`+0x20`. Derives status from global `the_0x300` struct
`field_0x165` (0 when cmd word==0, else field value defaulting to 1) — same
idiom as `hci_global_field_0x165_status_echo_send_cmd_complete`. Echoes cmd-word
bytes; packs 4-byte Command Complete (`hci_event_sender(0xe,…)`). Neighbor of
`invoke_teardown_hook_triplet_with_lmp_power_gate` (`0x8004a4bc`) and
`clear_lmp_precheck_entry_and_arm_connection_active_bitmask` (`0x8004a6ec`).
No direct callers found (function-pointer registration).

Post-rename: **195 unnamed** in-region (109 in 1-150B tier).

**Next (at Pass 52bt):** rank-75+ — completed Pass 52bu below.

## Pass 52bu (2026-06-30) — rank-75 conn-struct field4 HCI status-echo handler rename

**Refreshed cold-triage (ranks 1-74 skipped as artifacts, deferred, or already done):**
rank-75 `0x80045454` (74B, 0 xrefs in triage) — substantive HCI Command Complete
sender in the LE Meta Event / OGF8-stub neighborhood (`0x800454a8`); copies 8 bytes
from HCI params into conn-struct `+0x4` staging, then emits status echo.

**Rank-75 decompiled and renamed (HIGH):** `FUN_80045454` →
`hci_copy_conn_struct_field4_8byte_field_0x165_send_cmd_complete` (74B) via
`RenamePass52buRegion80040000Fun80045454.java` (`renamed=1`, live-verified).

```c
undefined4 hci_copy_conn_struct_field4_8byte_field_0x165_send_cmd_complete(short *hci_cmd)
{
  cmd_word = *hci_cmd;
  optimized_memcpy(conn_struct_field4_staging, hci_cmd + 3, 8);
  status = (cmd_word == 0) ? 0 : field_0x165 defaulting to 1;
  hci_event_sender(0xe, {status, cmd_lo, cmd_hi, 0}, 4);
  return 0;
}
```

HCI command handler: copies 8 bytes from HCI command params at offset +3 into
the per-connection `0x1ac` struct staging area at `+0x4` (via
`PTR_base_of_0x1ac_struct_array_0xA_large2_0__field4_0x4_800454a0`). Derives
status from global `the_0x300` struct `field_0x165` (0 when cmd word==0, else
field value defaulting to 1) — same idiom as
`hci_global_field_0x165_status_echo_send_cmd_complete`. Echoes cmd-word bytes;
packs 4-byte Command Complete (`hci_event_sender(0xe,…)`). Sits immediately
before `FUN_800454a8` (OGF8 command-status stub) in the LE Meta Event cluster
address range. No direct callers found (function-pointer registration).

Post-rename: **194 unnamed** in-region (108 in 1-150B tier).

**Next (at Pass 52bu):** rank-76+ — completed Pass 52bv below.

## Pass 52bv (2026-06-30) — rank-76 access-code sync-word XOR-mask wrapper rename

**Refreshed cold-triage (ranks 1-75 skipped as artifacts, deferred, or already done):**
rank-76 `0x800428ec` (72B, 0 xrefs in triage) — substantive thin wrapper in the
`0x800428xx` access-code sync-word cluster immediately following
`compute_access_code_sync_word_from_bdaddr` (`0x8004287c`).

**Rank-76 decompiled and renamed (HIGH):** `FUN_800428ec` →
`compute_access_code_sync_word_xor_fixed_mask_from_bdaddr` (72B) via
`RenamePass52bvRegion80040000Fun800428ec.java` (`renamed=1`, live-verified).

```c
void compute_access_code_sync_word_xor_fixed_mask_from_bdaddr(uint bdaddr_low24, byte *out_buf)
{
  compute_access_code_sync_word_from_bdaddr(bdaddr_low24, out_buf);
  out_buf[0] ^= 0xfc;
  out_buf[1] ^= 0x54;
  out_buf[2] ^= 0xcc;
  out_buf[3] ^= 0xbb;
  out_buf[4] ^= 0x02;
}
```

Thin wrapper: delegates to `compute_access_code_sync_word_from_bdaddr` to derive
the 5-byte Bluetooth access-code sync word from BD_ADDR low-24, then XOR-masks
each output byte with fixed constants `{0xfc, 0x54, 0xcc, 0xbb, 0x02}`. Sits
immediately after the core LFSR compute function in the page-train baseband
programming cluster (`0x80041900` neighborhood). No direct callers found
(function-pointer registration).

Post-rename: **193 unnamed** in-region (107 in 1-150B tier).

**Next (at Pass 52bv):** rank-77+ — completed Pass 52bw below.

## Pass 52bw (2026-06-30) — rank-77 LE-cluster HCI fb8-template status-echo rename

**Refreshed cold-triage (ranks 1-76 skipped as artifacts, deferred, or already done):**
rank-77 `0x80045408` (72B, 0 xrefs in triage) — substantive HCI Command Complete
sender in the LE Meta Event / OGF8-stub neighborhood (`0x80045454`/`0x800454a8`);
packs a fixed 7-byte response template with `0xfb`/`8` tail bytes.

**Rank-77 decompiled and renamed (HIGH):** `FUN_80045408` →
`hci_global_field_0x165_status_echo_fb8_template_send_cmd_complete` (72B) via
`RenamePass52bwRegion80040000Fun80045408.java` (`renamed=1`, live-verified).

```c
undefined4 hci_global_field_0x165_status_echo_fb8_template_send_cmd_complete(short *hci_cmd)
{
  cmd_word = *hci_cmd;
  if (cmd_word == 0)
    status = 0;
  else {
    status = PTR_struct_of_at_least_0x300_size_80045450->field_0x165;
    if (status == '\0') status = '\x01';
  }
  // Pack 7-byte Command Complete (0xe): status + echoed cmd-word + 0xfb/8 template
  response[0] = status;
  response[1] = (byte)cmd_word;
  response[2] = (byte)(cmd_word >> 8);
  response[3] = 0;
  response[4] = 0;
  response[5] = 0xfb;
  response[6] = 8;
  hci_event_sender(0xe, &response, 7);
  return 0;
}
```

HCI command handler: derives status from global `the_0x300` struct
`field_0x165` (0 when cmd word==0, else field value defaulting to 1) — same
idiom as `hci_copy_conn_struct_field4_8byte_field_0x165_send_cmd_complete` and
`hci_global_field_0x165_status_echo_send_cmd_complete`; echoes cmd-word bytes
then appends fixed template bytes `0xfb` and `8` in the 7-byte Command Complete
response. Sits immediately before the conn-struct `+0x4` copy handler at
`0x80045454` in the LE Meta Event cluster. No direct callers found
(function-pointer registration).

Post-rename: **192 unnamed** in-region (106 in 1-150B tier).

**Next (at Pass 52bw):** rank-78+ — completed Pass 52bx below.

## Pass 52bx (2026-06-30) — rank-78 0xfc record-pool free-list linker rename

**Refreshed cold-triage (ranks 1-77 skipped as artifacts, deferred, or already done):**
rank-78 `0x8004e1c4` (64B, 0 xrefs in triage) — substantive 0xfc-record-pool
free-list linker in the conn-record allocator cluster (sibling of
`link_0x54_record_pool_into_free_list_by_config_count` and callee consumer of
`free_list_lifo_push_0xfc_record_pool`).

**Rank-78 decompiled and renamed (HIGH):** `FUN_8004e1c4` →
`link_0xfc_record_pool_into_free_list_by_config_count` (64B) via
`RenamePass52bxRegion80040000Fun8004e1c4.java` (`renamed=1`, live-verified).

```c
void link_0xfc_record_pool_into_free_list_by_config_count(void)
{
  PTR_PTR_80124e7c = 0;
  count = (config_byte_0x251 & 3) << 3 | (config_byte_0x250 >> 5);
  for (i = 0; i < count; i++) {
    record = pool_base + i * 0xfc;   // PTR_PTR_8004e208
    *record = *free_list_head;       // chain next pointer
    free_list_head = record;         // via free_list_lifo_push_0xfc_record_pool
  }
}
```

Links pre-allocated `0xfc`-byte records into the singly-linked free list headed
at `PTR_PTR_8004e21c`; record count derived from config bytes
`DAT_80120251 & 3` and `DAT_80120250 >> 5` (same config-field family as
`link_0x54_record_pool_into_free_list_by_config_count` and region-`0x80050000`'s
`alloc_and_link_0xfc_record_pool`). Clears `PTR_PTR_80124e7c` before wiring.
Each iteration prepends one record via `free_list_lifo_push_0xfc_record_pool`
(Pass 52ad). No direct callers found (function-pointer registration).

Post-rename: **191 unnamed** in-region (105 in 1-150B tier).

**Next (at Pass 52bx):** rank-79+ — completed Pass 52by below.

## Pass 52by (2026-06-30) — rank-79 noirq mask-merge HW-channel dispatch rename

**Refreshed cold-triage (ranks 1-78 skipped as artifacts, deferred, or already done):**
rank-79 `0x800429d4` (56B, 0 xrefs in triage) — substantive mask-merge indexed
HW-channel table dispatch without IRQ protection; noirq twin of Pass 52g's
`mask_merge_hw_channel_table_entry_and_indexed_dispatch` (`0x800430ac`, 88B,
IRQ-disabled) in the SCO/eSCO HW channel parameter-commit cluster.

**Rank-79 decompiled and renamed (HIGH):** `FUN_800429d4` →
`mask_merge_hw_channel_table_entry_indexed_dispatch_noirq` (56B) via
`RenamePass52byRegion80040000Fun800429d4.java` (`renamed=1`, live-verified).

```c
void mask_merge_hw_channel_table_entry_indexed_dispatch_noirq(uint index, ushort value, ushort mask)
{
  table = DAT_80042a0c;
  fptr_table = PTR_DAT_80042a10;
  (*fptr_table)(index & 0xffff,
                table[index] & ~mask | mask & value);
}
```

Mask-merge indexed dispatch without IRQ protection: merges `value`/`mask` onto the
per-index ushort HW-channel parameter table entry at `DAT_80042a0c` via
`(table & ~mask) | (mask & value)`, then calls through the function-pointer table
at `PTR_DAT_80042a10`. Structural noirq twin of
`mask_merge_hw_channel_table_entry_and_indexed_dispatch` (IRQ-guarded, 88B) and
sibling of `and_mask_hw_channel_table_entry_indexed_dispatch_noirq` (AND-mask,
32B) in the SCO/eSCO HW channel parameter-commit cluster alongside the irq-guarded
OR/AND/mask trio. No direct callers found (function-pointer registration).

Post-rename: **190 unnamed** in-region (104 in 1-150B tier).

**Next (at Pass 52by):** rank-80+ — completed Pass 52bz below.

## Pass 52bz (2026-06-30) — rank-80 HCI hw-reg pool remove handler rename

**Refreshed cold-triage (ranks 1-79 skipped as artifacts, deferred, or already done):**
rank-80 `0x8004993c` (46B, 0 xrefs in triage) — substantive HCI Command Complete
handler for HW-reg pool entry removal; sibling of other `hci_*_send_cmd_complete`
handlers in the LE Meta / OGF8 neighborhood and consumer of region-`0x80050000`'s
`remove_pool_entry_by_bdaddr_and_release_hw_or_bitmap_slot` (pool 0).

**Rank-80 decompiled and renamed (HIGH):** `FUN_8004993c` →
`hci_hw_reg_pool_entry_remove_handler_send_cmd_complete` (46B) via
`RenamePass52bzRegion80040000Fun8004993c.java` (`renamed=1`, live-verified).

```c
undefined1 hci_hw_reg_pool_entry_remove_handler_send_cmd_complete(short *param_1)
{
  index = *(char *)((int)param_1 + 3);
  if (0xfc < (byte)(index - 2U)) return send_cmd_complete(param_1, 0x12); /* invalid */
  /* gate on conn-array state + is_any_conn_lmp_procedure_busy_with_link_mode_mask_0x180 */
  if (index == -1) {
    PTR_PTR_80049a20[5] &= 0xbf;  /* clear bit-6 sentinel path */
    status = 0;
  } else {
    remove_pool_entry_by_bdaddr_and_release_hw_or_bitmap_slot(0, index, param_1 + 2);
    status = 0;
  }
  /* status from field_0x165 when cmd word nonzero; hci_event_sender(0xe, …, 4) */
}
```

Re-entrancy-gated HCI command handler: validates pool-index byte at params+3 (range
2..0xfe or 0xff sentinel); gates on per-connection state fields in the `0x1ac` struct
array plus `is_any_conn_lmp_procedure_busy_with_link_mode_mask_0x180`. Index `0xff`:
clears bit-6 of global byte at `PTR_PTR_80049a20[5]`. Otherwise calls
`remove_pool_entry_by_bdaddr_and_release_hw_or_bitmap_slot(0, index, bdaddr at
params+4)` — pool-0 HW-reg-base `0xa0` remove-by-key path documented in region
`0x80050000` Pass 47. Always terminates via 4-byte Command Complete
(`hci_event_sender(0xe,…)`) with `field_0x165` status idiom shared across the HCI
handler cluster. No direct callers found (indirect HCI router).

Post-rename: **189 unnamed** in-region (103 in 1-150B tier).

**Next (at Pass 52bz):** rank-81+ — completed Pass 52ca below.

## Pass 52ca (2026-06-30) — rank-81 weighted-bit checksum utility rename

**Refreshed cold-triage (ranks 1-80 skipped as artifacts, deferred, or already done):**
rank-81 `0x8004e7b4` (42B, 0 xrefs in triage) — substantive weighted-bit
checksum utility over conn-record `field_0xb` → `field_0x9`; standalone extracted
variant of the post-dispatch checksum inlined in `conn_type_dispatch_hook`
(`0x80050810`, uses lookup table `PTR_DAT_800508f4` instead of `PTR_DAT_8004e7e0`).
Sits in the `0x8004e7xx` conn-record pool allocator neighborhood adjacent to
rank-83 `FUN_8004e7e4` (free-list pop).

**Rank-81 decompiled and renamed (HIGH):** `FUN_8004e7b4` →
`compute_weighted_bit_checksum_field_0xb_store_at_0x9` (42B) via
`RenamePass52caRegion80040000Fun8004e7b4.java` (`renamed=1`, live-verified).

```c
void compute_weighted_bit_checksum_field_0xb_store_at_0x9(int conn_record)
{
  checksum = 1;
  for (bit = 0; bit < 7; bit++) {
    if (conn_record->field_0xb & (1 << bit))
      checksum += PTR_DAT_8004e7e0[bit];
  }
  conn_record->field_0x9 = checksum;
}
```

Iterates bits 0–6 of flag byte `+0xb`; for each set bit adds the corresponding
entry from 7-byte lookup table `PTR_DAT_8004e7e0` to a running sum starting at 1;
stores result at `+0x9`. Same algorithm as the tail of `conn_type_dispatch_hook`
(documented in `reverse_engineering_conn_type_dispatch_and_esco.md`) but factored
into a standalone callable with a region-local weight table. No direct callers
found (function-pointer registration or inlined-duplicate pattern).

Post-rename: **188 unnamed** in-region (102 in 1-150B tier).

**Next (at Pass 52ca):** rank-82+ — completed Pass 52cb below.

## Pass 52cb (2026-06-30) — rank-82 conn-record free-list pop rename

**Refreshed cold-triage (ranks 1-81 skipped as artifacts, deferred, or already done):**
rank-82 `0x8004e7e4` (32B, 0 xrefs in triage) — substantive conn-record
free-list LIFO pop; pop counterpart to `free_list_lifo_push` (`0x8004e808`) with
head at `PTR_PTR_8004e804`; sibling of rank-81
`compute_weighted_bit_checksum_field_0xb_store_at_0x9` in the `0x8004e7xx`
conn-record pool neighborhood.

**Rank-82 decompiled and renamed (HIGH):** `FUN_8004e7e4` →
`free_list_lifo_pop_conn_record_set_state_and_clear_if_zero` (32B) via
`RenamePass52cbRegion80040000Fun8004e7e4.java` (`renamed=1`, live-verified).

```c
void free_list_lifo_pop_conn_record_set_state_and_clear_if_zero(byte state)
{
  head = PTR_PTR_8004e804;
  node = *head;
  if (node != NULL) {
    *head = *node;                    /* LIFO pop */
    node->byte0 = state;
    if (state == 0) {
      node->field_0x2 = 0;
      node->field_0xb = 0;
      node->field_0xe = 0;
      node->field_0x1c = 0;
      node->field_0x20 = 0;
    }
  }
}
```

Pops the head node from the singly-linked conn-record free list at
`PTR_PTR_8004e804` (adjacent to `free_list_lifo_push` at `0x8004e808`), writes
`state` to byte 0, and when `state==0` clears key conn-record fields at offsets
`+0x2`, `+0xb`, `+0xe`, `+0x1c`, `+0x20` (partial fresh-record init). Pop
counterpart to the push primitives documented in Pass 52ad (`0x8004e204`) and
`free_list_lifo_push` (`0x8004e808`). No direct callers found (function-pointer
registration).

Post-rename: **187 unnamed** in-region (101 in 1-150B tier).

**Next (at Pass 52cb):** rank-83+ — completed Pass 52cc below.

## Pass 52cc (2026-06-30) — rank-83 baseband link-setup buffer adapter rename

**Refreshed cold-triage (ranks 1-82 skipped as artifacts, deferred, or already done):**
rank-83 `0x80048948` (28B, 0 xrefs in triage) — substantive thin wrapper in the
`0x800483c0` baseband link-setup neighborhood; unpacks a 7-byte param buffer and
delegates to the 866B link-setup handler documented at MEDIUM-HIGH in Pass 3.

**Rank-83 decompiled and renamed (HIGH):** `FUN_80048948` →
`invoke_baseband_link_setup_from_param_buffer` (28B) via
`RenamePass52ccRegion80040000Fun80048948.java` (`renamed=1`, live-verified).

```c
void invoke_baseband_link_setup_from_param_buffer(undefined2 *param_buffer)
{
  FUN_800483c0(*param_buffer,
               *(byte *)((int)param_buffer + 3),
               *(byte *)(param_buffer + 2),
               *(byte *)((int)param_buffer + 5),
               *(byte *)(param_buffer + 3));
}
```

Unpacks buffer layout: `[0:2]` uint16 handle, `[3]` timing/param byte,
`[4]` param3, `[5]` param4, `[6]` packet-mode byte — then calls
`FUN_800483c0` (baseband link setup + HCI Command Complete sender, Pass 3
MEDIUM-HIGH). Standard function-pointer-registration adapter pattern; no direct
callers found.

Post-rename: **186 unnamed** in-region (100 in 1-150B tier).

**Next (at Pass 52cc):** rank-84+ — completed Pass 52cd below.

## Pass 52cd (2026-06-30) — rank-84 param-buffer u16 triplet global-store rename

**Refreshed cold-triage (ranks 1-83 skipped as artifacts, deferred, or already done):**
rank-84 `0x8004e584` (26B, 0 xrefs in triage) — substantive thin adapter in the
`0x8004e5xx` conn-record / LMP-procedure neighborhood; copies three consecutive
uint16 fields from a param buffer into three global slot pointers.

**Rank-84 decompiled and renamed (HIGH):** `FUN_8004e584` →
`copy_param_buffer_u16_triplet_to_global_slots` (26B) via
`RenamePass52cdRegion80040000Fun8004e584.java` (`renamed=1`, live-verified).

```c
void copy_param_buffer_u16_triplet_to_global_slots(int param_buffer)
{
  *PTR_DAT_8004e5a0 = *(ushort *)(param_buffer + 0x40);
  *PTR_DAT_8004e5a4 = *(ushort *)(param_buffer + 0x42);
  *PTR_DAT_8004e5a8 = *(ushort *)(param_buffer + 0x44);
  return;
}
```

Copies three consecutive uint16 fields at offsets `+0x40`/`+0x42`/`+0x44` from
caller param buffer into global slot pointers `PTR_DAT_8004e5a0`/`a4`/`a8`.
Standard function-pointer-registration data-persistence adapter; sibling
neighborhood of `is_any_conn_lmp_procedure_busy_by_index` (`0x8004e500`) and
`FUN_8004e5ac` hardware sub-object teardown. No direct callers found.

Post-rename: **185 unnamed** in-region (99 in 1-150B tier).

**Next (at Pass 52cd):** rank-85+ — completed Pass 52ce below.

## Pass 52ce (2026-06-30) — rank-85 LE Meta subevent param-buffer adapter rename

**Refreshed cold-triage (ranks 1-84 skipped as artifacts, deferred, or already done):**
rank-85 `0x80044ef8` (24B, 0 xrefs in triage) — substantive thin adapter in the
LE Meta Event cluster (`0x80044730`–`0x80046620` neighborhood); unpacks a 6-byte
param buffer and delegates to `send_evt_Meta_subevent_0_or_1` (`0x80044c7c`).

**Rank-85 decompiled and renamed (HIGH):** `FUN_80044ef8` →
`invoke_send_evt_meta_subevent_0_or_1_from_param_buffer` (24B) via
`RenamePass52ceRegion80040000Fun80044ef8.java` (`renamed=1`, live-verified).

```c
void invoke_send_evt_meta_subevent_0_or_1_from_param_buffer(undefined2 *param_buffer)
{
  send_evt_Meta_subevent_0_or_1
            (*param_buffer,
             *(byte *)((int)param_buffer + 3),
             *(byte *)(param_buffer + 2),
             *(byte *)((int)param_buffer + 5));
}
```

Unpacks buffer layout: `[0:2]` uint16 handle, `[3]` param2 byte, `[4]` param3 byte,
`[5]` param4 byte — then calls `send_evt_Meta_subevent_0_or_1` (594B LE Meta
subevent 0/1 dispatcher documented in `reverse_engineering_ble_link_layer.md`).
Standard function-pointer-registration adapter pattern; sibling of
`invoke_baseband_link_setup_from_param_buffer` (`0x80048948`, Pass 52cc). No
direct callers found.

Post-rename: **184 unnamed** in-region (98 in 1-150B tier).

**Next (at Pass 52ce):** rank-86+ — completed Pass 52cf below.

## Pass 52cf (2026-06-30) — rank-86 LMP procedure slot ptr-store adapter rename

**Refreshed cold-triage (ranks 1-85 skipped as artifacts, deferred, or already done):**
rank-86 `0x8004e39c` (22B, 0 xrefs in triage) — substantive thin adapter in the
LMP procedure permission-gate cluster (`0x8004e2xx`–`0x8004e3xx` neighborhood);
stores param-buffer slot pointer to global context `PTR_DAT_8004e3b4+0x10` or
per-context record `+0x4`, then back-references context into the value pointer.

**Rank-86 decompiled and renamed (HIGH):** `FUN_8004e39c` →
`store_param_buffer_slot_ptr_with_context_backref` (22B) via
`RenamePass52cfRegion80040000Fun8004e39c.java` (`renamed=1`, live-verified).

```c
void store_param_buffer_slot_ptr_with_context_backref(int *param_buffer)
{
  if (*param_buffer == 0) {
    *(int *)(PTR_DAT_8004e3b4 + 0x10) = param_buffer[1];
  }
  else {
    *(int *)(*param_buffer + 4) = param_buffer[1];
  }
  *(int *)param_buffer[1] = *param_buffer;
}
```

Param-buffer layout: `[0]` context pointer (0 selects global `PTR_DAT_8004e3b4`),
`[1]` slot/value pointer. When context is zero, writes `param_buffer[1]` to
global+0x10; otherwise writes to `context+4`. Always stores context back into
`*param_buffer[1]`. Standard function-pointer-registration adapter pattern;
sibling of `arm_lmp_procedure_slot_pending_by_active_link_count` (`0x8004e340`,
Pass 52as) and `copy_param_buffer_u16_triplet_to_global_slots` (`0x8004e584`,
Pass 52cd). No direct callers found.

Post-rename: **183 unnamed** in-region (97 in 1-150B tier).

**Next (at Pass 52cf):** rank-87+ — completed Pass 52cg below.

## Pass 52cg (2026-06-30) — rank-87 param-buffer zeroing adapter rename

**Refreshed cold-triage (ranks 1-86 skipped as artifacts, deferred, or already done):**
rank-87 `0x8004a4fc` (10B, 0 xrefs in triage) — substantive thin adapter in the
`0x8004a4xx` LMP-check/teardown neighborhood; zeroes two u32 fields and a trailing
byte in a param buffer.

**Rank-87 decompiled and renamed (HIGH):** `FUN_8004a4fc` →
`zero_param_buffer_two_u32_and_byte` (10B) via
`RenamePass52cgRegion80040000Fun8004a4fc.java` (`renamed=1`, live-verified).

```c
void zero_param_buffer_two_u32_and_byte(undefined4 *param_buffer)
{
  *param_buffer = 0;
  param_buffer[1] = 0;
  *(undefined1 *)(param_buffer + 2) = 0;
  return;
}
```

Param-buffer layout: `[0]` and `[1]` uint32 fields zeroed, plus byte at offset 8
(`param_buffer+2` as byte pointer). Standard function-pointer-registration clear
adapter pattern; sibling of `hci_copy_conn_struct_1c_6byte_hw_regs_field_0x165_send_cmd_complete`
(`0x8004a464`, Pass 52bt) and `invoke_teardown_hook_triplet_with_lmp_power_gate`
(`0x8004a4bc`, Pass 52bc). No direct callers found.

Post-rename: **182 unnamed** in-region (96 in 1-150B tier).

**Next (at Pass 52cg):** rank-88+ — completed Pass 52ch below.

## Pass 52ch (2026-06-30) — rank-88 global-ctx slot triplet clear/bind adapter rename

**Refreshed cold-triage (ranks 1-87 skipped as artifacts, deferred, or already done):**
rank-88 `0x8004e94c` (22B, 0 xrefs in triage) — substantive thin adapter in the
`0x8004e9xx` active-link-mask neighborhood; clears two u32 fields at global-context
offsets `+0x238`/`+0x23c` and binds `PTR_DAT_8004e968` at `+0x240`.

**Rank-88 decompiled and renamed (HIGH):** `FUN_8004e94c` →
`clear_global_ctx_u32_pair_and_bind_data_ptr_at_240` (22B) via
`RenamePass52chRegion80040000Fun8004e94c.java` (`renamed=1`, live-verified).

```c
void clear_global_ctx_u32_pair_and_bind_data_ptr_at_240(void)
{
  undefined *puVar1;
  undefined *puVar2;

  puVar1 = PTR_PTR_8004e964;
  puVar2 = PTR_DAT_8004e968;
  *(undefined4 *)(puVar1 + 0x23c) = 0;
  *(undefined **)(puVar1 + 0x240) = puVar2;
  *(undefined4 *)(puVar1 + 0x238) = 0;
  return;
}
```

Global context base from `PTR_PTR_8004e964`; zeroes u32 slots at `+0x238` and
`+0x23c`, then stores data pointer `PTR_DAT_8004e968` at `+0x240`. Standard
function-pointer-registration init/clear adapter pattern; sibling of
`is_active_link_mask_bit_four` (`0x8004e96c`, Pass 52bj) and
`lookup_active_link_slot_status_by_index` (`0x8004e9a4`, Pass 52bk). No direct
callers found.

Post-rename: **181 unnamed** in-region (95 in 1-150B tier).

**Next:** continue refreshed 1-150B cold-triage — decompile next rank-89+
substantive candidate; skip rank-1–88 artifacts, deferred, and already-done ranks.

## Pass 52ci (2026-06-30) — 1-150B tail artifact triage + >150B rank-1 orchestrator rename

**Refreshed cold-triage (ranks 1-87 already done; rank-88+ tail is artifact-only):**
ranks 88-95 in the refreshed 1-150B queue are all 1-4B stubs with 0 xrefs —
non-substantive mis-disassembly artifacts, not rename candidates:

| Rank | Address | Size | Notes |
|------|---------|------|-------|
| 88 | `0x8004f370` | 4B | decompile = empty `return;` |
| 89 | `0x80049926` | 2B | stub |
| 90-95 | `0x8004498d`…`0x80049155` | 1B each | stubs |

**1-150B substantive cold-triage queue exhausted** (95 entries total; ranks 1-87
renamed HIGH, ranks 88-95 artifact tail). Pivot to >150B tier (86 unnamed functions).

**>150B rank-1 decompiled and renamed (HIGH):** `FUN_80043a60` →
`remote_name_request_feature_apply_orchestrator` (358B, 15 xrefs) via
`RenamePass52ciRegion80040000Fun80043a60.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3 continuation 2026-06-23) — structural parent of
already-HIGH siblings `remote_name_request_feature_apply_4` (`0x80043984`) and
`remote_name_request_feature_apply_8` (`0x80043884`): identical gating on
`the_0x300`/`config_struct` fields (`field208_0xd8`, `byte_0x16a`/`int_0x10`,
`field_0x171`/`field_0x173`, `ushort_0x24`/`byte_0x16f`), clears `+0x164` bit
mask, then dispatches to `FUN_8003c94c` (apply_4 path) or `FUN_8003ca28`
(apply_8 path) writing timing offset into global struct `+0x11c`. Called from
inquiry-cancel, periodic-inquiry-exit, role-switch-completion, and ACL-finalize
handlers.

Post-rename: **180 unnamed** in-region (95 in 1-150B tier unchanged); **85** in
>150B tier.

**Next:** continue >150B cold-triage — decompile+rename rank-4 `0x8004bde8`
(354B, 6 xrefs).

## Pass 52ck (2026-06-30) — >150B rank-3 dual-list sorted event insert rename

**>150B rank-3 decompiled and renamed (HIGH):** `FUN_8004f580` →
`dual_list_sorted_event_insert_with_overlap_pushback` (314B, 7 xrefs) via
`RenamePass52ckRegion80040000Fun8004f580.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 4, 2026-06-23). Sorted doubly-linked-list insert
keyed by wraparound-masked delta `param_1[3]` relative to list head, with
overlap-pushback adjustment of neighbor duration field `ushort+7` when inserting
between nodes. Selects one of two lists rooted at `PTR_DAT_8004f6c0` (`+8`/`+0xc`
or `+0x10`/`+0x14`) via bit 0 of `*(byte*)(param_1+2)`; gated on flag bits
`0x12` in that byte. Early-return path logs via `possible_logging_function__var_args`
when `param_1[1]!=0`. Structural sibling of `sorted_event_list_insert_by_relative_key`
(`0x8004ee94`) and `sched_event_sorted_insert_with_overlap_pushback`
(`0x800538b4`); insert counterpart to `FUN_8004ef08` dequeue/dispatch cluster.

Post-rename: **178 unnamed** in-region (95 in 1-150B tier unchanged); **83** in
>150B tier.

**Next:** continue >150B cold-triage — decompile+rename rank-4 `0x8004bde8`
(354B, 6 xrefs).

## Pass 52cj (2026-06-30) — >150B rank-2 BD_ADDR slot reconcile + LMP 0x259 dispatch

**>150B rank-2 decompiled and renamed (HIGH):** `FUN_8004090c` →
`reconcile_nonmatching_bdaddr_slot_and_dispatch_lmp_259` (246B, 11 xrefs) via
`RenamePass52cjRegion80040000Fun8004090c.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 4, 2026-06-23). IRQ-disabled BD_ADDR slot
reconciliation handler gated on `the_0x300->int_0x10==2` and a pending byte at
`PTR_DAT_80040a0c != 0xff`: builds BD_ADDR from `PTR_another_bdaddr_80040a10`,
calls `look_for_non_matching_bdaddr_bos_index_i_e__free_connection_slot`, reads
per-slot `byte_0xCC` from `big_ol_struct`, calls
`and_mask_hw_channel_table_entry_and_indexed_dispatch` (AND-mask=0) to clear the
HW-channel table entry, clears the pending byte to `0xff`, then dispatches
`LMP_259_opcode_handler`. Secondary path when `int_0x10==1` emits event 600 via
`possible_logger_called_if_no_patch3`. Optional pre-hook at `PTR_DAT_80040a04`
can short-circuit. Called from the shared connect-procedure dispatcher
(`called_by_fHCI_Remote_Name_Request_5`).

Post-rename: **179 unnamed** in-region (95 in 1-150B tier unchanged); **84** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cl below.

## Pass 52cl (2026-06-30) — >150B rank-4 verify + rank-5 conn-table gate sweep rename

**>150B rank-4 already resolved (Pass 51, live-verified):** `0x8004bde8` →
`esco_sco_lmp_pdu_validate_negotiate_and_dispatch` (354B, 6 xrefs). Cold-triage
rank-4 was formally closed this pass — function was opportunistically renamed in
Pass 51 (2026-06-28); live decompile confirms name persisted in Ghidra.

**>150B rank-5 decompiled and renamed (HIGH):** `FUN_800431a0` →
`sweep_conn_table_clear_esco_gate_bytes_and_apply_codec_config` (184B, 5 xrefs)
via `RenamePass52clRegion80040000Fun800431a0.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 4/5, 2026-06-23). Sweeps all 10
`big_ol_struct` connection slots: for valid entries reads `byte_0xCC` (role-switch
hook index) and `bos_connection__array_index`, remaps index `+8` into eSCO range
when gate-table bit1 set and bit0 clear (`PTR_DAT_8004325c`), IRQ-disables and
clears per-index gate bytes at `PTR_DAT_80043260`/`PTR_DAT_80043264`, then when
`field200_0x206==0`, secondary gate byte clear, role-switch hook bit1 armed, and
status-array index not 1/5 — calls `FUN_80014dac` (codec-config apply). Post-
apply housekeeping callee of `apply_codec_type_and_role_switch_hook_dispatch`
(region `0x80000000`); structural sibling of `FUN_8004326c` (Pass 5 per-slot
packet-type sweep).

Post-rename: **177 unnamed** in-region (95 in 1-150B tier unchanged); **82** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cm below.

## Pass 52cm (2026-06-30) — >150B rank-6 dual-list timer queue dispatch rename

**>150B rank-6 decompiled and renamed (HIGH):** `FUN_8004ef08` →
`dispatch_inflight_timer_queue_drain_pending_and_promote_next` (526B, 4 xrefs)
via `RenamePass52cmRegion80040000Fun8004ef08.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 4/5, 2026-06-23). Timer/event-queue dispatch
over three list heads at `PTR_DAT_8004f118` (`+4` in-flight, `+8` pending,
`+0x10` active): dispatches in-flight entry via `PTR_DAT_8004f128` callback after
budget check (`try_reschedule_timer_queue_entry_deadline_within_budget`), drains
pending list entries that exceed budget (unlink + dispatch), moves active-list
entries back into sorted pending via `sorted_event_list_insert_by_relative_key`
while wraparound-masked delta `DAT_8004f12c` budget remains, then promotes next
entry to in-flight (`+4`) via `PTR_DAT_8004f13c` arm callback. Dequeue/dispatch
counterpart to Pass 52ck's `dual_list_sorted_event_insert_with_overlap_pushback`
insert primitive and Pass 52ab/52ay deadline-reschedule siblings.

Post-rename: **176 unnamed** in-region (95 in 1-150B tier unchanged); **81** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cn below.

## Pass 52cn (2026-06-30) — >150B rank-7 conn hw sub-object hierarchy teardown rename

**>150B rank-7 decompiled and renamed (HIGH):** `FUN_8004e5ac` →
`teardown_conn_hw_resource_subobject_tree_and_free_to_pools` (188B, 4 xrefs)
via `RenamePass52cnRegion80040000Fun8004e5ac.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 5, 2026-06-23) — formalizes prior analysis in
`reverse_engineering_conn_record_subsystem.md` §11. Releases the 3-level hardware
sub-object tree rooted at `conn_rec[+0x50]`: walks up to 11 nodes each on
`hw_resource[+0x20]` and `[+0x24]` sub-chains, LIFO-pushes SCO sub-resources
(when type bits `[+0x08]&7` nonzero) to `PTR_PTR_8004e668` and all nodes to
`PTR_PTR_8004e66c`, clears `conn_rec[+0x50]`. Conn-record free-path callee
alongside `is_conn_record_pkt_modes_cleared_for_free` gate; neighborhood sibling
of `is_any_conn_lmp_procedure_busy_by_index` (`0x8004e500`) and
`copy_param_buffer_u16_triplet_to_global_slots` (`0x8004e584`).

Post-rename: **175 unnamed** in-region (95 in 1-150B tier unchanged); **80** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52co below.

## Pass 52co (2026-06-30) — >150B rank-8 LMP-25C scheduling-readiness probe rename

**>150B rank-8 decompiled and renamed (HIGH):** `FUN_80040060` →
`probe_lmp_25c_scheduling_readiness_by_credit_window` (184B, 4 xrefs) via
`RenamePass52coRegion80040000Fun80040060.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 5, 2026-06-23). Per-connection
scheduling-readiness gate for LMP-0x25C procedure slots: when coexistence flag
`PTR_DAT_80040118` is set, clears deadline sentinel at subrecord `+0x40c` and
sets mode bits at `param_1+1`; derives initial state byte at `conn_rec+0x247`
from 2-bit mode field; when sentinel is `-1` marks ready (`+0x247=1`); else
under IRQ-disable calls `LMP__25C_called2()` and compares timing credit
`ushort+0x6e` doubled against wraparound-masked global window minus deadline
`*piVar9`, promoting `+0x247` to 1 when credit window permits. Called by
`sweep_linked_conn_slots_reschedule_timing_window_by_index_and_type` and
siblings in the LMP-25C busy-wait / slot-timing-reprogram cluster.

Post-rename: **174 unnamed** in-region (95 in 1-150B tier unchanged); **79** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cp below.

## Pass 52cp (2026-06-30) — >150B rank-9 conn-table eSCO packet-type sweep rename

**>150B rank-9 decompiled and renamed (HIGH):** `FUN_8004326c` →
`sweep_conn_table_program_esco_packet_type_and_clear_gate_bytes` (166B, 4 xrefs)
via `RenamePass52cpRegion80040000Fun8004326c.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 5, 2026-06-23). Sweeps all 10 `big_ol_struct`
connection slots (skipping `param_1`): for valid entries reads `byte_0xCC`
(role-switch hook index) and `bos_connection__array_index`, remaps index `+8`
into eSCO range when gate-table bit1 set and bit0 clear, and when secondary gate
byte is `1` calls `FUN_80014450()` then `FUN_80034c5c(0x1c00, 0xc000, codec_idx)`
— programming eSCO (`0x1c00`)/max-rate-SCO (`0xc000`) packet types using the same
constant pair as `apply_codec_type_and_role_switch_hook_dispatch` — then IRQ-
disables and clears per-index gate bytes at `PTR_DAT_80043324`/`PTR_DAT_80043328`
to `0xff`. Post-apply housekeeping callee in the codec-type/role-switch cluster;
structural sibling of `sweep_conn_table_clear_esco_gate_bytes_and_apply_codec_config`
(`0x800431a0`, Pass 52cl).

Post-rename: **173 unnamed** in-region (95 in 1-150B tier unchanged); **78** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cq below.

## Pass 52cq (2026-06-30) — >150B rank-10 overflow-record list splice rename

**>150B rank-10 decompiled and renamed (HIGH):** `FUN_8004b3c0` →
`splice_overflow_record_into_conn_list_a_or_b` (162B, 4 xrefs) via
`RenamePass52cqRegion80040000Fun8004b3c0.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 5, 2026-06-23). IRQ-locked intrusive-list splice
helper gated on nonzero byte-count at `param_1[2]`: selects per-connection
list-A (`field289_0x128`, `param_4==0`) or list-B (`field403_0x1a0`,
`param_4!=0`) on the `0x1ac`-strided connection-record array; when
`param_3==0` appends the 3-word overflow record onto the list tail (unaligned
4-byte link store at `+0x100..0x103`), else prepends at head, then accumulates
the list byte-count field. Shared post-drain step in the quota/pending-event
pipeline — callee of `atomically_take_conn_list_a_collect_overflow_and_schedule_tx`
(list-A/`+0x128`) and `atomically_take_conn_list_b_and_apply_quota_overflow`
(list-B/`+0x1a0`).

Post-rename: **172 unnamed** in-region (95 in 1-150B tier unchanged); **77** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cr below.

## Pass 52cr (2026-06-30) — >150B rank-11 channel-slot enable/refcount setter rename

**>150B rank-11 decompiled and renamed (HIGH):** `FUN_8004fd6c` →
`set_channel_slot_enable_refcount_and_conn_record_mode` (226B, 3 xrefs) via
`RenamePass52crRegion80040000Fun8004fd6c.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 5, 2026-06-23). Gated by
`is_conn_record_pkt_modes_cleared_for_free(param_3)`; for slot index `param_1<4`
switches on operation code `param_2-1` over global 4-entry × 7-byte-stride table
`PTR_DAT_8004fe60`: op 1 programs 2-bit mode nibbles into conn-record `+0x1e`
and sets enable bit 1; ops 2/4 clear enable bit; ops 5/6 set/clear refcount bit
2 with shared counter increment/decrement at `puVar1[1]`; invalid index logs
error `0xd2`. Per-channel-slot enable/ref-count state-machine setter — callee of
`arm_lmp_procedure_slot_pending_by_active_link_count` caller chain (HCI sync-conn
param commit) and `le_channel_selection_algorithm_event_dispatch` (region
`0x80050000`).

Post-rename: **171 unnamed** in-region (95 in 1-150B tier unchanged); **76** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cs below.

## Pass 52cs (2026-06-30) — >150B rank-12 feature-mask-gated conn param setter rename

**>150B rank-12 decompiled and renamed (HIGH):** `FUN_8004b898` →
`set_feature_mask_gated_conn_param_0x91_with_hw_hook_notify` (194B, 2 confirmed
callers) via `RenamePass52csRegion80040000Fun8004b898.java` (`renamed=1`,
live-verified). Upgraded from MEDIUM-HIGH (Pass 5, 2026-06-23). Bounds-checks
index `param_1-0x10 < 0xb` (11 conn slots); under IRQ-disable verifies per-conn
valid-entry bit (`field3_0x3 & 1`) and header feature mask
(`field453_0x1d2`/`field454_0x1d3` bit-indexed by slot); on pass writes
`param_2` to `field138_0x91`, sets `field136_0x8f` bit `0x20`, sets/clears
`field139_0x92` bit 0 per `param_3`, invokes optional HW hook at
`PTR_DAT_8004b960`, returns `1`/`0xff`; always logs. Callers:
`fHCI_Disconnect_0x06` + `FUN_8004c2f0` (conn-slot-10 maintenance cluster
sibling of `irq_safe_stage_conn10_diagnostic_dword_pair_and_incr_count`).

Post-rename: **170 unnamed** in-region (95 in 1-150B tier unchanged); **75** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52ct below.

## Pass 52ct (2026-06-30) — >150B rank-13 link-setup baseband register writer rename

**>150B rank-13 decompiled and renamed (HIGH):** `FUN_8004eb18` →
`write_link_setup_timing_params_to_baseband_register_block` (404B, 2 confirmed
callers) via `RenamePass52ctRegion80040000Fun8004eb18.java` (`renamed=1`,
live-verified). Upgraded from MEDIUM (Pass 6, 2026-06-23). Packs negotiated
link-timing fields from global ctx `PTR_PTR_8004ecac` (offsets
5/6/8/0xc/0xe/0x10/0x11/0x12/0x14/0x16) and param buffer `param_2` (offsets
4/0xa/0xc/0xe/0x10/0x12) into ~14 unaligned baseband HW register pointers
(`DAT_8004ecb0`…`DAT_8004ecf4`); `param_3` low 3 bits OR into bits 12:14 of one
packed register; conditional OR-flag set at `DAT_8004ece8` when 2-bit sub-field
from ctx byte 5 exceeds 1. Full register-block writer — callee of twin link-setup
PDU handlers `0x80051c60`/`0x80051f14` (region `0x80050000`), paired with smaller
sibling `write_compact_link_setup_timing_to_baseband_hw_regs` (`0x8004ea2c`, renamed
Pass 52cx).

Post-rename: **169 unnamed** in-region (95 in 1-150B tier unchanged); **74** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cu below.

## Pass 52cu (2026-06-30) — >150B rank-14 eSCO/SCO negotiation diagnostic logger verify

**>150B rank-14 live-verified (HIGH):** `FUN_8004f374` →
`esco_sco_negotiation_diagnostic_logger` (368B, 2 callers) — already renamed
Pass 12 region `0x80050000` (2026-06-27); Pass 52cu batch-decompiled live from
GZF (`batch_decompile_functions`, `renamed=alreadyOk`). Upgraded from MEDIUM
(Pass 6, 2026-06-23). Pure diagnostic logger (no state mutation): five
`possible_logging_function__var_args` dumps gated on LMP PDU byte `param_1+2`
low 6 bits — header fields, up to four sub-record pointers from `param_2[0..4]`
(packet-type/window/offset), and trailing variable-length region when
`(param_1+1)-mask` nonzero. Callees of `lmp_esco_sco_negotiation_packet_handler`
(`0x80052c64`) and patch dispatch slot `0x8010baa6` — see
`reverse_engineering_conn_feature_dispatch.md` §6.

Post-rename: **169 unnamed** in-region (95 in 1-150B tier unchanged); **74** in
>150B tier (no count change — name predated this pass).

**Next:** continue >150B cold-triage — completed Pass 52cv below.

## Pass 52cv (2026-06-30) — >150B rank-15 bitmasked timing subfield sync

**>150B rank-15 decompiled+renamed (HIGH):** `FUN_8004f730` →
`sync_bitmasked_timing_subfields_from_active_conn_buffer` (230B, 1 caller via
`find_callers`) — upgraded from MEDIUM (Pass 6, 2026-06-23). Optional early-exit
hook at `PTR_DAT_8004f818`; when hook absent or returns 0, scans bits 0–4 of
`param_1+0xb` and for each set bit copies the low 6 bits of
`*(byte*)(active+0x10)` into destination bytes (preserving high 2 bits via
`& 0xc0 | & 0x3f`), advancing destination offset via stride table
`PTR_DAT_8004f820`. Type selector `param_1+8 & 7` picks active/destination
pointer pair: type 0 → `+0x4c`/`+0x50` (+ extra `+0x34`/`+0x38` byte-pointer
writes on bit 4); types 1–3 → `+0x18`/`+0x20`; types ≥4 → `+0x4c`/`+0x50` with
diagnostic log (`0xd39`). Sole caller `LE_connection_channel_update_timing_handler`
(`0x800555bc`, region `0x80050000`) — invoked after
`propagate_timing_offset_to_peer_record_by_type` /
`assemble_role_bitmask_param_fields` on the LE channel-update / AFH timing
propagation path. Via `RenamePass52cvRegion80040000Fun8004f730.java`, `renamed=1`,
live-verified.

Post-rename: **168 unnamed** in-region (95 in 1-150B tier unchanged); **73** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cw below.

## Pass 52cw (2026-06-30) — >150B rank-16 conn subsystem global reset

**>150B rank-16 decompiled+renamed (HIGH):** `FUN_800442bc` →
`reset_conn_subsystem_global_state_and_reinit_slot_entries` (222B) — upgraded
from MEDIUM-HIGH (Pass 6, 2026-06-23). Sets sentinel `0xff`, clears
`the_0x300->int_0x10`, calls `reset_four_conn_lap_and_dual_slot_role_records`,
zeros ~20 link-manager globals, loops 12 iterations calling `FUN_800425e0` per
index while clearing parallel per-slot byte/dword arrays, conditionally invokes
`LMP__25B__most_common_for_VSCs1()` when pending-VSC sentinel `!= -1`, then
IRQ-off clears a 10-entry ushort/byte table before final global field zeroing.
Subsystem-wide connection-state teardown/reset routine — structural sibling of
`global_link_state_reset_dispatcher` (`0x8001347c`, region `0x80010000`) and the
codec/role-switch gate-byte sweeps at `0x8004326c`/`0x800431a0`. Cold-triage
listed 2 xrefs; live `find_callers`/`xrefs_to` returned 0 (likely fn-ptr
registration). Via `RenamePass52cwRegion80040000Fun800442bc.java`, `renamed=1`,
live-verified.

Post-rename: **167 unnamed** in-region (95 in 1-150B tier unchanged); **72** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cx below.

## Pass 52cx (2026-06-30) — >150B rank-17 compact link-timing baseband writer

**>150B rank-17 decompiled+renamed (HIGH):** `FUN_8004ea2c` →
`write_compact_link_setup_timing_to_baseband_hw_regs` (220B, 2 confirmed
callers) via `RenamePass52cxRegion80040000Fun8004ea2c.java` (`renamed=1`,
live-verified). Upgraded from MEDIUM (Pass 6, 2026-06-23). Compact 3-register
sibling of `write_link_setup_timing_params_to_baseband_register_block`
(`0x8004eb18`): packs global-ctx byte 5 sub-fields from `PTR_PTR_8004eb0c`
with `param_2` LSB and `param_3` low 3 bits into `DAT_8004eb08`/`DAT_8004eb10`/
`DAT_8004eb14`; conditional OR-flags `0x2000`/`0x4000` when 2-bit sub-fields
exceed 1. Callees: `program_hw_channel_and_slot_params` (`0x80052008`, region
`0x80050000`) and twin link-setup PDU handlers `0x80051c60`/`0x80051f14`.

Post-rename: **166 unnamed** in-region (95 in 1-150B tier unchanged); **71** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cy below.

## Pass 52cy (2026-06-30) — >150B rank-18 pending-completed-packet drain

**>150B rank-18 decompiled+renamed (HIGH):** `FUN_8004c940` →
`drain_conn_pending_completed_packets_and_emit_hci_event` (194B, 2 xrefs) via
`RenamePass52cyRegion80040000Fun8004c940.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 6, 2026-06-23). Per-connection pending-packet
list drain: IRQ-off swap/clear of embedded list head at conn-record
`field403_0x1a0`/`field407_0x1a4`/`field411_0x1a8` (`0x1ac` stride array);
walk extracted list clearing node `+0x100`, calling fn-ptr hook `(node, 5)` with
log on nonzero return; increments `field89_0x59` by saved count; terminates in
`send_evt_HCI_Number_Of_Completed_Packets`. Callee of
`atomically_take_conn_list_b_and_apply_quota_overflow` (quota/pending-event
pipeline).

Post-rename: **165 unnamed** in-region (95 in 1-150B tier unchanged); **70** in
>150B tier.

**Next:** continue >150B cold-triage — completed Pass 52cz below.

## Pass 52cz (2026-06-30) — >150B rank-19 pending-negotiation hash pop verify

**>150B rank-19 live-decompiled+verified (HIGH):** `FUN_8004f25c` →
`pending_negotiation_hash_pop_by_distance` (186B, 2 xrefs) via
`RenamePass52czRegion80040000Fun8004f25c.java` (`renamed=0 alreadyOk=1`,
live-verified). Upgraded from MEDIUM-HIGH (Pass 6, 2026-06-23); name first
assigned Pass 12 region `0x80050000` (2026-06-27). Two-bucket hash-table
lookup-and-remove: walks list heads at `PTR_DAT_8004f318`/`PTR_DAT_8004f31c`,
compares wraparound-masked signed distance `(key - node[3]) & DAT_8004f324`
against threshold byte at `PTR_DAT_8004f320+0x21`, optional 12-bit secondary
match on packed ushort from child record fields `+0xb`/`+0xc`; unlinks and
returns matching node. Pop counterpart to
`dual_list_sorted_event_insert_with_overlap_pushback` /
`dispatch_inflight_timer_queue_drain_pending_and_promote_next` timer-queue
cluster; callee of `lmp_esco_sco_negotiation_packet_handler` (pending-negotiation
refcounted object pool).

Post-verify: **165 unnamed** in-region unchanged (name predated this pass);
**70** in >150B tier; live named **1473** unchanged.

**Next:** continue >150B cold-triage — decompile+rename rank-21 `0x80040a24`
(988B).

## Pass 52da (2026-06-30) — >150B rank-20 dual-slot LMP-25C packet credit programmer

**>150B rank-20 decompiled+renamed (HIGH):** `FUN_80043e04` →
`program_dual_slot_lmp25c_packet_credits_by_conn_index` (1168B, 6 xrefs) via
`RenamePass52daRegion80040000Fun80043e04.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 3, 2026-06-23). Per-connection-index
(`param_1 & 0xff`) dual-slot role-record programmer on the 0x84-stride table at
`PTR_DAT_800442a4`: gates on LAP entry at `the_0x300->_x142_LAP[index+0x45]`,
requires pending credit byte `entry[1]!=0` with `entry[2]==0` and no active
per-slot flags at `+0x32`/`+0x72`; iterates credit count decrementing
`entry[1]` while toggling slot selector `entry[0]^=2`. Two packet-source paths:
(1) fresh allocation via `FUN_8006b05c` with packet-type `0x3000` or `0x8000`
depending on conn-record `field273_0x250` gate; (2) existing LMP PDU lookup via
`FUN_80018654` / `lookup_up_to_3_bos_array_indices_by_connection_handle` with
`0x400`/`0x800` slot flags. IRQ-off programs per-slot fields at
`entry[slot*0x40+0x2c..0x40]` (conn index, role bits `>>6`, mapped conn-array
index, packet ptr), calls `LMP__25C_called2()` before each slot commit.
Inquiry/LAP/role-switch cluster sibling of
`reset_four_conn_lap_and_dual_slot_role_records` (`0x80043bfc`) and
`probe_lmp_25c_scheduling_readiness_by_credit_window` (`0x80040060`).

Post-rename: **164 unnamed** in-region (95 in 1-150B tier unchanged); **69** in
>150B tier; live named **1474**.

## Pass 52db (2026-06-30) — >150B rank-21 dual-slot LMP-25C role-record packet completion

**>150B rank-21 decompiled+renamed (HIGH):** `FUN_80040a24` →
`process_dual_slot_lmp25c_role_record_packet_completion` (988B) via
`RenamePass52dbRegion80040000Fun80040a24.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). Per-slot (`param_1`) +
per-conn-index (`param_2`) completion handler on the 0x84-stride dual-slot role
table at `PTR_DAT_80040e00`: increments credit byte `entry[1]` (caps at 2),
maps conn-array index from `entry[0x30]`, calls
`remap_role_index_to_esco_slot_if_pending` and optionally
`sweep_linked_conn_slots_reschedule_timing_window_by_index_and_type` when timing
window empty. Three state paths on `entry[+0x33]`: (0x02) TX/restart via
`FUN_8001840c` or error-log when `FUN_8001772c` fails; (0x01) RX completion
decodes LMP opcode from `(packet[+4]>>1)` — role-switch opcode 3 triggers HW
channel table programming (`or_merge`/`and_mask` on indices `0x5e`/`0xac`),
`FUN_800348c0`/`FUN_8003491c` role-switch dispatch,
`reconcile_nonmatching_bdaddr_slot_and_dispatch_lmp_259`, and
`test_pattern_buffer_fill_or_hw_mode_select`; config-gated BB register write at
`0xd8` when `field_0x179` is 3 or 4. Always terminates by calling
`program_dual_slot_lmp25c_packet_credits_by_conn_index` — the credit-chain
successor to Pass 52da's programmer. Inquiry/LAP/role-switch cluster sibling.

Post-rename: **163 unnamed** in-region (95 in 1-150B tier unchanged); **68** in
>150B tier; live named **1475**.

## Pass 52dc (2026-06-30) — >150B rank-22 inquiry/esco baseband programmer rename

**>150B rank-22 decompiled+renamed (HIGH):** `FUN_8004147c` →
`program_inquiry_or_esco_baseband_from_hci_command` (934B) via
`RenamePass52dcRegion80040000Fun8004147c.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM-HIGH (Pass 3, 2026-06-23). HCI command-parameter baseband
programmer reached via `fptr_DAT_80036f5c` for opcodes `0x401` (Inquiry),
`0x419` (Create_Connection_Cancel), and `0x43f` (Setup_Synchronous_Connection)
per `reverse_engineering_lc_lmp_state_machine.md`. IRQ-off path: feature-index
selector, BOS slot lookup, programs BD_ADDR halves + access-code sync word +
clock offset via `PTR_DAT_80041838` HW-write vtable (~15 registers incl.
role/AM_ADDR encode at `0xaa` with `0x2000` bit set when opcode `0x43f`),
clears role-switch hook via `clear_bos_e4_role_switch_hook_bit`, programs
channel-table via `or_merge_hw_channel_table_entry_and_indexed_dispatch`,
optional veto at `PTR_DAT_8004187c`; success arms BB reg `0` value `3`, sets
`the_0x300->int_0x10=2`, calls `FUN_800362b4`. Inquiry/LAP/role-switch cluster
sibling of Pass 52da/52db dual-slot LMP-25C programmers.

Post-rename: **162 unnamed** in-region (95 in 1-150B tier unchanged); **67** in
>150B tier; live named **1476**.

## Pass 52dd (2026-06-30) — >150B rank-23 inquiry/LAP slot teardown rename

**>150B rank-23 decompiled+renamed (HIGH):** `FUN_80041dac` →
`teardown_inquiry_lap_slot_baseband_cleanup_and_release` (876B) via
`RenamePass52ddRegion80040000Fun80041dac.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). Inquiry/LAP slot teardown
orchestrator on `param_1`-indexed `big_ol_struct` record: extracts LAP index
from `byte_0xCC` and conn index from `bos_connection__array_index`, calls
`remap_role_index_to_esco_slot_if_pending` + `LMP__25C_called2`, IRQ-off
baseband register teardown via `PTR_DAT_80042120` HW-write vtable (mirrors
Pass 52dc programmer shape), clears role-switch hook, decrements inquiry
refcount and may emit LMP `0x264`, calls `release_inquiry_lap_slot_pending_bitmask`,
restores channel-table entry when `_x142_LAP[slot+0x45]` clear,
`clear_afh_lap_channel_map_for_matching_offset_group`, clears multiple global
bitmask bytes, terminates with `FUN_800362b4` +
`triple_inverted_mask_hw_dispatch_for_lap_slot_if_0x45_clear` +
`remote_name_request_feature_apply_orchestrator`. Confirmed callers:
`fHCI_conn_req_cancel` (region `0x80030000`) and
`connection_teardown_HCI_event_finalizer` (region `0x80070000`). Inquiry/LAP/role-switch
cluster sibling of Pass 52dc programmer and Pass 52n release-bitmask setter.

Post-rename: **161 unnamed** in-region (95 in 1-150B tier unchanged); **66** in
>150B tier; live named **1477**.

## Pass 52de (2026-06-30) — >150B rank-24 SCO/eSCO LMP PDU builder rename

**>150B rank-24 decompiled+renamed (HIGH):** `FUN_80040594` →
`build_and_submit_sco_esco_lmp_pdu_for_conn_type_1_or_2` (566B) via
`RenamePass52deRegion80040000Fun80040594.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). SCO/eSCO LMP PDU assembler for
conn-type nibble 1–2: validates role/sub-index via
`lookup_codec_or_role_type_table_7x4`, allocates TX buffer via patch-hook fptr
(split on `the_0x300->field_0x179 == 2` inquiry vs normal path), packs connection
handle + mode bits into header, calls `FUN_800145e8` for PDU body, submits via
`possible_logger_called_if_no_patch3` (timeout `800` inquiry / `0x191` normal).
Inquiry path clamps conn index against `config_base->field174_0xb6` counter.
Confirmed caller: `FUN_80041230` (ring-buffer connection-event dequeue
dispatcher) when `local_3a >> 4` nibble is 1 or 2. SCO/eSCO conn-type cluster
sibling of Pass 52dc baseband programmer.

Post-rename: **160 unnamed** in-region (95 in 1-150B tier unchanged); **65** in
>150B tier; live named **1478**.

## Pass 52df (2026-06-30) — >150B rank-25 conn-event ring dequeue dispatcher rename

**>150B rank-25 decompiled+renamed (HIGH):** `FUN_80041230` →
`dequeue_conn_event_ring_and_dispatch_lmp_by_conn_type_nibble` (560B) via
`RenamePass52dfRegion80040000Fun80041230.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). IRQ-masked dequeue from 16-slot
16-byte conn-event ring (`PTR_DAT_80041460`/`41464`): copies one entry, advances
head mod 16, re-enables IRQs, parses handle (`local_3c & 0x3ff`), conn-type
nibble (`local_3a >> 4`), role sub-index (`local_39 >> 3 & 7`), and flag bits.
Early exits for LE/inquiry codec-match (`FUN_80015a68`), duplicate-codec reject
(`FUN_800158f8`), and config-gated special path (`FUN_80015c98`). Main path
filters via `gate_conn_event_ring_dequeue_by_sco_esco_type_flags`, validates role via
`lookup_codec_or_role_type_table_7x4`, then dispatches by conn-type nibble:
nibble 2 → `FUN_800411a4`; nibble 3 (in `local_3c` byte-1 bits 4–5) →
`build_and_submit_lmp_0x480_for_conn_type_3`; nibbles 1–2 → callee
`build_and_submit_sco_esco_lmp_pdu_for_conn_type_1_or_2` (Pass 52de). Logs on
empty ring. SCO/eSCO conn-type cluster hub sibling of Pass 52de PDU builder.

Post-rename: **159 unnamed** in-region (95 in 1-150B tier unchanged); **64** in
>150B tier; live named **1479**.

## Pass 52dg (2026-06-30) — >150B rank-26 SCO/eSCO packet-type selector rename

**>150B rank-26 decompiled+renamed (HIGH):** `FUN_80042640` →
`select_sco_esco_packet_type_and_cap_window_by_conn_index` (530B) via
`RenamePass52dgRegion80040000Fun80042640.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). Per-conn-index packet-type resolver
on `big_ol_struct[conn].field273_0x250` feature bitmask: early-exit when bit0
set copies fixed type from `PTR_DAT_80042858` and caps in/out window via
`PTR_DAT_8004285c`; otherwise walks bit-position tables (`PTR_DAT_8004286c` for
eSCO/`field_0xb7==2`, `PTR_DAT_80042868` for SCO) against window thresholds
(`0x12` floor → default `0x3000`), with config `field208_0xd8` bit `0x100`
gating alternate slot advance; optional pre/post hooks at `PTR_DAT_80042864` /
`PTR_DAT_80042878`. Sole caller `FUN_80018654` (LMP PDU linked-list walker) on
the non-LMP-25C-ready scheduling path. SCO/eSCO cluster sibling of Pass 52de/52df
PDU builder and conn-event ring dispatcher.

Post-rename: **158 unnamed** in-region (95 in 1-150B tier unchanged); **63** in
>150B tier; live named **1480**.

## Pass 52dh (2026-06-30) — >150B rank-27 dual-slot LMP-25C role-record commit rename

**>150B rank-27 decompiled+renamed (HIGH):** `FUN_800401c4` →
`commit_dual_slot_lmp25c_role_record_state_and_chain_credits` (426B) via
`RenamePass52dhRegion80040000Fun800401c4.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). IRQ-masked per-conn-index
(`param_1`) + role/type index (`param_2`) commit handler on the 0x84-stride
dual-slot role table at `PTR_DAT_80040370`: gates on active-slot state byte
`+0x32` being 2 or 3; calls `FUN_8002bb50` for role remap; when `+0x2e` flag
set dispatches via `FUN_800177a4` or `FUN_8006aedc` (when `+0x2f==1`);
when `+0x32` pending dispatches via `FUN_800179bc`/`FUN_80017c5c` or
`FUN_8006af6c`, updating `big_ol_struct` conn entry `field154_0xc7` and
per-conn byte at `PTR_DAT_8004037c`; increments credit byte `entry[1]` (caps
at 2), toggles dual-slot selector `entry[0]^=1`, clears pending flags.
Re-enables IRQs, optionally calls
`sweep_linked_conn_slots_reschedule_timing_window_by_index_and_type` when
`PTR_DAT_80040380` entry clear, then chains to
`program_dual_slot_lmp25c_packet_credits_by_conn_index`. Error path logs when
state not 2–3. Inquiry/LAP/role-switch cluster sibling of Pass 52db packet
completion handler (simpler commit path without packet decode).

Post-rename: **157 unnamed** in-region (95 in 1-150B tier unchanged); **62** in
>150B tier; live named **1481**.

## Pass 52di (2026-06-30) — >150B rank-28 dual-slot LMP role connection accept rename

**>150B rank-28 decompiled+renamed (HIGH):** `FUN_80040e60` →
`accept_dual_slot_lmp_role_connection_and_program_baseband_regs` (386B) via
`RenamePass52diRegion80040000Fun80040e60.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). IRQ-masked dual-slot LMP role
connection acceptance handler: reads active slot index from `PTR_DAT_80040fe4`
(0xff error path logs via `possible_logging_function__var_args`); stores role
index (`param_2`) to `PTR_DAT_80040ff0`; OR-merges HW channel table entry via
`or_merge_hw_channel_table_entry_and_indexed_dispatch` using conn id at
`param_1+0x16`; dual-slot toggle `alt_slot = (active_slot + 8) & 0xff` copies
8-byte stride entries between slots in `PTR_DAT_80040ff8`; programs baseband
registers through fptr `PTR_DAT_80041000` (mask-merge on first reg, then
bit-packed `(role << 5 | slot << 11)` write); calls
`LMP_accept_or_mirror_connection_handler` for BOS slot allocation/state setup;
programs remaining BB regs from per-slot tables; derives access-code sync word
via `compute_access_code_sync_word_from_bdaddr`; calls `FUN_80013c64` with
sub-opcode byte; restores IRQs. Sole caller `FUN_800411a4` (conn-type-nibble-2
dispatch path from `dequeue_conn_event_ring_and_dispatch_lmp_by_conn_type_nibble`).
Inquiry/LAP/role-switch cluster sibling of Pass 52dh role-record commit and Pass
52dc inquiry baseband programmer.

Post-rename: **156 unnamed** in-region (95 in 1-150B tier unchanged); **61** in
>150B tier; live named **1482**.

## Pass 52dj (2026-06-30) — >150B rank-29 HCI Create Connection page-train programmer rename

**>150B rank-29 decompiled+renamed (HIGH):** `FUN_80041900` →
`program_page_train_baseband_regs_and_start_paging` (376B) via
`RenamePass52djRegion80040000Fun80041900.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). HCI `Create_Connection` (opcode `0x405`)
page-train baseband programmer: calls `FUN_8003785c` pre-page setup; programs peer
BD_ADDR halves into HW reg `0x14`/`0x16`; when peer BD_ADDR low-24 matches cached
local `DAT_80041a80` uses default sync-word constants, else derives access-code via
`compute_access_code_sync_word_from_bdaddr`; programs clock-offset high bits to reg
`0x2e`; busy/abort guard when `the_0x300->ushort_0x24==0x20` and
`byte_0x16f!=0` returns `0xc`; scans up to 3 conn slots for status==4 collision
with `bdaddr_random==0` and bumps `byte_0x16f` to 2; selects page-timeout class
(`0x100`/`0x200`/`0x300`) from `(byte_0x16f + field_0x171)` sum; optional veto
hook at `PTR_DAT_80041a90`; writes `1` to HW reg `0` (start paging), sets
`the_0x300->int_0x10=1` busy flag, arms watchdog via `FUN_800362b4`. Dispatched
from `fptr_DAT_80036f54` for HCI opcode `0x405` (see
`reverse_engineering_lc_lmp_state_machine.md` §3). Paging/LC cluster sibling of
Pass 52di dual-slot role accept and Pass 52dc inquiry baseband programmer.

Post-rename: **155 unnamed** in-region (95 in 1-150B tier unchanged); **60** in
>150B tier; live named **1483**.

## Pass 52dk (2026-06-30) — >150B rank-30 automatic-flush-timeout calculator rename

**>150B rank-30 decompiled+renamed (HIGH):** `FUN_80043c7c` →
`compute_automatic_flush_timeout_ticks_by_connection_handle` (372B) via
`RenamePass52dkRegion80040000Fun80043c7c.java` (`renamed=1`, live-verified).
Upgraded from MEDIUM (Pass 3, 2026-06-23). HCI automatic-flush-timeout
calculator gated on per-handle link-type byte `0x03` in `PTR_DAT_80043df0`:
resolves connection via `lookup_up_to_3_bos_array_indices_by_connection_handle`;
optional veto hook at `PTR_DAT_80043df4`; walks dual TX-buffer slots in
`PTR_DAT_80043dfc` (0x84-stride per link-type row) weighting active ACL packet
types (`ushort@+0x2c >> 12`) as +1/+2/+3 by 1-slot vs 2-slot vs 3-slot masks
(`0x18`/`0x110` when `field_0xb7==2`); adds pending-queue counts from
`FUN_8006ae48` + `FUN_800177f8`; scales sum by `param_3` defaulting to
`config_base->field53_0x3b` and `param_2` defaulting to
`config_base->field52_0x3a` low nibble; returns
`((param_2 * sum) * 10) >> 3` slot ticks (or `0` on lookup/type mismatch).
No direct xrefs (indirect dispatch). ACL/flush-timeout cluster sibling of Pass
52dj page-train programmer and `OGC_3_OCF_2a` automatic-flush-timeout setter
(region `0x80010000`).

Post-rename: **154 unnamed** in-region (95 in 1-150B tier unchanged); **59** in
>150B tier; live named **1484**.

**Next:** continue >150B cold-triage — decompile+rename rank-32 `0x8004a9e4`
(152B, xrefs:1).

## Pass 52dl (2026-06-30) — >150B rank-31 list-A slot-chain overflow collect rename

**Stale pointer resolved:** rank-31 candidate `0x80043a60` was already renamed in Pass
52ci (`remote_name_request_feature_apply_orchestrator`). Refreshed cold-triage
(`ColdTriageRegion80040000Pass52dl.java`) shows actual rank-31 is `0x8004b29c`.

**>150B rank-31 decompiled+renamed (HIGH):** `FUN_8004b29c` →
`walk_conn_list_a_slot_chain_collect_overflow_records` (162B, 1 xref) via
`RenamePass52dlRegion80040000Fun8004b29c.java` (`renamed=1`, live-verified).
Walks the list-A slot-table chain at `PTR_PTR_8004b340` starting from saved head
index `param_2` until sentinel `10`: for each 0xc-stride table entry iterates
8-byte slot records, deduplicates against output tail `param_1[1]`, clears link
bytes at record `+0x100..+0x103`, appends to 3-word output list (`head`/`tail`/
`count`), copies metadata byte to `+0x106`, zeroes `+0x107`, advances chain via
`FUN_8004b170` + next-index byte at entry `+0xb`. Sole caller
`atomically_take_conn_list_a_collect_overflow_and_schedule_tx` (`0x8004b6ec`);
list-A twin of still-unnamed `FUN_8004b1d0` (list-B quota-overflow walk used by
`atomically_take_conn_list_b_and_apply_quota_overflow`).

Post-rename: **153 unnamed** in-region (95 in 1-150B tier unchanged); **58** in
>150B tier; live named **1485**.

**Next:** continue >150B cold-triage — decompile+rename rank-32 `0x8004a9e4`
(152B, xrefs:1).

## Pass 52dm (2026-06-30) — >150B rank-32 TX segment advance clamp helper rename

**>150B rank-32 decompiled+renamed (HIGH):** `FUN_8004a9e4` →
`compute_length_prefixed_segment_advance_clamped_to_remainder` (152B, 1 xref) via
`RenamePass52dmRegion80040000Fun8004a9e4.java` (`renamed=1`, live-verified).

```c
ushort compute_length_prefixed_segment_advance_clamped_to_remainder(byte *buf, ushort remain)
{
  byte len_byte = buf[1];
  byte adjusted = len_byte + 2;
  if ((len_byte + 2U & 1) != 0) adjusted = len_byte + 3;  /* align to even */
  ushort seg_len = *PTR_DAT_8004aa7c * 2 + 6 + adjusted;
  /* diagnostic logs on buf[0] low nibble + seg_len; clamp if remain < seg_len */
  return (remain < seg_len) ? remain : seg_len;
}
```

Single-segment variant of Pass 52ag's
`walk_tx_reassembly_buffer_consuming_length_prefixed_segments` formula
(`*scale * 2 + 6 + adjusted_len_byte`). Sole caller `FUN_8004ae74` (452B TX
fragment type-nibble dispatcher): invoked as fallback when packet-type nibble
`>= 9` or when LE type `4` bypasses the primary dispatch table
(`PTR_PTR_8004b044`); return value advances the reassembly buffer offset.
Connection/feature dispatch cluster sibling of `walk_tx_reassembly_buffer_*`
and `FUN_8004ce70` type-0 multi-chunk TX path.

Post-rename cold-triage (`ColdTriageRegion80040000Pass52dm.java`): **57**
unnamed in >150B tier; **152 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1486**.

**Next:** continue >150B cold-triage — decompile+rename refreshed rank-30
`0x800407e8` (168B, xrefs:1).

## Pass 52dn (2026-06-30) — >150B rank-30 conn-event update with RSSI/deferral rename

**>150B rank-30 decompiled+renamed (HIGH):** `FUN_800407e8` →
`update_conn_record_from_incoming_event_with_rssi_and_flag4_deferral` (168B, 1 xref) via
`RenamePass52dnRegion80040000Fun800407e8.java` (`renamed=1`, live-verified).

```c
void update_conn_record_from_incoming_event_with_rssi_and_flag4_deferral(conn_rec *rec, event *evt)
{
  if ((byte)(the_0x300->field_0x17d - 1) < 2) {   /* modes 1 or 2 */
    ushort rssi = return_RSSI_value(evt->field_6);
    evt->field_6 = rssi;
    rec->field_0x17 = (byte)rssi;
  }
  if (*PTR_DAT_80040894 != 0) {                    /* drain deferred record ptr */
    possible_logging_function__var_args(...);
    wraps_uninteresting_if_0x80100000__0_which_its_not_in_my_tests(...);
    *PTR_DAT_80040894 = 0;
  }
  if (((evt->flags & 4) == 0) || ((rec->field_0xb & 4) == 0)) {
    rec->field_0xb &= ~4;
    *rec = evt->field_8;                         /* apply dword update */
    possible_logger_called_if_no_patch3(..., 0xff, 0x47f, ...);
  } else {
    *PTR_DAT_80040894 = rec;                     /* defer when both flag-4 set */
  }
}
```

Per-connection record updater on incoming conn events: when global `the_0x300->field_0x17d`
is mode 1 or 2, refreshes RSSI via named `return_RSSI_value` into both the event buffer and
record `+0x17`; drains any previously deferred record pointer through
`wraps_uninteresting_if_0x80100000__0_which_its_not_in_my_tests`; when either the incoming
event or the record lacks flag bit 4 (`0x4`), clears that bit, copies the event's dword at
`+0x8` into `*rec`, and logs via `possible_logger_called_if_no_patch3` (tag `0x47f`); when
both sides have flag 4 set, defers by stashing `rec` in `PTR_DAT_80040894` for the next
invocation's drain path. Sole caller `FUN_800411a4` (LMP role-connection accept path
sibling of `accept_dual_slot_lmp_role_connection_and_program_baseband_regs`, Pass 52di).

Post-rename cold-triage (`ColdTriageRegion80040000Pass52dn.java`): **56**
unnamed in >150B tier; **151 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1487**.

**Next:** continue >150B cold-triage — decompile+rename refreshed rank-30
`0x80047628` (832B, xrefs:0).

## Pass 52do (2026-06-30) — >150B rank-30 LMP PDU fragment reassembly (role-bit-set) rename

**>150B rank-30 decompiled+renamed (HIGH):** `FUN_80047628` →
`reassemble_lmp_pdu_fragments_into_subrecord_chain_with_role_bit_set` (832B, 0 xrefs) via
`RenamePass52doRegion80040000Fun80047628.java` (`renamed=1`, live-verified).

HCI/LMP variable-length fragment-reassembly handler (role-bit-set variant, sibling of
`FUN_80047304`): resolves connection by handle via `conn_record_get_4byte_field_by_handle`;
validates fragment-phase byte at `param+4` (0/2=continue, 1/3=start, 4=start+poll); payload
length at `param+6`; accumulates running total in conn `+0x2c`; appends bytes into payload
subrecord chain at conn `+0x50` via `find_tail_of_payload_subrecord_chain_at_field0x50`
(`+0x11` used-count, `+0x14` buffer ptr); on overflow allocates fresh subrecord via
`setup_type3_esco_sco_conn_record_with_role_bit_set`; new-sequence paths call
`poll_hw_slot_counter_into_conn_record_field0x3e` (phase 1/3/4); optional hook at
`PTR_DAT_80047970`; completion teardown via `teardown_conn_hw_resource_subobject_tree_and_free_to_pools`;
terminates with `hci_event_sender(0xe,...)` Command Complete + status byte; calls
`conn_diagnostic_batch_dump` before event emit. Exact HCI/LMP opcode identity still not pinned
(role-bit-set vs role-bit-clear pairing is the distinguishing axis).

Post-rename cold-triage (`ColdTriageRegion80040000Pass52do.java`): **55**
unnamed in >150B tier; **150 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1488**.

**Next:** continue >150B cold-triage — decompile+rename refreshed rank-30
`0x8004cb48` (722B, xrefs:0).

## Pass 52dp (2026-06-30) — >150B rank-30 LMP PDU fragment reassembly (role-bit-clear) rename

**>150B rank-30 decompiled+renamed (HIGH):** `FUN_80047304` →
`reassemble_lmp_pdu_fragments_into_subrecord_chain_with_role_bit_clear` (780B, 0 xrefs) via
`RenamePass52dpRegion80040000Fun80047304.java` (`renamed=1`, live-verified).

HCI/LMP variable-length fragment-reassembly handler (role-bit-clear variant, sibling of
`reassemble_lmp_pdu_fragments_into_subrecord_chain_with_role_bit_set`): resolves connection by
handle via `conn_record_get_4byte_field_by_handle`; gates on conn `+0x1d` bit 1 (role-bit-clear
axis); validates fragment-phase byte at `param+4` (0/2=continue, 1/3=start); payload length at
`param+6`; accumulates running total in conn `+0x2c`; appends bytes into nested payload subrecord
chain via `find_tail_of_payload_subrecord_chain_at_field0x50_0x24` (`+0x11` used-count,
`+0x14` buffer ptr); on overflow allocates fresh subrecord via
`setup_type3_esco_sco_conn_record_with_role_bit_clear`; initial setup via
`setup_type2_esco_sco_conn_record_for_multilink`; new-sequence paths call
`poll_hw_slot_counter_into_conn_record_field0x3e` (phase 1/3); optional hook at
`PTR_PTR_80047618`; on completion (`+0x2e`==0) walks `+0x20` chain calling
`prepend_payload_subrecord_to_pending_lists_if_low3bits_set`; terminates with
`hci_event_sender(0xe,...)` Command Complete + status byte.

Post-rename: **149 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1489**.

**Next:** continue >150B cold-triage — decompile+rename refreshed rank-31
`0x80047c50` (700B, xrefs:0).

## Pass 52dq (2026-06-30) — >150B rank-30 eSCO slot programmer from LMP 0x22 PDU rename

**>150B rank-30 decompiled+renamed (HIGH):** `FUN_8004cb48` →
`program_esco_slot_from_lmp0x22_negotiation_pdu_and_emit_0x26f` (722B, 0 xrefs) via
`RenamePass52dqRegion80040000Fun8004cb48.java` (`renamed=1`, live-verified).

eSCO/SCO LMP PDU processor for length-type `0x22` (34-byte payload): validates buffer budget
against computed header+payload size; resolves SCO link-type from role context via
`resolve_parent_context_by_role` + `map_sco_link_type_to_hw_register_code`; IRQ-off sets
pending bit 2 on the `0x1ac`-stride slot struct entry; stages negotiated params via
`esco_sco_param_negotiate_and_stage`; copies TX/RX window/packet-type fields from staged
negotiation table or direct PDU bytes into slot fields `+0x2d..+0x32`/`+0x22..+0x2a`; on
active-slot path (`field3_0x3` bit 1) emits LMP status `0x26f` via
`possible_logger_called_if_no_patch3` and triggers
`conn_link_quality_history_reset_and_vsc_0xfc95_trigger`. Returns consumed byte count.
Sibling of `esco_sco_lmp_pdu_validate_negotiate_and_dispatch` and the eSCO cluster around
`esco_packet_type_validate_and_set_air_mode`.

Post-rename: **148 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1490**.

**Next:** continue >150B cold-triage — decompile+rename refreshed rank-31
`0x80047c50` (700B, xrefs:0).

## Pass 52dr (2026-06-30) — >150B rank-31 eSCO/SCO config PDU validator+committer rename

**>150B rank-31 decompiled+renamed (HIGH):** `FUN_80047c50` →
`parse_validate_and_commit_esco_sco_config_pdu_to_conn_record` (700B, 0 xrefs) via
`RenamePass52drRegion80040000Fun80047c50.java` (`renamed=1`, live-verified).

28-byte eSCO/SCO configuration PDU parser, validator, and connection-record committer
(documented in depth at `reverse_engineering_vsc_dispatcher.md`). Reached via patch
gateway pool `DAT_8010bc64` on the LMP VSC 0x268 path through `FUN_8010bba4` — not
dynamically installed by the patch installer. Parses PDU bytes at offsets 3–27 (handle,
flags word, bandwidth triplets, packet-type/retransmission fields); validates against
range constraints (returns `0x12` on failure); looks up or allocates a link record via
`conn_record_get_4byte_field_by_handle` / `alloc_link_record_and_register_by_index`;
writes ~15 conn-record fields (`+0x10` index, `+0x14`/`+0x18` bandwidth masks,
`+0x20` flags, `+0x1c`/`+0x1d` codec/mode nibbles, `+0x3d` power level, etc.);
on eSCO-flag path calls `esco_packet_type_validate_and_set_air_mode`; optional hook at
`PTR_DAT_80047f18`; non-eSCO path dispatches `dispatch_hw_hook_for_main_and_substates`
with teardown via `FUN_8004fcec` on alloc failure. Returns `0x00` OK, `0x07` alloc fail,
`0x0c` busy (record `+0x1d` bit 1), `0x12` invalid param.

Post-rename: **147 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1491**.

**Next:** continue >150B cold-triage — decompile+rename refreshed rank-33
`0x800480b0` (682B, xrefs:0).

## Pass 52ds (2026-06-30) — >150B rank-32 HCI SCO packet-type table validator+stager rename

**>150B rank-32 decompiled+renamed (HIGH):** `FUN_80046900` →
`validate_and_stage_sco_packet_type_table_from_hci_params` (682B, 0 xrefs) via
`RenamePass52dsRegion80040000Fun80046900.java` (`renamed=1`, live-verified).

HCI synchronous-connection parameter validator for a multi-entry SCO packet-type
table (sibling cluster to `HCI_Setup_Synchronous_Connection_handler` /
`HCI_Accept_Synchronous_Connection_Request_handler`). Parses command bytes at
`param+3..+5` (role/mode nibbles) and `param+0xc` (3-bit packet-type enable mask,
upper bits must be zero); validates total length `param+2 == enabled_count*16+10`.
Allocates a tag-9 singleton via `lazy_alloc_tag9_singleton_and_encode_lowbit_index`
for the bitmask; builds per-type index map for enabled packet types 0–2. For each
enabled 16-byte entry block: validates SCO/eSCO interval/latency/retransmission-window
bounds (same family as Setup/Accept handlers — max interval 6..0xc80, retrans latency
≤499, retrans window 10..0xc80, buffer-dimension cross-checks); writes validated
fields into the global staging block at `Ram80046bac` (`0x14` stride per type,
offsets `+0x22..+0x34`); calls `align_sco_slots_and_derive_retx_buffer_dims` per
entry. Copies all enabled entries to the first slot's staging area, updates global
timing fields on the tag-9 record (`+0xc/+0xd/+0xe`), merges mode nibbles into
`Ram80046bac+5`, and copies 6 bytes to `Ram80046bb4`. Returns `0x00` OK,
`0x12` invalid parameter, `0x43` alloc failure. 0 xrefs (indirect HCI dispatch).

Post-rename: **146 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1492**.

## Pass 52dt (2026-06-30) — >150B rank-33 HCI SCO air-mode change validator+stager rename

**>150B rank-33 decompiled+renamed (HIGH):** `FUN_800480b0` →
`validate_and_stage_sco_air_mode_change_from_hci_command` (682B, 0 xrefs) via
`RenamePass52dtRegion80040000Fun800480b0.java` (`renamed=1`, live-verified).

HCI synchronous-connection air-mode change handler (structural sibling of
`HCI_Accept_Synchronous_Connection_Request_handler` and
`validate_and_stage_sco_packet_type_table_from_hci_params`). Gated on global
`field451_0x1d0` bit 0; parses connection handle + air-mode/packet-type bytes
from HCI command buffer; resolves conn record via
`query_config_struct_0x1ac_by_index`; selects override/default packet-type pair
via `select_override_or_default_byte_pair_and_log`; validates via
`validate_esco_packet_type_params_with_hook`. Writes air-mode fields to conn
record (`+0x115..+0x117`, `+0x124`, `+0x118` low-nibble encoding for eSCO-flag
sub-cases 0/1/2). No-change path (staged bytes `+0x122`/`+0x123` match): sends
`send_evt_HCI_Command_Status` OK + logs LMP `0x26f` via
`possible_logger_called_if_no_patch3` (same idiom as Accept handler). Change
path when `+0x90` bit 2 clear and `+0x114` low nibble idle: stages new bytes at
`+0x11a`/`+0x11b`, sets `+0x60` bit 3, calls
`initiate_pending_procedure_or_defer`. Returns `0x00` OK, `0x02` lookup fail,
`0x0c` busy, `0x12` invalid param. 0 xrefs (indirect HCI dispatch).

Post-rename: **145 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1493**.

## Pass 52du (2026-06-30) — >150B rank-34 TX fragmentation scheduler rename

**>150B rank-34 decompiled+renamed (HIGH):** `FUN_8004b468` →
`fragment_conn_tx_overflow_chain_into_hw_descriptor_slots_by_budget` (624B, 0 xrefs) via
`RenamePass52duRegion80040000Fun8004b468.java` (`renamed=1`, live-verified).

IRQ-off snapshot of per-connection overflow queue head/count at `field289_0x128` /
`field293_0x12c` / `field297_0x130` / `field298_0x131` on the `0x1ac`-strided conn array,
then clears those fields. Walks the overflow record linked list via `+0x100` chain links.
For each record: allocates a HW descriptor slot index via `FUN_8004b344` (returns `0x0a` on
pool exhaustion — then splices back via `splice_overflow_record_into_conn_list_a_or_b` and
returns); builds 8-byte TX fragment descriptors in the per-slot table at `PTR_PTR_8004b6dc`
within the byte budget (`param_2`); advances record offset at `+0x106`; on completion either
returns the slot to the free pool via `FUN_8004b170` or enqueues the slot index to list-A
via `irq_masked_append_byte_to_conn_list_a_tail`. Sets descriptor termination flags (`0x80`
on last fragment, `local_18` bit7 for special-case records where type byte `+1` bits `0x30`
clear and offset `+0x106` zero). Core TX fragmentation scheduler invoked by
`atomically_take_conn_list_a_collect_overflow_and_schedule_tx` and the list-B quota path.
Upgraded from MEDIUM (Pass 3). 0 xrefs (indirect function-pointer registration).

Post-rename: **144 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1494**.

## Pass 52dv (2026-06-30) — >150B rank-35 HCI scan-activity validator rename

**>150B rank-35 decompiled+renamed (HIGH):** `FUN_80045964` →
`validate_and_commit_hci_scan_activity_params_from_command` (560B, 0 xrefs) via
`RenamePass52dvRegion80040000Fun80045964.java` (`renamed=1`, live-verified).

HCI scan-activity parameter validator+committer (upgraded from MEDIUM-HIGH, Pass 3).
Re-entrancy guard on global `the_0x300` struct byte at `+0x15dc`; gated on
`field208_0xd8` bit 0 and config flag `PTR_DAT_80045b98` bit 4. Two validation
paths: (a) scan-enable fast-path when type byte is 1 or 4 with `param_len < 4`,
copies 6 bytes via `optimized_memcpy` into template at
`PTR_base_of_0x1ac_struct_array_0xA_large2_0__field48_0x30_80045ba0` and
bit-packs mode flags into `+0x28`/`+0x29`; (b) interval/window pair path with
HCI-spec bounds `0x1f < value < 0x4001`, window ≤ interval, optional page-scan
repetition-mode check via `PTR_DAT_80045ba4`, writes interval to `+0x2c` and
window to `+0x2e`. Commits packed fields into global BT-state struct at
`+0x28..0x2f`. Returns `0x00` OK, `0x0c` busy (re-entrant), `0x12` invalid
param; logs via `possible_logging_function__var_args` then emits HCI Command
Complete via `hci_event_sender(0xe, &local_38, 4)`. Shape matches both
HCI_Write_Page_Scan_Activity and HCI_Write_Inquiry_Scan_Activity (indistinguishable
without confirming xref); named generically per Pass 3 precedent. 0 xrefs
(indirect HCI dispatch).

Post-rename: **143 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1495**.

## Pass 52dw (2026-06-30) — >150B rank-36 SCO/eSCO HW init blob rename

**>150B rank-36 decompiled+renamed (HIGH):** `FUN_8004d294` →
`init_or_reset_sco_esco_hw_registers_and_link_slots` (1280B, 0 xrefs) via
`RenamePass52dwRegion80040000Fun8004d294.java` (`renamed=1`, live-verified).

SCO/eSCO HW register init/reset blob (upgraded from MEDIUM, Pass 3 — prior
"LE Meta Event cluster adjacent" label was incorrect). Dual-mode on `param_1`:
baseline path always programs ~40 HW register globals via literal-pool
addresses, resets timing counters to `0xffff`, sets control/status flag bits,
calls `set_masked_nibble_field_on_sco_esco_hw_register(7,7)` and
`set_hw_control_flag_bit6(1)`. When `param_1==0` (full reset): derives
timing from `PTR_config_base_8004d794`, copies config fields into global
BT-state `+0x28..0x2f` and per-connection struct fields from
`PTR_base_of_0x1ac_struct_array_0xA_large2`, calls `FUN_8004a908` with
packed timing, caps channel-map index at 5 and builds enable bitmask,
optionally invokes feature-gated hook at `PTR_DAT_8004d86c`, clears 64
indexed link-register-B slots (`0xa0..0xdf`) plus 64 more (`0xe0..0x11f`)
via `write_indexed_link_register_b_with_slot_check`, and clears 11 eSCO link
register high nibbles via `write_esco_link_register_high_nibble_field_with_retry`.
Terminates with final timing copy from per-connection struct and completion
hook at `PTR_DAT_8004d8b4(0)`. Reached from HCI-Reset teardown chain
(`release_all_conn_records_and_invoke_teardown_chain`), link-loss SCO reset
(`reset_sco_esco_hw_subsystem_on_link_loss`), and teardown hook triplet
(`invoke_teardown_hook_triplet_with_lmp_power_gate` hook-3 with arg 0). 0 xrefs
(indirect fptr registration).

Post-rename: **142 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1496**.

## Pass 52dx (2026-06-30) — >150B rank-37 conn TX packet-type dispatcher rename

**>150B rank-37 decompiled+renamed (HIGH):** `FUN_8004ce70` →
`dispatch_conn_tx_by_packet_type_nibble_with_reassembly` (908B, 0 xrefs) via
`RenamePass52dxRegion80040000Fun8004ce70.java` (`renamed=1`, live-verified).

Connection/feature-dispatch cluster TX handler (upgraded from MEDIUM-HIGH, Pass 3).
Gated on feature bitmask `DAT_8004d1fc & *param_1`; extracts TX byte length from
descriptor bits 11–25 and conn index from bits 5–8. Dispatches on low 5-bit packet
type nibble:

- **Type 0** — multi-chunk TX reassembly loop: copies into staging buffer
  `PTR_DAT_8004d20c` in 0x400-byte chunks, programs HW descriptor per chunk via
  `FUN_8002b558`, walks length-prefixed segments via
  `walk_tx_reassembly_buffer_consuming_length_prefixed_segments`, dispatches
  fragments via `FUN_8004ae74`, then `refcount_decrement_and_free(param_2)`.
- **Type 1** — single-chunk path: programs HW descriptor, calls `FUN_8004c4a8`
  for conn-index slot work, compares clock-offset fields with optional
  `atomic_saturating_byte_decrement` on mismatch, optional timing diagnostic via
  hook at `PTR_DAT_8004d21c`; clears conn `+0x90` bit3 and may call
  `initiate_pending_procedure_or_defer`.
- **Type 4** — LE accumulate path:
  `walk_le_tx_segments_validate_slot10_clock_offset_and_return_count` consume +
  adds to global BT-state field at `+0xf8`.
- **Other** — `FUN_80014524(uVar15,1)` release + diagnostic log.

0 xrefs (indirect fptr registration). Callee cluster for Pass 52ag/52dm TX
reassembly helpers.

Post-rename: **141 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1497**.

## Pass 52dy (2026-06-30) — >150B rank-38 conn TX single-chunk segment walker rename

**>150B rank-38 decompiled+renamed (HIGH):** `FUN_8004c4a8` →
`walk_conn_tx_single_chunk_segments_dispatch_payload_by_type` (894B, 0 xrefs) via
`RenamePass52dyRegion80040000Fun8004c4a8.java` (`renamed=1`, live-verified).

Type-1 single-chunk callee of `dispatch_conn_tx_by_packet_type_nibble_with_reassembly`
(Pass 52dx). Optional pre-hook at `PTR_DAT_8004c828`. Walks length-prefixed segments in
the TX buffer (`param_1`, length `param_2`, conn index `param_3`):

- Parses per-segment header byte: low 2 bits = payload dispatch type; bit `0x20` =
  extended header with extra length/flag bytes and clock-offset field sizing.
- Indexes per-connection `0x1ac` struct at `local_5c * 0x1ac`; stores RSSI-derived
  value at `+0x5e` from trailer words.
- Calls `update_rssi_iir_filtered_for_connection` per segment.
- On success path (conn `+5` bit2 clear, segment fits budget): emits
  `send_evt_Meta_subevent_0x16` (LE Data Length Change) and dispatches payload —
  type 1/2 → `FUN_8004c2f0`, type 3+ → hook at `PTR_DAT_8004c83c` +
  `atomic_saturating_byte_decrement_by1_cnt1`.
- Error/overflow paths: `atomic_saturating_byte_decrement_by1_cnt1` + diagnostic log;
  tail diagnostic counter via `possible_logger_called_if_no_patch3`.

0 xrefs (indirect fptr registration). Connection TX dispatch cluster sibling of
`walk_tx_reassembly_buffer_consuming_length_prefixed_segments`.

Post-rename: **140 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1498**.

## Pass 52dz (2026-06-30) — >150B rank-39 baseband link setup programmer rename

**>150B rank-39 decompiled+renamed (HIGH):** `FUN_800483c0` →
`program_baseband_link_setup_slot10_and_send_hci_cmd_complete` (866B, 0 xrefs) via
`RenamePass52dzRegion80040000Fun800483c0.java` (`renamed=1`, live-verified).

Baseband link-setup programmer on conn-record slot 10 of the `0x1ac` struct array.
Takes handle u16 + four byte params + packet-mode 1–4:

- Bounds-checks eSCO-style limits (`param_2 > 0x27` or `param_4 > 7` → error path).
- Programs slot 10 fields `+0xf4..+0x106`, sets control bit `0x2000` on
  `DAT_80048734`, programs multiple baseband HW register globals.
- Dispatches packet-mode: mode 1 → category 0; mode 2 → category 1 + flag;
  mode 3 → category 2; mode 4 → category 2 + flag + alternate HW encoding.
- Calls `compute_indexed_table_addr_by_category_and_bank` for timing interval;
  derives slot count `(interval + 0x369) / 0x271` and writes to `DAT_80048744`.
- Optional veto callback at `PTR_DAT_80048748` — early return on non-zero.
- Always ends with `hci_event_sender(0xe, &{status_id, handle_lo, handle_hi, status}, 4)`
  Command Complete pattern.

Callee of `invoke_baseband_link_setup_from_param_buffer` (`0x80048948`, Pass 52cc).
Cross-region caller `FUN_80054b14` (0x80050000) confirmed in prior analysis.
Upgraded from MEDIUM-HIGH (Pass 3).

Post-rename: **139 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1499**.

## Pass 52ea (2026-06-30) — >150B rank-1 LE TX segment consumer rename

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004a730` →
`walk_le_tx_segments_validate_slot10_clock_offset_and_return_count` (456B, 1 xref
in cold-triage) via `RenamePass52eaRegion80040000Fun8004a730.java` (`renamed=1`,
live-verified).

Type-4 LE accumulate callee of
`dispatch_conn_tx_by_packet_type_nibble_with_reassembly` (Pass 52dx). Optional
pre-hook at `PTR_DAT_8004a8f8`. Walks length-prefixed segments in TX buffer
(`param_1`, length `param_2`):

- Parses per-segment header: bit `0x20` = extended header with clock-offset
  sizing; length byte at `buf[offset+1]`.
- Indexes conn-record slot 10 in the `0x1ac` struct array
  (`PTR_base_of_0x1ac_struct_array_0xA_large2_8004a8fc`); on clock-offset
  mismatch vs `+0x106`/`+0x101` low nibble, increments diagnostic counter at
  `+0xfc` and logs via `possible_logging_function__var_args`.
- Segment size uses global scale `*PTR_DAT_8004a904 * 2 + 6 + adjusted payload`
  (same family as `walk_tx_reassembly_buffer_consuming_length_prefixed_segments`).
- Returns accumulated segment count (`local_30[0]`); caller adds to global
  BT-state field at `+0xf8`.

Connection TX dispatch cluster sibling of
`walk_conn_tx_segments_dispatch_by_packet_type_nibble` (452B, adjacent segment
dispatcher).

Post-rename: **138 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1500**.

## Pass 52eb (2026-06-30) — >150B rank-1 TX segment dispatcher rename

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004ae74` →
`walk_conn_tx_segments_dispatch_by_packet_type_nibble` (452B, 1 xref in
cold-triage) via `RenamePass52ebRegion80040000Fun8004ae74.java` (`renamed=1`,
live-verified).

Type-0 multi-chunk reassembly callee of
`dispatch_conn_tx_by_packet_type_nibble_with_reassembly` (Pass 52dx). Optional
pre-hook at `PTR_DAT_8004b038`. Walks length-prefixed segments in TX buffer
(`param_1`, length `param_2`):

- Per-segment packet-type nibble from `buf[offset] & 0xf`.
- When nibble `< 9`: checks conn-record slot-10 status via
  `field68_0x44 & 0x40` or `scan_active_link_mask_for_slot_status_flag()`;
  type-4 with no active slot bypasses table dispatch; otherwise dispatches via
  function-pointer table `PTR_PTR_8004b044[nibble]`.
- When nibble `>= 9` or type-4 bypass: falls back to
  `compute_length_prefixed_segment_advance_clamped_to_remainder`.
- Diagnostic logging when slot-10 `field327_0x154 & 4` set; per-segment trace
  when type `!= 3`.
- On completion, if `field68_0x44 & 1`, calls
  `flush_rssi_batch_arrays_via_meta_subevent_0x2_or_0xb`.

Connection TX dispatch cluster sibling of
`walk_le_tx_segments_validate_slot10_clock_offset_and_return_count` (type-4 LE
accumulate path).

Post-rename: **137 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1501**.

## Pass 52ec (2026-06-30) — >150B rank-1 ext-adv param gate rename

**Cold-triage refresh:** `ColdTriageRegion80040000Pass52ec.java` — 42 unnamed
>150B; rank-1 `0x8004ab64` (414B, 1 xref).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004ab64` →
`gate_ext_adv_param_bitfields_and_apply_via_vsc_0xfc97` (414B, 1 xref in
cold-triage) via `RenamePass52ecRegion80040000Fun8004ab64.java` (`renamed=1`,
live-verified).

Per-link extended-advertising parameter gate on conn-record slot
`param_1` (`0x1ac` struct array at `PTR_base_of_0x1ac_struct_array_*`):

- When `field274_0x119 & 1`: clears bit 0, calls
  `commit_link_power_state_bits_to_hw_register_with_retry`, ORs `0xf` into
  `field273_0x118` low nibble.
- Validates 2-bit subfields from `param_2` at bits `[14:13]` and `[12:11]`
  against per-link status bytes `field283_0x122` / `field284_0x123` (bit masks
  `1`/`2`/`4` per subfield value).
- When `field283_0x122 & 4` and power-mode nibble mismatches, adjusts `param_2`
  with `0x4000`/`0x6000` power-state bits before apply.
- On success calls `VSC_0xfc97_Set_Extended_Advertising_Parameters_variant_2
  (link_idx, 0xe, param_2)`; diagnostic logging via
  `possible_logging_function__var_args`.

LE extended-advertising cluster sibling of rank-2 `FUN_8004c2f0` (414B, type-1
TX payload dispatcher in connection TX path).

Post-rename: **136 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1502**.

**Next:** continue refreshed >150B cold-triage — refresh rank list and
decompile+rename next rank-1 unnamed >150B candidate.

## Pass 52ed (2026-06-30) — >150B rank-1 type-12 TX payload dispatcher rename

**Cold-triage refresh:** `ColdTriageRegion80040000Pass52ed.java` — 41 unnamed
>150B; rank-1 `0x8004c2f0` (414B, 1 xref).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004c2f0` →
`dispatch_type12_conn_tx_payload_enqueue_slot10_or_teardown_inactive_link` (414B,
1 xref in cold-triage) via `RenamePass52edRegion80040000Fun8004c2f0.java`
(`renamed=1`, live-verified).

Type-1/2 connection TX payload dispatcher — callee target of
`walk_conn_tx_single_chunk_segments_dispatch_payload_by_type` (Pass 52dy):

- Optional pre-hook at `PTR_DAT_8004c490`; early return when hook vetoes.
- `param_4 < 0xb` conn-index bound; indexes `0x1ac` conn-record array.
- **Inactive link** (`field4_0x4 < 0`): state-machine on `field231_0xee` /
  `field232_0xef` — when `ee==1 && ef==4` emits `hci_event_sender` notification
  and clears param `0x3d` via `set_feature_mask_gated_conn_param_0x91_with_hw_hook_notify`;
  when `ee==0 && ef==4` invokes hook at `PTR_DAT_8004c49c`; always
  `atomic_saturating_byte_decrement_by1_cnt1` + diagnostic log.
- **Active link**: allocates TX buffer via
  `call_fptr_if_set_with_2_args_possibly_allocates_buf_at_arg2_`, packs 4-byte
  header (conn handle, type flag in bit4, payload length) + memcpy payload,
  enqueues via `FUN_8004b83c` into conn-slot-10 linked list at `+0xd0`/`+0xd4`.

Connection TX dispatch cluster sibling of
`gate_ext_adv_param_bitfields_and_apply_via_vsc_0xfc97` (Pass 52ec).

Post-rename: **135 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1503**.

**Next:** continue refreshed >150B cold-triage — refresh rank list and
decompile+rename next rank-1 unnamed >150B candidate (rank-2 `0x80047980`,
380B).

## Pass 52ee (2026-06-30) — >150B rank-2 HCI sync-conn param commit rename

**Cold-triage (from Pass 52ed refresh):** rank-2 `0x80047980` (380B, 1 xref).

**>150B rank-2 decompiled+renamed (HIGH):** `FUN_80047980` →
`validate_unique_handles_and_commit_sync_conn_params` (380B, 1 xref) via
`RenamePass52eeRegion80040000Fun80047980.java` (`renamed=1`, live-verified).

HCI synchronous-connection parameter commit helper — sole callee from
`FUN_80047b10` (outer handler that terminates via `hci_event_sender(0xe,…)`
Command Complete):

- **Duplicate-handle guard:** nested loop over `param_3` 4-byte entries at
  `param_2` (handle at byte 0); returns HCI `0x12` (Invalid HCI Command
  Parameters) on any duplicate.
- **Per-entry commit** (`param_1 != 0` commit mode): lookup via
  `conn_record_get_4byte_field_by_handle`; missing record → `0x42`.
- **New procedure** (`conn_rec+0x1d` bit2 clear): slot-offset bound check on
  `+0x20==0x1d` links; when `+0x20` bit0 set calls
  `arm_lmp_procedure_slot_pending_by_active_link_count` — returns HCI `9` on
  `0xff` capacity, else `set_channel_slot_enable_refcount_and_conn_record_mode`;
  writes `+0x1b`, `+0x22` (slot offset), `+0x17` (air mode); packet-type hook
  at `_FUN_80047afc` merges into `+0x28`; sets `+0x1d` bit2; schedules
  `build_linked_conn_param_buffers_and_schedule_link_timing_setup`.
- **Re-commit** (bit2 already set): re-merges packet-type mask into `+0x28`,
  diagnostic log via `possible_logging_function__var_args`.
- **Probe mode** (`param_1 == 0`): when bit2 set dispatches
  `conditional_dispatch_LE_channel_selection_algorithm`; gates on
  `config_base+0x1e0` low-5-bits vs entry index.

SCO/eSCO connection-setup cluster sibling of
`HCI_Setup_Synchronous_Connection_handler` /
`HCI_Accept_Synchronous_Connection_Request_handler` and the Pass 52do/52dp
LMP fragment-reassembly pair.

Post-rename: **134 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1504**.

**Next:** continue refreshed >150B cold-triage — refresh rank list and
decompile+rename next rank-1 unnamed >150B candidate (rank-2 `0x80041a94`,
352B).

## Pass 52ef (2026-06-30) — >150B rank-1 conn-type timing finalize rename

**Cold-triage (refreshed):** 39 unnamed >150B remain. rank-1 `0x80045000`
(358B, 1 xref); rank-2 `0x80041a94` (352B); rank-3 `0x800435a8` (338B);
rank-4 `0x80041028` (336B); rank-5 `0x8004704c` (296B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80045000` →
`hci_finalize_conn_type_timing_delta_send_cmd_complete` (358B, 1 xref) via
`RenamePass52efRegion80040000Fun80045000.java` (`renamed=1`, live-verified).

HCI command handler in the connection-type dispatch upper half
(`0x80045000` boundary) — sole xref from `0x8000ecc0` (function-pointer
registration, no enclosing function):

- **Timing delta:** reads conn-type index from
  `PTR_base_of_0x1ac_struct_array_0xA_large2->_x02_byte_0x1ac_index`.
  When index≠2: computes `field241_0xf8/field242_0xf9 −
  field245_0xfc/field246_0xfd` (directional timing-counter delta, same field
  family as `bump_retry_or_timeout_counter_and_log`). When index==2: reads
  `DAT_80045170`, checks `field248_0xff` bit0, tests sign of counter at
  `DAT_80045174` for overflow diagnostic.
- **State cleanup:** clears conn-type index to 0; AND-masks global HW status
  words at `DAT_80045168`/`DAT_8004517c`/`DAT_80045180`; diagnostic log via
  `possible_logging_function__var_args`.
- **Command Complete:** derives status from `the_0x300->field_0x165`
  (0 when cmd word==0, else field defaulting to 1); packs 6-byte
  `hci_event_sender(0xe,…)` with status + echoed cmd-word bytes + timing
  delta u16.

LE Meta Event / connection-type dispatch cluster sibling of
`hci_reentry_gate_field_0x165_cmd_echo_u16_send_cmd_complete` (Pass 52bq) and
`validate_unique_handles_and_commit_sync_conn_params` (Pass 52ee).

Post-rename: **133 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1505**.

**Next:** continue refreshed >150B cold-triage — refresh rank list and
decompile+rename next rank-1 unnamed >150B candidate (rank-2 `0x80041a94`,
352B).

## Pass 52eg (2026-06-30) — >150B rank-2 periodic inquiry configure rename

**Cold-triage (refreshed):** 38 unnamed >150B remain. rank-1 `0x800435a8`
(338B); rank-2 `0x80041028` (336B); rank-3 `0x8004704c` (296B);
rank-4 `0x80043a60` (358B, 15 xrefs); rank-5 `0x8004c4a8` (894B).

**>150B rank-2 decompiled+renamed (HIGH):** `FUN_80041a94` →
`configure_periodic_inquiry_lap_delays_baseband_and_arm_lmp` (352B, 1 xref)
via `RenamePass52egRegion80040000Fun80041a94.java` (`renamed=1`,
live-verified).

HCI Periodic Inquiry Mode configure handler — sole callee from
`fHCI_Periodic_Inquiry_Mode_0x03` (`0x8001bf44`, OGF 0x04 OCF 0x03):

- **State guard:** returns `0xff` when `the_0x300->int_0x10 == 2`
  (already in periodic inquiry); if `int_0x10 != 0` calls
  `reconcile_nonmatching_bdaddr_slot_and_dispatch_lmp_259()` first.
- **Shared init:** `FUN_800408ec()` (same teardown-path helper as
  `fHCI_Exit_Periodic_Inquiry_Mode_0x04`); sets `ptr_to_EIR_data` to
  `0x00000002`.
- **LAP/timing commit:** stores LAP (`param_1`) and min delay (`param_2`)
  to globals; encodes inquiry length (`param_4`) as `(length & 0xff) << 10`;
  programs max-delay low/high via shared HW-write fptr (`0x14`, `0x16`).
- **Baseband setup:** `or_merge_hw_channel_table_entry_and_indexed_dispatch`,
  `compute_access_code_sync_word_from_bdaddr`, programs sync-word halves +
  channel-table entry via fptr opcodes `0x10`/`0x12`/`0x2e`/`0x2c`.
- **Mode select:** derives `0x100`/`0x200`/`0x300` from
  `field_0x171`/`byte_0x16f`/`ushort_0x24` inquiry-mode byte triplet.
- **LMP arm:** `LMP__25B__most_common_for_VSCs1` + `VSC_0xfc95_called2`;
  on success scales delays × `0x500`, calls
  `LMP__268__most_common_for_VSCs2_checks_fptr_patch`, sets
  `the_0x300->int_0x10 = 1`, returns 0.

Inquiry/LAP cluster sibling of `program_inquiry_or_esco_baseband_from_hci_command`
(Pass 52dc), `fHCI_Exit_Periodic_Inquiry_Mode_0x04` (`0x80041c18`), and
`teardown_inquiry_lap_slot_baseband_cleanup_and_release` (Pass 52dd).

Post-rename: **132 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1506**.

**Next:** continue refreshed >150B cold-triage — refresh rank list and
decompile+rename next rank-1 unnamed >150B candidate (rank-1 `0x80041028`,
336B).

## Pass 52eh (2026-06-30) — >150B rank-3 inquiry LAP slot reassignment rename

**Cold-triage (refreshed):** 37 unnamed >150B remain. rank-1 `0x80041028`
(336B); rank-2 `0x8004704c` (296B); rank-3 `0x80043a60` (358B, 15 xrefs);
rank-4 `0x8004c4a8` (894B); rank-5 `0x8004704c` (296B).

**>150B rank-3 decompiled+renamed (HIGH):** `FUN_800435a8` →
`reassign_inquiry_lap_slot_refcount_pending_and_program_channel` (338B, 1 xref)
via `RenamePass52ehRegion80040000Fun800435a8.java` (`renamed=1`,
live-verified).

Inquiry/LAP slot transition handler — sole xref from `FUN_8003fcc8`
(`0x8003fd9c`, region `0x80030000`):

- **Slot context:** reads active LAP slot (`PTR_DAT_800436fc[0]`), pending new
  slot (`[2]`), and subfield at `[0xf]`; both slots must be `<4` or logs error
  `0xbf2`/`0xc19`.
- **Old-slot teardown (IRQ-off):** decrements `_x142_LAP[old+0x45]` refcount;
  when slot changes calls
  `triple_inverted_mask_hw_dispatch_for_lap_slot_if_0x45_clear`; calls
  `release_inquiry_lap_slot_pending_bitmask`; when refcount hits zero clears
  HW channel table entry via
  `and_mask_hw_channel_table_entry_indexed_dispatch_noirq` and dispatches
  teardown hook `(0, 5)`.
- **New-slot arm:** increments `_x142_LAP[new+0x45]` refcount; branches on
  `big_ol_struct[conn_index].bdaddr_random_`: public-address path calls
  `set_inquiry_lap_slot_pending_bitmask(1, …)` and ORs bit0 into channel-table
  ushort; random-address path calls
  `set_inquiry_lap_slot_pending_bitmask(0x21, …)` and merges low-3-bit field
  from context `[0xf]` into channel-table ushort; programs HW via shared fptr at
  `PTR_DAT_8004370c`.

Inquiry/LAP cluster sibling of `set_inquiry_lap_slot_pending_bitmask` (Pass 52o),
`release_inquiry_lap_slot_pending_bitmask` (Pass 52n), and
`teardown_inquiry_lap_slot_baseband_cleanup_and_release` (Pass 52dd).

Post-rename: **131 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1507**.

**Next:** continue refreshed >150B cold-triage — decompile+rename next rank-1
unnamed >150B candidate (`0x8004704c`, 296B).

## Pass 52ei (2026-06-30) — >150B rank-1 LMP connection-setup BB programmer rename

**Cold-triage (refreshed):** 37 unnamed >150B remain. rank-1 `0x80041028`
(336B); rank-2 `0x8004704c` (296B); rank-3 `0x80043a60` (358B, 15 xrefs);
rank-4 `0x8004c4a8` (894B); rank-5 `0x8004704c` (296B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80041028` →
`accept_lmp_conn_setup_and_program_baseband_from_unpacked_pdu` (336B, 0 xrefs)
via `RenamePass52eiRegion80040000Fun80041028.java` (`renamed=1`,
live-verified). Upgraded from MEDIUM (Pass 3, 2026-06-23).

IRQ-masked single-slot LMP connection-setup baseband programmer:

- **Guard:** logs via `possible_logging_function__var_args` when
  `PTR_DAT_80041178` flag zero; advances flag to `0x02` when non-zero.
- **IRQ-off setup:** AND-masks HW channel table entry (`0xfeff`) via
  `and_mask_hw_channel_table_entry_indexed_dispatch_noirq`; calls
  `FUN_800429ac` with `0x100`; stores role/conn-type byte (`param_2`) at
  context `+0xf`; calls `FUN_800607dc` for connection-state update.
- **BB reg programming:** indirect fptr `PTR_DAT_80041184` writes
  bit-packed `(slot << 0xb | role << 5)` then opcode `0`; indexes per-slot
  tables at `PTR_DAT_80041188`…`80041198` by conn index `puVar3[2]`.
- **PDU unpack:** `unpack_lmp_pdu_packed_6byte_field_from_offset4` on
  `param_1+4`; feeds unpacked bytes through fptr BB writes; derives
  access-code sync word via `compute_access_code_sync_word_from_bdaddr`;
  stores sync-word halves to `PTR_DAT_8004119c`.
- **Finalize:** `FUN_80013c64` with sub-opcode byte; restores IRQs; calls
  `wraps_uninteresting_if_0x80100000…` wrapper on PDU buffer.

No direct callers found (consistent with indirect conn-event-ring dispatch).
Connection-setup cluster sibling of Pass 52di
`accept_dual_slot_lmp_role_connection_and_program_baseband_regs` and region
`0x80070000` `LMP_accept_or_mirror_connection_handler` (both call the same
`unpack_lmp_pdu_packed_6byte_field_from_offset4` helper).

Post-rename: **130 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1508**.

**Next:** continue refreshed >150B cold-triage — decompile+rename next rank-1
unnamed >150B candidate (`0x8004c4a8`, 894B).

## Pass 52ej (2026-06-30) — >150B rank-1 HCI link policy param commit body rename

**Cold-triage (refreshed):** 36 unnamed >150B remain. rank-1 `0x8004704c`
(296B); rank-2 `0x8004c4a8` (894B); rank-3 `0x8004bde8` (354B, 6 xrefs);
rank-4 `0x8004c4a8` (894B); rank-5 `0x8004bde8` (354B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004704c` →
`commit_hci_link_policy_slot_intervals_from_cmd_payload` (296B, 1 xref from
`hci_link_policy_param_setup_handler_send_cmd_complete`) via
`RenamePass52ejRegion80040000Fun8004704c.java` (`renamed=1`, live-verified).

HCI Link Policy parameter commit body (delegate of Pass 52w handler):

- **Validation:** policy mode bytes at `param+3/+4` must be `<4`; link bitmask
  at `param+5` must have only bits 0/1/3 set (`&0xfa==0`).
- **Per-link iteration:** for each set bit in the link bitmask, reads 5-byte
  entries (`mode` byte + min/max interval ushorts); rejects when `mode>1`,
  `min<max`, or `max<4`.
- **Status-byte writes:** per-link mode byte stored at
  `global_ctx + is_active_link_mask_bit_four(bit) + 0x22` (the `(bit==4)+0x22`
  offset idiom from Pass 52bj).
- **Interval aggregation:** tracks min/max interval pair across all selected
  links; applies timing slack from `(byte@global+0xe3 >> 5) * 0xc`.
- **Tag-5 subrecord:** `lazy_alloc_tag5_singleton_and_encode_lowbit_index`
  allocates singleton; stores `+0x25=1`, interval triple at `+0x18/+0x1a/+0x1c`;
  packs policy mode bytes into global context `+5`.
- **Returns:** `0` success, `0x12` invalid params, `0x43` alloc failure.

Sibling of `hci_link_policy_settings_read_send_cmd_complete` (read-only path).

Post-rename: **129 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1509**.

**Next:** superseded by Pass 52ek cold-triage refresh (stale target `0x8004c4a8`
was already renamed Pass 52dy).

## Pass 52ek (2026-06-30) — >150B rank-1 inquiry/LAP slot completion rename

**Note:** Prior [NEXT] targeted `0x8004c4a8` (894B) but that function was already
renamed in Pass 52dy (`walk_conn_tx_single_chunk_segments_dispatch_payload_by_type`).
Fresh cold-triage (`ColdTriageRegion80040000Pass52ek.java`) reports **34** unnamed
>150B remain. rank-1 `0x80041c70` (288B); rank-2 `0x80040384` (254B); rank-3
`0x80049550` (252B); rank-4 `0x8004ad68` (244B); rank-5 `0x80040494` (238B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80041c70` →
`complete_inquiry_lap_slot_apply_lmp268_remote_name_and_arm_timer` (288B, 0 xrefs
in cold-triage) via `RenamePass52ekRegion80040000Fun80041c70.java` (`renamed=1`,
live-verified).

Inquiry/LAP slot completion handler — success-path counterpart to
`teardown_inquiry_lap_slot_baseband_cleanup_and_release` (Pass 52dd, which
*terminates* `FUN_800362b4`). Takes role index + sub-index bytes:

- **Lookup:** `lookup_codec_or_role_type_table_7x4` — on failure logs diagnostic
  and returns.
- **LMP 0x268 gate:** when global mode/feature bytes differ from the resolved
  table index and `big_ol_struct[+0x74]` non-zero, calls
  `LMP__268__most_common_for_VSCs2_checks_fptr_patch` with timer `0x280`.
- **Slot commit:** sets `big_ol_struct[table_idx].field_0xd9 = 1` (active flag).
- **Feature apply:** `remote_name_request_feature_apply_orchestrator()` (Pass 52ci
  structural parent).
- **Timer arm:** `FUN_800362b4()` — page/inquiry watchdog arm (sibling of Pass
  52dc/52dj success paths).
- **Diagnostic log:** emits BD_ADDR + LAP slot refcount via
  `possible_logging_function__var_args`.
- **Pending clear:** `remap_role_index_to_esco_slot_if_pending` then zeroes
  `PTR_DAT_80041da8[remapped_index]`.

0 xrefs (indirect fptr registration). Inquiry/LAP cluster sibling of Pass 52dd
teardown and Pass 52eg periodic-inquiry configure.

Post-rename: **128 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1510**.

**Next:** superseded by Pass 52el below.

## Pass 52el (2026-06-30) — >150B rank-2 conn-event ring gate rename

**>150B rank-2 decompiled+renamed (HIGH):** `FUN_80040384` →
`gate_conn_event_ring_dequeue_by_sco_esco_type_flags` (254B, 1 xref from
`dequeue_conn_event_ring_and_dispatch_lmp_by_conn_type_nibble`) via
`RenamePass52elRegion80040000Fun80040384.java` (`renamed=1`, live-verified).

Conn-event ring entry gate on the 16-byte dequeued record (`ushort *param_1`):
evaluates SCO/eSCO type-flag bits in `param_1[1]` (bit2 `0x4`, nibble `0xf0`,
bit1 `0x2`) and handle flag `param_1[0] & 0x800`. Uses scratch struct
`PTR_DAT_80040484` (+0x18 handle byte, +0xb status bitmask). On qualifying
type-2 paths with byte3 bits `0x38` clear, calls `FUN_800145e8` (LMP 0x25C
family) with handle `param_1[0] & 0x3ff` and sets scratch +0x18. Logs via
`possible_logging_function__var_args` on reject paths. Returns **0** = allow
caller to continue dispatch (`lookup_codec_or_role_type_table_7x4` path);
returns **1** = skip further dispatch (caller early-exits). Clears
`*PTR_DAT_80040484` on exit. SCO/eSCO conn-type cluster filter sibling of Pass
52df ring hub.

Post-rename: **127 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1511**.

**Next:** superseded by Pass 52em below.

## Pass 52em (2026-06-30) — >150B rank-3 PSM/QoS bitmask commit rename

**>150B rank-3 decompiled+renamed (HIGH):** `FUN_80049550` →
`commit_psm_qos_5byte_bitmask_and_send_hci_cmd_complete_and_sync` (252B) via
`RenamePass52emRegion80040000Fun80049550.java` (`renamed=1`, live-verified).

PSM/QoS 5-byte eligibility bitmask commit handler on `short *param_1` (37-bit
bitmask at offset +3): gates on `DAT_80049650 & *PTR_DAT_8004964c`; when byte+7
high nibble clear (`& 0xe0 == 0`), popcounts set bits across 37 indices; when
count >1 calls `FUN_80064360` to build eligibility mask, ANDs into five bitmask
bytes, `optimized_memcpy` copies 5 bytes to four staging buffers (BOS
`field16_0x10`, `field456_0x1d5`, `PTR_DAT_8004965c`, `field434_0x1bf`).
Returns status **0** (success), **0xc** (gate fail), or **0x12** (single-bit /
nibble reject). Always emits 4-byte HCI Command Complete via
`hci_event_sender(0xe,…)` with status + echoed cmd word from `*param_1`. On
success when BOS `field_0xf0 & 4`, calls
`sync_psm_qos_5byte_eligibility_to_conn_slots_by_channel_bitmask(param_1+3,0)`.
PSM/QoS cluster sibling of region `0x80070000` Pass 12eq/12ep finalize/sync
paths.

Post-rename: **126 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1512**.

**Next:** superseded by Pass 52en below.

## Pass 52en (2026-06-30) — >150B rank-4 LMP 0x26f status logger rename

**>150B rank-4 decompiled+renamed (HIGH):** `FUN_8004ad68` →
`log_conn_slot_status_word_0x26f_after_connection_init` (244B, 1 xref from
`conn_class_mode_apply_and_log_variant2` case-0 success path) via
`RenamePass52enRegion80040000Fun8004ad68.java` (`renamed=1`, live-verified).

Conn-slot LMP `0x26f` status-word logger on `uint param_1` (slot index): reads
global `_x1F4_struct` `field5_0x5` bit2 → low status nibble `0` or `6`; packs
`(param_1 & 0xf) << 5` into bits 5–8; ANDs with `DAT_8004ae60` mask; merges
`PTR_PTR_8004ae68[4] & 3` into high-byte bits 1–2; emits via
`possible_logger_called_if_no_patch3` (tag `0x26f`). When `config_struct`
`field_0xe0` bit0 set and `PTR_DAT_8004ae70` bitmask has bit `param_1` set,
re-emits with status `| 5`. Sole caller: `conn_class_mode_apply_and_log_variant2`
after `init_connection_record` + `find_duplicate_bdaddr_and_disconnect` in
case-0 class-mode apply path — sibling of inline `0x26f` logging in cases 1–4 of
the same dispatcher and `pack_and_log_param_pair_0x26f` (region `0x80050000`).

Post-rename: **125 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1513**.

**Next:** superseded by Pass 52eo below.

## Pass 52eo (2026-06-30) — >150B rank-5 conn-type-3 LMP 0x480 builder rename

**Refreshed cold-triage (`ColdTriageRegion80040000Pass52eo.java`):** **29** unnamed
>150B remain (down from 34 at Pass 52ek). rank-1 `0x8004c844` (234B); rank-2
`0x8004fbc0` (216B); rank-3 `0x80044490` (202B); rank-4 `0x8004b1d0` (200B);
rank-5 `0x8004a908` (186B); rank-6 `0x8004996a` (172B).

**>150B rank-5 decompiled+renamed (HIGH):** `FUN_80040494` →
`build_and_submit_lmp_0x480_for_conn_type_3` (238B, 1 xref from
`dequeue_conn_event_ring_and_dispatch_lmp_by_conn_type_nibble`) via
`RenamePass52eoRegion80040000Fun80040494.java` (`renamed=1`, live-verified).

Conn-event ring conn-type-nibble-3 LMP `0x480` PDU builder — sibling of
`build_and_submit_sco_esco_lmp_pdu_for_conn_type_1_or_2` (nibbles 1–2) and
`FUN_800411a4` (nibble 2 role-connection path). Called when dequeued record
byte-1 bits 4–5 encode conn-type `3` with handle `param_1`, role sub-index
`param_2`, and encoded role/type byte `param_3`:

- **Buffer alloc:** patch-hook fptr at `PTR_DAT_80040584` via
  `call_fptr_if_set_with_2_args_possibly_allocates_buf_at_arg2_`.
- **PDU body:** `FUN_800145e8(buf+4, handle, 0)` fills LMP payload.
- **Slot resolve:** when `param_2==0`, walks `big_ol_struct[0..9]` for valid
  non-random entry with `byte_0xCC == param_3`; else
  `lookup_codec_or_role_type_table_7x4`.
- **Header pack:** writes conn index at `+0x16`, handle at `+0x18`, role byte
  at `+0x19`.
- **Submit:** `possible_logger_called_if_no_patch3` with LMP opcode `0x480`.
- **Failure path:** `FUN_80014524(handle,0)` + diagnostic log.

Post-rename: **124 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1514**.

**Next:** superseded by Pass 52ep below.

## Pass 52ep (2026-06-30) — >150B rank-1 LC TX conn-event gate+enqueue rename

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004c844` →
`gate_lc_tx_conn_event_types_0_1_enqueue_or_emit_lmp_fallback` (234B, 1 xref from
`LC_event_TX_dispatcher` @ `0x80042530`) via
`RenamePass52epRegion80040000Fun8004c844.java` (`renamed=1`, live-verified).

LC TX conn-event handler for conn-type nibbles 0–1 (byte-1 bits 4–5) when role
bits (byte-1 >> 6) are clear. Parses connection handle from bytes 0–1, looks up
`query_config_struct_0x1ac_by_index`, and gates on config byte `+3` bitmask:

- **Success path:** clears 8 bytes at `param+0x100`, IRQ-masked enqueue via
  `FUN_8004b76c` into per-conn `0x1ac` struct-array linked list (index from
  config byte `+2`), then
  `dispatch_slot_timing_reprogram_if_feature_enabled_and_ready`.
- **Failure path:** patch-hook at `PTR_DAT_8004c930`, diagnostic log
  (`0xc9`/`0xb12`/`0xcaf`), optional LMP TX fallback via
  `invoke_lmp_tx_hook_with_length_word_from_pdu_buffer` with bytes
  `{0x10,0x01,0x02}` when `PTR_DAT_8004c938` bit0 set.

Sibling of conn-event-ring LMP builders (`build_and_submit_sco_esco_lmp_pdu_for_conn_type_1_or_2`,
`build_and_submit_lmp_0x480_for_conn_type_3`) but on the LC TX dispatch side via
`LC_event_TX_dispatcher`.

Post-rename: **123 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1515**.

**Next:** superseded by Pass 52eq below.

## Pass 52eq (2026-06-30) — >150B rank-2 conn-slot timing re-arm rename

**>150B rank-2 decompiled+renamed (HIGH):** `FUN_8004fbc0` →
`rearm_conn_slot_timing_instant_to_target_and_walk_linked_windows` (216B) via
`RenamePass52eqRegion80040000Fun8004fbc0.java` (`renamed=1`, live-verified).

Conn-slot timing instant re-arm helper on per-connection context `param_1`:
computes wraparound step from masked `+0x14` and advances `+0xc` clock toward
`param_2` target instant using global wrap masks (`DAT_8004fc98`/`fca0`/`fca4`).
When diagnostic gate `PTR_DAT_8004fc9c[1]` bit3 clear and timing gap exceeds
`step*10`, optional hook at `PTR_DAT_8004fca8` + diagnostic log, then bumps
target by +10. Sets global `DAT_8004fcb0 = 5`, commits new instant
`+0xc += aligned_target + (DAT_8004fcb4 & 0xf)`, then when masked `+0x18` exceeds
masked `+0x14` walks linked slot windows via `FUN_8004fa24` (clock-in-window
probe on singly-linked list at `PTR_DAT_8004fa60`) incrementing `+0xc` until
match fails. Callee of `le_channel_selection_algorithm_periodic_timing_check`
(`0x800553dc`, region `0x80050000`) for LE channel-selection re-arm path;
sibling of `dispatch_slot_timing_reprogram_if_feature_enabled_and_ready` cluster.

Post-rename: **122 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1516**.

**Next:** superseded by Pass 52er below.

## Pass 52er (2026-06-30) — >150B rank-3 LMP power/CLK_ADJ opcode param packer rename

**>150B rank-3 decompiled+renamed (HIGH):** `FUN_80044490` →
`pack_lmp_power_clk_adj_fallback_opcode_response_params` (202B) via
`RenamePass52erRegion80040000Fun80044490.java` (`renamed=1`, live-verified).

Feature-gated opcode-specific HCI response param packer reached from
`OGF1_3_extended_OCF_0x51_0x5b_fallback_handler` when
`config->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` bit2 is set (third handler
in the 4-function chain after `FUN_8001fb70`/`FUN_8001c9d4`). Returns `-1` when
opcode not handled; otherwise returns `payload_len - 4` for caller to add base
header offset. Opcode cases:
- `0x2022`: writes 2-byte `param_3` to output `+4/+5` (returns 2)
- `0xc6c`: reads feature-page byte+8 bits 1–2 into `+4/+5` (returns 2)
- `0xc6d`: no payload write (returns 0) — RX-side complement of neighbor
  `LMP_PDU_0xc6d_feature_page_bit_toggle` (`0x80044430`)
- `0x2024`: copies 4 bytes from BOS `+0x1f0..+0x1f3` (returns 4)
- `0x202f`: packs dual CLK_ADJ timing pairs from BOS `+0x1d0` bit0x40 gate
  (`0x1b/0x148` vs `0xfb/0x4290`) into 8 output bytes (returns 8)

Post-rename: **121 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1517**.

**Next:** superseded by Pass 52es below.

## Pass 52es (2026-06-30) — >150B rank-4 list-B quota-overflow walk rename

**>150B rank-4 decompiled+renamed (HIGH):** `FUN_8004b1d0` →
`walk_conn_list_b_slot_chain_collect_quota_overflow_records` (200B) via
`RenamePass52esRegion80040000Fun8004b1d0.java` (`renamed=1`, live-verified).

List-B quota-overflow slot-chain walker at `PTR_PTR_8004b298` — list-B twin of
Pass 52dl's `walk_conn_list_a_slot_chain_collect_overflow_records`. Clears
3-word output list (`head`/`tail`/`count`), walks saved head index `param_2`
until sentinel `10`: for each 0xc-stride table entry iterates 8-byte slot
records, increments per-record quota counter at `+0x104` by increment byte at
slot `+5`, and when counter reaches threshold at `+2` clears link bytes
`+0x100..+0x103` and appends record to output list; advances chain via
`FUN_8004b170` + next-index byte at entry `+0xb`. Sole caller
`atomically_take_conn_list_b_and_apply_quota_overflow` (`0x8004ca10`).

Post-rename: **120 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1518**.

**Next:** superseded by Pass 52et below.

## Pass 52et (2026-06-30) — >150B rank-5 SCO/eSCO timing globals programmer rename

**>150B rank-5 decompiled+renamed (HIGH):** `FUN_8004a908` →
`program_sco_esco_timing_hw_globals_from_slot_interval` (186B) via
`RenamePass52etRegion80040000Fun8004a908.java` (`renamed=1`, live-verified).

SCO/eSCO timing HW globals programmer — clamps slot-interval `param_2` to
`0x20a` (with logging when `param_1` flag set or value exceeds bound), then
programs seven global HW register pointers using `0x271` (625 µs slot)
packed quotient/remainder encoding `(q << 10) | r`; final register set to
`0x410`. Sole caller `init_or_reset_sco_esco_hw_registers_and_link_slots`
(`0x8004d294`) during full-reset path with packed timing from per-connection
struct.

Post-rename: **119 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1519**.

**Next:** superseded by Pass 52eu below.

## Pass 52eu (2026-06-30) — >150B rank-6 HW-reg pool remove HCI handler rename

**Cold-triage (refreshed, `ColdTriageRegion80040000Pass52eu.java`):** **23** unnamed
>150B remain. rank-1 `0x80045c70` (520B); rank-2 `0x80048754` (478B); rank-3
`0x80046e40` (474B); rank-4 `0x800451cc` (408B); rank-5 `0x80046798` (352B);
rank-6 `0x8004635c` (320B); rank-7 `0x80046ce8` (316B).

**>150B rank-6 decompiled+renamed (HIGH):** `FUN_8004996a` →
`hci_hw_reg_pool_entry_remove_gated_handler_send_cmd_complete` (172B, 0 xrefs in
cold-triage) via `RenamePass52euRegion80040000Fun8004996a.java` (`renamed=1`,
live-verified).

Re-entrancy-gated HCI command handler in the HW-reg pool entry cluster (sibling
of `hci_hw_reg_pool_entry_remove_handler_send_cmd_complete` at `0x8004993c`,
`FUN_80049a2c` add handler, and `FUN_80049b40` flush handler):

- **Reentry gate:** when `param_1 & 1`, requires
  `is_any_conn_lmp_procedure_busy_with_link_mode_mask_0x180()` to return 0.
- **State guards:** `0x1ac` struct array fields `field68_0x44`, `field407_0x1a4`,
  plus bytes at `PTR_PTR_80049a20`/`PTR_PTR_80049a24` — failure → HCI status
  `0x0c` (Command Disallowed).
- **Index `0xff`:** clears bit-6 of `PTR_PTR_80049a20[5]` (`&= 0xbf`).
- **Otherwise:** `remove_pool_entry_by_bdaddr_and_release_hw_or_bitmap_slot(0)`.
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status-idiom (same pattern as Pass 52bz sibling).

Post-rename: **118 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1520**.

**Next:** superseded by Pass 52ev below.

## Pass 52ev (2026-06-30) — >150B rank-1 LE adv data HCI handler rename

**Cold-triage (refreshed, from Pass 52eu):** **22** unnamed >150B remain.
rank-1 `0x80048754` (478B); rank-2 `0x80046e40` (474B); rank-3
`0x800451cc` (408B); rank-4 `0x80046798` (352B); rank-5 `0x8004635c`
(320B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80045c70` →
`hci_le_adv_data_pack_validate_commit_slot10_dispatch_meta_subevent_send_cmd_complete`
(520B, 0 xrefs in cold-triage) via
`RenamePass52evRegion80040000Fun80045c70.java` (`renamed=1`, live-verified).

520B HCI command handler in the LE Meta Event cluster (`0x80045c00` LTK
request sender neighbor, `0x80045e8c` Connection Complete sender neighbor):

- **Disable path:** when param byte at `+6` (`local_28`) == 0, clears bit-7 of
  conn-slot-10 `field249_0x100` (`&= 0x7f`).
- **Enable/validate path:** operation type at `+7` (`bVar1`) must be 0–2;
  `local_28` must be ≤ `0x14`. Type 0: subtype at `+8` must be 1 or 2; when
  subtype==1 requires `field328_0x155 & 0x14`; data length at `+9` must be
  2–75 with `length+7 == total_cmd_len`; packs payload bytes from `+10` with
  nibble interleave into 38-byte (`0x26`) buffer, each byte ≤
  `field329_0x156`; `optimized_memcpy` to `PTR_DAT_80045e7c`. Types 1/2: simpler
  encoding (`uVar8` 2 or 3).
- **Slot-10 commit:** writes `field256_0x107`, `field255_0x106`,
  `field250_0x101` low nibble, `field249_0x100` (sets bit-7 `0x80`, bits 4–6
  from subtype flag), `field257_0x108` (data length).
- **Meta dispatch:** calls `send_evt_Meta_subevent_0_or_1` with handle + three
  param bytes from cmd buffer offsets `+3`/`+2`/`+5`.
- **Failure:** status `0x11` (disallowed state) or `0x12` (invalid parameters).
- **Command Complete:** when validation fails (`local_28==0` after error),
  4-byte `hci_event_sender(0xe,…)` with `field_0x165` status idiom.

Post-rename: **117 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1521**.

**Next:** superseded by Pass 52ew below.

## Pass 52ew (2026-06-30) — >150B rank-1 LE adv data baseband-setup variant rename

**Cold-triage (refreshed, from Pass 52ev):** **21** unnamed >150B remain.
rank-1 `0x80046e40` (474B); rank-2 `0x800451cc` (408B); rank-3
`0x80046798` (352B); rank-4 `0x8004635c` (320B); rank-5 `0x80046a00`
(318B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80048754` →
`hci_le_adv_data_pack_validate_commit_slot10_program_baseband_setup_send_cmd_complete`
(478B, 0 xrefs in cold-triage) via
`RenamePass52ewRegion80040000Fun80048754.java` (`renamed=1`, live-verified).

478B HCI command handler — structural twin of Pass 52ev's
`hci_le_adv_data_pack_validate_commit_slot10_dispatch_meta_subevent_send_cmd_complete`
in the `0x800483xx` baseband link-setup neighborhood (`program_baseband_link_setup_slot10`
at `0x800483c0`, thin wrapper `invoke_baseband_link_setup_from_param_buffer` at
`0x80048948`):

- **Disable path:** when param byte at `+7` (`local_24`) == 0, clears bit-7 of
  conn-slot-10 `field249_0x100` (`&= 0x7f`).
- **Enable/validate path:** operation type at `+8` (`bVar1`) must be 0–2;
  `local_24` must be ≤ `0x14`. Type 0: subtype at `+8` must be 1 or 2; when
  subtype==1 requires `field328_0x155 & 0x14`; data length at `+9` must be
  2–75 with `length+7 == total_cmd_len`; packs payload bytes from `+10` with
  nibble interleave into 38-byte (`0x26`) buffer, each byte ≤
  `field329_0x156`; `optimized_memcpy` to `PTR_DAT_80048938`. Types 1/2: simpler
  encoding (`cVar11` 2 or 3).
- **Slot-10 commit:** writes `field256_0x107`, `field255_0x106`,
  `field250_0x101` low nibble, `field249_0x100` (sets bit-7 `0x80`, bits 4–6
  from subtype flag), `field257_0x108` (data length).
- **Success terminus:** calls
  `program_baseband_link_setup_slot10_and_send_hci_cmd_complete` (which itself
  emits HCI Command Complete) instead of Pass 52ev's
  `send_evt_Meta_subevent_0_or_1`.
- **Failure:** status `0x11` (disallowed state) or `0x12` (invalid parameters);
  logs via `possible_logging_function__var_args`, then 4-byte
  `hci_event_sender(0xe,…)` with `field_0x165` status idiom.

Post-rename: **116 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1522**.

**Next:** superseded by Pass 52ex below.

## Pass 52ex (2026-06-30) — >150B rank-1 LE adv/scan enable HCI handler rename

**Cold-triage (refreshed, from Pass 52ew):** **20** unnamed >150B remain.
rank-1 `0x800451cc` (408B); rank-2 `0x80046798` (352B); rank-3
`0x8004635c` (320B); rank-4 `0x80046a00` (318B); rank-5 `0x8004ab64`
(414B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80046e40` →
`hci_le_adv_scan_enable_feature_gate_commit_flush_send_cmd_complete`
(474B, 0 xrefs in cold-triage) via
`RenamePass52exRegion80040000Fun80046e40.java` (`renamed=1`, live-verified).

474B HCI command handler in the LE advertising/scan cluster (neighbor of
Pass 52ev/52ew adv-data handlers and
`process_link_feature_toggle_command_and_send_status_event`):

- **Re-entrancy gate:** per-control-record state byte at `+0x15dc`; returns
  `0x0c` (Command Disallowed) when busy and global flag bit `0x10` set.
- **Parameter validation:** two boolean bytes at HCI cmd `+2` (`local_18`) and
  `+3` (`bVar1`); both must be ≤1 else `0x12` (Invalid Parameters).
- **Feature permission:** when `bVar1 != 0`, calls
  `check_feature_permission_by_category_and_index(2, (field68_0x44 >> 6) & 1)`.
- **State commit:** updates `field68_0x44` bit 0 ← `bVar1`, bit 5 ←
  `local_18`; may set/clear `field69_0x45` bit `0x80`.
- **Link-state branch:** when active link mode byte `0x268 == 2` and
  `0x26a != 0`, enable path calls fn-ptr pair + copies timing fields at
  `0x15c8`/`0x15ca`; disable path calls alternate fn-ptr +
  `flush_pending_hw_writes_or_dispatch_mode1(0/1)`.
- **BDADDR scramble:** when config `field208_0xd8 & 0x80`, calls
  `set_channel_bdaddr_scramble_fields`.
- **Command Complete:** `hci_event_sender(0xe, &local_20, 4)` with
  `field_0x165` status idiom; diagnostic log tag `0x515`.

Post-rename: **115 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1523**.

**Next:** superseded by Pass 52ey below.

## Pass 52ey (2026-06-30) — >150B rank-1 LE scan-params HCI handler rename

**Cold-triage (refreshed, from Pass 52ex):** **19** unnamed >150B remain.
rank-1 `0x80046798` (352B); rank-2 `0x8004635c` (320B); rank-3
`0x80046a00` (318B); rank-4 `0x8004ab64` (414B); rank-5 `0x8004c2f0`
(414B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_800451cc` →
`hci_le_scan_params_reentry_gate_interval_window_commit_send_cmd_complete`
(408B, 0 xrefs in cold-triage) via
`RenamePass52eyRegion80040000Fun800451cc.java` (`renamed=1`, live-verified).

408B HCI command handler in the LE scan/advertising cluster (sibling of
Pass 52ex enable handler and Pass 52dv classic scan-activity validator):

- **Re-entrancy gate:** per-control-record state byte at `bos[0xb].field96_0x60`
  (0→1; returns `0x0c` when busy and global flag bit `0x10` set).
- **Enable guard:** only commits when `field68_0x44` bit 0 clear (scan/adv
  not currently enabled).
- **Parameter validation:** scan interval at cmd `+4` and window at cmd `+6`
  must satisfy `4 < value < 0x4001` and window ≤ interval; type byte at
  `+8` and filter-policy byte at `+9` must be `< 4`; own-address-type
  byte at `+3` must be `< 2`.
- **Interval fudge:** when `interval − window < (config field216_0xe3 >> 5) * 0xc`,
  bumps interval by that margin before commit.
- **State commit:** packs type/filter/own-addr bits into `field68_0x44`,
  stores interval to `field70_0x46`/`field71_0x47`, window to
  `field72_0x48`/`field73_0x49`, merges low 3 bits from `PTR_DAT_80045370`
  into `field68`/`field69`.
- **Command Complete:** `hci_event_sender(0xe, &local_18, 4)` with
  `field_0x165` status idiom; diagnostic log tag `0x446`.

Post-rename: **114 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1524**.

**Next:** superseded by Pass 52ez below.

## Pass 52ez (2026-06-30) — >150B rank-1 LE ext-adv data HCI handler rename

**Cold-triage (refreshed, from Pass 52ey):** **18** unnamed >150B remain.
rank-1 `0x8004635c` (320B); rank-2 `0x80046a00` (318B); rank-3
`0x8004ab64` (414B); rank-4 `0x8004c2f0` (414B); rank-5 `0x80046ce8`
(316B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80046798` →
`hci_le_ext_adv_data_pack_validate_commit_apply_vsc_0xfc97_send_cmd_complete`
(352B, 0 xrefs in cold-triage) via
`RenamePass52ezRegion80040000Fun80046798.java` (`renamed=1`, live-verified).

352B HCI command handler in the LE extended-advertising cluster (sibling of
Pass 52ev/52ew adv-data handlers and Pass 52ec's
`gate_ext_adv_param_bitfields_and_apply_via_vsc_0xfc97`):

- **Feature gate:** requires `bos[0xb].field333_0x15a & 2`; failure →
  `0x11` (Command Disallowed).
- **Handle validation:** HCI cmd handle at `+3` must be `≤ 0xfff`; lookup via
  `query_config_struct_0x1ac_by_index()`; record must have bit 0 of byte `+3`
  set else status `0x02`.
- **Pending-state guard:** bitmask at conn-record `+0x14c` must include
  pending flag `2`; mismatch → `0x0c` (Command Disallowed).
- **Payload validation:** operation byte at `+5` (`local_20`); when `== 1`
  requires `field328_0x155 & 0x14`; data length at `+6` must be 2–75 with
  `length + 4 == total_cmd_len`; each payload byte at `+7..` must be
  `≤ field329_0x156`.
- **Nibble-pack + commit:** packs payload into 38-byte (`0x26`) buffer,
  `optimized_memcpy` to conn-record `+0x154`; stores operation byte to
  `+0x150`, length to `+0x152`.
- **VSC apply:** reads link index from record `+2`, calls
  `VSC_0xfc97_Set_Extended_Advertising_Parameters_variant_1(link, 0xf)` then
  `..._variant_2(link, 0xf, (field150>>1&3)<<10 | prior_read)`; on success
  sets bit 0 of `+0x14c`.
- **Command Complete:** `hci_event_sender(0xe, &local_50, 4)` with
  `field_0x165` status idiom.

Post-rename: **113 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1525**.

**Next:** superseded by Pass 52fa below.

## Pass 52fa (2026-06-30) — >150B rank-1 LE ext scan-rsp data HCI handler rename

**Cold-triage (refreshed, from Pass 52ez):** **17** unnamed >150B remain.
rank-1 `0x80046a00` (318B); rank-2 `0x8004ab64` (414B); rank-3
`0x8004c2f0` (414B); rank-4 `0x80046ce8` (316B); rank-5 `0x8004c844`
(310B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004635c` →
`hci_le_ext_scan_rsp_data_pack_validate_commit_send_cmd_complete`
(320B, 0 xrefs in cold-triage) via
`RenamePass52faRegion80040000Fun8004635c.java` (`renamed=1`, live-verified).

320B HCI command handler in the LE extended-advertising cluster (sibling of
Pass 52ez's `hci_le_ext_adv_data_pack_validate_commit_apply_vsc_0xfc97_send_cmd_complete`
and Pass 52ev/52ew adv-data handlers):

- **Feature gate:** requires `bos[0xb].field333_0x15a & 4`; failure →
  `0x11` (Command Disallowed).
- **Handle validation:** HCI cmd handle at `+3` must be `< 0x1000`; lookup via
  `query_config_struct_0x1ac_by_index()`; record must have bit 0 of byte `+3`
  set else status `0x02`.
- **Pending-state guard:** bitmask at conn-record `+0x14c` must not include
  pending flag `8`; when set → `0x0c` (Command Disallowed).
- **Operation-byte validation:** byte at `+5` — when bit 1 (`0x02`) set
  requires `field328_0x155 & 10`; when bits 1+2 (`0x06`) set requires
  `field333_0x15a & 0x20`; mismatch → `0x11`.
- **Payload validation:** data length at `+6` must be 2–74 with
  `length + 4 == total_cmd_len`; each payload byte at `+7..` must be
  `≤ field329_0x156`.
- **Nibble-pack + commit:** packs payload into 38-byte (`0x26`) buffer,
  `optimized_memcpy` to conn-record `+0x17a`; stores operation byte to
  `+0x151`, length to `+0x153`.
- **Pending flag:** sets bit 2 (`0x04`) of conn-record `+0x14c` on success.
- **Command Complete:** `hci_event_sender(0xe, &local_48, 4)` with
  `field_0x165` status idiom (no VSC apply — commit-only variant).

Post-rename: **112 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1526**.

**Next:** superseded by Pass 52fb below.

## Pass 52fb (2026-06-30) — >150B rank-1 LE periodic adv interval HCI handler rename

**Stale-target note:** prior rank list listed `0x80046a00` (318B) as rank-1;
live Ghidra shows that address is mid-instruction inside already-named
`validate_and_stage_sco_packet_type_table_from_hci_params` (`0x80046900`, 682B,
Pass 52ds) — not a function entry. Fresh `ColdTriageRegion80040000Pass52eu.java`
run: **16** unnamed >150B remain; rank-1 `0x80046ce8` (316B); rank-2
`0x80049a2c` (254B); rank-3 `0x80049c10` (248B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80046ce8` →
`hci_le_periodic_adv_interval_params_validate_commit_send_cmd_complete`
(316B, 0 xrefs in cold-triage) via
`RenamePass52fbRegion80040000Fun80046ce8.java` (`renamed=1`, live-verified).

316B HCI command handler in the LE extended-advertising cluster (sibling of
Pass 52fa scan-rsp data and Pass 52ez ext-adv data handlers):

- **Init gate:** when bos[0xb].field96_0x60 is zero, sets to `2`; when
  non-zero requires `PTR_DAT_80046e28` bit `0x10` clear or mode already `2`.
- **Param validation:** operation byte at `+3` must be `≤2`; mode byte at `+4`
  must be `≤2`; when mode==2 both interval words must be non-zero and satisfy
  `max_interval << 7 > min_interval`.
- **Feature gate:** when operation byte non-zero, requires
  `check_feature_permission_by_category_and_index(2, scan_active_link_mask())`.
- **Commit:** stores mode low-2-bits to global struct byte `+5`; min interval
  `*10` to `+0xc`; max interval `*0x500` to `+0x10`.
- **Apply path:** disable (`operation==0`) invokes runtime hook with `0` +
  `flush_pending_hw_writes_or_dispatch_mode1` for modes 0/1; enable invokes
  hook with `1` and copies bos timing fields `field76/78` → `field77/79`.
- **Command Complete:** `hci_event_sender(0xe, &local_20, 4)` with
  `field_0x165` status idiom; status `0x0c`/`0x12` on validation failure.

Post-rename: **111 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1527**.

**Next:** superseded by Pass 52fc below.

## Pass 52fc (2026-06-30) — >150B rank-2 HW-reg pool entry add HCI handler rename

**>150B rank-2 decompiled+renamed (HIGH):** `FUN_80049a2c` →
`hci_hw_reg_pool_entry_add_gated_handler_send_cmd_complete`
(254B, 0 xrefs in cold-triage) via
`RenamePass52fcRegion80040000Fun80049a2c.java` (`renamed=1`, live-verified).

254B HCI command handler in the HW-reg pool entry cluster (sibling of
`hci_hw_reg_pool_entry_remove_gated_handler_send_cmd_complete` at `0x8004996a`,
`hci_hw_reg_pool_entry_remove_handler_send_cmd_complete` at `0x8004993c`, and
`FUN_80049b40` flush handler):

- **Param validation:** pool-index byte at `+3` must be in range `2..0xfe`
  (else HCI status `0x12` Invalid HCI Command Parameters).
- **State guards:** conn-array `field40_0x28`/`field68_0x44`/`field407_0x1a4`
  plus bytes at `PTR_PTR_80049b34`/`PTR_PTR_80049b38` — failure → status
  `0x0c` (Command Disallowed); when `PTR_DAT_80049b30` bit0 set, also requires
  `is_any_conn_lmp_procedure_busy_with_link_mode_mask_0x180()` to return 0.
- **Index `0xff`:** sets bit-6 (`0x40`) of `PTR_PTR_80049b34[5]` — inverse
  of remove handler's bit-6 clear.
- **Otherwise:** `find_link_record_by_bdaddr_and_flag(0, index, bdaddr)`; when
  index `>0x1f`, `conn_slot_alloc_and_commit_dispatch(0, index, bdaddr, 0, 0)`
  with status `7` on alloc failure (`0x20`).
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom (same pattern as Pass 52eu remove sibling).

Post-rename: **110 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1528**.

**Next:** superseded by Pass 52fd below.

## Pass 52fd (2026-06-30) — >150B rank-3 HW-reg pool commit HCI handler rename

**>150B rank-3 decompiled+renamed (HIGH):** `FUN_80049c10` →
`hci_hw_reg_pool_commit_gated_handler_send_cmd_complete_and_le_conn_complete`
(248B, 0 xrefs in cold-triage) via
`RenamePass52fdRegion80040000Fun80049c10.java` (`renamed=1`, live-verified).

248B HCI command handler in the HW-reg pool entry cluster (sibling of
`hci_hw_reg_pool_entry_add_gated_handler_send_cmd_complete` at `0x80049a2c`,
remove handlers at `0x8004993c`/`0x8004996a`, and `FUN_80049b40` flush handler):

- **Idle path (`PTR_PTR_80049d08[4]==0`):** requires conn-array
  `field407_0x1a4` bit0 set (else status `0x0c` Command Disallowed); calls
  `check_state_ready_and_invoke_or_busy()` — when return value is `4`, logs via
  `possible_logger_called_if_no_patch3` and aborts with internal status `0x1f`
  without Command Complete; otherwise clears bit0 of `field407_0x1a4`.
- **Active path (`PTR_PTR_80049d08[4]!=0`):** invokes fn-ptr at
  `PTR_DAT_80049d0c` with arg `0` (flush/apply pending pool state); sets
  conn-type indicator `iVar10=1`.
- **Command Complete:** 4-byte `hci_event_sender(0xe,…)` with opcode byte
  `0x20` and `field_0x165` status idiom.
- **Success follow-up:** when commit succeeded, emits LE Meta subevent via
  `send_evt_Meta_subevent_0x01_or_0x0a_HCI_LE_Connection_Complete_or_HCI_LE_Enhanced_Connection_Complete`
  (type `2`) and clears one bit of conn-array `field453_0x1d2`/`field454_0x1d3`
  bitmask indexed by `field440_0x1c5` or `PTR_PTR_80049d08[0x11]`.

Post-rename: **109 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1529**.

**Next:** superseded by Pass 52fe below.

## Pass 52fe (2026-06-30) — >150B rank-4 eSCO timing-pair HCI handler rename

**>150B rank-4 decompiled+renamed (HIGH):** `FUN_80049158` →
`hci_validate_and_commit_esco_timing_pair_hook1_or_pending_masks`
(222B, 0 xrefs in cold-triage) via
`RenamePass52feRegion80040000Fun80049158.java` (`renamed=1`, live-verified).

222B HCI command handler in the `0x800491xx`/`0x800494xx` SCO/eSCO handler
cluster (sibling of `validate_conn_and_start_lmp_power_clk_adj_if_enabled` at
`0x800494b0` and `validate_conn_copy_sync_params_and_alloc_tag3_dispatch` at
`0x80049420`). Terminates via
`OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10` with
opcode `0x2022`:

- **Conn lookup:** `query_config_struct_0x1ac_by_index(handle@+3)` — failure
  returns `2`.
- **State gate:** requires conn `+6` bit0 set (else status `0x1a`).
- **Validation:** `validate_value_pair_within_threshold_range(pair1@+5,
  pair2@+7, handle)` — failure returns `0x12` (Invalid HCI Command Parameters).
- **Pending-mask path:** when `+0x7c` or `+0x78` nonzero, commits pair to
  `+0x10a`/`+0x10c` and sets `+0x90` bit4 (defer while pending procedures
  active).
- **Active path:** when stored `+0xf0`/`+0xf8` differ from cmd pair, commits
  and dispatches hook at `PTR_DAT_80049238(rec, 1)`.

Post-rename: **108 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1530**.

**Next:** superseded by Pass 52ff below.

## Pass 52ff (2026-06-30) — >150B rank-5 AFH poll read link RX timing HCI handler rename

**>150B rank-5 decompiled+renamed (HIGH):** `FUN_80048d6c` →
`hci_afh_poll_read_link_rx_timing_triple_send_cmd_complete`
(204B, 0 xrefs in cold-triage) via
`RenamePass52ffRegion80040000Fun80048d6c.java` (`renamed=1`, live-verified).

204B HCI command handler in the `0x80048dxx` AFH/link-timing cluster (sibling of
`FUN_80048fb8` link-slot alloc/busy handler at `0x80048fb8` and
`dispatch_lmp_25c_25b_and_optional_vsc_fc95_lmp_268_recovery` at `0x80048b80`):

- **AFH gate:** requires `field327_0x154` bit2 set (else status `0x1a`); index
  byte at `+3` must be `<2` (else `0x12`).
- **Poll path:** calls `afh_channel_quality_poll_commit()`; when
  `field40_0x28` bit0 set and poll result `<` threshold `PTR_DAT_80048e3c`,
  stores quality nibble in `field42_0x2a` bits 5:2.
- **Link lookup:** `find_link_record_by_bdaddr_and_flag(2, index, bdaddr@+2)`;
  reads RX timing triple at sub-record `+0x28`/`+0x2a`/`+0x2c` — all-zero →
  status `2` (Unknown Connection Identifier).
- **Terminus:** 10-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  status + echoed opcode + timing dword/u16 on success.

Post-rename: **107 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1531**.

**Next:** superseded by Pass 52fg below.

## Pass 52fg (2026-06-30) — >150B rank-6 HW-reg pool flush HCI handler rename

**>150B rank-6 decompiled+renamed (HIGH):** `FUN_80049b40` →
`hci_hw_reg_pool_flush_gated_handler_send_cmd_complete`
(186B, 0 xrefs in cold-triage) via
`RenamePass52fgRegion80040000Fun80049b40.java` (`renamed=1`, live-verified).

186B HCI command handler in the `0x800499xx`/`0x80049cxx` HW-reg pool cluster
(sibling of add/remove/commit handlers at `0x8004993c`/`0x8004996a`/`0x80049a2c`/
`0x80049c10`):

- **State gates:** conn-array fields at `_FUN_80049bfc+0x28`/`+0x44`,
  `PTR_DAT_80049c00` bit0, `PTR_PTR_80049c04`/`PTR_PTR_80049c08` pool-state
  bytes, and `field407_0x1a4` bitmask `0x21` — any failure → status `0x0c`.
- **LMP-busy probe:** `is_any_conn_lmp_procedure_busy_with_link_mode_mask_0x180()`
  when `PTR_DAT_80049c00` bit0 set.
- **Flush path:** on success calls `flush_hw_pending_slots_by_connection_type(0)`
  and clears bit-6 of `PTR_PTR_80049c04[5]`.
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.

Post-rename: **106 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1532**.

**Next:** superseded by Pass 52fh below.

## Pass 52fh (2026-06-30) — >150B rank-7 bitmap-pool entry remove HCI handler rename

**>150B rank-7 decompiled+renamed (HIGH):** `FUN_80048ef8` →
`hci_bitmap_pool_entry_remove_gated_handler_send_cmd_complete`
(172B, 0 xrefs in cold-triage) via
`RenamePass52fhRegion80040000Fun80048ef8.java` (`renamed=1`, live-verified).

172B HCI command handler in the `0x80048exx`/`0x80048fxx` bitmap-pool cluster
(sibling of HW-reg pool handlers at `0x800499xx`, but targets pool type 2 per
`remove_pool_entry_by_bdaddr_and_release_hw_or_bitmap_slot`):

- **Re-entrancy gate:** bit-2 of pool state at `_FUN_80048fa4+0x14dc` set →
  status `0x12`.
- **Index validation:** pool-index byte at `param+3` must be `< 2`.
- **State gates:** conn-array fields at `_FUN_80048fa4+0x28`/`+0x44`/`+0x1a4`,
  `PTR_DAT_80048fa8` bit0, `PTR_PTR_80048fac`/`PTR_PTR_80048fb0` pool-state
  bytes — any failure → status `0x0c`.
- **Remove path:** on success calls
  `remove_pool_entry_by_bdaddr_and_release_hw_or_bitmap_slot(2, index, bdaddr)`
  then `noop_handler_stub()`.
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.

Post-rename: **105 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1533**.

**Next:** superseded by Pass 52fi below.

## Pass 52fi (2026-06-30) — >150B rank-1 link-slot alloc/commit HCI handler rename

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80048fb8` →
`hci_link_slot_alloc_and_commit_gated_handler_send_cmd_complete`
(240B, 0 xrefs in cold-triage) via
`RenamePass52fiRegion80040000Fun80048fb8.java` (`renamed=1`, live-verified).

240B HCI command handler in the `0x80048fxx`/`0x800490xx` link-slot cluster
(sibling of `hci_afh_poll_read_link_rx_timing_triple_send_cmd_complete` at
`0x80048d6c` and bitmap-pool handlers at `0x80048exx`):

- **AFH gate:** `field327_0x154` bit2 set (else status `0x1a`).
- **Index validation:** pool-index byte at `param+3` must be `<2`; when
  index==1, byte at `param+9` bits 6:7 must be `0xc0` (else `0x12`).
- **State gates:** when `field327_0x154` bit1 set, conn-array fields at
  `+0x28`/`+0x44`/`+0x1a4`, `PTR_DAT_800490ac` bit0, and
  `PTR_PTR_800490b0`/`PTR_PTR_800490b4` pool-state bytes — any failure →
  status `0x0c`.
- **Lookup path:** `find_link_record_by_bdaddr_and_flag(2, index, bdaddr@+2)`.
- **Alloc path:** when slot index `>=` threshold at `PTR_DAT_800490b8`, calls
  `conn_slot_alloc_and_commit_dispatch(2, index, bdaddr, keys@+0x1a,
  keys@+0xa)` — alloc failure → status `7`.
- **Success path:** status `0` + `noop_handler_stub()`.
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.

Post-rename: **104 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1534**.

**Next:** superseded by Pass 52fj below.

## Pass 52fj (2026-06-30) — >150B rank-1 eSCO packet-type broadcast HCI handler rename

**Cold-triage (refreshed):** 9 unnamed >150B remain. rank-1 `0x80047fb4`
(238B, 0 xrefs).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80047fb4` →
`hci_esco_packet_type_broadcast_validate_and_commit_send_cmd_complete`
(238B, 0 xrefs) via
`RenamePass52fjRegion80040000Fun80047fb4.java` (`renamed=1`, live-verified).

238B HCI command handler in the `0x80047fxx`/`0x800480xx` SCO/eSCO cluster
(sibling of `validate_and_stage_sco_air_mode_change_from_hci_command` at
`0x800480b0` and `validate_and_stage_sco_packet_type_table_from_hci_params` at
`0x80046900`):

- **Param extract:** three bytes from HCI command buffer at `param+2/+3/+5`.
- **Validate:** `select_override_or_default_byte_pair_and_log` then
  `validate_esco_packet_type_params_with_hook` — reject → status `0x12`.
- **Global commit:** on success writes the three bytes to master struct at
  `_FUN_800480a4` (`+0x1475..+0x1477`).
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.
- **Broadcast path** (success only): walks 11-slot active-link bitmask at
  `+0x1d2` on master struct; for each set bit updates the corresponding
  `0x1ac`-stride config record at `+0x115..+0x117` (raw bytes) and
  `+0x11a/+0x11b` (validated pair).

Post-rename: **103 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1535**.

**Next:** superseded by Pass 52fk below.

## Pass 52fk (2026-06-30) — >150B rank-1 SCO/eSCO link-reg pair snapshot HCI handler rename

**Cold-triage (refreshed):** 8 unnamed >150B remain. rank-1 `0x8004653c`
(224B, 0 xrefs).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004653c` →
`hci_read_sco_esco_link_reg_pair_snapshot_send_cmd_complete`
(224B, 0 xrefs) via
`RenamePass52fkRegion80040000Fun8004653c.java` (`renamed=1`, live-verified).

224B HCI command handler in the `0x800465xx` LE/link-timing cluster (sibling
of Pass 52fa scan-rsp data handler at `0x8004635c` and Pass 52ez ext-adv data
handler at `0x80046798`):

- **Handle extract:** ushort at `param+3`; reject `≥0x1000` → status `0x12`.
- **Conn lookup:** `query_config_struct_0x1ac_by_index(handle)`; null →
  status `2` (Unknown Connection Identifier).
- **HW read path:** branch on conn-record `+3` bit4 (SCO vs eSCO): SCO uses
  `read_indexed_link_register((slot-8)*0x1e+3/+4)`; eSCO uses
  `read_indexed_esco_link_register(slot*0x14+3/+4)`; masks second register
  to `0x1f`.
- **Commit:** copies 5-byte pair snapshot to conn-record `+0x54`.
- **Terminus:** 11-byte HCI Command Complete via `hci_event_sender(0xe,…)`
  with `field_0x165` status idiom + echoed handle + 5-byte snapshot on
  success.

Post-rename: **102 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1536**.

**Next:** superseded by Pass 52fl below.

## Pass 52fl (2026-06-30) — >150B rank-1 AFH-gated link-slot flag bitmap commit HCI handler rename

**Cold-triage (refreshed):** 7 unnamed >150B remain. rank-1 `0x800489e8`
(206B, 0 xrefs).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_800489e8` →
`hci_afh_gated_link_slot_flag_bitmap_commit_send_cmd_complete`
(206B, 0 xrefs) via
`RenamePass52flRegion80040000Fun800489e8.java` (`renamed=1`, live-verified).

206B HCI command handler in the `0x800489xx`/`0x80048axx` AFH/link-slot cluster
(sibling of `hci_afh_poll_read_link_rx_timing_triple_send_cmd_complete` at
`0x80048d6c` and `hci_link_slot_alloc_and_commit_gated_handler_send_cmd_complete`
at `0x80048fb8`):

- **AFH gate:** `field327_0x154` bit2 set (else status `0x1a`).
- **Index validation:** pool-index byte at `param+3` must be `<2`.
- **Lookup path:** `find_link_record_by_bdaddr_and_flag(2, index, bdaddr@+2)`;
  slot index must be `<` bound at `PTR_DAT_80048abc` (else status `0x0c`).
- **Flag commit:** byte at `param+7` bit0 drives per-slot bit in global
  bitmask at `DAT_80048ac0` and bit1 of conn sub-record `+0x26` at
  `field319_0x14c + slot*0x34`.
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.

Post-rename: **101 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1537**.

**Next:** superseded by Pass 52fm below.

## Pass 52fm (2026-06-30) — >150B rank-1 AFH-gated conn10 HW-ctrl + LMP recovery HCI handler rename

**Cold-triage (refreshed):** 6 unnamed >150B remain. rank-1 `0x80048c0c`
(200B, 0 xrefs).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80048c0c` →
`hci_afh_gated_conn10_hw_ctrl_and_lmp_recovery_send_cmd_complete`
(200B, 0 xrefs) via
`RenamePass52fmRegion80040000Fun80048c0c.java` (`renamed=1`, live-verified).

200B HCI command handler in the `0x80048cxx` AFH/LMP-recovery cluster (sibling of
`hci_afh_poll_read_link_rx_timing_triple_send_cmd_complete` at `0x80048d6c` and
`dispatch_lmp_25c_25b_and_optional_vsc_fc95_lmp_268_recovery` at `0x80048b80`):

- **AFH gate:** `field327_0x154` bit2 set (else status `0x1a`).
- **State gates:** conn-array fields at `+0x28`/`+0x44`/`+0x1a4`,
  `PTR_DAT_80048cd8` bit0, `PTR_PTR_80048cdc`/`PTR_PTR_80048ce0` pool-state
  bytes — any failure → status `0x0c`.
- **Enable path:** byte at `param+3` drives enable flag; calls
  `set_hw_ctrl_bits_and_update_conn10_0x154(enable)`; updates timing word at
  master struct `+0xe` and related globals at `DAT_80048ce4`/`DAT_80048ce8`/
  `DAT_80048cec`; then
  `dispatch_lmp_25c_25b_and_optional_vsc_fc95_lmp_268_recovery(enable)`.
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.

Post-rename: **100 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1538**.

**Next:** superseded by Pass 52fn below.

## Pass 52fn (2026-06-30) — >150B rank-1 HCI sync-conn param dispatch wrapper rename

**Cold-triage (refreshed):** 5 unnamed >150B remain. rank-1 `0x80047b10`
(182B, 0 xrefs).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80047b10` →
`hci_sync_conn_param_commit_or_le_channel_scan_send_cmd_complete`
(182B, 0 xrefs) via
`RenamePass52fnRegion80040000Fun80047b10.java` (`renamed=1`, live-verified).

182B HCI command handler in the `0x80047bxx` SCO/eSCO sync-connection cluster
(outer wrapper for `validate_unique_handles_and_commit_sync_conn_params` at
`0x80047980`, Pass 52ee):

- **State init:** when master struct byte at `+0x15dc` is zero, sets it to `2`;
  else gates on `PTR_DAT_80047bcc` bit4 and state `0x02` (else status `0x0c`).
- **Zero-param path:** when bytes at `param+2`/`param+3` are both zero, calls
  `scan_indexed_link_slots_le_channel_select_and_lmp_vsc_dispatch()`.
- **Commit path:** else calls
  `validate_unique_handles_and_commit_sync_conn_params(cVar2, param+5)` with
  index bound at `PTR_DAT_80047bd0`; on success probes
  `is_any_conn_lmp_procedure_busy()` and sets/clears bit0 of
  `PTR_DAT_80047bd4`, copying timing words into master struct on busy.
- **Terminus:** `conn_diagnostic_batch_dump()` then 4-byte HCI Command Complete
  via `hci_event_sender(0xe,…)` with `field_0x165` status idiom.

SCO/eSCO connection-setup cluster sibling of
`HCI_Setup_Synchronous_Connection_handler` /
`HCI_Accept_Synchronous_Connection_Request_handler`.

Post-rename: **99 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1539**.

**Next:** superseded by Pass 52fo below.

## Pass 52fo (2026-06-30) — >150B rank-1 AFH-gated link-reg 0x2e timing triple HCI handler rename

**Cold-triage (refreshed):** 4 unnamed >150B remain. rank-1 `0x80048ac8`
(170B, 0 xrefs).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80048ac8` →
`hci_afh_gated_read_link_reg_0x2e_timing_triple_send_cmd_complete`
(170B, 0 xrefs) via
`RenamePass52foRegion80040000Fun80048ac8.java` (`renamed=1`, live-verified).

170B HCI command handler in the `0x80048axx` AFH/link-slot cluster (sibling of
`hci_afh_gated_link_slot_flag_bitmap_commit_send_cmd_complete` at `0x800489e8`
and `hci_afh_poll_read_link_rx_timing_triple_send_cmd_complete` at
`0x80048d6c`):

- **AFH gate:** `field327_0x154` bit2 set (else status `0x1a`).
- **Index validation:** pool-index byte at `param+3` must be `<2` (else status
  `0x12`).
- **Lookup path:** `find_link_record_by_bdaddr_and_flag(2, index, bdaddr@+2)`;
  when sub-record has nonzero timing words at `+0x2e`/`+0x30`/`+0x32`, copies
  dword at `+0x2e` and word at `+0x32` into response (status `0`); else status
  `2`.
- **Terminus:** 10-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.

Post-rename: **98 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1540**.

**Next:** superseded by Pass 52fp below.

## Pass 52fp (2026-06-30) — >150B rank-1 LE ext-adv enable bit3 commit HCI handler rename

**Cold-triage (refreshed):** 3 unnamed >150B remain. rank-1 `0x800462b0`
(164B, 0 xrefs); rank-2 `0x80048e44` (158B); rank-3 `0x8004923c` (156B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_800462b0` →
`hci_le_ext_adv_enable_bit3_commit_send_cmd_complete`
(164B, 0 xrefs) via
`RenamePass52fpRegion80040000Fun800462b0.java` (`renamed=1`, live-verified).

164B HCI command handler in the LE extended-advertising cluster (sibling of
Pass 52fa's `hci_le_ext_scan_rsp_data_pack_validate_commit_send_cmd_complete`
at `0x8004635c` and Pass 52ez ext-adv data handlers):

- **Handle validation:** HCI cmd handle at `+3` must be `< 0x1000`; failure →
  `0x12`.
- **Feature gate:** requires `bos[0xb].field333_0x15a & 4`; failure →
  `0x11` (Command Disallowed).
- **Conn lookup:** `query_config_struct_0x1ac_by_index()` using byte at `+5`;
  record must have bit 0 of byte `+3` set else status `0x02`.
- **State guards:** conn-record `+0x14c` bit2 must be set and `+0x122` bit2
  must be clear; mismatch → `0x0c` (Command Disallowed).
- **Enable commit:** sets/clears bit3 of conn-record `+0x14c` from byte at
  `+5` bit0.
- **Command Complete:** `hci_event_sender(0xe, &local_20, 4)` with
  `field_0x165` status idiom.

Post-rename: **97 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1541**.

**Next:** superseded by Pass 52fq below.

## Pass 52fq (2026-06-30) — >150B rank-1 bitmap-pool flush HCI handler rename

**Cold-triage (refreshed):** 2 unnamed >150B remain. rank-1 `0x80048e44`
(158B, 0 xrefs); rank-2 `0x8004923c` (156B).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_80048e44` →
`hci_bitmap_pool_flush_gated_handler_send_cmd_complete`
(158B, 0 xrefs) via
`RenamePass52fqRegion80040000Fun80048e44.java` (`renamed=1`, live-verified).

158B HCI command handler in the `0x80048exx` bitmap-pool cluster (sibling of
`hci_bitmap_pool_entry_remove_gated_handler_send_cmd_complete` at `0x80048ef8`
and parallel to `hci_hw_reg_pool_flush_gated_handler_send_cmd_complete` at
`0x80049b40`, but targets connection type 2):

- **AFH gate:** `field327_0x154` bit2 set (else status `0x1a`).
- **State gates:** when `field327_0x154` bit1 set, conn-array fields at
  `+0x28`/`+0x44`/`+0x1a4`, `PTR_DAT_80048ee8` bit0, and
  `PTR_PTR_80048eec`/`PTR_PTR_80048ef0` pool-state bytes — any failure →
  status `0x0c`.
- **Flush path:** on success calls `flush_hw_pending_slots_by_connection_type(2)`
  then `noop_handler_stub()`.
- **Terminus:** 4-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.

Post-rename: **96 unnamed** in-region (95 in 1-150B tier unchanged);
live named **1542**.

**Next:** superseded by Pass 52fr below.

## Pass 52fr (2026-06-30) — >150B rank-1 conn pending-flag param6 commit HCI handler rename

**Cold-triage (refreshed):** 1 unnamed >150B remain. rank-1 `0x8004923c`
(156B, 0 xrefs).

**>150B rank-1 decompiled+renamed (HIGH):** `FUN_8004923c` →
`hci_conn_pending_ee_flag_param6_commit_or_reject_send_cmd_complete`
(156B, 0 xrefs) via
`RenamePass52frRegion80040000Fun8004923c.java` (`renamed=1`, live-verified).

156B HCI command handler in the `0x800491xx`/`0x800492xx` SCO/eSCO cluster
(sibling of `hci_validate_and_commit_esco_timing_pair_hook1_or_pending_masks`
at `0x80049158` and `hci_hw_crypto_dual_block_digest_handler_send_cmd_complete`
at `0x800492d8`):

- **Conn lookup:** `query_config_struct_0x1ac_by_index(handle@+3)` — handle
  `>=0x1000` → status `0x12`; lookup failure → `2`.
- **Pending gate:** requires byte at `+0xee == 1` (else status unchanged from
  lookup).
- **Commit path:** when substate `+0xef == 4`, calls
  `set_feature_mask_gated_conn_param_0x91_with_hw_hook_notify(handle, 6, 0)`;
  else `report_procedure_outcome_and_update_param_type_bitmask(rec, 3, 6, 2)`.
- **Cleanup:** clears `+0xee` pending byte; success → status `0`.
- **Terminus:** 6-byte HCI Command Complete via `hci_event_sender(0xe,…)` with
  `field_0x165` status idiom.

Post-rename: **0 unnamed >150B** (tier exhausted); **95 unnamed** in-region
(95 in 1-150B tier unchanged); live named **1543**.

**Next:** superseded by Pass 52fs below.

## Pass 52fs (2026-06-30) — 1-150B rank-1 artifact triage + rank-2 inquiry teardown rename

**Cold-triage (refreshed):** 95 unnamed 1-150B remain. rank-1 `0x8004a2e4`
(10 xrefs, 1B — artifact, unchanged from Pass 52h); rank-2 `0x800408ec`
(6 xrefs, 26B); rank-3 `0x80045b94` (6 xrefs, 1B — likely artifact).

**Rank-1 triaged (non-function artifact):** `0x8004a2e4` — decompile returns
`halt_baddata()`; 1-byte mis-disassembly artifact. Substantive work skips to
rank-2.

**1-150B rank-2 decompiled+renamed (HIGH):** `FUN_800408ec` →
`dispatch_inquiry_lifecycle_teardown_fptr_0x8e_and_finalize`
(26B, 6 xrefs) via
`RenamePass52fsRegion80040000Fun800408ec.java` (`renamed=1`, live-verified).

```c
void dispatch_inquiry_lifecycle_teardown_fptr_0x8e_and_finalize(void)
{
  (*(code *)*PTR_DAT_80040908)(0, 0x8e);
  FUN_800408cc();   // dispatches fptr 0x81 chain
}
```

Shared inquiry-lifecycle teardown helper invoked from both inquiry-entry and
inquiry-exit HCI paths — e.g. `fHCI_inquiry_cancel` (`0x80036d44`),
`fHCI_Exit_Periodic_Inquiry_Mode_0x04` (`0x80041c18`), and
`configure_periodic_inquiry_lap_delays_baseband_and_arm_lmp` (Pass 52eg).
Dispatches the registered teardown hook at `PTR_DAT_80040908` with opcode
`0x8e`, then chains into `FUN_800408cc` (fptr `0x81` + further finalize).

Post-rename: **94 unnamed** in-region (94 in 1-150B tier); live named **1544**.

**Next:** superseded by Pass 52ft below.

## Pass 52ft (2026-06-30) — 1-150B rank-1–3 artifact triage + rank-4 subopcode descriptor init rename

**Cold-triage (refreshed):** 94 unnamed 1-150B remain. rank-1 `0x8004a2e4`
(10 xrefs, 1B — artifact); rank-2 `0x80045b94` (6 xrefs, 1B — artifact);
rank-3 `0x80048934` (5 xrefs, 1B — artifact); rank-4 `0x800425e0`
(4 xrefs, 76B); rank-5 `0x80043038` (4 xrefs, 50B).

**Rank-1–3 triaged (non-function artifacts):** `0x8004a2e4`, `0x80045b94`,
`0x80048934` — all 1B mis-disassembly artifacts (`halt_baddata()`).
Substantive work at rank-4.

**1-150B rank-4 decompiled+renamed (HIGH):** `FUN_800425e0` →
`init_conn_subopcode_slot_descriptor_from_timing_templates`
(76B, 4 xrefs) via
`RenamePass52ftRegion80040000Fun800425e0.java` (`renamed=1`, live-verified).

```c
void init_conn_subopcode_slot_descriptor_from_timing_templates(uint index)
{
  if (index < 0xc) {
    desc = PTR_DAT_8004262c + index * 8;
    desc[1] = 2;
    desc[2] = 2;
    desc[4..6] = timing from PTR_DAT_80042630/34 (index 8-11)
                 or PTR_DAT_80042638/3c (index 0-7);
    desc[3] = 0xff;   // caller overwrites with BOS conn slot index
    desc[0] = 1;      // active/initialized
  }
}
```

Per-subopcode 8-byte slot-descriptor initializer for indices 0–11. Populates
status bytes and timing fields from four template tables; leaves byte `+3` as
`0xff` sentinel for `init_subopcode_slot_descriptor_and_assign_conn_index`
(`0x80036370`) to overwrite with the BOS connection slot index. Also called
in a 12-iteration loop from
`reset_conn_subsystem_global_state_and_reinit_slot_entries` (`0x800442bc`).

Post-rename: **93 unnamed** in-region (93 in 1-150B tier); live named **1545**.

**Next:** superseded by Pass 52fu below.

## Pass 52fu (2026-06-30) — 1-150B rank-4 HW-channel dual-dispatch reset rename

**Cold-triage (refreshed):** 93 unnamed 1-150B remain. rank-1 `0x8004a2e4`
(10 xrefs, 1B — artifact); rank-2 `0x80045b94` (6 xrefs, 1B — artifact);
rank-3 `0x80048934` (5 xrefs, 1B — artifact); rank-4 `0x80043038`
(4 xrefs, 50B); rank-5 `0x8004701c` (4 xrefs, 1B — likely artifact);
rank-6 `0x8004b170` (3 xrefs, 88B).

**Rank-1–3 triaged (non-function artifacts):** unchanged from Pass 52ft.

**1-150B rank-4 decompiled+renamed (HIGH):** `FUN_80043038` →
`irq_guarded_hw_channel_dual_dispatch_slot_zero`
(50B, 4 xrefs) via
`RenamePass52fuRegion80040000Fun80043038.java` (`renamed=1`, live-verified).

```c
void irq_guarded_hw_channel_dual_dispatch_slot_zero(void)
{
  irq = disable_interrupts();
  fptr_table = PTR_DAT_8004306c;
  (*fptr_table)(10, 0);
  (*fptr_table)(0, 0xc);
  enable_interrupts(irq);
}
```

IRQ-disabled fixed dual indexed dispatch through `PTR_DAT_8004306c`: index 10
with constant `0` (slot-zero reset), index 0 with `0xc`. Zero-argument sibling
of Pass 52ba's `irq_guarded_hw_channel_raw_dual_indexed_dispatch` (which passes
`slot_index << 4` for index 10). Called from
`bump_retry_or_timeout_counter_and_log` (`0x800075dc`, region `0x80000000`) on
BLE timing recovery path; patch hooks it at `0x8010cc8c` per protocol-dispatch
layer doc.

Post-rename: **92 unnamed** in-region (92 in 1-150B tier); live named **1546**.

**Next:** superseded by Pass 52fv below.

## Pass 52fv (2026-06-30) — 1-150B rank-6 conn-list chain slot append rename

**Cold-triage (refreshed):** 92 unnamed 1-150B remain. rank-1 `0x8004a2e4`
(10 xrefs, 1B — artifact); rank-2 `0x80045b94` (6 xrefs, 1B — artifact);
rank-3 `0x80048934` (5 xrefs, 1B — artifact); rank-4 `0x80043038`
(4 xrefs, 50B — done Pass 52fu); rank-5 `0x8004701c` (4 xrefs, 1B — likely
artifact); rank-6 `0x8004b170` (3 xrefs, 88B).

**Rank-1–3 triaged (non-function artifacts):** unchanged from Pass 52fu.

**1-150B rank-6 decompiled+renamed (HIGH):** `FUN_8004b170` →
`irq_guarded_append_slot_to_conn_list_chain`
(88B, 3 xrefs) via
`RenamePass52fvRegion80040000Fun8004b170.java` (`renamed=1`, live-verified).

```c
void irq_guarded_append_slot_to_conn_list_chain(byte slot_index)
{
  irq = disable_interrupts();
  if ((slot_index < 10) && (chain_head[2] < 10)) {
    if (chain_head[0] == 10) {          // sentinel = empty list
      chain_head[0] = slot_index;
    } else {
      table[chain_head[1] * 0xc + 0xb] = slot_index;  // link tail next-index
    }
    chain_head[1] = slot_index;         // update tail
    table[slot_index * 0xc + 0xb] = 10; // mark new tail sentinel
    chain_head[2]++;                    // increment count
  }
  enable_interrupts(irq);
}
```

IRQ-guarded append of a slot index into the 0xc-stride conn-list chain table
(`PTR_PTR_8004b1cc` / `PTR_DAT_8004b1c8` for list-B; list-A uses parallel
pointers `PTR_PTR_8004b340`). Sentinel value `10` marks chain end. Next-index
byte at table entry offset `+0xb` links slots; count capped at 10. Called from
`walk_conn_list_a_slot_chain_collect_overflow_records`,
`walk_conn_list_b_slot_chain_collect_quota_overflow_records`, and
`fragment_conn_tx_overflow_chain_into_hw_descriptor_slots_by_budget` (slot
recycle path). Sibling of `irq_masked_append_byte_to_conn_list_a_tail`.

Post-rename: **91 unnamed** in-region (91 in 1-150B tier); live named **1547**.

**Next:** superseded by Pass 52fw below.

## Pass 52fw (2026-06-30) — 1-150B rank-5 LMP 0x26f conn-slot word logger rename

**Cold-triage (refreshed):** 91 unnamed 1-150B remain. rank-1 `0x8004a2e4`
(10 xrefs, 1B — artifact); rank-2 `0x80045b94` (6 xrefs, 1B — artifact);
rank-3 `0x80048934` (5 xrefs, 1B — artifact); rank-4 `0x8004701c`
(4 xrefs, 1B — artifact); rank-5 `0x8004ad0c` (3 xrefs, 84B); rank-6
`0x8004fcec` (3 xrefs, 60B); rank-7 `0x80044588` (3 xrefs, 40B).

**Rank-1–4 triaged (non-function artifacts):** unchanged from Pass 52fv.

**1-150B rank-5 decompiled+renamed (HIGH):** `FUN_8004ad0c` →
`pack_and_log_conn_slot_word_0x26f`
(84B, 3 xrefs) via
`RenamePass52fwRegion80040000Fun8004ad0c.java` (`renamed=1`, live-verified).

```c
void pack_and_log_conn_slot_word_0x26f(ushort slot_index, uint subcode)
{
  word = ((slot_index & 0xf) << 5) | 9;
  word &= DAT_8004ad60;
  word |= (subcode & 0xff) << 0xb;
  possible_logger_called_if_no_patch3(*PTR_DAT_8004ad64, word, 0, 0x26f);
}
```

Thin LMP `0x26f` status-word pack+log wrapper: packs conn-slot index low
nibble into bits 5–8 with base status nibble `9`, merges `subcode` low byte
into bits 11+, ANDs with `DAT_8004ad60` mask, emits via patch-hook fptr at
`PTR_DAT_8004ad64`. Sibling of `log_conn_slot_status_word_0x26f_after_connection_init`
(`0x8004ad68`, Pass 52en) and region-`0x80050000`
`pack_and_log_param_pair_0x26f`. Callers include
`conn_field_swap_and_notify_dispatcher_3_4` (field-swap changed path, subcode
`0`) and `finalize_esco_link_mode_and_dispatch_code_0xb` (pending-bit-3 path).

Post-rename: **90 unnamed** in-region (90 in 1-150B tier); live named **1548**.

**Next:** superseded by Pass 52fx below.

## Pass 52fx (2026-06-30) — 1-150B rank-6 conn-record release to free pool rename

**Cold-triage (refreshed):** 90 unnamed 1-150B remain (pre-rename). rank-1
`0x8004a2e4` (10 xrefs, 1B — artifact); rank-2 `0x80045b94` (6 xrefs, 1B —
artifact); rank-3 `0x80048934` (5 xrefs, 1B — artifact); rank-4 `0x8004701c`
(4 xrefs, 1B — artifact); rank-5 `0x8004ad0c` (3 xrefs, 84B — done Pass 52fw);
rank-6 `0x8004fcec` (3 xrefs, 60B); rank-7 `0x80044588` (3 xrefs, 40B).

**Rank-1–4 triaged (non-function artifacts):** unchanged from Pass 52fw.

**1-150B rank-6 decompiled+renamed (HIGH):** `FUN_8004fcec` →
`release_conn_record_to_free_pool`
(60B, 3 xrefs) via
`RenamePass52fxRegion80040000Fun8004fcec.java` (`renamed=1`, live-verified).

```c
void release_conn_record_to_free_pool(conn_rec_t *conn_rec)
{
  if (!is_conn_record_pkt_modes_cleared_for_free()) return;

  teardown_conn_hw_resource_subobject_tree_and_free_to_pools(conn_rec);
  deregister_link_record_handle_from_index_table(conn_rec);

  if ((conn_rec[0x08] & 0x7) != 0) {
    // push conn_rec[0x14] to SCO sub-resource pool at PTR_PTR_8004fd28
    sco_subobj = conn_rec[0x14];
    sco_subobj->next = *PTR_PTR_8004fd28;
    *PTR_PTR_8004fd28 = sco_subobj;
  }

  // push conn_rec to main free list at PTR_PTR_8004fd2c
  conn_rec[0x00] = *PTR_PTR_8004fd2c;
  *PTR_PTR_8004fd2c = conn_rec;
}
```

Conn-record teardown + free-pool return: gated on pkt_modes-cleared precondition
(`is_conn_record_pkt_modes_cleared_for_free`, Pass 52m), tears down HW-resource
subobject tree, deregisters handle from sorted index table, optionally returns
SCO sub-object at `+0x14` when pkt_mode bits set, then pushes slot onto main
free-list head. VSC dispatcher error path and conn-record subsystem documented
in `reverse_engineering_conn_record_subsystem.md` §7.

Post-rename cold-triage (`ColdTriageRegion80040000Pass52fx.java`): **89 unnamed**
in-region (89 in 1-150B tier); live named **1549**. Next substantive rank-5:
`0x80044588` (40B, 3 xrefs).

**Next:** superseded by Pass 52fy below.

## Pass 52fy (2026-06-30) — 1-150B rank-7 control-record state-byte reentry gate rename

**Cold-triage (refreshed):** 89 unnamed 1-150B remain (pre-rename). rank-1
`0x8004a2e4` (10 xrefs, 1B — artifact); rank-2 `0x80045b94` (6 xrefs, 1B —
artifact); rank-3 `0x80048934` (5 xrefs, 1B — artifact); rank-4 `0x8004701c`
(4 xrefs, 1B — artifact); rank-5 `0x8004ad0c` (3 xrefs, 84B — done Pass 52fw);
rank-6 `0x8004fcec` (3 xrefs, 60B — done Pass 52fx); rank-7 `0x80044588` (3
xrefs, 40B).

**Rank-1–4 triaged (non-function artifacts):** unchanged from Pass 52fx.

**1-150B rank-7 decompiled+renamed (HIGH):** `FUN_80044588` →
`gate_control_record_state_byte_init_or_match`
(40B, 3 xrefs) via
`RenamePass52fyRegion80040000Fun80044588.java` (`renamed=1`, live-verified).

```c
bool gate_control_record_state_byte_init_or_match(uint desired_state)
{
  ctrl = PTR_base_of_0x1ac_struct_array_0xA_large2_800445b0;
  state = (uint)(byte)ctrl[0xb].field96_0x60;
  if (state == 0) {
    ctrl[0xb].field96_0x60 = (byte)desired_state;
    return true;
  }
  return (*PTR_DAT_800445b4 & 0x10) == 0 || state == desired_state;
}
```

40B reusable re-entrancy gate on global control record `bos[0xb]` (`field96_0x60`
state byte): first caller sets state from `desired_state` and proceeds; later
callers allowed when global flag bit `0x10` clear or state already matches
`desired_state`. Same gate idiom as `hci_reentry_gate_field_0x165_cmd_echo_u16_send_cmd_complete`
and LE scan/adv HCI handlers in this region. No direct callers found (function-pointer
invocation).

Post-rename: **88 unnamed** in-region (88 in 1-150B tier); live named **1550**.

**Next:** superseded by Pass 52fz below.

## Pass 52fz (2026-06-30) — 1-150B rank-5 SCO/eSCO slot-timing byte table lookup rename

**Cold-triage (refreshed):** 88 unnamed 1-150B remain (pre-rename). rank-1
`0x8004a2e4` (10 xrefs, 1B — artifact); rank-2 `0x80045b94` (6 xrefs, 1B —
artifact); rank-3 `0x80048934` (5 xrefs, 1B — artifact); rank-4 `0x8004701c`
(4 xrefs, 1B — artifact); rank-5 `0x8004e9c4` (3 xrefs, 24B); rank-6
`0x8004498c` (3 xrefs, 1B — artifact); rank-7 `0x80046790` (3 xrefs, 1B —
artifact); rank-8 `0x80049bfc` (3 xrefs, 1B — artifact).

**Rank-1–4 triaged (non-function artifacts):** unchanged from Pass 52fy.

**1-150B rank-5 decompiled+renamed (HIGH):** `FUN_8004e9c4` →
`lookup_sco_esco_slot_timing_byte_by_opcode_index`
(24B, 3 xrefs) via
`RenamePass52fzRegion80040000Fun8004e9c4.java` (`renamed=1`, live-verified).

```c
byte lookup_sco_esco_slot_timing_byte_by_opcode_index(uint opcode)
{
  index = (opcode & 0xff) - 1;
  if (index < 2)
    return PTR_DAT_8004e9dc[index];
  return 0;
}
```

2-entry byte lookup for SCO/eSCO slot-timing opcodes 1–2: indexes
`(opcode - 1)` into the table at `PTR_DAT_8004e9dc`, returns 0 for out-of-range.
Sibling of Pass 52l's `get_credit_scheduler_context_active_entry_ptr`
(`0x8004f240`); callers include `sco_esco_slot_timing_offset_calc_variant1`/
`variant2` in region `0x80000000` (credit-scheduler slot-timing cluster).

Post-rename: **87 unnamed** in-region (87 in 1-150B tier); live named **1551**.

**Next:** superseded by Pass 52ga below.

## Pass 52ga (2026-06-30) — rank-6/7 artifact triage + rank-8 HW-channel status commit rename

**Cold-triage (refreshed):** 87 unnamed 1-150B remain. rank-6 `0x80046790`
(3 xrefs, 1B — artifact); rank-7 `0x80049bfc` (3 xrefs, 1B — artifact);
rank-8 `0x80042ab0` (2 xrefs, 120B); rank-9 `0x8004332c` (2 xrefs, 82B);
rank-10 `0x80042d34` (2 xrefs, 60B).

**Rank-6–7 triaged (non-function artifacts):** `0x80046790` and `0x80049bfc`
(3 xrefs each, 1B) — decompile yields `halt_baddata`/undefined-instruction stubs,
not substantive functions (same artifact class as rank-1–4).

**1-150B rank-8 decompiled+renamed (HIGH):** `FUN_80042ab0` →
`hw_channel_pack_slot_type_status_bytes_table_lookup_dispatch`
(120B, 2 xrefs) via
`RenamePass52gaRegion80040000Fun80042ab0.java` (`renamed=1`, live-verified).

```c
void hw_channel_pack_slot_type_status_bytes_table_lookup_dispatch(
    uint slot_index, int out_rec, uint type_index)
{
  fptr = PTR_DAT_80042b28;
  (*fptr)(2, (type_index & 0x1f) << 0xb | (slot_index & 0xff) << 5);
  *(byte *)(out_rec + 1) = 2;
  if ((type_index < 4) &&
      ((PTR_DAT_80042b2c[type_index + 4] >> 1 & 1) != 0) &&
      ((PTR_DAT_80042b2c[type_index + 4] & 1) == 0)) {
    *(byte *)(out_rec + 2) = 2;
  }
  idx = *(ushort *)(out_rec + 4);
  (*fptr)(idx, *(ushort *)(idx + DAT_80042b30) & 0xfff8 | 1);
  (*fptr)(0, 0x21);
}
```

Triple HW-channel fptr dispatch through `PTR_DAT_80042b28`: first packs
`type_index`/`slot_index` into index-2 register value; writes status byte `2`
to output record `+1` (and conditionally `+2` when per-type flag table
`PTR_DAT_80042b2c` indicates); second commit uses ushort index at `out_rec+4`
with table lookup at `DAT_80042b30`; final index-0 dispatch with `0x21`.
Noirq multi-step sibling of Pass 52ba's `irq_guarded_hw_channel_raw_dual_indexed_dispatch`
in the SCO/eSCO HW-channel cluster (`0x800429xx`–`0x80042bxx`).

Post-rename: **86 unnamed** in-region (86 in 1-150B tier); live named **1552**.

**Next:** superseded by Pass 52gb below.

## Pass 52gb (2026-06-30) — rank-9 HW-channel index-32 flag commit rename

**1-150B rank-9 decompiled+renamed (HIGH):** `FUN_8004332c` →
`reconcile_bdaddr_and_commit_hw_channel_index32_or_flags_with_slot_dispatch`
(82B, 2 xrefs) via
`RenamePass52gbRegion80040000Fun8004332c.java` (`renamed=1`, live-verified).

```c
void reconcile_bdaddr_and_commit_hw_channel_index32_or_flags_with_slot_dispatch(
    uint slot_param)
{
  reconcile_nonmatching_bdaddr_slot_and_dispatch_lmp_259();
  *PTR_DAT_80043380 = 1;
  and_mask_hw_channel_table_entry_and_indexed_dispatch(0x32, 0xfff);
  (*(code *)PTR_DAT_80043384)(0x30, (slot_param & 0x1ffff) >> 1);
  or_merge_hw_channel_table_entry_and_indexed_dispatch(0x32, 0x4000);
  or_merge_hw_channel_table_entry_and_indexed_dispatch(0x32, 0x2000);
  or_merge_hw_channel_table_entry_and_indexed_dispatch(0x32, 0x8000);
}
```

BD_ADDR slot reconciliation prelude (Pass 52cj's
`reconcile_nonmatching_bdaddr_slot_and_dispatch_lmp_259`), then HW-channel
index-0x32 programming: AND-mask clears lower 12 bits, fptr dispatch at index
0x30 commits shifted slot parameter, three OR-merge commits set flags
0x4000/0x2000/0x8000 on the same index. SCO/eSCO HW-channel cluster sibling
of Pass 52g's OR/AND/mask trio and Pass 52ga's status-byte commit path
(`0x800429xx`–`0x800433xx`).

Post-rename: **85 unnamed** in-region (85 in 1-150B tier); live named **1553**.

**Next:** superseded by Pass 52gc below.

## Pass 52gc (2026-06-30) — rank-10 inquiry/LAP pending-slot counter rename

**1-150B rank-10 decompiled+renamed (HIGH):** `FUN_80042d34` →
`count_consecutive_inquiry_lap_pending_slot_flags`
(60B, 2 xrefs) via
`RenamePass52gcRegion80040000Fun80042d34.java` (`renamed=1`, live-verified).

```c
int count_consecutive_inquiry_lap_pending_slot_flags(void)
{
  ctx = PTR_DAT_80042d70;
  count = 0;
  if ((ctx[4] >> 1 & 1) != 0) {
    count = 1;
    if ((ctx[5] >> 1 & 1) != 0) {
      count = 2;
      if ((ctx[6] >> 1 & 1) != 0)
        count = 4 - (uint)((ctx[7] >> 1 & 1) == 0);
    }
  }
  return count;
}
```

Counts consecutive inquiry/LAP slot pending flags (bit1 at `+4`..`+7`) in
global context `PTR_DAT_80042d70`; returns 0–4. Same bit1=pending semantics
as Pass 52n's `release_inquiry_lap_slot_pending_bitmask` (`PTR_DAT_80042d2c`).
Callers use the count as an index into ushort lookup tables for HW-channel
programming: `FUN_8003c94c` (remote-name-request feature apply_4 path,
region `0x80030000`) and `FUN_800161e4` (CPB/page-receive setup,
region `0x80010000`). Inquiry/LAP slot-state cluster sibling of Pass 52n/52o
(`0x80042cxx`–`0x80042dxx`).

Post-rename: **84 unnamed** in-region (84 in 1-150B tier); live named **1554**.

**Next:** superseded by Pass 52gd below.

## Pass 52gd (2026-06-30) — rank-8 substantive noirq OR-merge HW-channel dispatch rename

**Cold-triage (refreshed):** ranks 10–13 are 1–4B artifacts (`0x80047afc` 4B,
`0x80049924` 2B, `0x80046cc4`/`0x80047b00` 1B). Next substantive candidate
(overall rank-8, 32B, 2 xrefs, size≥20B filter): `0x800429ac`.

**1-150B rank-8 decompiled+renamed (HIGH):** `FUN_800429ac` →
`or_merge_hw_channel_table_entry_indexed_dispatch_noirq`
(32B, 2 xrefs) via
`RenamePass52gdRegion80040000Fun800429ac.java` (`renamed=1`, live-verified).

```c
void or_merge_hw_channel_table_entry_indexed_dispatch_noirq(uint index, ushort mask)
{
  table = DAT_800429cc;
  fptr_table = PTR_DAT_800429d0;
  (*fptr_table)(index & 0xffff, mask | table[index]);
}
```

OR-merge indexed dispatch without IRQ protection: OR-merges `mask` onto the
per-index ushort HW-channel parameter table entry at `DAT_800429cc`, then calls
through the function-pointer table at `PTR_DAT_800429d0`. Structural noirq twin
of `or_merge_hw_channel_table_entry_and_indexed_dispatch` (`0x8004310c`, 68B,
IRQ-disabled) and OR-variant sibling of Pass 52u's
`and_mask_hw_channel_table_entry_indexed_dispatch_noirq` (`0x80042984`, 32B)
in the SCO/eSCO HW channel parameter-commit cluster. Caller
`accept_lmp_conn_setup_and_program_baseband_from_unpacked_pdu` invokes with
index `0x100` on the IRQ-off LMP connection-setup path (after
`and_mask_hw_channel_table_entry_indexed_dispatch_noirq`).

Post-rename: **83 unnamed** in-region (83 in 1-150B tier); live named **1555**.

**Next:** superseded by Pass 52ge below.

## Pass 52ge (2026-06-30) — rank-1 BOS subindex connection-index resolver rename

**Cold-triage (refreshed):** xrefs≥2 size≥20B tier fully exhausted (zero
substantive candidates remain). Relaxed filter to size≥20B; rank-1
`0x80043774` (142B, 1 xref) — substantive BOS subindex→connection-index
resolver on the public-BD_ADDR path, sibling of Pass 52at's
`map_bos_subindex_to_lmp_txn_mode_when_bdaddr_random`.

**Rank-1 decompiled+renamed (HIGH):** `FUN_80043774` →
`resolve_bos_connection_index_for_subindex_when_bdaddr_public` (142B) via
`RenamePass52geRegion80040000Fun80043774.java` (`renamed=1`, live-verified).

```c
byte resolve_bos_connection_index_for_subindex_when_bdaddr_public(
    uint conn_idx, byte bos_subindex)
{
  result = 0xff;
  if (big_ol_struct[conn_idx].bdaddr_random_ == '\0') {
    if (big_ol_struct[conn_idx].byte_0xCC == bos_subindex)
      result = big_ol_struct[conn_idx].bos_connection__array_index;
    else if (lookup_bos_subindex_mapping(&result, bos_subindex) == 0)
      mapping_table[((result - 1) * 4 + bos_subindex) * 2 + 1] = (char)conn_idx;
  }
  return result;
}
```

When `bdaddr_random_` is clear (public/static BD_ADDR), resolves the BOS
connection-array index for a given `bos_subindex`: fast-path returns
`bos_connection__array_index` when `byte_0xCC` already matches; otherwise
looks up via `FUN_80060770` and records `conn_idx` in the subindex mapping
table at `PTR_DAT_8004380c`. Returns `0xff` when random-address mode is
active (handled by sibling `map_bos_subindex_to_lmp_txn_mode_when_bdaddr_random`).
Sole caller `FUN_8006fd20` (role-switch/LMP slot-offset handler): output feeds
`map_bos_subindex_to_lmp_txn_mode_when_bdaddr_random` as `default_mode` before
staging into pending LMP PDU struct `+0x11`.

Post-rename: **82 unnamed** in-region (45 in 1-150B size≥20B tier); live named **1556**.

**Next:** superseded by Pass 52gf below.

## Pass 52gf (2026-06-30) — rank-2 conn-type-nibble-2 LMP role-connection setup rename

**Cold-triage (continued):** size≥20B, xrefs≥1 tier; rank-2 `0x800411a4` (132B,
1 xref) — substantive conn-type-nibble-2 LMP role-connection setup coordinator,
sole callee of `dequeue_conn_event_ring_and_dispatch_lmp_by_conn_type_nibble`
when conn-type nibble == 2.

**Rank-2 decompiled+renamed (HIGH):** `FUN_800411a4` →
`allocate_conn_record_and_dispatch_lmp_role_connection_setup` (132B) via
`RenamePass52gfRegion80040000Fun800411a4.java` (`renamed=1`, live-verified).

```c
void allocate_conn_record_and_dispatch_lmp_role_connection_setup(
    conn_event_t *event, byte role_subindex, uint flags, int event_update_only)
{
  if (alloc_conn_record_via_fptr(PTR_DAT_80041228, &record) == 0) {
    init_conn_record_field(record + 4, 0x12, 1);
    if (event_update_only == 1)
      update_conn_record_from_incoming_event_with_rssi_and_flag4_deferral(record, event);
    else {
      role = *(byte *)(record + 0x12) & 7;
      *(byte *)(record + 0x16) = role;
      if (role_subindex == 0)
        accept_dual_slot_lmp_role_connection_and_program_baseband_regs(record, role, flags & 0xff);
      else
        accept_lmp_conn_setup_and_program_baseband_from_unpacked_pdu();
    }
  } else {
    release_conn_record(0x12, 0);
    possible_logging_function__var_args(...);
  }
}
```

Allocates a conn record via fptr `PTR_DAT_80041228`, initializes type `0x12`
at `record+4`, then branches: when `event_update_only==1` (outer `param_1` to
the ring dispatcher) updates RSSI/deferred fields only; otherwise extracts
role index from `record+0x12` bits 0–2 into `record+0x16` and dispatches
`accept_dual_slot_lmp_role_connection_and_program_baseband_regs` when
`role_subindex==0` (dual-slot path) or
`accept_lmp_conn_setup_and_program_baseband_from_unpacked_pdu` for non-zero
role sub-index (single-slot PDU-unpack path). Error path logs on allocation
failure. SCO/eSCO conn-type cluster hub sibling of Pass 52df ring dispatcher,
Pass 52di dual-slot accept, and Pass 52dn event updater.

Post-rename: **81 unnamed** in-region (44 in 1-150B size≥20B tier); live named **1557**.

**Next:** superseded by Pass 52gg below.

## Pass 52gg (2026-06-30) — rank-1 link-policy offset+dword commit HCI handler rename

**Cold-triage (refreshed):** size≥20B, xrefs≥1 tier; rank-1 `0x80045854` (124B,
1 xref) — substantive link-policy cluster HCI Command Complete sender; sibling of
`hci_link_policy_settings_read_send_cmd_complete` and
`hci_link_policy_param_setup_handler_send_cmd_complete`.

**Rank-1 decompiled+renamed (HIGH):** `FUN_80045854` →
`hci_link_policy_offset_dword_commit_send_cmd_complete` (124B) via
`RenamePass52ggRegion80040000Fun80045854.java` (`renamed=1`, live-verified).

```c
undefined1 hci_link_policy_offset_dword_commit_send_cmd_complete(short *hci_cmd)
{
  ctrl = PTR_base_of_0x1ac_struct_array[0xb];
  state = ctrl.field96_0x60;
  if (state == 0)
    ctrl.field96_0x60 = 1;
  else if ((*PTR_DAT_800458d4 & 0x10) && state != 1)
    return 0xc;  // Command Disallowed — skip param commit
  offset_byte = *(byte *)((int)hci_cmd + 3);
  status = 0x12;  // Invalid HCI Command Parameters
  if (offset_byte < 0x20) {
    ctrl.field55_0x37 = offset_byte;
    status = 0;
    if (offset_byte != 0)
      optimized_memcpy(ctrl.field60_0x3c..field63_0x3f, hci_cmd + 2, 4);
  }
  cmd_word = *hci_cmd;
  hci_status = (cmd_word == 0) ? 0 : field_0x165 defaulting to 1;
  hci_event_sender(0xe, {hci_status, cmd_lo, cmd_hi, status}, 4);
  return status;
}
```

Re-entrancy-gated HCI command handler on global control record `bos[0xb]`:
`field96_0x60` state byte (0→1 transition, returns `0xc` when disallowed) —
same gate idiom as `hci_link_policy_settings_read_send_cmd_complete` (read-only
sibling) but with lightweight param commit: when offset byte at `hci_cmd+3` is
`<0x20`, writes `field55_0x37` and optionally copies a 4-byte dword from
`hci_cmd+2` into `field60_0x3c..field63_0x3f`; otherwise leaves status
`0x12`. Derives HCI status byte from global `the_0x300` struct `field_0x165`
(0 when cmd word==0, else field value defaulting to 1); packs 4-byte Command
Complete (`hci_event_sender(0xe,…)`). No direct callers found (function-pointer
registration).

Post-rename: **80 unnamed** in-region (43 in 1-150B size≥20B tier); live named **1558**.

**Next:** continue 1-150B cold-triage — decompile+rename next candidate
(size≥20B; xrefs≥1 tier; refresh cold-triage ranks).
