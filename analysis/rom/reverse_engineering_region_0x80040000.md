# Phase 9: Exhaustive RE — ROM Region 0x80040000-0x8004ffff

**Status**: PASS 2 COMPLETE (2026-06-23); PASS 3 (top-15-per-half sweep, 30 functions) COMPLETE (2026-06-23); cold-triage of remaining unnamed functions outside top-15 lists still open

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
| `0x8004d8b8` | 1898B | Global BT-state/connection-table initializer: `memset`s the entire `PTR_base_of_0x1ac_struct_array_0xA_large2` array (the project's established 11-entry `big_ol_struct` connection-record array), then loops `uVar14 < 0xb` setting default LST `0xa0a` (same constant `init_connection_record`/0x8005b9d8 uses), default poll intervals, BD_ADDR/feature fields from `config_struct`, and calls sub-initializers `FUN_8005bde0`, `FUN_80058a34`, `FUN_80009774`, `FUN_8005c988`. Structurally the top-level/global counterpart to the already-named per-record `init_connection_record`. | **HIGH** — renamed `init_global_connection_table_and_bt_state` |
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
| `0x80047628` | 832B | Near-identical sibling shape to `0x80047304` — likely a paired HCI command for chunked variable-length data (e.g. a read/write variant pair). | MEDIUM-HIGH |
| `0x80047304` | 780B | Sibling of `0x80047628` (see above). | MEDIUM-HIGH |
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
