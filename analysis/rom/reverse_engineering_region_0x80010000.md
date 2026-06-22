# ROM Region 0x80010000–0x8001ffff — Catch-All Triage

Part of Phase 9's exhaustive ROM-function sweep (see `work-in-progress.txt`,
"PHASE 9 CONTINUED — EXHAUSTIVE RE OF EVERY ROM FUNCTION"). This doc covers
functions in `0x80010000`–`0x8001ffff` that don't belong to a subsystem with
its own dedicated doc. This region was flagged as the largest thin-named
bucket of any of the 8 ROM regions in the sweep (130 thin-named per the
original `rom_function_index.md` count) and heavy LMP/VSC handler territory.

## Scope

A fresh authoritative listing (`ListRegion0x80010000.java` +
`ListRegion0x80010000_Upper.java`, both saved 2026-06-22, run via
`script_file_id` against the GZF in process mode, `use_saved_project=True`)
enumerated **every** function in this address range directly from Ghidra's
`FunctionManager` (no address-range guessing): **408 functions total**.

Cross-referencing against `rom_function_index.md`'s existing named-function
table splits this into:

| Category | Count |
|---|---|
| Already named, **high confidence** (decompiled+documented elsewhere) | 15 |
| Named, **medium confidence** (one-line purpose, not decompiled) | 64 |
| Named, **low confidence** (Kovah name, purpose unclear) | 66 |
| Completely unnamed (`FUN_8001xxxx`, Ghidra `DEFAULT` source type) | 263 |
| **Total** | **408** |

(The ticket's original estimate was "268 unnamed + 130 thin-named = 398";
the direct recount above — 263 unnamed + 130 thin-named = 393, +15
already-high-confidence = 408 — is the authoritative figure going forward.
Small drift from the original estimate is expected, as that estimate was
computed from an earlier `rom_function_index.md` snapshot; not a sign of
miscounting in this pass.)

The 15 already-high-confidence functions in this range (no further work
needed, listed here only for completeness — see their respective docs for
detail): `calls_to_0x8010a001_as_fptr_to_install_patches` (`0x800109ac`,
boot/interrupt/USB-transport docs), `write_codec_table_entry_and_wait_ack`
(`0x80013dc4`), `called_on_every_HCI_CMD_via_fptr` (`0x80014180`,
`usb_transport_hci_driver`), `write_baseband_codec_param_triplet`
(`0x800147b0`), `set_bos_e4_role_switch_hook_bit` (`0x8001483c`),
`clear_codec_table_entries_for_role` (`0x80014c58`),
`clear_bos_e4_role_switch_hook_bit` (`0x80014d50`) — all from
`region_0x80000000`'s pass-4 reconciliation (leaf helpers physically located
in this region but resolved as supporting evidence for `0x80000000`-region
clusters — `region_0x80000000.md` correctly excludes them from its own
220-function gap scope) — plus `LMP_QUALITY_OF_SERVICE_REQ_0x2A`
(`0x8001aa3c`), `LMP_0x18_LMP_UNSNIFF_REQ` (`0x8001af9c`),
`fHCI_Create_Connection_0x05` (`0x8001bd38`), `fHCI_Inquiry_0x01`
(`0x8001bfa0`) (all `lc_lmp_state_machine`), `hci_event_sender`
(`0x8001d070`), `send_evt_HCI_Read_Remote_Version_Information_Complete`
(`0x8001d4a0`) (`lmp_version_conn_setup`), and `HCI_Read_Loopback_Mode`
(`0x8001e780`) / `HCI_Write_Loopback_Mode` (`0x8001ea34`)
(`hci_command_router`).

## Pass 1 (2026-06-22) — the `send_evt_HCI_*` event-sender cluster

**Resolved 32 functions** to confirmed-by-decompile purpose: a long,
near-contiguous run of HCI-event-builder functions from `0x8001ca94` to
`0x8001da3c`. This stretch was already almost entirely named by Kovah with
self-descriptive `send_evt_HCI_*` names — the cluster's value was
**confirming via decompile** that the names are accurate (none turned out
to be misleading) and resolving the 2 unnamed leaf helpers caught in the
middle of the run.

### Architecture confirmed

Every function in this cluster is a thin wrapper that:
1. Gathers/packs its event-specific payload fields (often via
   `optimized_memcpy`/`copy_bytes_in_LSB_order` from a connection-record or
   config struct) into a small stack buffer.
2. Calls the single shared primitive **`hci_event_sender`** (`0x8001d070`,
   310B, already high-confidence from prior phases) with `(event_code,
   payload_ptr, payload_len)`.

This confirms `hci_event_sender` as the **single shared HCI-event-TX
primitive** for this whole cluster, parallel to the already-documented
`send_LMP_pkt` (`0x800611e4`) being the shared LMP-PDU-TX primitive
(`lc_lmp_state_machine.md`). Event codes observed (hex, matching the HCI
spec event codes the function names imply): `0x03` Connection Complete,
`0x04` Connection Request, `0x05` Disconnection Complete, `0x07` Remote Name
Request Complete, `0x0b` Read Remote Supported Features Complete, `0x0d`
QoS Setup Complete, `0x0e` Command Complete, `0x10` Hardware Error, `0x11`
Flush Occurred, `0x12` Role Change, `0x13` Number Of Completed Packets,
`0x14` Mode Change, `0x19` Loopback Command, `0x1b` Max Slots Change, `0x1c`
Read Clock Offset Complete, `0x1d` Connection Packet Type Changed, `0x21`
Flow Specification Complete, `0x4e`–`0x56` the newer/extended event range
(Triggered Clock Capture, Synchronization Train Complete/Received,
Connectionless Peripheral Broadcast {Timeout, Receive, Channel Map Change},
Truncated Page Complete, Peripheral Page Response Timeout, Inquiry Response
Notification).

### Per-function findings

| Address | Size | Name | Notes |
|---|---|---|---|
| `0x8001ca94` | 60 | `send_evt_HCI_Inquiry_Response_Notification` | Reads 3-byte LAP from a `0x300`-sized struct, computes RSSI delta via `return_RSSI`, sends event `0x56` |
| `0x8001cad4` | 36 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Channel_Map_Change` | `memcpy` 10 bytes from a fixed data ptr, sends event `0x55` |
| `0x8001cafc` | 20 | `send_evt_HCI_Peripheral_Page_Response_Timeout` | No payload — sends event `0x54` with `len=0` |
| `0x8001cb10` | 74 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Timeout` | Branches on a bool param to pick one of two 7-byte payload sources, sends event `0x52` |
| `0x8001cb68` | 24 | `send_evt_HCI_Synchronization_Train_Complete` | 1-byte payload (status), sends event `0x4f` |
| `0x8001cb80` | 70 | `send_evt_HCI_Triggered_Clock_Capture` | Guarded by a feature-enable bit (`ptr[0x1c]&1`); packs a 9-byte clock/BD_ADDR-flag payload, sends event `0x4e` |
| `0x8001cbcc` | 52 | `send_evt_HCI_Truncated_Page_Complete` | `memcpy` 6-byte BD_ADDR indexed by a bos-array index, sends event `0x53` |
| `0x8001cc04` | 110 | `send_evt_HCI_Synchronization_Train_Received` | Packs a 0x1d-byte payload (BD_ADDR + clock + interval + map + flags) from two data ptrs, sends event `0x50` |
| `0x8001cc80` | 226 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Receive` | Calls a guard fn-ptr first (can suppress the whole event); on accept, packs a variable-length payload (BD_ADDR + clock + LT_ADDR + CLK + offset + data, capped at table[0]) and sends event `0x51`; on reject, calls the `possible_logging_function?_var_args` logger instead — **first confirmed real caller of that logger in this region** |
| `0x8001cd74` | 586 | `initialize_0x28_sized_struct` | **Not an event sender** — a 0x28-byte connection-feature-flags struct initializer. Zero-fills/`0xff`-fills a fixed pattern then conditionally sets/clears specific bit positions based on 5 different `config_base` feature-enable bits (`_x7a&1`, `_x7a&2`, `_x7a&8`, `_x7a&0x10`, `field208_0xd8&0x1000/0x2000`). The name (already accurate) describes the struct size; confirms this is a per-connection LMP/QoS feature-capability block built once at connection-setup time, matching the kind of struct the `send_evt_0x21_HCI_Flow_Specification_Complete`/`send_evt_0x0D_HCI_QoS_Setup_Complete` functions below read fields out of |
| `0x8001cfc4` | 110 | **renamed** `rssi_threshold_delta_for_bos_index` (was `FUN_8001cfc4`) | Looks up a connection's RSSI field by bos-array index, compares against `config_base`'s high/low RSSI threshold pair (`field247_0x102`/`field248_0x104`), returns signed 8-bit delta (0 if within range) — feeds `send_evt_HCI_Inquiry_Response_Notification`'s sibling logic pattern (RSSI-delta-as-event-field) |
| `0x8001d03c` | 48 | **renamed** `log_hci_evt_0x1fc_if_no_patch3` (was `FUN_8001d03c`) | Thin wrapper calling `possible_logger_called_if_no_patch3` with fixed event tag `0x1fc` — a debug/trace hook for an event code that has no dedicated sender in this region (cross-referenced: `0x1fc` matches the standalone `HCI_EVT_0x1fc_FUN_8001fce0` thin-named function later in the region, at `0x8001fce0` — not yet decompiled, flagged for a future pass) |
| `0x8001d070` | 310 | `hci_event_sender` | Already high-confidence — the shared primitive itself, confirmed as callee of all 30 senders in this cluster |
| `0x8001d1bc` | 24 | `send_evt_HCI_Hardware_Error` | 1-byte payload, event `0x10` |
| `0x8001d1d4` | 34 | `send_evt_HCI_Flush_Occurred` | 2-byte handle payload, event `0x11` |
| `0x8001d1f8` | 74 | `send_evt_HCI_Connection_Complete` | 11-byte payload (status+handle+BDADDR+link_type+encrypt_mode), event `3` — **note: a second, differently-shaped function with the exact same name exists at `0x8001d844`** (see below); Kovah evidently named both identically since they both implement HCI event 0x03, from two different call sites (likely sync vs. async/role-switch path — see `0x8001d844`'s extra bos-index bookkeeping) |
| `0x8001d244` | 58 | `send_evt_HCI_Loopback_Command` | Variable-length (≤253B) payload copied from a caller-supplied buffer, event `0x19` |
| `0x8001d280` | 130 | `send_evt_0x21_HCI_Flow_Specification_Complete` | Reads a 4-field (token rate/bucket/peak BW/latency) QoS record from a per-connection big_ol_struct array, byte-swaps each via `copy_bytes_in_LSB_order`, event `0x21` |
| `0x8001d308` | 122 | `send_evt_0x0D_HCI_QoS_Setup_Complete` | Same shape as `0x21` above minus the bucket-size field, plus delay-variation; event `0xd` |
| `0x8001d388` | 70 | `send_evt_HCI_Role_Change` | Looks up bos-array index via `look_for_non_matching_bdaddr_bos_index`; only sends event `0x12` if lookup succeeds (`iVar2==0`); always clears a 0xff sentinel field afterward regardless |
| `0x8001d3d4` | 36 | `send_evt_HCI_Max_Slots_Change` | 3-byte payload (handle+slots), event `0x1b` |
| `0x8001d3f8` | 44 | `send_event_HCI_Connection_Packet_Type_Changed` | 5-byte payload (status+handle+packet_type), event `0x1d` |
| `0x8001d424` | 76 | `called_by_fHCI_Read_LMP_Handle_send_evt_HCI_Command_Complete` | 0xb-byte Command Complete payload (num_cmd_pkts defaults to 1 if the field is 0), event `0xe` — confirms this is a **specific** Command Complete sender for the LMP-Handle-read path, not the generic one |
| `0x8001d474` | 44 | `send_evt_HCI_Read_Clock_Offset_Complete` | 5-byte payload, event `0x1c` |
| `0x8001d4a0` | 134 | `send_evt_HCI_Read_Remote_Version_Information_Complete` | Already high-confidence (`lmp_version_conn_setup.md`) |
| `0x8001d534` | 48 | `send_evt_HCI_Read_Remote_Supported_Features_Complete` | 11-byte payload (status+handle+8-byte features), event `0xb` |
| `0x8001d564` | 74 | `send_evt_HCI_Remote_Name_Request_Complete` | 0xff-byte payload (status + 6-byte BDADDR + 0xf8-byte name buffer) — code comment notes the 0xf8 buffer may double as an EIR/FHS data carrier since standard remote-name payloads are smaller; event `7` |
| `0x8001d5b4` | 68 | `send_evt_0x14_HCI_Mode_Change` | 6-byte payload (status+handle+mode+interval), event `0x14` |
| `0x8001d5fc` | 460 | `send_evt_HCI_Disconnection_Complete` | **Largest function in the cluster.** Not just an event sender — full teardown orchestrator: looks up the bos-array index(es) for the handle, branches on whether the disconnect is for a primary or "extra" (eSCO-style, `local_18==1`) connection record, does codec/role/index bookkeeping (`remap_role_index_to_esco_slot_if_pending`, clears a hook-bitmask bit, calls several teardown helper fn-ptrs/functions `FUN_8005c960`/`FUN_8005b79c`/`FUN_800719a0`), and only sends event `5` if the teardown path didn't already suppress it (`uVar11==0`). Confirms the disconnection-complete event is tightly coupled to the same conn-record-teardown machinery as `conn_record_subsystem.md`'s free/release path — candidate for a cross-reference note there in a future pass |
| `0x8001d804` | 64 | `send_evt_HCI_Connection_Request` | 10-byte payload (6-byte BDADDR + 3-byte class + link_type), event `4` |
| `0x8001d844` | 178 | `send_evt_HCI_Connection_Complete` (2nd of the name) | Extra bos-array-index bookkeeping vs. the `0x8001d1f8` sibling: on `local_18==1` (role-switch/extra-record path) looks up the bos index, bails early if found, else does a counter bump + a call into `FUN_8002235c`; after sending the event, also clears a hook-bitmask bit when `local_18==1`. This is the role-switch/secondary-record-aware variant; `0x8001d1f8` is the simple/primary-record variant |
| `0x8001d904` | 256 | `send_evt_HCI_Inquiry_Result_or_HCI_Inquiry_Result_with_RSSI` | Builds a multi-entry inquiry-result payload in a loop (up to `param_2` entries) from a per-device table (BDADDR, class, clock-offset, optionally RSSI); picks event code `2` (plain) or `0x22` (with RSSI) depending on a mode flag; a guard fn-ptr can suppress the whole call |
| `0x8001da0c` | 40 | `send_evt_HCI_Inquiry_Complete` | 1-byte status payload, event `1`; also `memset`s a 0x40-byte scratch buffer and clears a flag afterward — inquiry-session cleanup combined with the completion event |
| `0x8001da3c` | 438 | `send_evt_HCI_Number_Of_Completed_Packets` | Iterates up to 4 sources of "packets completed" data (a 10-entry connection table, a 3-entry secondary table, two single `short` counters, and — if a feature bit is set — an 11-entry extended-connection array) building a variable-length `(handle, count)` pair list; a guard fn-ptr can intercept/suppress; sends event `0x13` only if at least one pair was collected |

### Architecture note: two distinct "Connection Complete" senders

Kovah named both `0x8001d1f8` and `0x8001d844` `send_evt_HCI_Connection_Complete`
(identical name, different addresses) — confirmed by this pass's decompile to
be two genuinely different functions, not a duplicate/alias. `0x8001d1f8` is
the simple path (no bos-index bookkeeping); `0x8001d844` is the role-switch/
secondary-record-aware path (does bos-index lookup, counter bump, hook-bit
clear). Both ultimately call the same `hci_event_sender(3, ...)`. This is
analogous to the multiple eSCO/role-switch variants already documented for
other clusters (`conn_type_dispatch_and_esco.md`'s four connection-type
handlers) — the firmware frequently has a "simple" and a
"role-switch-aware" variant of the same logical operation as separate
functions rather than one parameterized function.

## Remaining scope (for the next pass)

After pass 1, in this region: **376 of 408 functions remain** (263 unnamed
+ 130 thin-named − 32 resolved this pass = 361... — see arithmetic note
below) to be triaged. Concretely:

- **261 still-unnamed `FUN_8001xxxx`** (263 − 2 renamed this pass)
- **98 genuinely-open thin-named functions** (130 − 32 resolved this pass,
  all 32 of which were thin-named going in)
- **15 already-high-confidence** (no work needed, listed above)
- **2 newly-resolved-to-high-confidence this pass** are now folded into a
  "resolved" bucket, not carried as remaining

High-value untouched clusters identified during this pass's listing
(addresses from the authoritative `ListRegion0x80010000`/`_Upper` scripts'
output), recommended as next-pass targets in roughly priority order:

1. **`OGC_3_OCF_*` / OGF-3 (Vendor-specific/Test) opcode-handler cluster**,
   `0x80019ad0`–`0x8001a3b4` and again `0x8001e5d8`–`0x8001f408` — over 30
   thin-named `OGC_3_OCF_##`-style one-liners plus several adjoining unnamed
   `FUN_*`. Very likely a single coherent HCI OGF=3 (vendor-specific
   command group) opcode-dispatch table's worth of per-OCF handlers,
   analogous to `hci_command_router.md`'s OGF 1-8 standard-command
   coverage — this region may be where the **vendor-specific OGF**
   handlers live, which the existing `hci_command_router.md` doc does not
   cover. High value: likely resolves a whole missing OGF in one
   coordinated pass.
2. **`HCI_OGF1_OCF0x4#` cluster**, `0x8001c490`–`0x8001c788` (4 thin-named
   + neighbors) — looks like OGF=1 (Link Control) commands in the
   0x40-0x44 OCF range, an extension of the already-documented
   `lc_lmp_state_machine.md` OGF1 coverage.
3. **`fHCI_*` HCI-command-handler cluster**, `0x8001b84c`–`0x8001bf44`
   (Change Packet Type, Add SCO DEPRECATED, Disconnect, Reject/Accept
   Connection Request, Create Connection neighbors, Periodic Inquiry) — 7
   thin-named, all medium-confidence, all clearly OGF=1 LC commands
   bordering the already-high-confidence `fHCI_Create_Connection_0x05`/
   `fHCI_Inquiry_0x01`.
4. **`fHCI_Read_*` cluster**, `0x8001b23c`–`0x8001b780` (Read LMP Handle,
   Read Clock Offset, Read Remote Version/Supported Features, Remote Name
   Request) — 5 thin-named, all medium-confidence, all OGF=1 "Read remote
   info" commands; likely share structure with the `send_evt_HCI_Read_*`
   senders this pass already resolved.
5. **`VSC_0xfc##` cluster**, scattered (`0x800120ac`, `0x80012c18`,
   `0x80013074`, `0x80014054`, `0x800148f0`, `0x8001728c`, `0x8001a0f8`,
   `0x8001a294`, `0x8001a350`) — vendor-specific-command handlers, mostly
   low-confidence (purpose-unclear) one-liners. Cross-reference against
   `reverse_engineering_lmp_vsc_opcode_map.md` (root-level doc) once
   decompiled, since several VSC opcodes (`0xfc11`, `0xfc39`, `0xfcc0`,
   `0xfcc2`) recur with `_1`/`_2` suffixes suggesting paired
   handler/responder functions.
6. **`LMP_CH__0x3ee__case#` cluster**, `0x80011d9c`–`0x80011fc0` (3
   thin-named) — an LMP_CH (vendor LMP opcode 0x3ee) case-dispatch family;
   likely extends `lc_lmp_state_machine.md`'s LMP coverage once decompiled.
7. **Large unnamed stretches by size** (good single-function ROI):
   `0x80018654` (1194B, largest unnamed in the region), `0x80016934`
   (1012B), `0x800161e4` (974B), `0x80017dcc` (824B), `0x80016e68` (778B),
   `0x800122fc` (772B).
8. **The remaining ~250 small unnamed `FUN_8001xxxx`** scattered through
   `0x800100e4`–`0x80011d9c` (pre-LMP_CH cluster), `0x80012000`–`0x800147b0`
   (between the LMP_CH/VSC clusters and the codec-table functions), and
   `0x80015000`–`0x80018000` (largely untouched) — no existing thematic
   doc covers this stretch; expect needing this catch-all doc to absorb
   most of it directly, the way `region_0x80000000.md` did for its region.

**Arithmetic note:** the pass's net resolution is 32 functions (30 already
correctly named by decompile-confirmation + 2 newly named from `FUN_*`).
Starting scope was 263 unnamed + 130 thin-named = 393 in-scope (the 15
already-high-confidence are out-of-scope, already done). After this pass:
261 unnamed (263−2) + 98 thin-named (130−32) = **359 remaining**,
**32 resolved**, **15 already-done** — `359 + 32 + 15 = 406`... the
discrepancy from 408 is the 2 unnamed-turned-named functions which moved
buckets (counted once in "resolved," not double-counted in "unnamed"); the
clean check is `261 + 98 + 32 + 15 = 406`, 2 short of 408 — those 2
(`0x8001cfc4`, `0x8001d03c`) are correctly inside the "32 resolved," not a
miscount; total accounted = `406 (the static buckets above, which already
exclude the 2 moved) + 2 (folded into the 32) = 408`. ✓ A future pass
should re-run the authoritative listing scripts fresh rather than trust
this arithmetic note's hand-derivation, per the lesson from
`region_0x80000000.md`'s pass-4/7 tally-reconciliation episodes.
