# Phase 9: Exhaustive RE — ROM Region 0x80040000-0x8004ffff

**Status**: PASS 2 COMPLETE (2026-06-23); PASS 3 (cold-triage of 307 unnamed) STAGED

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
| Already high-confidence | ~54 (16.1%, +2 from Pass 3) | 336 (100%) |
| Thin-named (decompile pending) | 0 (Pass 2 complete) | 0 (all high/medium) |
| Unnamed (cold triage) | 303 (90.2%, 6 newly at MEDIUM/MEDIUM-HIGH from Pass 3, 2 renamed HIGH) | 0 (all named) |

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

**NEXT**: Batch-decompile the remaining top-15-per-half candidates listed
above (11 lower + 11 upper = 22 functions, in 2-4-function batches), with
`0x80043a60` (xrefs:15) prioritized first given its unusually high
cross-reference count for its size tier.
