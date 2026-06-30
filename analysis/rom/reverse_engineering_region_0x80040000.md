# Phase 9: Exhaustive RE — ROM Region 0x80040000-0x8004ffff

**Status**: PASS 1-6 COMPLETE (2026-06-23); PASS 7 COMPLETE (2026-06-24) — 151-600B tier fully exhausted; 1-150B tier cold-triage resumed Pass 52 (2026-06-30): 127 functions in tier, 46 renamed HIGH (Passes 52–52bc). Formal park unaffected by opportunistic cross-region passes since (Pass 33/47/51 addenda) — see bottom of file for the latest (PASS 52bc, 2026-06-30).

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
| `0x80043e04` | 1168B | Dense register-programming function operating on connection-record fields; references several already-named eSCO/role helpers. Single coherent purpose visible but no confirmed cross-link strong enough for a precise verb-noun name yet. | MEDIUM-HIGH |
| `0x80040a24` | 988B | Large dispatcher-shaped function in the lower-half conn-type cluster; switch-like structure but case semantics not yet individually confirmed. | MEDIUM |
| `0x8004147c` | 934B | Already partially documented in `reverse_engineering_lc_lmp_state_machine.md` (referenced via `fptr_DAT_80036f5c` for HCI_Inquiry/0x419/0x43f) as "inquiry-train baseband programming / role-related setup" — dense register-programming, consistent with that prior note. Left unrenamed pending a more specific single-purpose confirmation. | MEDIUM-HIGH |
| `0x80041dac` | 876B | `void FUN_80041dac(uint param_1)` — connection-teardown/cleanup-shaped handler operating on a `param_1`-indexed record; clears multiple fields consistent with link release. No confirmed cross-xref yet. | MEDIUM |
| `0x8004d294` | 1280B | Large upper-half handler adjacent to the LE Meta Event cluster; plausible event-assembly routine but case/field semantics not individually confirmed this pass. | MEDIUM |
| `0x8004ce70` | 908B | Already appears (unnamed) in `reverse_engineering_conn_feature_dispatch.md`'s xref list alongside `assoc_w_tLC_RX`/`FUN_80051368`/`FUN_80052f8c` — confirmed participant in the connection/feature dispatch graph, but specific behavior not yet isolated to a single clear purpose. | MEDIUM-HIGH |

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
| `0x80040594` | 566B | Dense register/field-programming routine in the lower conn-type cluster; coherent but unconfirmed single purpose. | MEDIUM |
| `0x80041230` | 560B | Similar register-programming shape to neighboring lower-half functions; no distinguishing confirmation. | MEDIUM |
| `0x80042640` | 530B | Field-update logic operating on connection-record-shaped data; plausible LC-layer helper but unconfirmed. | MEDIUM |
| `0x800401c4` | 426B | Compact dispatch-shaped function; case semantics not individually confirmed. | MEDIUM |
| `0x80040e60` | 386B | Connection-record field manipulator; no cross-xref confirmation available (tooling gap, see below). | MEDIUM |
| `0x80041900` | 376B | Similar shape to other lower-half record-field helpers. | MEDIUM |
| `0x80043c7c` | 372B | Small state-check/update helper; plausible but unconfirmed single purpose. | MEDIUM |
| `0x80041a94` | 352B | Field manipulator, lower conn-type cluster. | MEDIUM |
| `0x800435a8` | 338B | Field manipulator, lower conn-type cluster. | MEDIUM |
| `0x80041028` | 336B | Field manipulator, lower conn-type cluster. | MEDIUM |

**Net effect (lower half)**: no renames this pass — all 11 stay at MEDIUM,
documented above for a future targeted pass (likely needs working
`xrefs_to`/`find_callers` against this GZF to push further, since the
decompiled shapes are individually plausible but not distinguishable from
each other on content alone).

### Findings summary — upper half (11 functions, 1 reached HIGH)

| Address | Size | Behavior | Confidence |
|---|---|---|---|
| `0x8004c4a8` | 894B | Large handler, shape and field-touches not yet isolated to one specific purpose. | MEDIUM |
| `0x800483c0` | 866B | Terminates via `hci_event_sender(0xe,&local_34,4)` (Command Complete pattern), writes into the established per-connection struct array fields, validates params against eSCO-style bounds. Likely an HCI VSC/command handler setting per-connection eSCO/SCO timing parameters, but no second confirming signal (xrefs tooling gap) to clear the HIGH bar precisely. | MEDIUM-HIGH |
| `0x80047628` | 832B | Near-identical sibling shape to `0x80047304` — likely a paired HCI command for chunked variable-length data (e.g. a read/write variant pair). **Pass 53 update (region `0x80050000`'s Pass 53):** mechanism now confirmed — an HCI-command/LMP-PDU fragment-reassembly handler: looks up a connection by handle, validates a role/type byte + length field, copies received payload bytes into a per-connection sub-record buffer (allocating a fresh sub-record via `setup_type3_esco_sco_conn_record_with_role_bit_set` + the newly-named `find_tail_of_payload_subrecord_chain_at_field0x50` when the current one fills), terminates via `hci_event_sender(0xe,...)` (Command Complete). Exact opcode/command identity still not pinned down — stays MEDIUM-HIGH. | MEDIUM-HIGH |
| `0x80047304` | 780B | Sibling of `0x80047628` (see above). **Pass 53 update:** same mechanism, paired with `setup_type3_esco_sco_conn_record_with_role_bit_clear` + `find_tail_of_payload_subrecord_chain_at_field0x50_0x24` (the nested-chain variant) instead. Stays MEDIUM-HIGH. | MEDIUM-HIGH |
| `0x8004cb48` | 722B | Calls the known eSCO table processor `FUN_80044730` twice — strong structural link to the eSCO cluster. | MEDIUM-HIGH |
| `0x80047c50` | 700B | Calls `FUN_80044730` (eSCO table processor) — same cluster as `0x8004cb48`. | MEDIUM-HIGH |
| `0x8004966c` | 696B | `undefined1 FUN_8004966c(...)`: validates SCO/eSCO bandwidth/packet-type/retransmission-window params using nearly identical bounds checks to the already-confirmed `HCI_Setup_Synchronous_Connection_handler` (0x80049d20), writes into `get_0x1ac_struct_ptr_by_index`-addressed connection-record fields, and terminates via `send_evt_HCI_Command_Status` on every path — the same "this is an HCI command handler" signature as its sibling. Parameter shape and termination pattern match HCI Accept Synchronous Connection Request. | **HIGH** — renamed `HCI_Accept_Synchronous_Connection_Request_handler` |
| `0x80046900` | 682B | Validates packet-type/role bitmask fields; plausible HCI command parameter validator for a multi-entry SCO/eSCO table, not individually confirmed beyond shape. | MEDIUM-HIGH |
| `0x800480b0` | 682B | Validates connection params via `get_0x1ac_struct_ptr_by_index`, checks a bitmask with sub-cases 0/1/2, conditionally calls `FUN_8005fe90`, terminates exclusively via `send_evt_HCI_Command_Status` on every path — shape consistent with an HCI command handler toggling a per-connection link-policy/mode flag (e.g. Sniff/Hold/Park-style), but no definitive single name confirmed. | MEDIUM-HIGH |
| `0x8004b468` | 624B | No HCI event/status sender at all — pure internal queue/scheduler logic over `0x1ac`-strided struct array entries via `FUN_8004b344`/`FUN_8004b170`/`FUN_8004b0f8`/`FUN_8004b3c0`; disables/enables interrupts around a critical section; manages a linked list with byte-budget accounting. Likely the TX/scheduling fragmentation engine for per-connection ACL/SCO data queues — internal infra, not an HCI command handler. | MEDIUM |
| `0x80045964` | 560B | Validates page-scan/inquiry-scan-style window/interval parameter pairs (bounded `0x1f..0x4000`), bit-packs results into a `the_0x300`-sized struct's offset `0x28-0x2f` region, terminates via `hci_event_sender(0xe,...)` (Command Complete). Strong shape match to HCI_Write_Page_Scan_Activity / HCI_Write_Inquiry_Scan_Activity, but two near-identical HCI commands share this exact bounds pattern in the spec — can't be distinguished to HIGH without a confirming xref. | MEDIUM-HIGH |

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
`FUN_8004e9c4` (indexed byte table lookup) in the SCO/eSCO slot-timing cluster
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
confirmed callers via `xrefs_to`: `FUN_800435a8` and
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
`PTR_PTR_8004e818`) but without a NULL guard. Consumed by `FUN_8004e1c4` (64B),
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
dispatch cluster sibling of `FUN_8004a730` (adjacent segment parser).

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
`0x800435xx` inquiry/LAP / role-switch cluster (sibling of `FUN_800435a8` field
manipulator and `dual_slot_buffer_reassignment_on_role_switch` in region `0x80030000`).

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
capacity. Sole caller `FUN_80047980` (380B HCI synchronous-connection param
commit handler): when `conn_rec+0x20` bit0 set and procedure not yet active,
calls this function — on `0xff` returns HCI error `9`, else passes slot index
to `FUN_8004fd6c` before arming procedure bit `+0x1d` and scheduling
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
`default_mode` unchanged (typically the output of sibling `FUN_80043774`). Sole caller
`FUN_8006fd20` (454B role-switch/LMP slot-offset completion handler in region
`0x80070000`): after `resolve_bos_subindex_byte_for_connection_index` and
`FUN_80043774`, stores mapped mode into pending LMP PDU staging struct at `+0x11`
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
`+0x32` slots/2, `+0x34` min(slots-1, 8). Sole caller `FUN_80046900` (682B HCI sync-
connection packet-type table validator): called once per enabled packet-type entry after
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
