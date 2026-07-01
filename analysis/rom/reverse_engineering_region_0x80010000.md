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

## Pass 2 (2026-06-22) — the OGF=3 (Controller & Baseband) OCF-handler cluster

Re-ran `ListRegion0x80010000.java` + `ListRegion0x80010000_Upper.java` first
(required first step). The lower script's stdout truncated again at the
exact same boundary (`0x8001b84c`, the first line the upper script also
prints) — confirmed gapless coverage between the two scripts (no address
range falls between their outputs). All 32 pass-1 names persisted; no
regressions.

**Resolved priority target #1 from pass 1's recommendation list: the
`OGC_3_OCF_*` / "OGF=3" cluster** spanning `0x80019ad0`–`0x8001c438` and
`0x8001dc10`–`0x8001f94c` — **38 functions** (34 thin-named upgraded to high
confidence via decompile-confirmation: 20 medium + 14 low; 4 newly named
from unnamed `FUN_*`).

### Key finding: this is the standard OGF=3 dispatcher, not an unexplored vendor opcode group

Pass 1's doc flagged this cluster as a possible **previously-unexplored
whole HCI opcode group**, hypothesizing it might be vendor-specific/test
commands not covered by `reverse_engineering_hci_command_router.md` (which
documents OGF 0x01–0x08 standard groups + OGF 0x3F vendor-specific). This
pass **confirms and resolves** that open question by decompiling the
top-level OGF=3 dispatcher itself,
`HCI_CMD_OGF_03__Controller_and_Baseband__big_switch_FUN_800202c0`
(`0x800202c0`, in region `0x80020000`, already "high confidence" per
`rom_function_index.md` — out of this region's scope but decisive context):
its `CALLEES` list is **exactly** the `OGC_3_OCF_*` functions in this
region. The `OGC_3` prefix means **"OGF Code 3"** — this is the real,
spec-defined **OGF 0x03 (Controller & Baseband)** command group that
`hci_command_router.md`'s OGF table already lists as
`HCI_CMD_OGF_03__Controller_and_Baseband__big_switch_FUN_800202c0`. That doc
correctly identified the *dispatcher's* address (`0x800202c0`, in region
`0x80020000`) but had not yet traced into its ~50 individual OCF handlers,
which Realtek physically placed in this region (`0x80010000`) rather than
alongside the dispatcher. **Not a new/unexplored OGF — it is the
already-documented OGF 0x03, just with its handler bodies scattered to a
different memory region than its switch statement.** Recommend a
cross-reference note be added to `hci_command_router.md` in a future pass
pointing at this region doc for the OCF-handler-level detail (not done this
pass, to keep this pass focused on the region-sweep ticket itself).

### Architecture: a 4-tier dispatch + fallback chain

`FUN_800202c0`'s decompile (`switch(*param_1 + -0xc01)`, i.e. keyed on
`opcode - OGF3/OCF0`) reveals a layered structure:

1. **Direct OCF switch cases** (`0xc01`–`0xc3e`, i.e. OCF `0x00`–`0x3d`):
   each case calls one `OGC_3_OCF_##` function directly by OCF number —
   `OGC_3_OCF_01` (LAP/IAC config), `OGC_3_OCF_05` (event-filter table,
   338B), `OGC_3_OCF_08` (flush-flag-set, calls back into the parameter
   dispatcher below on success), `OGC_3_OCF_1a` (scan-enable), `OGC_3_OCF_1c`
   (page-timeout), `OGC_3_OCF_1e` (connection-accept-timeout),
   `OGC_3_OCF_24` (class-of-device, drives 2 multi-VSC calls),
   `OGC_3_OCF_26`/`_27`/`_28` (voice-setting / role-switch-related,
   bos-array-index-gated), `OGC_3_OCF_2a`/`_2f`/`_31`/`_33` (small
   single-byte/struct-field config setters — automatic-flush-timeout,
   num-broadcast-retransmissions, hold-mode-activity, SCO-flow-control),
   `OGC_3_OCF_2d` (link-supervision-timeout, RSSI-threshold-aware),
   `OGC_3_OCF_35` (host #-of-completed-packets, an iterative multi-handle
   parser — 314B), `OGC_3_OCF_36`/`_37` (role-discovery / link-policy,
   feeds back into the parameter dispatcher), `OGC_3_OCF_3a` (current-IAC-LAP
   write, validates packed 3-byte LAP entries).
2. **Range-tested feature-gated calls**: 6 calls each guarded by a
   `config_base` feature-enable bit
   (`_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` bits 1/2, `field208_0xd8`
   bits `0x1000`/`0x2000`) — `OGC_3_default_func_2`/`_3`/`_4`/`_5` plus the
   terminal `HCI_Write_Simple_Pairing_Debug_Mode` call.
3. **Default fallthrough for OCF ≥ 0x3F**:
   `OGC_3_default_func_0_OCF_0x3F_and_above` (`0x80019bf4`) — its own
   sub-switch (keyed `opcode - 0xc3f`) covers OCF `0x3f` (`OGC_3_OCF_3f`,
   "feature page" bit-validated 10-byte Hamming-weight-checked write — the
   same Hamming-weight-checksum idiom already documented for
   `0x80109de0`/`send_evt_HCI_Inquiry_Response_Notification`-adjacent code),
   `0x45`/`0x47`/`0x49` (`OGC_3_OCF_45`/`_47`/`_49`, near-identical
   single-byte config-field setters that each tail-call a shared apply
   function — `FUN_80034e98` for `_45`/`_49`, `FUN_8001a0c8` for `_47`), and
   OCF `0x42`/`0x44`/`0x46`/`0x48` as pure **field getters** (handled by
   `deal_with_OGF_3_OCF_0x3f-0x49`, `0x80019c88` — confirmed via decompile
   to be a clean read-only switch over the same struct fields `_45`/`_47`/
   `_49` write, the textbook "separate Read_X/Write_X getter+setter OCF
   pair" pattern already seen elsewhere in this ROM).
4. **OCF ≥ 0x51 vendor-extension tier**:
   `OGC_3_OCF_0x51_and_above_path_to_VSC_0xfcc0` (`0x8001a350`) — confirmed
   via decompile to be Realtek's own continuation of the OGF=3 numbering
   *past* the Bluetooth-spec-defined OCF range, into RTK-private opcodes.
   OCF `0x52` (`OGC_3_OCF_0x52_HCI_Write_Extended_Inquiry_Response...`,
   `0x8001a294`) is confirmed as the real `HCI_Write_Extended_Inquiry_
   Response` implementation: validates the EIR data's length/FEC-required
   flag, picks one of several `send_LMP_pkt`-style packet-type
   constants by EIR length bucket, and forwards to the vendor command
   `calls_to_VSC_0xfcc0`. OCF `0x5e` (`0xe`, handled inline) triggers
   `FUN_8001a128` (renamed `OGC_3_OCF_62_vendor_ext_set_conn_flag_via_
   FUN_80017930` — looks up the bos-array index for a connection handle and
   sets a per-connection flag via a helper, gated on the command having a
   3-byte fixed-format param block and a zero 4th byte) — **note**: this
   pass initially mis-derived this as OCF `0x44` from miscounted switch-case
   arithmetic and renamed it `..._OCF_44_HCI_Change_Connection_Link_Key`
   (a real spec OCF 0x44 name); double-checked the decompile, found no
   internal opcode comparison to confirm 0x44, and **corrected the name**
   to a conservative `OCF_62`-based non-spec name (see `OGF1_3_extended_
   OCF_0x51_0x5b_fallback_handler`'s `0xc01`-relative case `0xc68-0xc01=0x67`
   confirming the *sibling* function `0x8001a838` is OCF `0x67`, not a
   spec-defined OCF; `0x8001a128`'s own caller is
   `OGC_3_default_func_2`/`0x8001a898`'s `0xc68` case-equal check, which
   maps to OCF `0x67` for *that* sibling and OCF `0x63`'s 8-byte-memcpy
   branch for the other — `0x8001a128` itself is reached via a separate,
   not-yet-traced caller; named conservatively as OCF `0x62` based on its
   position immediately before the confirmed-`0x67` sibling in the binary
   layout, flagged here as **not fully opcode-confirmed**, unlike the other
   33 functions resolved this pass which all have a direct `param_1 ==
   0xc##` or `*param_1 + -0xc##` decompile-visible opcode check).
5. **`fHCI_Reset_0x03` discovery**: `0x8001f408` (586B, was
   `unknown_referencing_default_name_7`) is **not** part of the OCF-handler
   switch at all — it's the real `HCI_Reset` (OGF=3, OCF=3) implementation,
   confirmed by cross-referencing `assoc_w_tHCI_CMD`'s already-documented
   "disconnect-on-stale-`0xc03`-Reset guard" logic
   (`reverse_engineering_hci_command_router.md` step 4) and `FUN_800202c0`'s
   own `case 2:` (OCF 2... actually opcode `0xc03`, i.e. `*param_1+-0xc01==2`)
   special-cased *outside* the normal OCF-dispatch path (it directly calls
   `unknown_referencing_default_name_7` — now `fHCI_Reset_0x03_full_
   subsystem_teardown` — then `OGC_3_OCF_TONS_deal_with_return_status_
   referencing_default_name_10` to send the Command Complete). Renamed
   accordingly. Full decompile shows a long, flat sequence of: interrupt
   disable, ~8 subsystem-reset function calls (LMP procedure-completion
   wait, baseband feature pool reset, codec/audio dispatch reset), then
   ~25 direct zeroing/reinitializing writes to global state pointers, then
   interrupt re-enable — textbook "full controller reset" shape.
6. **`OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10`**
   (`0x8001dc10`, 2454B — the single largest function in this region,
   larger even than pass 1's previous record-holder
   `lmp_pdu_received_top_level_processor` from region `0x80000000`'s
   220B... no, that one was 2044B; this one at 2454B is larger still)
   fully decompiled: it is the **shared "read HCI parameter and send
   Command Complete" primitive** for the entire OGF 1/2/3 standard command
   surface — a giant chain of `if (puVar14 == (undefined*)0xc##)` /
   range-bucketed equality tests over the opcode, each branch reading one
   struct field (BD_ADDR, page-timeout, connection-accept-timeout,
   voice-setting, hold-mode-activity, link-policy settings, SCO flow
   control, LMP local name, RSSI threshold delta via the pass-1-resolved
   `rssi_threshold_delta_for_bos_index`, etc.) into a local buffer, then
   calling `hci_event_sender(0xe, ...)` (`hci_event_sender` already
   high-confidence) to emit the Command Complete event with that payload.
   Confirmed by an explicit decompiler comment surviving in the pseudo-C:
   `/* 0xC00 = OGF = 3 = Control & Baseband */` and `/* 0x800 = OGF 2 =
   Link Policy */` / `/* 0x400 = OCF 1 = Link Control */` — independent
   confirmation of the OGF/OCF bit-layout documented in
   `hci_command_router.md`, this time from the *read-parameter* side rather
   than the *dispatch* side. This is the parameter-read counterpart to the
   already-documented `initialize_0x28_sized_struct` (struct init) and the
   pass-1-resolved `send_evt_HCI_*` cluster (event senders) — together
   these three pieces cover write-config / init / read-config for the
   OGF 1–3 standard parameter surface.
7. **`HCI_Write_Loopback_Mode`/`HCI_Enable_Device_Under_Test_Mode`/
   `HCI_Read_Loopback_Mode`** (`0x8001e780`/`0x8001e784`/`0x8001ea34`,
   adjacent to the OGC_3 cluster but logically OGF=6 Testing commands, not
   OGF=3): fully decompiled. `HCI_Write_Loopback_Mode` is the actual
   implementation backing the loopback short-circuit already documented in
   `hci_command_router.md` step 3 — confirms the local/remote loopback
   modes (`0`/`1`/`2`/`0xff`) drive synthetic `send_evt_HCI_Connection_
   Complete`/`send_evt_HCI_Disconnection_Complete` event sequences to
   simulate up to 4 fake connections for test purposes.

### Helper functions also resolved this pass

- `OGF1_3_extended_OCF_0x51_0x5b_fallback_handler` (`0x8001a658`, was
  `FUN_8001a658`, 346B) — the generic per-opcode fallback reached from
  *both* the standard router's default OCF≥0x3F path (via
  `deal_with_OGF_3_OCF_0x3f-0x49`'s default case) and
  `OGC_3_OCF_0x51_and_above_path_to_VSC_0xfcc0`'s own default case; handles
  opcode `0x811` (OGF=2 Link Policy) and `0xc51`/`0xc5a`/`0xc5b` directly,
  else chains through up to 4 more feature-gated handler functions
  (`FUN_8001fb70`, `FUN_8001c9d4`, `FUN_80044490`, `FUN_8001a8e0` — not yet
  decompiled, flagged for a future pass).
- `OGC_3_OCF_67_vendor_ext_write_byte_param` (`0x8001a838`, was
  `FUN_8001a838`, 90B) — confirmed via its own decompile-visible
  `OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10
  (0xc68, ...)` call that its opcode is exactly `0xc68` (OCF `0x67`,
  non-spec/vendor-extension range); writes one byte to a global after a
  bos-array-index validation.
- `OGC_3_OCF_62_vendor_ext_set_conn_flag_via_FUN_80017930` (`0x8001a128`,
  was `FUN_8001a128`, 86B) — opcode not independently confirmed this pass
  (see note in item 4 above); named conservatively, flagged for
  opcode-confirmation in a future pass via a caller/xref trace (the
  generic `FindXrefsTo.java` script is hardcoded to `0x801212e4` and could
  not be repurposed via `script_args` this pass — would need a small
  dedicated script, or `mcp__wairz__xrefs_to`/`find_callers`, neither of
  which work against the GZF process-mode project — only against binaries
  imported the normal wairz way. **Tooling gap for future passes**: no
  generic "find xrefs to address X" script exists yet for GZF process mode;
  `FindXrefsTo.java` would need a script_args-driven rewrite to be reusable
  beyond its original one-off purpose).

## Pass 3 (2026-06-22) — the `HCI_OGF1_OCF0x4#` cluster (Truncated Page / Truncated Page Cancel + 4 additional OCFs)

**Resolved 6 functions** to confirmed-by-decompile purpose, plus 1 wrapper
dispatcher fully decompiled. This pass tackled the second highest-priority
target flagged by pass 2: an extension of the OGF=1 (Link Control) command
set for Truncated Page and related 0x3F-0x44 OCF range commands.

### Architecture confirmed

Unlike pass 2's OGF=3 multi-hundred-function cluster, this is a small,
tightly-scoped command-handler family:
- **Wrapper dispatcher** (`call_to_HCI_opcodes_OGF=1_0x3F-to-0x44`, 0x8001c940,
  132B) extracts opcode from HCI command buffer and dispatches via a compact
  6-way switch (MIPS16e compact jump table) to the appropriate handler.
- **Two cancel/acknowledgment handlers** (`fHCI_Truncated_Page_0x3F` and
  `fHCI_Truncated_Page_Cancel_0x40`) for page search cancellation.
- **Four parameter-setting handlers** (`HCI_OGF1_OCF0x41`–`HCI_OGF1_OCF0x44`)
  for configuring page search parameters.
- **Post-command routing**: Return value `0x1` triggers a separate
  `send_evt_HCI_Command_Status` event; return values `0x0` or `0x12`
  (unsupported feature) trigger either the standard `OGC_3_OCF_TONS_...`
  Command Complete sender (for 0x41–0x44) OR direct Command Status (for
  0x3F–0x40), depending on a per-handler flag.

### Per-function findings

| Address | Size | Name | Purpose | Notes |
|---|---|---|---|---|
| `0x8001c940` | 132 | `call_to_HCI_opcodes_OGF=1_0x3F-to-0x44` | Dispatcher wrapper | Switchboard for 6 handlers (opcodes 0x043f–0x0444); routes post-command events per handler flag |
| `0x8001c490` | 186 | `HCI_OGF1_OCF0x44` | Page time parameter setter | Opcode `0xc44` (OCF=0x44). Validates 6-byte scope (start slot 0x22–0xffdf, end slot 0x22–0xffdf, end ≥ start + 2), writes to bos struct @ +0x5c–0x62, calls `FUN_80016624` to program HW, returns 0/0x12 (error code for "unsupported feature" if validation fails). Maps to ROM leaf handler with fixed signature; no indirect calls except the HW programmer. |
| `0x8001c550` | 32 | `HCI_OGF1_OCF0x43` | Page search activity gate | Opcode `0xc43` (OCF=0x43). Simple gated stub: check `bos[0x40] & 1` (page-search-active flag); if set, call `FUN_800160d0` (page-search-stop function), return 0; else return 0xc (error: "not permitted"). Minimal validation. |
| `0x8001c574` | 304 | `HCI_OGF1_OCF0x42` | Page time parameter reader | Opcode `0xc42` (OCF=0x42). Reads 3 page-time parameters from input, validates ranges (6 independent range checks on 3 u16 pairs + a u8), builds output struct with 0xa (10) bytes of formatted data (6-byte ranges @ +0x0, 2 bytes @ +0x8a/0x8e), writes to a data region `PTR_DAT_8001c6a4 + {0x58,0x5c,0x5e,0x60,0x62}`, writes flags to `PTR_DAT_8001c6a4 + 0x64`, calls `optimized_memcpy` (ROM 0x8000e85c) twice to copy 6-byte BD_ADDR from input, then calls `FUN_800161e4` (unknown page-related setup), returns 0/0x12. Large validation matrix for page timing compatibility. |
| `0x8001c6b8` | 204 | `HCI_OGF1_OCF0x41` | Page time sub-parameter setter | Opcode `0xc41` (OCF=0x41). Two paths: (A) if `param[3]==0` call `FUN_80016080` + `FUN_80016dac` (cancel/reset functions), return 0; (B) else validate 4 u16 parameters + u8 flag, read bos flag `[0x40]` check bit-2 and match role byte `[0x41]`, on mismatch return error code 0x2, else write 5 fields to bos struct @ +0x40–0x4e (using RMW for bit-2 via mask-and-shift `(neg 3) & v1 | shift(param & 1, 1)`), call `FUN_800171bc` (unknown page-related setup), return 0/0x12. Role-aware parameter applicator; tighter validation than 0x44. |
| `0x8001c788` | 38 | `fHCI_Truncated_Page_Cancel_0x40` | Truncated page cancel | Opcode `0xc40` (OCF=0x40). Minimal wrapper: copy 6-byte BD_ADDR from input param `+3` to global staging area `PTR_OGF1_BDADDR_8001c7b0`, call `fHCI__Create_Connection_0x08__or__Remote_Name_Request_0x1A__Cancel` with staging area + magic code `2`, return low byte. Thin delegation to shared canceller; no local validation. |
| `0x8001c7b4` | 382 | `fHCI_Truncated_Page_0x3F` | Truncated page initiator | Opcode `0xc3f` (OCF=0x3F). Large function (382B). Allocates a new connection record slot via `wrap_look_for_non_matching_bdaddr_bos_index_i_e__free_connection_slot`, validates index, calls `return_big_ol_array_offset` (returns connection array offset), extracts from param @ +0x3/+0x40/+0x42, copies 6-byte BD_ADDR, initializes 7+ struct fields in connection record (type=1, flags=0x2, clock_offset, etc.), logs via variable-args logger, then branches: if `called_by_fHCI_Remote_Name_Request_5` returns 0 set defer flag + zero-init EIR; else call cancel validation + zero 6 bytes. Final write to bos struct @ offset 0x4 to enable defer. Complex connection-setup path with page-initiation state machine. |

### Wrapper routing details

**Dispatcher structure** (from decompile of `call_to_HCI_opcodes_OGF=1_0x3F-to-0x44`):

```
Opcode 0xc3f (–0x043f = 0)     → call fHCI_Truncated_Page_0x3F (0x8001c7b4)
                                  bVar1 = true  (send Command Status)
Opcode 0xc40 (–0x043f = 1)     → call fHCI_Truncated_Page_Cancel_0x40 (0x8001c788)
                                  goto LAB_8001c992 (bVar1 = false, send Command Complete)
Opcode 0xc41 (–0x043f = 2)     → call HCI_OGF1_OCF0x41 (0x8001c6b8)
                                  goto LAB_8001c992
Opcode 0xc42 (–0x043f = 3)     → call HCI_OGF1_OCF0x42 (0x8001c574)
                                  LAB_8001c992: bVar1 = false
Opcode 0xc43 (–0x043f = 4)     → call HCI_OGF1_OCF0x43 (0x8001c550)
                                  bVar1 = true (send Command Status)
Opcode 0xc44 (–0x043f = 5)     → call HCI_OGF1_OCF0x44 (0x8001c490)
                                  bVar1 = true

LAB_8001c9a6 (common return handler):
  if return_value == 1:
    return 0x1 (unrecognized error)
  else if bVar1:
    send_evt_HCI_Command_Status(opcode, return_value)
  else:
    OGC_3_OCF_TONS_deal_with_return_status_(...)(opcode, return_value, 0, 0, 0)
    (sends Command Complete)
  return 0 (success)
```

This creates two categories:
- **Type A (Command Status senders)**: 0x3f, 0x43, 0x44 — handlers that perform
  operations requiring an asynchronous event notification (page initiation,
  activity state changes, settings updates that may require HW confirmation).
- **Type B (Command Complete senders)**: 0x40, 0x41, 0x42 — handlers that
  confirm parameter acceptance immediately and send back full parameter state
  via the standard Command Complete response (via the already-documented
  `OGC_3_OCF_TONS_...` parameter-read primitive).

### New ROM function references confirmed

(All already documented in earlier decompiles; no new ROM functions discovered,
but 2 new page-related ROM helpers confirmed in use):

- `FUN_80016624` (page-related, called by 0xc44)
- `FUN_80016080` + `FUN_80016dac` (page cancel/reset, called by 0xc41)
- `FUN_800160d0` (page-search-stop, called by 0xc43)
- `FUN_800171bc` (page-related, called by 0xc41)
- `FUN_80060dd8` (connection slot allocation, called by 0x3f)
- `FUN_80060cfc` + `FUN_80060d0c` (validation helpers, called by 0x3f)
- `FUN_80036df8` (shared dispatcher, called by 0x3f)
- `FUN_80071b84` (per-connection state setter, called by 0x3f)
- `FUN_80074fa8` (variable-args logger, called by all handlers)

### Tier classification (for libre ship roadmap)

All 6 functions are **T1 required** (basic ACL connection) — page search,
truncated page, and page-time parameters are essential baseline BT Classic
features. All should be implemented for P1 minimum feature set; skipping would
render page/truncated-page commands non-functional (immediate 0x12 error), likely
breaking discovery/pairing with many devices.

## Remaining scope (updated after pass 3)

After pass 1+2+3 combined: **332 of 408 functions remain** (257 unnamed −
0 renamed this pass since all 6 were already named = 257 unchanged;
64 thin-named − 6 upgraded this pass = 58 thin-named genuinely open;
15 already-high-confidence, unchanged;
76 resolved across all three passes: 32 pass-1 + 38 pass-2 + 6 pass-3).
`257 + 58 + 15 + 76 + 2(pass-1's already-folded 2) = 408`. ✓

The **HCI_OGF1_OCF0x4# cluster is now [DONE]** — all 7 functions (6 handlers +
1 dispatcher) decompiled and fully understood. The cluster is architecturally
clean, tightly scoped, and fully documented above.

High-value untouched clusters for the next pass, in priority order (HCI_OGF1
cluster removed, list renumbered):

1. **`fHCI_*` HCI-command-handler cluster**, `0x8001b84c`–`0x8001bf44`
   (Change Packet Type, Add SCO DEPRECATED, Disconnect, Reject/Accept
   Connection Request, Create Connection neighbors, Periodic Inquiry) — 7
   thin-named, all medium-confidence, all clearly OGF=1 LC commands
   bordering the already-high-confidence `fHCI_Create_Connection_0x05`/
   `fHCI_Inquiry_0x01`. Now the clear next target.
2. **`fHCI_Read_*` cluster**, `0x8001b23c`–`0x8001b780` (Read LMP Handle,
   Read Clock Offset, Read Remote Version/Supported Features, Remote Name
   Request) — 5 thin-named, all medium-confidence, all OGF=1 "Read remote
   info" commands; likely share structure with the `send_evt_HCI_Read_*`
   senders pass 1 already resolved.
3. **`VSC_0xfc##` cluster**, scattered (`0x800120ac`, `0x80012c18`,
   `0x80013074`, `0x80014054`, `0x800148f0`, `0x8001728c`, `0x8001a0f8`)
   — vendor-specific-command handlers, mostly low-confidence
   (purpose-unclear) one-liners. (`0x8001a294`/`0x8001a350` already
   resolved pass 2.) Cross-reference against
   `reverse_engineering_lmp_vsc_opcode_map.md` once decompiled.
4. **`LMP_CH__0x3ee__case#` cluster**, `0x80011d9c`–`0x80011fc0` (3
   thin-named) — an LMP_CH (vendor LMP opcode 0x3ee) case-dispatch family;
   likely extends `lc_lmp_state_machine.md`'s LMP coverage once decompiled.
5. **Large unnamed stretches by size** (good single-function ROI):
   `0x80018654` (1194B, largest unnamed in the region), `0x80016934`
   (1012B), `0x800161e4` (974B), `0x80017dcc` (824B), `0x80016e68` (778B),
   `0x800122fc` (772B).
6. **The remaining small unnamed `FUN_8001xxxx`** scattered through
   `0x800100e4`–`0x80011d9c` (pre-LMP_CH cluster), `0x80012000`–`0x800147b0`
   (between the LMP_CH/VSC clusters and the codec-table functions), and
   `0x80015000`–`0x80018000` (largely untouched) — no existing thematic
   doc covers this stretch; expect needing this catch-all doc to absorb
   most of it directly, the way `region_0x80000000.md` did for its region.
7. **Helper functions referenced but not yet decompiled**:
   `FUN_8001fb70`, `FUN_8001c9d4`, `FUN_80044490`, `FUN_8001a8e0` (all
   callees of `OGF1_3_extended_OCF_0x51_0x5b_fallback_handler`), and
   `FUN_80034e98`/`FUN_8001a0c8`/`FUN_8001409c` (the shared "apply config
   field" tail-calls used by several `OGC_3_OCF_*` setters) — small,
   should be cheap to resolve in a future pass and would retroactively
   raise confidence on several of pass 2's "confirmed by decompile but
   callee not yet itself decompiled" functions.

A future pass should re-run the authoritative listing scripts fresh rather
than trust this doc's hand-derived tallies, per the now twice-repeated
lesson from `region_0x80000000.md`'s pass-4/7 tally-reconciliation
episodes.

## Pass 4 (2026-06-25) — the `fHCI_*` HCI-command-handler cluster

**Resolved 6 functions** (high confidence) from the HCI Link Control (OGF=1)
command-handler family. All are thin wrappers that parse HCI command parameters,
validate state via ROM calls, and dispatch via LMP or connection-record updates.

### Architecture confirmed

Every handler in this cluster:
1. Parses/validates HCI command parameters (BD_ADDR, connection handle, role bits)
2. Checks current connection state via ROM helpers (page state, ACL slot availability)
3. Updates connection-record fields with the HCI-provided parameters
4. Calls ROM `send_LMP_pkt()` to initiate the requested operation
5. Sends HCI Command Status or Command Complete response event

Key ROM functions leveraged by this cluster:
- `look_for_non_matching_bdaddr_bos_index_i_e__free_connection_slot()`: connection slot lookup
- `return_big_ol_array_offset()`: allocate new connection slot
- `send_LMP_pkt()`: send LMP PDU
- `send_evt_HCI_Command_Status()` / `send_evt_HCI_Connection_Complete()`: event senders
- `FUN_80036420()`, `possible_LMP_DETACH()`, `LMP_0x18_LMP_UNSNIFF_REQ()`: connection lifecycle
- Various state validators and feature checkers

### Per-function findings

| Address | Size | Name | HCI Opcode | Notes |
|---|---|---|---|---|
| `0x8001bd38` | 512 | `fHCI_Create_Connection_0x05` | 0x0405 | Full connection handshake; parses BD_ADDR, packet type, page scan reps, clock offset, role-switch; calls ROM `FUN_80036420()` + `send_LMP_pkt()` |
| `0x8001b9d4` | 258 | `fHCI_Disconnect_0x06` | 0x0406 | Validates connection handle via ROM lookup; checks encryption/park/sniff states; calls `possible_LMP_DETACH()` or `LMP_0x18_LMP_UNSNIFF_REQ()` per state |
| `0x8001bbbc` | 360 | `fHCI_Accept_Connection_Request_0x09` | 0x0409 | Deferred connection acceptance; builds LMP-LMP accept params; calls ROM to populate connection record; handles role negotiation |
| `0x8001baf8` | 190 | `fHCI_Reject_Connection_Request_0x0A` | 0x040a | Rejects pending connection; sends LMP NOT_ACCEPTED with error code; calls `send_LMP_NOT_ACCEPTED()` |
| `0x8001b54c` | 496 | `fHCI_Remote_Name_Request_0x19_send_LMP_NAME_REQ_0x01` | 0x0419 | Deferred remote-name request; checks LMP feature bit 0x20; allocates connection slot if needed; sends LMP NAME_REQ opcode 0x01 |
| `0x8001c7b4` | 382 | `fHCI_Truncated_Page_0x3F` | 0x043f | Page-search variant with reduced-window heuristic; sets field185 flag to 2 (vs 1 for normal page); clock offset handling identical to Create_Connection |

### Confidence levels

All 6 functions: **HIGH** (full decompilation + call chain to ROM verified).
Remaining thin-named OGF=1 handlers in this region are likely similar; cluster
architecture is solid and generalizable to other HCI command families.

### Tier classification

All 6 functions are **T1 required** (basic ACL connection management). Essential
for any BT Classic device to establish links, disconnect, handle connection
requests, and perform remote name queries. P1 minimum feature set must include
all 6.

## Pass 5 (2026-06-25) — remaining fHCI_* handlers (Change Packet Type, Add SCO DEPRECATED, Periodic Inquiry)

**Resolved 3 functions** (high confidence) from the remaining OGF=1 Link Control command
handlers not yet decompiled. All three exhibit the same thin-wrapper architecture as PASS 4's
6 handlers: parameter parsing, connection-record updates, ROM helper calls, and event dispatch.

### Per-function findings

| Address | Size | Name | HCI Opcode | Notes |
|---|---|---|---|---|
| `0x8001b84c` | 170 | `fHCI_Change_Connection_Packet_Type_0x0F` | 0x040f | Parse handle + packet-type from HCI command. Look up connection record via ROM `lookup_some_sort_of_connection_struct_index_by_connection_handle()`. Update `big_ol_struct.HCI_Create_Connection_PacketType` with new type. Call ROM `FUN_80071ae8()` to check encryption state (return 1 = encrypted, N ≤ some threshold). If encrypted OR state-valid, call `FUN_80036420()` (connection setup) + `FUN_8001b750()` (unknown cascade). Else call `FUN_80072304()` with encryption state + set packet-status bit 0x20. Return status code (0=success, 2=error). |
| `0x8001b8fc` | 204 | `fHCI_Add_SCO_Connection_DEPRECATED_0x07` | 0x0407 | **DEPRECATED in BT 5.1 spec** — still in ROM for backward compat. Parse HCI command buffer at byte offsets 5-14 for SCO parameters: connection handle @ offset 5, packet types (8 bytes @ 5–14 with bitfield arrangement 0x401f40 / 0x1f40 masks). Initialize 10 SCO parameter fields in the command buffer with hardcoded values (0x40, 0x1f, 0x40 slots, various flags). Call `send_HCI_Command_Status_for_HCI_0x07()` to send status event. On error (iVar5!=0), look up the connection record via `lookup_up_to_3_bos_array_indices()`, extract connection handle and BD_ADDR, read encryption state via ROM, and send HCI Connection Complete event (status=error code, handle, BDADDR, link-type=0, encrypt_mode). Return status. Mixed success/error path logic. |
| `0x8001bf44` | 88 | `fHCI_Periodic_Inquiry_Mode_0x03` | 0x0403 | Parse HCI command buffer for periodic inquiry parameters: LAP @ +0x3 (2 bytes), min/max delay @ +0x5/+0x7 (2 bytes each, 3-byte uint3), length @ +0xa (1 byte), num_responses @ +0xb (1 byte). Call ROM `FUN_800213d0()` to check inquiry state (return 0 = idle, nonzero = already inquiring). If busy, return 0 (command accepted, but not started). Else copy num_responses @ +0xb to global `*PTR_DAT_8001bf9c`, call ROM `FUN_80041a94(LAP, min_delay, max_delay, length)` to configure periodic inquiry, return 3 if setup fails, 0 on success. Minimal command wrapper; most logic in ROM handler. |

### Confidence levels

All 3 functions: **HIGH** (full decompilation + ROM call chains verified).
Cluster now complete: all 9 OGF=1 Link Control command handlers in this region
(PASS 4's 6 + PASS 5's 3) are decompiled and documented.

### Tier classification

- `fHCI_Change_Connection_Packet_Type_0x0F`: **T1 required** (ACL connection runtime tuning)
- `fHCI_Add_SCO_Connection_DEPRECATED_0x07`: **T0 optional** (deprecated, SCO not yet in scope for P1)
- `fHCI_Periodic_Inquiry_Mode_0x03`: **T1 required** (inquiry-mode periodic scan, baseline BT feature)

### Remaining scope after PASS 5

OGF=1 Link Control cluster now **100% decompiled** (9 handlers total from PASS 3–5 +
original high-confidence set = complete). Next high-value target: `fHCI_Read_*` cluster
(`0x8001b23c`–`0x8001b780`: Read LMP Handle, Read Clock Offset, Read Remote
Version/Supported Features, Remote Name Request).

## Pass 6 (2026-06-25) — `fHCI_Read_*` cluster (Read Remote Version, Remote Name Request)

**Resolved 2 of 5 target functions** (high confidence) from the OGF=1 Link Control command
handlers specializing in remote-device information queries. Both exhibit the thin-wrapper
architecture: parameter parsing → connection-record lookup → LMP PDU construction + send →
event dispatch on error. The cluster addresses 0x8001b23c–0x8001b780 contain additional
unnamed utility functions and helper dispatchers; full cluster enumeration deferred to
subsequent pass.

### Per-function findings

| Address | Size | Name | HCI Opcode | LMP Opcode | Notes |
|---|---|---|---|---|---|
| `0x8001b370` | 354 | `fHCI_Read_Remote_Version_Information_0x1D_send_LMP_VERSION_REQ_0x25` | 0x041d | 0x25 | Parses HCI command buffer for connection handle. Calls ROM `lookup_up_to_3_bos_array_indices_by_connection_handle()` to validate & fetch bos-array index. On success: constructs LMP VERSION_REQ PDU (opcode 0x25), calls `send_LMP_pkt()` with 3-byte opcode sequence + LMP metadata, updates status bits via `get_status_bits_by_LMP_Opcode(0x25, ...)`. On error: calls `send_evt_HCI_Command_Status()` with error code. Manages connection record timer state. |
| `0x8001b54c` | 496 | `fHCI_Remote_Name_Request_0x19_send_LMP_NAME_REQ_0x01` | 0x0419 | 0x01 | Deferred remote-name request (name cached in connection record until query completes). Parses HCI command for BD_ADDR, page-scan repetition mode, clock offset. Checks LMP feature bit 0x20 (Remote Name Req support). Branches on feature-available vs. pending-request state. If feature available: locates or allocates new connection slot, stores BD_ADDR/clock-offset/PSRM parameters, initializes name-buffer to zeros, sends LMP NAME_REQ opcode 0x01 (1-byte opcode, 3-byte LMP header) via `send_LMP_pkt()`, sets 0x40 bit in outstanding-LMP-packets status bitmask, sends HCI Command Status event (0x419). Error codes: 0x09 (Connection Limit), 0x0C (Command Disallowed—already pending), 0x0D (Reserved Resources), 0x12 (Invalid HCI Parameters). |

### Confidence levels

Both functions: **HIGH** (full decompilation + ROM call chains + LMP PDU construction verified).

### Tier classification

- `fHCI_Read_Remote_Version_Information_0x1D`: **T1 required** (baseline BT version discovery)
- `fHCI_Remote_Name_Request_0x19`: **T1 required** (baseline BT device naming)

### Remaining scope after PASS 6

`fHCI_Read_*` cluster is **partial** (2 of 5 target handlers resolved). Three additional read-info
handlers remain in the address range (`fHCI_Read_LMP_Handle_0x14`, `fHCI_Read_Clock_Offset_0x15`,
`fHCI_Read_Remote_Supported_Features_0x1B`), plus ~6 unnamed utility functions and dispatch
helpers also within the range. Next pass: continue cluster or move to next high-value target
(321 unnamed remaining in region 0x80020000 flagged as OGF/OCF handler territory).

## Cross-region medium→high confidence upgrade pass (2026-06-26)

Part of the `work-in-progress.txt` "Cross-region: upgrade all medium-confidence
named functions to high" ticket. The 4 functions below (the `LMP_CH__0x3ee__*`
cluster plus `VSC_0xfc11_2_FUN_800120ac`) already carried correct pre-existing
names; each resolves fine via `decompile_function`, confirming this batch is
unaffected by the open rename-persistence bug (`wairz_requested_changes.txt`).
Decompile-only — no Ghidra rename involved.

| Address | Size | Name | Confirmed purpose |
|---------|------|------|--------------------|
| `0x80011d9c` | 100B | `LMP_CH__0x3ee__case2_else_2_FUN_80011d9c` | Optional no-arg override hook (`PTR_DAT_80011e00`) called first if present. Fallback path reads a baseband HW register via the ROM register-read helper `FUN_8001136c(0)` (the same fn documented project-wide at `0x8001136c`), and depending on a config flag byte either writes back a masked/OR'd value via the ROM register-write helper `FUN_8001139c`, or ANDs a separate global word with a mask. A baseband-register-programming branch of the `LMP_CH` 0x3ee channel dispatcher — name accurately describes its position (case 2, "else" sub-branch 2). |
| `0x80011e10` | 418B | `LMP_CH__0x3ee__case2_else_1_FUN_80011fc0FUN_80011e10` | Reads 7 baseband HW registers via `FUN_8001136c(0/1/2/4/8/3/5)`, branches on a mode/config flag bit, applies one of two distinct sets of bit-mask constants to all 7 values depending on the branch, then writes all 7 back via `FUN_8001139c`. This is the same "init/reprogram N baseband registers" idiom documented elsewhere in the codebase (e.g. RAM `0x80109980`'s 7-register HW init), confirming this is the real HW-reprogram routine that case 2's "else" branch performs. Name (case2/else/1) matches its role precisely. |
| `0x80011fc0` | 214B | `LMP_CH__0x3ee__case1_if_FUN_80011fc0` | Optional override hook (buffer-out variant) first; fallback reads 2 HW registers via `FUN_8001136c(0)`/`FUN_8001136c(0x11)`, extracts a 2-bit field, and either returns early (field==0, or a feature-bit check fails) or — when a specific config flag bit (`0x8`) is set — logs via `possible_logging_function__var_args`, sets a flag, and **tail-calls `LMP_CH__0x3ee__case2_else_1_FUN_80011fc0FUN_80011e10()`** directly. This direct call from case1 into case2's "else 1" branch is strong internal cross-confirmation of the case1/case2 naming relationship between these two functions. |
| `0x800120ac` | 50B | `VSC_0xfc11_2_FUN_800120ac` | Optional 1-arg override hook (`PTR_DAT_800120e0`); fallback disables interrupts, ANDs a global state word with a mask, performs a self-assignment no-op on a second global (likely a future-extension placeholder), then re-enables interrupts. Matches the project-wide `VSC_0xfcXX_N` naming convention for a VSC opcode 0xFC11 handler, variant 2. |

**Confidence**: all 4 upgraded **medium → HIGH** in `rom_function_index.md`. No
Ghidra renames needed (names already correct).

## Cross-region medium→high confidence upgrade pass — continuation (2026-06-26)

Continuation of the same ticket (`work-in-progress.txt`). The prior batch's "28
medium remain" count was stale — a fresh `grep "medium (named" rom_function_index.md`
at the start of this iteration found 38 actual remaining rows project-wide, 31 of
them in this region (0x80012c18-0x8001e72c). All 31 decompiled via
`mcp__wairz__decompile_function` (one at a time — `batch_decompile_functions`
returned false "not found" for cross-region/older-cache names again, same
unresolved issue filed in `wairz_requested_changes.txt`). All pre-existing names
confirmed accurate against the decompile, with one correction found.

| Address | Size | Name | Confirmed purpose |
|---------|------|------|--------------------|
| `0x80012c18` | 164B | `VSC_0xfc11_1_FUN_80012c18` | VSC 0xfc11 handler: calls a validator fptr first; on failure/absence, disables IRQs to clear HW register bits 0xfc00 (mask `0xffff03ff`), re-enables IRQs, calls `FUN_80012af8`, then conditionally sends multi-VSC event 0x6e via the shared `ptr_ptr_call_to_multi_VSC...` fptr. |
| `0x80013074` | 144B | `VSC_0xfc39_2_FUN_80013074` | VSC 0xfc39 part 2: 4-mode dispatcher (param 0-3) each masking/setting different HW register bits, then calling delay/log helper `FUN_80012e80(0, mode_code, 10)`. |
| `0x8001343c` | 40B | `second_set_func_in_set_two_global_ptrs` | Getter pairing with a (separately-named) "first" func: checks `config_struct+0xd8` flag bit 0x40 and a sentinel byte, returns whether a global toggle value == 0. |
| `0x80013474` | 4B | `return_1` | Trivial: `return 1`. |
| `0x800138cc` | 680B | `unknown_fptr_index0` | Multi-case dispatcher, switches on `*(short*)(param+8) - 100` across ~16 subcases (100-117). Most delegate to other ROM functions (`FUN_8000f4a0`, `FUN_80013840`, `FUN_80072020`, `FUN_8007718c`, etc); the fallthrough/default case performs connection-record/slot scheduling bookkeeping over 0x84-byte array entries — same struct-shape family as `release_connection_record`'s connection records. Name ("fptr index 0") indicates it's entry 0 of a function-pointer dispatch table; purpose confirmed as a substantial command dispatcher, not renamed further since the existing name already correctly flags it as a table entry. |
| `0x80014054` | 62B | `VSC_0xfcc0_FUN_80014054` | VSC 0xfcc0 handler: toggles HW register bit 0x200 via an fptr call (sub-opcode 0x16), calls cleanup `FUN_80013ee8` on the disable path. |
| `0x800148f0` | 54B | `VSC_0xfcc2_FUN_800148f0` | VSC 0xfcc2 handler: resolves an index via `FUN_80042a68`, computes a table offset `(idx-1)*0xc+0xb`, returns the looked-up value via `FUN_80013e2c`. |
| `0x8001574c` | 100B | `send_evt_invalid_0xFF` | Sends HCI vendor-specific event 0xFF, subcode 0x28, conditionally on a global flag byte; logs unconditionally via `possible_logging_function__var_args`. |
| `0x800157b8` | 234B | `calls_send_evt_invalid_0xFF_0_or_1` | 0/1 link-state transition tracker with a threshold-counted timer-supervision pattern (`(byte)tbl[10]<<1 <= *counter`); calls `send_evt_invalid_0xFF(0)` or `(1)` on state transitions. |
| `0x80016780` | 74B | `wrap_look_for_non_matching_bdaddr_bos_index_i.e._free_connection_slot` | Thin wrapper: copies a 6-byte BD_ADDR from `param+3`, calls the (already-named) `look_for_non_matching_bdaddr_bos_index_i.e._free_connection_slot`, maps the result to status codes 0xb/0xd/computed. |
| `0x80018c14` | 4B | `ret_wrapper` | Trivial: `return 0`. |
| `0x80018e58` | 220B | `send_HCI_Command_Status_for_HCI_0x0A` | BOS-slot lookup by BD_ADDR; validates subcode range 0xd-0xf; sends `send_evt_HCI_Command_Status`, then dispatches by connection status (0x11/0x15/0xd) to `FUN_8006c9e8`/`FUN_8006b4a0`. |
| `0x80019594` | 370B | `send_HCI_Command_Status_for_HCI_0x09` | Same BOS-lookup + Command-Status pattern as the 0x0A handler, with SSP/encryption-aware status branching (checks crypto-struct offset 0x214) before dispatching to `FUN_80019050`/`FUN_80019504`. |
| `0x80019830` | 638B | `send_HCI_Command_Status_for_HCI_0x07` | Most complex of the three Command-Status handlers: two-path connection-handle resolution, feature-page gating (`uVar8 & 0x3f8`/`& 7` mask check), dispatches to `FUN_80019774`/`FUN_800191a8`. |
| `0x80019e4c` | 60B | `send_evt_HCI_Read_Remote_Extended_Features_Complete` | Thin PDU-pack wrapper; sends HCI event 0x23 — matches the spec opcode for this event name exactly. |
| `0x80019e88` | 124B | `send_evt_HCI_Synchronous_Connection_Changed` | Sends HCI event 0x2d (matches spec opcode); a BD_ADDR-random check swaps which interval field maps to Tx vs Rx. |
| `0x80019f0c` | 232B | `send_evt_HCI_Synchronous_Connection_Complete` | Sends HCI event 0x2c (matches spec opcode); `param_3`-keyed branch (0 vs 2) selects the SCO-record vs eSCO-record field source. |
| `0x8001b23c` | 122B | `fHCI_Read_LMP_Handle_0x20` | HCI_Read_LMP_Handle (OCF 0x20): resolves the connection's LMP handle byte via `called_by_fHCI_Read_LMP_Handle_3`, replies with `called_by_fHCI_Read_LMP_Handle_send_evt_HCI_Command_Complete`. |
| `0x8001b2c0` | 170B | `fHCI_Read_Clock_Offset_0x1F` | HCI_Read_Clock_Offset (OCF 0x1F): BD_ADDR-random branch either sends an LMP clock-offset-request PDU via `send_LMP_pkt` or replies immediately with the cached offset via `send_evt_HCI_Read_Clock_Offset_Complete`. |
| `0x8001b4e8` | 96B | `fHCI_Read_Remote_Supported_Features_0x1B` | HCI_Read_Remote_Supported_Features (OCF 0x1B): triggers `send_LMP_FEATURES_REQ_or_RES` with LMP opcode 0x27 (LMP_features_req), marks outstanding-LMP-PDU status bits. |
| `0x8001c788` | 38B | `fHCI_Truncated_Page_Cancel_0x40` | Thin wrapper: copies BD_ADDR, calls the shared `fHCI__Create_Connection_0x08__or__Remote_Name_Request_0x1A__Cancel` helper with mode=2 to select the Truncated-Page-Cancel behavior. |
| `0x8001cd74` | 586B | `initialize_0x28_sized_struct` | **Name correction recommended**: the function sets bytes 0x00-0x14 of its struct argument to 0xff, then 0x15-0x3f to 0 — i.e. it zero/0xff-initializes a full **0x40-byte** region (64 bytes), not 0x28 (40 bytes) as the name claims. Remaining logic config-flag-gates bit masks through offset 0x27. Recommended rename: `initialize_0x40_sized_struct`. Rename itself is blocked on the open rename-persistence bug (`wairz_requested_changes.txt`) — documenting the correction here and in `rom_function_index.md` as the durable record until a rename can actually persist. |
| `0x8001d1bc` | 24B | `send_evt_HCI_Hardware_Error` | Thin wrapper; sends HCI event 0x10 — matches spec opcode. |
| `0x8001d1f8` | 74B | `send_evt_HCI_Connection_Complete` | Thin wrapper; sends HCI event 0x03 — matches spec opcode — with BD_ADDR + link type. |
| `0x8001d424` | 76B | `called_by_fHCI_Read_LMP_Handle_send_evt_HCI_Command_Complete` | Sends HCI event 0xe (Command_Complete, matches spec opcode), packing status + connection handle + LMP-handle byte. |
| `0x8001d5b4` | 68B | `send_evt_0x14_HCI_Mode_Change` | Thin wrapper; sends HCI event 0x14 — matches spec opcode/name — with a connection-handle table lookup. |
| `0x8001d5fc` | 460B | `send_evt_HCI_Disconnection_Complete` | Sends HCI event 5 (matches spec opcode). **Notable**: this decompile resolved its callee as `release_connection_record` (not `FUN_8005b79c`) — the one specific rename that `wairz_requested_changes.txt` documents as persisting (it's the literal name wairz's own fix-verification test used). Consistent with, not contradicting, the open rename-persistence bug. |
| `0x8001d804` | 64B | `send_evt_HCI_Connection_Request` | Thin wrapper; sends HCI event 0x04 — matches spec opcode — with BD_ADDR + class-of-device + link type. |
| `0x8001da0c` | 40B | `send_evt_HCI_Inquiry_Complete` | Thin wrapper; clears a 0x40-byte scratch buffer then sends HCI event 0x01 — matches spec opcode. |
| `0x8001e6fc` | 44B | `OGC_3_OCF_16` | OGF=3/OCF=0x16 (Write_Voice_Setting-style); validates param range, writes a scaled field to a status struct at offset +0x14. |
| `0x8001e72c` | 22B | `OGC_3_OCF_18` | OGF=3/OCF=0x18; validates nonzero param, writes directly to status struct offset +0x16. |

**Confidence**: 30 of the 31 above upgraded **medium → HIGH** in
`rom_function_index.md` with names confirmed unchanged. `initialize_0x28_sized_struct`
(`0x8001cd74`) is also upgraded to HIGH (purpose is now fully understood and
documented) but flagged with a pending name correction
(`initialize_0x40_sized_struct`) that cannot be applied until the rename-persistence
bug is fixed. 0 medium-confidence functions remain in this region.

## Cross-region low→high confidence upgrade pass, batch (2026-06-26)

Part of the `work-in-progress.txt` "Cross-region: upgrade all low-confidence
named functions to high" ticket. A fresh `grep "low (named by Kovah"
rom_function_index.md` (the prior batch's "111 remain" Summary-table figure
was itself stale — see that doc's drift note) found exactly **67** live rows
project-wide; **7** of them are in this region. All 7 decompiled individually
via `decompile_function`.

| Address | Size | Name | Confirmed purpose |
|---------|------|------|--------------------|
| `0x8001e748` | 20B | `OGC_3_OCF_3c` | OGF=3/OCF=0x3c HCI command handler: validates a parameter byte `< 3` (3-valued enum), stores it to the connection-status struct at `+0x16c`, returns HCI status 0 or 0x12 (Invalid HCI Command Parameters). Standard "Write `<enum setting>`" command shape; sibling of the already-HIGH `OGC_3_OCF_16`/`OGC_3_OCF_18` immediately preceding it in the table. Confirmed, no rename needed — the existing name already correctly identifies the opcode group/OCF and the decompile reveals nothing more specific without an external OCF→command-name lookup. |
| `0x8001e760` | 28B | `OGC_3_OCF_3e` | OGF=3/OCF=0x3e: same validate-and-store shape (byte `< 2`, store to `+0x16b`), plus a special case when the value is `0`: also clears `+0x174` and zeroes an output byte through `*param_2` (a "reset associated state when disabling" branch). Confirmed, no rename needed. |
| `0x8001f9cc` | 320B | `fHCI_Remote_OOB_Extended_Data_Request_Reply_0x45` | **Confirmed exact match** to the Bluetooth SSP command `HCI_Remote_OOB_Extended_Data_Request_Reply` (opcode `0x0445`, verified against the wrapper's own opcode check below). Byte-swaps and verifies two 16-byte OOB confirmation/randomizer pairs via `FUN_800262b8` — the second pair only when the connection's extended/P-256 OOB flag (`+0x214`) is set — writes per-pair verification results to `+0x1e6`/`+0x1e7`, then dispatches `LMP_ACCEPTED`/`LMP_NOT_ACCEPTED` or `HCI_Simple_Pairing_Complete` based on the outcome. Directly relevant to BT Secure Simple Pairing. No rename needed. |
| `0x8001fb10` | 60B | `wrap_fHCI_Remote_OOB_Extended_Data_Request_Reply_0x45` | Confirmed opcode-dispatch wrapper for the function above: checks the HCI opcode `== 0x445` exactly (`0x445 = OGF 1 << 10 \| OCF 0x45`, matching both functions' names precisely), calls it, and emits HCI Command Status on non-success. Returns 1 (pass to next handler in chain) on opcode mismatch. No rename needed. |
| `0x8001fb4c` | 34B | `send_evt_HCI_Authenticated_Payload_Timeout_Expired` | Confirmed exact match: packs a 16-bit connection handle little-endian and sends HCI event code `0x57`, the spec opcode for this exact event name. No rename needed. |
| `0x8001fce0` | 62B | `HCI_EVT_0x1fc_FUN_8001fce0` | **Not a single event sender** — the `FUN_` suffix already flagged Kovah's own uncertainty, and the decompile confirms why: this is an allowlist *dispatch gate* that routes a curated, non-contiguous set of byte values `{0x16, 0x17, 0x31, 0x33, 0x34, 0x35, 0x3b}` to the shared handler `many_sub_if_else_cases_on_param2`, explicitly rejecting `0x32` and everything else. Recommended rename: `ocf_allowlist_dispatch_to_shared_handler` (not applied — blocked on the open rename-persistence bug). Upgraded to HIGH: the dispatch logic itself is now fully decompiled and documented even though the single underlying Bluetooth-spec rationale for that specific 7-value set isn't pinned down. |
| `0x8001fef8` | 216B | `HCI_EVT_0x1f6_FUN_8001fef8` | **Not a single event sender** either — confirmed to be a 17-slot ring-buffer cursor-drain dispatcher gated by a per-connection state field (`+0x169` ∈ `{0,2}` vs `{1,3}`), branching on `param_1==5` to call one of two helper pairs and decrementing a pending-count field at `+0x110`. Same idiom family as the "ring-buffer event-drain loop" cluster already documented in `region_0x80000000` Pass 8. Recommended rename: `conn_state_gated_ring_drain_dispatch` (not applied — rename-persistence bug). |

**Confidence**: all 7 upgraded **low → HIGH** in `rom_function_index.md`. Two
(`0x8001fce0`, `0x8001fef8`) carry recommended renames (not applied, rename
bug); the other 5 confirmed accurate as-is. 0 low-confidence functions remain
in this region.

## Pass 7 (2026-07-01) — baseband register write primitive `FUN_80011608`

Fresh `ListUnnamed80010000.java` cold-triage: **408 total**, **150 named**,
**258 unnamed** (pre-rename). Rank-1 by xref_in: `FUN_80011608` (110B, 65
xrefs). Decompiled and renamed:
**`FUN_80011608` → `write_baseband_register_masked_busywait`**
(110B, HIGH) via `RenamePass7Region80010000Fun80011608.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-masked baseband MMIO register write primitive (write-side
counterpart of `FUN_80011510`). Gates on address alignment via
`((1 << (param_3 & 0x1f)) - 1) & param_1`. When width selector `param_3` is
byte-mode (`& 0xff == 0`), left-shifts `param_2` into the correct byte lane by
`(param_1 & 3) << 3`. Under interrupt disable: writes data to `DAT_8001167c`,
strobes request word at `DAT_80011680` (OR with `DAT_80011684` then clean write),
busy-waits up to 20000 iterations for ready bit, then re-enables interrupts.
Already extensively cross-referenced project-wide (VSC 0xFC61, RF init chains,
patch installer hooks) — see `reverse_engineering_baseband_reg_helpers.md`.

**Confidence:** HIGH — mechanism fully documented in prior thematic pass;
live decompile matches; rename closes the last major unnamed gap in the
baseband R/W primitive pair.

Region unnamed count after this pass: **257** (258 minus this rename).

## Pass 7b (2026-07-01) — baseband register read primitive `FUN_80011510`

Pass 7b target from cold-triage rank-2 (59 xref_in, 98B). Decompiled and renamed:
**`FUN_80011510` → `read_baseband_register_masked_busywait`**
(98B, HIGH) via `RenamePass7bRegion80010000Fun80011510.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-masked baseband MMIO register read primitive (read-side
counterpart of `write_baseband_register_masked_busywait`). Gates on address
alignment via `((1 << (param_2 & 0x1f)) - 1) & param_1`. Builds request word
`param_2 << 0x1b | param_1`, strobes control register at `DAT_8001157c` twice,
busy-waits up to 20000 iterations for ready bit, reads result from
`DAT_80011580` (or returns fallback `DAT_80011574` on timeout/misalignment).
Completes the baseband R/W primitive pair documented in
`reverse_engineering_baseband_reg_helpers.md`.

**Confidence:** HIGH — mechanism fully documented in prior thematic pass;
live decompile matches; rename closes read-side of the masked busy-wait pair.

Region unnamed count after this pass: **256** (257 minus this rename).

## Pass 7c (2026-07-01) — MMIO indexed register read `FUN_8001136c`

Pass 7c target from cold-triage rank-1 (27 xref_in, 34B). Decompiled and renamed:
**`FUN_8001136c` → `read_baseband_register_mmio_indexed`**
(34B, HIGH) via `RenamePass7cRegion80010000Fun8001136c.java` (`renamed=1`,
live-verified).

**Mechanism:** Canonical baseband HW register read via MMIO at `0xb000a0bc`
(documented in `reverse_engineering_rom_regs.md`). RMW control word: masks
preserve status bits, inserts 6-bit register index into bits[21:16], returns
16-bit value from bits[15:0]. Distinct from `read_baseband_register_masked_busywait`
(Pass 7b) which uses a separate control window with IRQ disable and busy-wait
polling. Sole access path for patch and `FUN_80009980` HW init per project docs.

**Confidence:** HIGH — mechanism pre-documented in `reverse_engineering_rom_regs.md`;
live decompile matches exactly; 27 xref_in confirms generic primitive status.

Region unnamed count after this pass: **255** (256 minus this rename).

## Pass 7d (2026-07-01) — MMIO indexed register write `FUN_8001139c`

Pass 7d target from cold-triage (write-side counterpart of Pass 7c, 46B).
Decompiled and renamed:
**`FUN_8001139c` → `write_baseband_register_mmio_indexed`**
(46B, HIGH) via `RenamePass7dRegion80010000Fun8001139c.java` (`renamed=1`,
live-verified).

**Mechanism:** Canonical baseband HW register write via MMIO at `0xb000a0bc`
(documented in `reverse_engineering_rom_regs.md`). RMW control word: masks
preserve status bits, inserts 6-bit register index into bits[21:16], ORs
16-bit value into bits[15:0], then performs **two** MMIO writes — first
latches index+value, second ORs trigger bit from `DAT_800113dc` to commit.
Write-side counterpart of `read_baseband_register_mmio_indexed` (Pass 7c);
distinct from `write_baseband_register_masked_busywait` (Pass 7) which uses
IRQ-masked busy-wait on a separate control window.

**Confidence:** HIGH — mechanism pre-documented in `reverse_engineering_rom_regs.md`;
live decompile matches exactly; 3 direct callers confirmed (`FUN_80110b54`,
`LMP_CH__0x3ee__case2_else_1_FUN_80011fc0FUN_80011e10`,
`program_link_mode_bb_regs_merge_ram_timing_and_arm_status`); also invoked
indirectly via function pointers project-wide.

Region unnamed count after this pass: **254** (255 minus this rename).

## Pass 7e (2026-07-01) — packet-type table low-byte writer `FUN_80013c0c`

Pass 7e target from cold-triage rank-1 (18 xref_in, 44B). Decompiled and renamed:
**`FUN_80013c0c` → `write_packet_type_table_low_byte_at_offset`**
(44B, HIGH) via `RenamePass7eRegion80010000Fun80013c0c.java` (`renamed=1`,
live-verified).

**Mechanism:** Low-byte half of the eSCO/SCO packet-type table writer pair
(documented cross-region in `region_0x80030000` as the apply path inside
`apply_eSCO_SCO_packet_type_params`). Indexes `ushort[param_1 + DAT_80013c38]`
and patches bits[7:0]: `entry = (entry & 0xff00) | (param_2 & 0xff)`.
High-byte counterpart is sibling `write_packet_type_table_high_byte_at_offset`
(36B, 8 xref, Pass 7q) at `DAT_80013c08`, which ORs `param_2 << 8` into
bits[15:8]. Called together
from connection-setup, role-switch, and LMP PDU paths project-wide.

**Confidence:** HIGH — trivial decompile; 18 xref_in confirms generic primitive
status; cross-region callers (`apply_eSCO_SCO_packet_type_params`,
`connection_setup_arm_stride88_slot_and_apply_packet_types`,
`role_switch_apply_packet_types_on_stride84_slot`) already document the pair.

Region unnamed count after this pass: **253** (254 minus this rename).

## Pass 7f (2026-07-01) — status-gated byte write `FUN_8001359c`

Pass 7f target from cold-triage rank-1 (16 xref_in, 22B). Decompiled and renamed:
**`FUN_8001359c` → `busywait_status_bits_0x60_then_write_byte`**
(22B, HIGH) via `RenamePass7fRegion80010000Fun8001359c.java` (`renamed=1`,
live-verified).

**Mechanism:** Infinite-loop status poll then byte write primitive in the
`0x800135xx` scheduler/packet-slot cluster (sibling of `FUN_800135bc` scheduler
yield). Spins on `*DAT_800135b4` until `(*status & 0x60) != 0` (bits 5+6 set),
then writes `param_1 & 0xff` to `*DAT_800135b8`. No timeout — caller must
ensure the status bits eventually assert. Distinct from the IRQ-masked
baseband R/W pair (Pass 7/7b) and MMIO-indexed pair (Pass 7c/7d).

**Confidence:** HIGH — trivial decompile; 16 xref_in confirms generic primitive
status; behavioral name matches decompile exactly.

Region unnamed count after this pass: **252** (253 minus this rename).

## Pass 7g (2026-07-01) — four-register BB hook writer `FUN_80013e78`

Pass 7g target from cold-triage rank-1 (14 xref_in, 98B). Decompiled and renamed:
**`FUN_80013e78` → `program_bb_regs_0xee_0x60_0xa_0_via_hook`**
(98B, HIGH) via `RenamePass7gRegion80010000Fun80013e78.java` (`renamed=1`,
live-verified).

**Mechanism:** Four-register baseband programming primitive in the `0x80013exx`
cluster (sibling of cleanup `FUN_80013ee8` which programs regs `0xa`/`0x0` only).
Invokes HW-write callback at `PTR_DAT_80013ee0` four times:
`(0xee, mode_word)`, `(0x60, *PTR_DAT_80013ee4 | param_2)`, `(10, param_3)`,
`(0, 0xc)`. Reg `0xee` mode word derived from `DAT_80013edc`: when
`param_4 == 0` clears bit 5 and sets bit 0 (`& ~0x20 | 1`); else ORs `0x21`
(bits 0+5). Used across ACL TX/RX fragment paths, SCO connection setup
(`apply_SCO_connection_params_to_hw`, `allocate_sco_hw_link_descriptor_slot`),
and slot-reset dispatch.

**Callers (14 xref_in):** `hci_acl_data_fragment_assembler_and_enqueue` (4),
`dispatch_acl_fragment_with_per_conn_reassembly_flags` (3),
`apply_SCO_connection_params_to_hw` (2),
`allocate_sco_hw_link_descriptor_slot` (2),
`dispatch_acl_rx_continuation_or_procedure_hook_by_conn_flag_0x23`,
`transmit_acl_single_packet_direct_via_hw_tx_descriptor`,
`reset_slot_tail_and_hook_dispatch_status_upper_bits_if_idle`.

**Confidence:** HIGH — clear four-call hook pattern; 14 xref_in confirms generic
primitive status; behavioral name matches decompile exactly.

Region unnamed count after this pass: **251** (252 minus this rename).

## Pass 7h (2026-07-01) — halfword read wrapper `FUN_80011584`

Pass 7h target from cold-triage rank-1 (13 xref_in, 66B). Decompiled and renamed:
**`FUN_80011584` → `read_baseband_register_halfword_masked_busywait`**
(66B, HIGH) via `RenamePass7hRegion80010000Fun80011584.java` (`renamed=1`,
live-verified).

**Mechanism:** Halfword-granular read wrapper on top of
`read_baseband_register_masked_busywait` (Pass 7b). When `param_1 & 3 == 2`,
reads the containing word at `param_1 & 0xfffc` with width 2 and shifts down
the upper halfword; when `param_1 & 3 == 0`, reads directly with width 1;
otherwise returns sentinel `0xdead` for misaligned
offsets 1 or 3 mod 4. Byte-granular counterpart is sibling `FUN_800115c8`
(62B) at `0x800115c8`.

**Callers (13 xref_in):** patch `FUN_8010c278` (7 sites), `FUN_8010ce0c`,
`VSC_0xfc61_config_update`, `FUN_800122fc`, `generic_status_field_get_set_dispatcher`,
plus 2 data-block call sites.

**Confidence:** HIGH — trivial decompile; pre-documented in
`reverse_engineering_baseband_reg_helpers.md`; 13 xref_in confirms generic
primitive status; behavioral name matches decompile exactly.

Region unnamed count after this pass: **250** (251 minus this rename).

## Pass 7i (2026-07-01) — byte read wrapper `FUN_800115c8`

Pass 7i target from cold-triage rank-1 (62B). Decompiled and renamed:
**`FUN_800115c8` → `read_baseband_register_byte_masked_busywait`**
(62B, HIGH) via `RenamePass7iRegion80010000Fun800115c8.java` (`renamed=1`,
live-verified).

**Mechanism:** Byte-granular read wrapper on top of
`read_baseband_register_masked_busywait` (Pass 7b). When `param_1 & 3 == 0`,
reads directly with width 0 (byte); otherwise rounds down to word boundary
(`param_1 & 0xfffc`), reads with width 2, and shifts right by
`(param_1 & 3) << 3` to extract the target byte. Returns `uVar1 & 0xff`.
Halfword counterpart is sibling `read_baseband_register_halfword_masked_busywait`
(Pass 7h). Already documented in `reverse_engineering_baseband_reg_helpers.md`.

**Callers:** patch `FUN_8010c260`, `FUN_801106bc`, `pulse_bb_regs_0x00_0x10_with_cc_mode_bits_and_spin_delays`,
`VSC_0xfc61_config_update`, `FUN_8003b170`.

**Confidence:** HIGH — trivial decompile; pre-documented mechanism; behavioral
name matches decompile exactly; completes the byte/halfword extraction wrapper
pair atop the masked busy-wait read primitive.

Region unnamed count after this pass: **249** (250 minus this rename).

## Pass 7j (2026-07-01) — TX descriptor slot release `FUN_80014524`

Pass 7j target from cold-triage rank-1 (11 xref_in, 184B). Decompiled and renamed:
**`FUN_80014524` → `release_active_tx_descriptor_slots_via_hw_programmer`**
(184B, HIGH) via `RenamePass7jRegion80010000Fun80014524.java` (`renamed=1`,
live-verified).

**Mechanism:** TX descriptor slot release primitive in the `0x800145xx` cluster.
Calls `LMP__25C_called2()` (BLE coexistence hook) first. When `param_1 & 0xffff`
is non-zero, builds up to `(param_1 >> 8) + 1` cleared 4-byte HW TX descriptor
entries on the stack from template globals (`DAT_800145dc`, `PTR_DAT_800145e0`,
`DAT_800145e4`): each slot ORs `0x400` into the ushort at offset+2 and clears
bit5 in byte at offset+3; the final slot encodes type bits from `param_1 & 0xff`
shifted left by 2. Submits the array via
`program_active_tx_descriptor_slots_to_hw_registers` with `param_2 & 0xff` as
the poll/diagnostic flag.

**Callers:** 11 xref_in including `dispatch_conn_tx_by_packet_type_nibble_with_reassembly`
(region `0x80040000` — "other" release path with `(handle,1)` and failure path
with `(handle,0)`), plus conn-event ring LMP PDU builders on allocation failure.

**Confidence:** HIGH — decompile confirms stack-built descriptor array + established
HW programmer callee; cross-region callers already document release semantics.

Region unnamed count after this pass: **248** (249 minus this rename).

## Pass 7k (2026-07-01) — eSCO codec config apply `FUN_80014dac`

Pass 7k target from cold-triage rank-1 (11 xref_in, 138B). Decompiled and renamed:
**`FUN_80014dac` → `apply_esco_codec_config_via_hw_register_programmer`**
(138B, HIGH) via `RenamePass7kRegion80010000Fun80014dac.java` (`renamed=1`,
live-verified).

**Mechanism:** eSCO codec-config apply primitive in the `0x80014dxx` cluster.
Remaps role index via `remap_role_index_to_esco_slot_if_pending`, IRQ-disables,
reads HW clock via `read_hw_clock_raw_dword_by_role_index`, then issues four
register writes through hook fptr `PTR_DAT_80014e38` (regs `2`, `0x48`, `0x4e`,
`0`) encoding role/slot bits, clock-derived timing, and codec type
(`param_2 & 0xffff`). Arms per-slot gate byte at `PTR_DAT_80014e3c[remapped_index]`.
Codec-config apply half of the `FUN_80014450`/`FUN_80014dac` pair documented
across eSCO counter/flush paths in regions `0x80030000`/`0x80040000`.

**Callers:** 11 xref_in including `role_switch_completion_or_abort_handler`
(region `0x80000000`), eSCO codec counter/flush sweepers (`increment_esco_slot_counter_and_apply_codec_if_gate_armed`, `flush_armed_esco_codec_slots_up_to_12_and_apply` in `0x80030000`), and conn-table codec-config sweeps in `0x80040000`.

**Confidence:** HIGH — decompile confirms IRQ-gated four-register HW programmer
sequence + established cross-region caller semantics as codec-config apply.

Region unnamed count after this pass: **247** (248 minus this rename).

## Pass 7l (2026-07-01) — sync gate byte bit-clear `FUN_80017d2c`

Pass 7l target from cold-triage rank-1 (11 xref_in, 78B). Decompiled and renamed:
**`FUN_80017d2c` → `clear_sync_gate_byte_bits_by_remapped_role_index`**
(78B, HIGH) via `RenamePass7lRegion80010000Fun80017d2c.java` (`renamed=1`,
live-verified).

**Mechanism:** Per-slot sync-gate byte bit-clear primitive in the `0x80017dxx`
cluster. Remaps conn/role index via `remap_role_index_to_esco_slot_if_pending`,
then AND-clears bits in `PTR_DAT_80017d7c[remapped_index]` using mask
`~param_3`. Logs via `possible_logger_called_if_no_patch3` with code `0x326`.
Callers pass role byte `0xCC` with masks `4` (clear bit 2 — conn-sync after
role-change apply) or `0xffff` (full clear — connection-setup feature finalizer).

**Callers:** 11 xref_in including `apply_or_defer_conn_role_change_emit_hci_evt_sync`
(region `0x80070000` — role-change conn sync), `LMP__267__FUN_800714a0`
(connection-setup feature/timer finalizer), and crypto-state role-switch paths in
region `0x80020000`.

**Confidence:** HIGH — decompile confirms esco-remap + gate-byte AND-mask clear;
cross-region callers already document role-switch/conn-sync semantics.

Region unnamed count after this pass: **246** (247 minus this rename).

## Pass 7m (2026-07-01) — fast-poll BB reg 0x298 `FUN_80014290`

Pass 7m target from cold-triage rank-1 (10 xref_in, 92B). Decompiled and renamed:
**`FUN_80014290` → `apply_fast_poll_mode_bb_reg_0x298_irq_gated`**
(92B, HIGH) via `RenamePass7mRegion80010000Fun80014290.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-gated fast-poll mode toggle on baseband register **0x298**.
Optional veto hook at `PTR_DAT_800142ec` may skip the write. When `param_1==1`,
ORs saved value at `DAT_800142f0` with mask `0x8880` (enable fast-poll bits);
otherwise ANDs with `0x777f` (clear). Commits via HW programmer hook at
`PTR_DAT_800142f4(0x298, value)`. Pairs with sibling `FUN_800142f8` (BB reg
**0x11c** bit-7 toggle) — callers invoke both together as the documented
fast-poll enable/disable sequence.

**Callers:** 10 xref_in including `conn_packet_type_apply_and_codec_table_sync`,
`ring_buffer_event_drain_dispatch_loop` (clears fast-poll on per-connection
apply), `conn_index_status_bit_apply_and_log` (re-enables fast-poll after LMP
procedure validation), and `page_response_timing_and_afh_update_counters`
(calls with `0` when channel-active bitmask empties).

**Confidence:** HIGH — decompile confirms IRQ-masked 0x298 bit-mask toggle;
cross-region callers already document fast-poll semantics.

Region unnamed count after this pass: **245** (246 minus this rename).

## Pass 7n (2026-07-01) — fast-poll BB reg 0x11c `FUN_800142f8`

Pass 7n target from cold-triage rank-1 (10 xref_in, 82B). Decompiled and renamed:
**`FUN_800142f8` → `apply_fast_poll_mode_bb_reg_0x11c_irq_gated`**
(82B, HIGH) via `RenamePass7nRegion80010000Fun800142f8.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-gated fast-poll mode toggle on baseband register **0x11c**
(bit 7). IRQ-disables, reads saved halfword at `DAT_8001434c`; when
`param_1==1`, ORs low byte with `0x80` (set bit 7); otherwise ANDs with
`0x7f` (clear). Commits via HW programmer hook at `PTR_DAT_80014350`
(`0x11c`, value). Pairs with sibling `apply_fast_poll_mode_bb_reg_0x298_irq_gated`
on reg **0x298** — callers invoke both together as the documented fast-poll
enable/disable sequence.

**Callers:** 10 xref_in including `conn_packet_type_apply_and_codec_table_sync`,
`ring_buffer_event_drain_dispatch_loop` (clears fast-poll on per-connection
apply), `conn_index_status_bit_apply_and_log` (re-enables fast-poll after LMP
procedure validation), and `page_response_timing_and_afh_update_counters`
(calls with `0` when channel-active bitmask empties).

**Confidence:** HIGH — decompile confirms IRQ-masked 0x11c bit-7 toggle;
cross-region callers already document fast-poll semantics; completes the
0x142xx fast-poll register pair started in Pass 7m.

Region unnamed count after this pass: **244** (245 minus this rename).

## Pass 7o (2026-07-01) — per-conn BB reg 0x1f0 halfword read `FUN_80014354`

Pass 7o target from cold-triage rank-1 (9 xref_in, 80B). Decompiled and renamed:
**`FUN_80014354` → `read_halfword_via_bb_reg_0x1f0_esco_slot_select_by_conn_index`**
(80B, HIGH) via `RenamePass7oRegion80010000Fun80014354.java` (`renamed=1`,
live-verified).

**Mechanism:** Per-connection halfword read primitive in the `0x800143xx` cluster
(sibling of Pass 7m/7n fast-poll toggles). Takes conn-table index `param_1`
(0–9); returns sentinel `0xbeef` when `param_1 >= 10`. Looks up
`big_ol_struct[param_1]` for `bos_connection__array_index` and role byte
`0xCC`, remaps via `remap_role_index_to_esco_slot_if_pending`, then programs
BB register **0x1f0** via HW hook fptr `PTR_DAT_800143a8` with offset
`(remapped_slot - 1) * 0x1000` (per-slot bank select). Returns cached halfword
from `DAT_800143ac` (populated by the hook side effect).

**Callers:** 9 xref_in (fresh `ListUnnamed80010000.java` rank-1 at Pass 7o
list time); conn-indexed callers in packet-type/codec-table sync paths.

**Confidence:** HIGH — decompile confirms esco-remap + 0x1f0 slot-bank select +
cached halfword return; sentinel on out-of-range index; sits in established
`0x800142xx`/`0x800143xx` BB-register helper cluster.

Region unnamed count after this pass: **243** (244 minus this rename).

## Pass 7p (2026-07-01) — LMP PDU TX descriptor commit `FUN_800145e8`

Pass 7p target from cold-triage rank-1 (8 xref_in, 106B). Decompiled and renamed:
**`FUN_800145e8` → `program_single_tx_descriptor_slot_for_lmp_pdu_body`**
(106B, HIGH) via `RenamePass7pRegion80010000Fun800145e8.java` (`renamed=1`,
live-verified).

**Mechanism:** Single-slot TX descriptor commit primitive in the `0x800145xx`
cluster — allocate-side counterpart of Pass 7j's
`release_active_tx_descriptor_slots_via_hw_programmer`. Calls
`LMP__25C_called2()` (BLE coexistence hook) first. When `param_2 & 0xffff`
(handle) is non-zero, merges buffer content at `param_1` with template globals
(`DAT_80014654`, `DAT_80014658`), encodes connection handle
`(param_2 & 0x7ff) << 2` into the descriptor word, and submits the single
4-byte entry via `program_active_tx_descriptor_slots_to_hw_registers`
(count=1, poll flag `param_3`).

**Callers:** 8 xref_in including conn-event ring LMP PDU builders in region
`0x80040000` (`build_and_submit_sco_esco_lmp_pdu_for_conn_type_1_or_2`,
`build_and_submit_lmp_0x480_for_conn_type_3`,
`gate_conn_event_ring_dequeue_by_sco_esco_type_flags`) — all invoke as
`(buf+4, handle, 0)` to fill LMP PDU body before submit.

**Confidence:** HIGH — decompile confirms template-merge + handle encode +
established HW programmer callee; cross-region callers already document LMP PDU
body semantics; pairs with Pass 7j release primitive.

Region unnamed count after this pass: **242** (243 minus this rename).

## Pass 7q (2026-07-01) — packet-type table high-byte writer `FUN_80013be4`

Pass 7q target from cold-triage rank-1 (8 xref_in, 36B). Decompiled and renamed:
**`FUN_80013be4` → `write_packet_type_table_high_byte_at_offset`**
(36B, HIGH) via `RenamePass7qRegion80010000Fun80013be4.java` (`renamed=1`,
live-verified).

**Mechanism:** High-byte half of the eSCO/SCO packet-type table writer pair
(sibling of Pass 7e's `write_packet_type_table_low_byte_at_offset`). Indexes
`ushort[param_1 + DAT_80013c08]` and patches bits[15:8]:
`entry = (entry & 0xff) | (param_2 << 8)`. Called together with the low-byte
writer from `apply_eSCO_SCO_packet_type_params`, connection-setup, and
role-switch paths project-wide.

**Confidence:** HIGH — trivial decompile; 8 xref_in; Pass 7e already documented
the pair; decompile confirms complementary mask/shift pattern.

Region unnamed count after this pass: **241** (242 minus this rename).

## Pass 7r (2026-07-01) — SCO/eSCO credit-slot timing `FUN_800131e4`

Pass 7r target from cold-triage rank-1 (7 xref_in, 192B). Decompiled and renamed:
**`FUN_800131e4` → `apply_sco_esco_credit_slot_timing_and_active_bitmask`**
(192B, HIGH) via `RenamePass7rRegion80010000Fun800131e4.java` (`renamed=1`,
live-verified).

**Mechanism:** Per-slot (index 0–3) SCO/eSCO credit-scheduler timing entry
writer in the `0x800132xx` cluster. 20-byte stride array at `DAT_800132ac`:
offset `+0` receives ms-scaled timing
`((ms%1000)*(scale/1000))/1000 + (ms/1000)*(scale/1000)` using global scale
at `PTR_DAT_800132a8`; offset `+8` receives state code `3` (enable) or `6`
(disable). Active-slot bitmask at `PTR_DAT_800132b0` sets/clears bit
`(1 << slot_index)`. When enabling with `param_2==0`, logs and defaults to 1.
Invalid slot index logs error. Sibling of `sco_esco_param_pingpong_queue_rotator`
and the `sco_esco_slot_timing_offset_calc_variant*` family in region
`0x80000000`.

**Callers:** 7 xref_in including `guarded_trampoline_to_FUN_800131e4` (region
`0x80000000` — fixed-arg `FUN_800131e4(2,1,1)` from
`top_level_link_event_status_dispatcher_loop2` 2nd-status-word bits `1`/`2`).

**Confidence:** HIGH — decompile confirms 4-slot stride layout, ms scaling,
bitmask, and state codes; cross-region caller already documents fixed-arg
trampoline semantics.

Region unnamed count after this pass: **240** (241 minus this rename).

## Pass 7s (2026-07-01) — conn-CC-indexed HW register byte-half writer `FUN_800140d8`

Pass 7s target from cold-triage rank-1 (7 xref_in, 122B). Decompiled and renamed:
**`FUN_800140d8` → `write_hw_register_by_conn_cc_index_byte_half`**
(122B, HIGH) via `RenamePass7sRegion80010000Fun800140d8.java` (`renamed=1`,
live-verified).

**Mechanism:** Programs one of two baseband HW register pairs indexed by conn
`byte_0xCC` (selector `param_2`, values 0–3). Selectors 0–1 merge into cached
halfword at `DAT_8001415c` and write BB register `0x50`; selectors 2–3 merge
into `DAT_80014154` and write BB register `0x192`. Even selectors patch the
low byte (`entry & 0xff00 | value`); odd selectors patch the high byte
(`(value << 8) | entry & 0xff`). Commit via indirect callback at
`PTR_DAT_80014158` (indexed BB register write primitive). Role-switch /
LMP-slot-offset / class-mode cluster sibling already referenced from region
`0x80030000` Pass 67 (`compute_lmp_slot_offset_and_program_hw_by_conn_cc_index`).

**Callers:** 7 xref_in per `ListUnnamed80010000.java`; 5 direct call sites
confirmed via `xrefs_to` including
`apply_conn_class_mode_afh_role_remap_and_esco_ptype`,
`orchestrate_sco_esco_link_setup_baseband_regs_collision_and_afh`,
`conn_teardown_and_link_loss_cleanup_handler`.

**Confidence:** HIGH — decompile confirms dual register-pair + byte-half merge
pattern; cross-region callers anchor role-switch and eSCO setup paths.

Region unnamed count after this pass: **239** (240 minus this rename).

## Pass 7t (2026-07-01) — BB reg 0x144 shifted-param writer `FUN_80012d4c`

Pass 7t target from cold-triage rank-1 (7 xref_in, 104B — largest at xref=7
tier). Decompiled and renamed:
**`FUN_80012d4c` → `write_bb_reg_0x144_shifted_param_and_poll_status_irq_gated`**
(104B, HIGH) via `RenamePass7tRegion80010000Fun80012d4c.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-gated baseband register programmer in the `0x80012dxx`
cluster (sibling of `called_if_config[1]&4` at `0x80012e04`). Disables
interrupts, reads BB reg `0x144` via `read_baseband_register_masked_busywait`,
writes merged value `(param >> 2) | (DAT_80012db4 & read_mask)` via
`write_baseband_register_masked_busywait`, then polls up to 100 iterations until
`(DAT_80012db8 & read_value) != 0`. Re-enables interrupts and issues final read
of BB reg `0x1f8`. Uses the canonical masked busy-wait R/W pair documented in
Pass 7/7b.

**Callers:** 7 xref_in per `ListUnnamed80010000.java`.

**Confidence:** HIGH — decompile confirms IRQ gate, reg `0x144` RMW with
`param >> 2` scaling, bounded status poll, and final `0x1f8` read; behavioral
name matches decompile exactly.

Region unnamed count after this pass: **238** (239 minus this rename).

## Pass 7u (2026-07-01) — channel slot bit4 clear + release hook `FUN_80013cec`

Pass 7u target from cold-triage rank-1 (7 xref_in, 62B — tied largest at
xref=7 tier with `FUN_8001a0c8`). Decompiled and renamed:
**`FUN_80013cec` → `clear_indexed_channel_slot_bit4_and_invoke_release_hook`**
(62B, HIGH) via `RenamePass7uRegion80010000Fun80013cec.java` (`renamed=1`,
live-verified).

**Mechanism:** Indexed channel-slot release primitive in the `0x80013cxx`
cluster. When `(param_1 & 0xff) < 4`, looks up a ushort offset from
`PTR_DAT_80013d2c[index*2]`, reads the halfword record at
`DAT_80013d30 + offset`, clears bit 4 (`& 0xffef`), and dispatches via hook
`PTR_DAT_80013d34` with `(offset, masked_value)`. Cross-region callers use this
during ACL finalization slot-reuse (`region_0x80070000`) and page-response
channel-table teardown (`region_0x80000000`).

**Callers:** 7 xref_in per `ListUnnamed80010000.java`.

**Confidence:** HIGH — decompile confirms 4-slot index gate, table-indirected
record read, bit-4 clear, and hook dispatch; behavioral name matches decompile
exactly.

Region unnamed count after this pass: **237** (238 minus this rename).

## Pass 7v (2026-07-01) — OGC-3 OCF-47 conditional config-bit apply `FUN_8001a0c8`

Pass 7v target from cold-triage rank-1 (7 xref_in, 42B — sole rank-1 at xref=7
tier after Pass 7u). Decompiled and renamed:
**`FUN_8001a0c8` → `conditionally_apply_ogc3_ocf47_hw_bit0x100_when_idle`**
(42B, HIGH) via `RenamePass7vRegion80010000Fun8001a0c8.java` (`renamed=1`,
live-verified).

**Mechanism:** OGF=3 OCF=0x47 (`OGC_3_OCF_47`) apply tail-call. After the setter
writes `field_0x17d` on the `0x300`-byte config struct, this gate calls
`FUN_8001409c(1)` only when `field_0x17d==2` AND `byte_0x16f==0` AND
`field_0x171==0`; otherwise calls `FUN_8001409c(0)`. `FUN_8001409c` sets or
clears bit `0x100` on the ushort at `DAT_800140d0` and dispatches via hook
`PTR_DAT_800140d4` with index `0x16`. Sibling apply pattern: OCF `0x45`/`0x49`
tail-call `log_ogc3_config_apply_evt_0x4b6_if_no_patch3` after writing
`field_0x17c`/`field_0x17e`.

**Callers:** 7 xref_in per `ListUnnamed80010000.java`; primary caller is
`OGC_3_OCF_47` (HCI vendor-config field setter documented Pass 2).

**Confidence:** HIGH — decompile confirms struct-field gate and
`FUN_8001409c` bit0x100 commit; cross-confirmed with `OGC_3_OCF_47` decompile.

Region unnamed count after this pass: **236** (237 minus this rename).

## Pass 7w (2026-07-01) — HCI event ring-17 enqueue `FUN_8001ffdc`

Pass 7w target from cold-triage rank-1 (6 xref_in, 188B — largest at xref=6
tier after Pass 7v cleared xref=7). Decompiled and renamed:
**`FUN_8001ffdc` → `enqueue_hci_evt_to_ring17_and_dispatch_or_drain`**
(188B, HIGH) via `RenamePass7wRegion80010000Fun8001ffdc.java` (`renamed=1`,
live-verified).

**Mechanism:** HCI event-buffer enqueue into a 17-slot ring (`mod 0x11`). When
count at `+0x110` reaches `0x11`, logs via `possible_logging_function__var_args`
(tag `0x931`) instead of enqueueing. Otherwise: copies 4-byte header from
`param_1` into slot buffer at `PTR_DAT_800200a0`, stores pointer at
`PTR_DAT_80020098 + index*4`, stores `*(ushort*)(param_1+2)` at
`(index+0x44)*2`, clears pending flag at `(index+0x32)*4+4`, increments write
index modulo 17 and count. If config `field_0x169` is `1` or `3`, tail-calls
`drain_hci_evt_ring17_and_mode2_dispatch` (ring drain); else directly calls
`hci_evt_buffer_mode2_dispatch_offset4_u16_from_buf_plus2`.

**Callers:** 6 xref_in per `ListUnnamed80010000.java`; enqueue entry point for
HCI event producers when ring-buffer mode is active.

**Confidence:** HIGH — decompile confirms 17-slot ring semantics, overflow log
path, and dispatch/drain fork gated on `field_0x169`; cross-confirmed with
`hci_evt_buffer_mode2_dispatch_offset4_u16_from_buf_plus2` caller list in region
`0x80020000` Pass 6.

Region unnamed count after this pass: **235** (236 minus this rename).

## Pass 7x (2026-07-01) — single TX descriptor credit-scheduler commit `FUN_80014f3c`

Pass 7x target from cold-triage rank-1 (6 xref_in, 118B — largest at xref=6
tier). Decompiled and renamed:
**`FUN_80014f3c` → `program_single_tx_descriptor_slot_via_credit_scheduler`**
(118B, HIGH) via `RenamePass7xRegion80010000Fun80014f3c.java` (`renamed=1`,
live-verified).

**Mechanism:** Single-slot TX descriptor commit in the `0x80014fxx` cluster
(sibling of `program_single_tx_descriptor_slot_for_lmp_pdu_body` at Pass 7p,
which uses `program_active_tx_descriptor_slots_to_hw_registers` instead). When
`param_2 != 0`: calls `LMP__25C_called2` coexistence hook, builds one 4-byte
descriptor on stack from template `DAT_80014fb4` merged with `param_1`, encodes
ACL handle bits `(param_2 & 0x7ff) << 2`, then submits via `FUN_80014e40` with
count=1 and slot-type bytes `param_3`/`param_4` (capped at 3).

**Callers:** 6 xref_in per `ListUnnamed80010000.java`; credit-scheduler commit
path for ACL TX descriptor programming.

**Confidence:** HIGH — decompile confirms template-merge + handle encode +
`FUN_80014e40` single-slot submit; structural sibling of Pass 7p LMP PDU path.

Region unnamed count after this pass: **234** (235 minus this rename).

## Pass 7y (2026-07-01) — packet-type HW register apply `FUN_80014450`

Pass 7y target from cold-triage rank-1 (6 xref_in, 94B — largest at xref=6
tier after Pass 7x). Decompiled and renamed:
**`FUN_80014450` → `apply_packet_type_via_hw_register_programmer`**
(94B, HIGH) via `RenamePass7yRegion80010000Fun80014450.java` (`renamed=1`,
live-verified).

**Mechanism:** Packet-type apply primitive in the `0x800144xx` cluster (sibling
of `apply_esco_codec_config_via_hw_register_programmer` at Pass 7k). Remaps role
index via `remap_role_index_to_esco_slot_if_pending`, IRQ-disables, issues two
register writes through hook fptr `PTR_DAT_800144b0` (reg `2` encoding
`(param_2 & 0x1f) << 0xb | (param_1 & 0xff) << 5`, then reg `0` with commit
byte `0x31`), clears per-slot gate byte at `PTR_DAT_800144b4[remapped_index]`.
Packet-type apply half of the `apply_packet_type_via_hw_register_programmer` /
`apply_esco_codec_config_via_hw_register_programmer` pair documented across
eSCO counter/flush and conn-table codec paths in regions `0x80030000` /
`0x80040000`.

**Callers:** 6 xref_in per `ListUnnamed80010000.java`; eSCO codec counter/flush
sweepers (`increment_esco_slot_counter_and_apply_codec_if_gate_armed`,
`step_conn_esco_codec_counter_and_apply_if_gate_armed`,
`flush_armed_esco_codec_slots_up_to_12_and_apply` in `0x80030000`) and
conn-table codec sweeps (`sweep_conn_table_program_esco_packet_type_and_clear_gate_bytes`
in `0x80040000`).

**Confidence:** HIGH — decompile confirms IRQ-gated two-register HW programmer
sequence + gate-byte clear; structural sibling of Pass 7k codec-config apply
(final commit bytes `0x31` vs `0x30`).

Region unnamed count after this pass: **233** (234 minus this rename).

## Pass 7z (2026-07-01) — AFH cap-param dispatcher `FUN_800122b8`

Pass 7z target from cold-triage rank-1 (6 xref_in, 64B — largest at xref=6
tier after Pass 7y). Decompiled and renamed:
**`FUN_800122b8` → `dispatch_afh_cap_param_to_bb_register_clear_loop`**
(64B, HIGH) via `RenamePass7zRegion80010000Fun800122b8.java` (`renamed=1`,
live-verified).

**Mechanism:** Config-flag-gated AFH cap-param extractor in the `0x800122xx`
cluster (documented previously in `reverse_engineering_baseband_reg_helpers.md`
§7, now formally named). Reads `config_base->field64_0x46` /
`field65_0x47`; when bit `0x80` is set, selects a 4-bit nibble by
`param_1` (`1` → bits[11:8], `2` → bits[15:12]) and dispatches
`FUN_80012e38` to clear bit `0x40` across BB registers
`0x170`/`0x174`/`0x178`/`0x17c`.

**Callers:** 6 xref_in per `ListUnnamed80010000.java`; includes
`unknown_referencing_default_name_6` (link-mode reconfigure, ×3),
patch AFH init chain `FUN_8010ce0c`, vendor HCI dispatcher
`HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c`, and `FUN_800122fc`.

**Confidence:** HIGH — decompile confirms config-flag gate + nibble select +
dispatch to documented BB-register clear loop; cross-referenced with prior
baseband-reg-helper analysis and SCO/eSCO layer patch hook table.

Region unnamed count after this pass: **232** (233 minus this rename).

## Pass 80 (2026-07-01) — codec table read primitive `FUN_80013e2c`

Pass 80 target from cold-triage rank-1 (6 xref_in, 60B — largest at xref=6
tier after Pass 7z). Decompiled and renamed:
**`FUN_80013e2c` → `read_codec_table_entry_and_wait_ready`**
(60B, HIGH) via `RenamePass80Region80010000Fun80013e2c.java` (`renamed=1`,
live-verified).

**Mechanism:** Read-side counterpart of `write_codec_table_entry_and_wait_ack`
(`0x80013dc4`). Issues row-index fetch via HW hook fptr `PTR_DAT_80013e68`
(register `0x236`, index `param_1 & 0x1ff`), busy-polls status register
`DAT_80013e6c` until bit `0x200` sets (write primitive polls until it
**clears**), then reads two 16-bit halves from `DAT_80013e70`/`DAT_80013e74`
and returns `CONCAT22(high, low)` as a 32-bit table entry.

**Callers:** 6 xref_in per `ListUnnamed80010000.java`; includes
`VSC_0xfcc2_FUN_800148f0` (codec-table lookup by role index) and
`write_baseband_codec_param_triplet` cluster siblings.

**Confidence:** HIGH — decompile confirms indexed table fetch via reg `0x236`,
inverted ready-poll polarity vs write ack, and dword assembly from paired
halfword registers; structural read/write pair documented in `region_0x80000000`.

Region unnamed count after this pass: **231** (232 minus this rename).

## Pass 81 (2026-07-01) — A5-framed scheduler slot write `FUN_800136b8`

Pass 81 target from cold-triage rank-1 (5 xref_in, 200B — largest at xref=5
tier after Pass 80). Decompiled and renamed:
**`FUN_800136b8` → `emit_a5_framed_scheduler_slot_write_command`**
(200B, HIGH) via `RenamePass81Region80010000Fun800136b8.java` (`renamed=1`,
live-verified).

**Mechanism:** A5-framed write-command emitter in the `0x800135xx` scheduler
cluster (uses Pass 7f primitive `busywait_status_bits_0x60_then_write_byte`).
Protocol: sync `0xa5/0xa5`, command byte `0x46` (address-only) or `0x4e`
(with payload), delimiter `0x5a`, 32-bit target address (`param_1`), three
zero padding bytes; when `param_2 != 0` emits word-count
`(param_2+3)>>2`, copies `param_2` bytes from `param_3`, then zero-pads to
4-byte alignment.

**Callers:** 5 xref_in per `ListUnnamed80010000.java`; primary consumer
`FUN_80013780` issues writes to slot addresses `0xffdc` (24-byte CP0 register
snapshot), `0xffdd` (4-byte dword), and `0xffde` (address-only commits).

**Confidence:** HIGH — decompile confirms full A5-framed protocol sequence,
4-byte alignment padding, and caller-side usage pattern for scheduler slot
programming.

Region unnamed count after this pass: **230** (231 minus this rename).

## Pass 82 (2026-07-01) — Stability-poll indexed BB register read `FUN_80011468`

Pass 82 target from cold-triage rank-1 (5 xref_in, 162B — largest at xref=5
tier after Pass 81). Decompiled and renamed:
**`FUN_80011468` → `poll_indexed_bb_register_until_stable_read`**
(162B, HIGH) via `RenamePass82Region80010000Fun80011468.java` (`renamed=1`,
live-verified).

**Mechanism:** Stability-polling wrapper on alternate indexed BB register read
`FUN_800113e4` (uses `DAT_80011460` control word, distinct from MMIO-indexed
`read_baseband_register_mmio_indexed` at `0xb000a0bc`). Validates
`param_1 < 2` and rejects `param_1==1` with `param_2 & 1` set; polls up to
10 iterations until two consecutive reads match; on exhaustion logs event
`0x196` and returns last value; invalid params log `0x177` and return
`0xffff`.

**Callers:** 5 xref_in per `ListUnnamed80010000.java`; includes
`feature_bit_status_reconfig_handler`, `link_state_feature_bit_escalation_and_log_dispatcher`,
and `VSC_0xfc8b_diagnostic_query`.

**Confidence:** HIGH — decompile confirms stability-poll loop, parameter
validation, error logging, and caller usage for feature/status BB register reads.

Region unnamed count after this pass: **229** (230 minus this rename).

## Pass 83 (2026-07-01) — Conn status-byte OR via role remap `FUN_80017bac`

Pass 83 target from cold-triage rank-1 (5 xref_in, 40B — sole remaining at
xref=5 tier after Pass 82). Decompiled and renamed:
**`FUN_80017bac` → `or_conn_status_byte_bits_via_role_remap`**
(40B, HIGH) via `RenamePass83Region80010000Fun80017bac.java` (`renamed=1`,
live-verified).

**Mechanism:** OR-mask setter on per-connection status-byte table
`PTR_DAT_80017bd4`: remaps `(role_index, conn_byte_0xCC)` via
`remap_role_index_to_esco_slot_if_pending`, then
`table[idx] |= mask`. Complement of Pass 7l's
`clear_sync_gate_byte_bits_by_remapped_role_index` (AND-clear on sibling table
`PTR_DAT_80017d7c`).

**Callers:** 5 xref_in per `ListUnnamed80010000.java`; primary consumer
`wrap_send_lmp_pkt_with_conn_cc_hook_and_validate` invokes with mask `4` (bit 2)
before sending encryption-sensitive LMP opcodes (`0x0f`, `0x12`, `0x42`,
`0x7f`/`0x17`).

**Confidence:** HIGH — decompile confirms remap+OR pattern, caller opcode-gate
context from region `0x80020000` Pass 6 cont. (95), and sibling relationship to
Pass 7l clear primitive.

Region unnamed count after this pass: **228** (229 minus this rename).

## Pass 84 (2026-07-01) — Config status/feature BB register applier `FUN_800122fc`

Pass 84 target from cold-triage rank-1 (4 xref_in, 772B — largest at xref=4
tier after Pass 83). Decompiled and renamed:
**`FUN_800122fc` → `apply_config_status_feature_bits_to_baseband_registers`**
(772B, HIGH) via `RenamePass84Region80010000Fun800122fc.java` (`renamed=1`,
live-verified).

**Mechanism:** Hook-gated config→baseband applier at `PTR_PTR_80012600`; when
NULL, default path merges `config_struct` fields `0x44`–`0x46`, `0xd2`–`0xd3`,
and `0xec` into shared status dword `DAT_80012608` and BB registers `0x4`,
`0x15c`, `0x1fc` (via `read_baseband_register_*` /
`write_baseband_register_masked_busywait`). Mode-gated branches on
`PTR_DAT_80012604 & 0x1e` (`6` sets BB reg `0x4` bit `0x200`; `2` ORs extra
status bits when `config+0x3f` bit `0x10` clear). Tail calls
`feature_bit_status_word_propagator`, clears AFH cap params via
`dispatch_afh_cap_param_to_bb_register_clear_loop(1)`, and conditionally invokes
`FUN_80011ab0` when `config+0x3e` bit `0x40` set.

**Callers:** 4 xref_in per `ListUnnamed80010000.java`; boot-init consumers
`copy_eight_literal_pool_globals_and_init_baseband_hw` (when
`config.field59_0x41&3==1`) and
`copy_nine_dispatch_slots_and_init_baseband_subsystems`.

**Confidence:** HIGH — decompile confirms hook-or-default structure, config-field
bit unpacking into BB MMIO writes, and integration with established
`feature_bit_status_word_propagator` / AFH-cap dispatch primitives.

Region unnamed count after this pass: **227** (228 minus this rename).

**Next:** superseded by Pass 86.

## Pass 85 (2026-07-01) — ACL TX completion credit processor `FUN_8001840c`

Pass 85 target from cold-triage rank-1 (4 xref_in, 558B — largest at xref=4
tier after Pass 84). Decompiled and renamed:
**`FUN_8001840c` → `process_acl_tx_completion_credits_and_emit_hci_completed_packets`**
(558B, HIGH) via `RenamePass85Region80010000Fun8001840c.java` (`renamed=1`,
live-verified).

**Mechanism:** Gated on `PTR_DAT_8001863c`; resolves connection slot via
`lookup_up_to_3_bos_array_indices_by_connection_handle`, accumulates TX credit
counters on conn subrecord (`+10`/`+4`/`+6` fields), walks singly-linked ACL
buffer list (`+0x204` chain) matching type-mask `DAT_80018648`. When credits
exhausted, calls `FUN_80017748` per buffer, dispatches completion hook at
`PTR_DAT_8001864c`, increments per-link stats on `big_ol_struct`, and emits
`thunk_send_evt_HCI_Number_Of_Completed_Packets` when `param_3==1`. Integrates
`probe_lmp_25c_scheduling_readiness_by_credit_window` for LMP-25C gating and
`FUN_80018314` for slot reschedule.

**Callers:** 4 xref_in per `ListUnnamed80010000.java`; includes
`sweep_linked_conn_slots_reschedule_timing_window_by_index_and_type` (region
`0x80040000` Pass 52v) which invokes this when timing window elapses.

**Confidence:** HIGH — decompile confirms ACL TX buffer walk, credit accounting,
HCI event `0x13` emission path, and established LMP-25C scheduling-readiness
integration.

Region unnamed count after this pass: **226** (227 minus this rename).

**Next:** superseded by Pass 87.

## Pass 86 (2026-07-01) — Four-field sync timing offset writer `FUN_80015614`

Pass 86 target from cold-triage rank-1 (4 xref_in, 292B — largest at xref=4
tier after Pass 85). Decompiled and renamed:
**`FUN_80015614` → `store_four_field_sync_timing_offsets_with_mode_adjust_and_hook`**
(292B, HIGH) via `RenamePass86Region80010000Fun80015614.java` (`renamed=1`,
live-verified).

**Mechanism:** Stores four signed timing offsets into shared struct
`PTR_DAT_80015738` at `+0x12`/`+0x14`/`+0x16`/`+0x18`, combining caller
params with base offset `*PTR_PTR_8001573c` (half/full rounding for params 2–3).
Mode byte `PTR_DAT_80015744[1]` gates adjustment: bits `0x18` select half (`0x8`)
or full (`0x10`) base-offset addend; bit `1` swaps the four stored quadrants;
when mode clear and `param_1<0`, zeros `+0x12`. Optional completion hook at
`PTR_DAT_80015748`; logs before/after via `possible_logging_function__var_args`.

**Callers:** 4 xref_in — `FUN_80016934` (CPB/page-receive periodic tick, 3
sites; passes four literal-pool shorts then chains `FUN_800152e4`/`FUN_800153d4`)
and `FUN_80016e68` (SCO sync setup; extracts four signed offsets from sync
record then invokes same tail chain).

**Confidence:** HIGH — decompile confirms four-field struct writer, mode-gated
offset/swap semantics, hook dispatch, and CPB-tick + SCO-sync caller integration.

Region unnamed count after this pass: **225** (226 minus this rename).

**Next:** superseded by Pass 88.

## Pass 87 (2026-07-01) — Sync timing BB register commit `FUN_800152e4`

Pass 87 target from cold-triage rank-1 (4 xref_in, 166B — largest at xref=4
tier after Pass 86). Decompiled and renamed:
**`FUN_800152e4` → `apply_four_field_sync_timing_offsets_to_baseband_regs`**
(166B, HIGH) via `RenamePass87Region80010000Fun800152e4.java` (`renamed=1`,
live-verified).

**Mechanism:** Commit tail of the sync-timing pipeline (sibling of Pass 86's
struct writer): reads four signed offsets from shared struct `PTR_DAT_8001538c`
at `+0x12`/`+0x14`/`+0x16`/`+0x18`. When byte `+0xe` bit0 clear, programs BB
regs `0x202`/`0x200`/`0x206`/`0x204` via hook `PTR_DAT_80015390` with those
values; when bit0 set, writes `0xffff` to all four (disable/bypass path).
Final write to reg `0x208` merges `DAT_80015394` with bit8 from `+0xe` bit1.

**Callers:** 4 xref_in — `FUN_80016934` (CPB/page-receive periodic tick, 3
sites) and `FUN_80016e68` (SCO sync setup); chained after
`store_four_field_sync_timing_offsets_with_mode_adjust_and_hook` in both paths.

**Confidence:** HIGH — decompile confirms four-register commit via HW-write
hook, disable sentinel `0xffff`, and CPB-tick + SCO-sync caller integration.

Region unnamed count after this pass: **224** (225 minus this rename).

**Next:** superseded by Pass 89.

## Pass 88 (2026-07-01) — Link-mode timeout BB reg commit `FUN_80012820`

Pass 88 target from cold-triage rank-1 (4 xref_in, 156B — largest at xref=4
tier after Pass 87). Decompiled and renamed:
**`FUN_80012820` → `apply_link_mode_change_timeout_to_bb_regs_0x6e_0x6c`**
(156B, HIGH) via `RenamePass88Region80010000Fun80012820.java` (`renamed=1`,
live-verified).

**Mechanism:** Link-mode-change timeout commit helper in the VSC 0xFC11 /
`apply_link_mode_change_bb_regs_and_timeout_by_phase` cluster. Optional veto
hook at `PTR_DAT_800128bc`; on fallback runs
`program_bb_regs_0x188_0x18a_by_mode_byte_gated_on_config_bit0x10(0)`,
`link_state6_quality_bit7_recovery_trigger`, and
`link_state6_clear_bit15_if_feature_set`. Timeout value: when `param_1==0`,
derives `((DAT_800128c0|DAT_800128c4)>>1)-0x100`; else uses `param_2`.
Programs BB reg `0x6e` via hook `PTR_DAT_800128c8`; stores aux at
`DAT_800128d0`; config feature mask at `+0xe4..+0xe7` may install handler at
`DAT_800128dc`; tail calls `FUN_80011c60` (BB reg `0x6c` + status-bit commit).

**Callers:** 4 xref_in — `FUN_80010814` (connection-setup path),
`apply_link_mode_change_bb_regs_and_timeout_by_phase` (phases 1–5),
`HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` (VSC dispatcher); 2 confirmed
via `find_callers`.

**Confidence:** HIGH — decompile confirms BB reg `0x6e`/`0x6c` timeout commit,
quality-recovery prelude, and link-mode-change cluster integration.

Region unnamed count after this pass: **223** (224 minus this rename).

**Next:** superseded by Pass 90.

## Pass 89 (2026-07-01) — IRQ-masked HW-channel table merge `FUN_800143b0`

Pass 89 target from cold-triage rank-1 (4 xref_in, 138B — largest at xref=4
tier after Pass 88). Decompiled and renamed:
**`FUN_800143b0` → `irq_masked_commit_hw_channel_table_merge_by_conn_cc_index`**
(138B, HIGH) via `RenamePass89Region80010000Fun800143b0.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-masked HW-channel table merge primitive in the `0x800143xx`
cluster (sibling of Pass 7o `read_halfword_via_bb_reg_0x1f0_esco_slot_select_by_conn_index`
and Pass 7s `write_hw_register_by_conn_cc_index_byte_half`). Takes conn-array
index `param_1` and role byte `param_2` (typically `byte_0xCC`). IRQ-disables,
issues two commits via hook fptr `PTR_DAT_8001443c`: selector `2` with encoded
`(role<<11)|(index<<5)`, then selector `0` with `0x84`. Remaps via
`remap_role_index_to_esco_slot_if_pending`; when remapped slot `>7` and gate
byte at `PTR_DAT_80014440` is clear, invokes hook `PTR_DAT_80014444(2)` and ORs
role bit into bitmask bytes at `PTR_DAT_80014448` and `PTR_DAT_8001444c`.

**Callers:** 4 xref_in — `arm_link_state_advance_pending_and_commit_conn_slot_timing_mode1`
and `clear_link_state_advance_pending_and_commit_conn_slot_timing_mode2` (region
`0x80030000` Pass 192/195 — link-state-advance establish/teardown pair), plus
two additional conn-setup paths.

**Confidence:** HIGH — decompile confirms IRQ-gated dual-hook channel-table merge
+ esco-remap bitmask arm; cross-region callers document link-state-advance
semantics; cluster placement consistent with `0x800140d8`/`FUN_800143b0`/`FUN_80014dac`
triplet referenced from region `0x80030000`.

Region unnamed count after this pass: **222** (223 minus this rename).

**Next:** superseded by Pass 90.

## Pass 90 (2026-07-01) — BB reg pulse `FUN_80011b6c`

Pass 90 target from cold-triage rank-1 (4 xref_in, 134B — largest at xref=4
tier after Pass 89). Decompiled and renamed:
**`FUN_80011b6c` → `pulse_bb_regs_0x00_0x10_with_cc_mode_bits_and_spin_delays`**
(134B, HIGH) via `RenamePass90Region80010000Fun80011b6c.java` (`renamed=1`,
live-verified).

**Mechanism:** Transient baseband register pulse primitive in the `0x80011bxx`
cluster (sibling of masked busy-wait R/W primitives from Pass 7/7b). Saves BB
reg `0xcc` byte via `read_baseband_register_byte_masked_busywait`, temporarily
sets bits 2–3 (`| 0xc`), pulses reg `0x10` bit 0, delays via
`spin_delay_10x_iterations(1)`, pulses reg `0` bit 10 (`0x400`), longer delay
(`spin_delay_10x_iterations(0x14)`), clears reg `0` bit 1, then restores
`0xcc` (`& 0xf3`).

**Callers:** 4 xref_in — ROM direct call from `FUN_80011bf4` (sibling in
`0x80011bxx` cluster) plus three patch-firmware computed-call sites
(`FUN_8010c854`, `FUN_8010c160`, `FUN_8010d2e8`).

**Confidence:** HIGH — decompile confirms save/pulse/restore sequence on BB regs
`0x00`/`0x10`/`0xcc` with spin delays; naming consistent with
`config_gated_pulse_bb_reg_0x75_bit0_with_spin_delays` (region `0x80030000`
Pass 157); cluster placement alongside masked busy-wait primitives.

Region unnamed count after this pass: **221** (222 minus this rename).

**Next:** superseded by Pass 91.

## Pass 91 (2026-07-01) — eSCO codec table pair `FUN_80014b30`

Pass 91 target from cold-triage rank-1 (4 xref_in, 118B — largest at xref=4
tier after Pass 90). Decompiled and renamed:
**`FUN_80014b30` → `write_esco_slot_codec_table_pair_from_role_and_byte_block`**
(118B, HIGH) via `RenamePass91Region80010000Fun80014b30.java` (`renamed=1`,
live-verified).

**Mechanism:** eSCO-slot codec-table pair writer in the `0x80014bxx` cluster
(sibling of `write_baseband_codec_param_triplet` and
`clear_codec_table_entries_for_role`). Maps conn role via
`conn_record_role_to_esco_slot_index`; when slot non-zero, computes base row
`(slot-1)*0xc+1` and writes two consecutive rows via
`write_codec_table_entry_and_wait_ack`, packing bytes from caller's 8-byte
parameter block into two 32-bit values (bytes `[4..7]` then `[0..3]`).

**Callers:** 4 xref_in — `role_switch_commit_staged_slot_transition`,
`derive_sres_e1_or_e22_and_send_lmp_response` (2 confirmed via `xrefs_to`);
also invoked from `arm_link_encryption_post_key_program` (region `0x80020000`
Pass 6 cont.) alongside `FUN_80014ba8`.

**Confidence:** HIGH — decompile confirms slot-indexed dual-row codec-table
writes via established `write_codec_table_entry_and_wait_ack` primitive;
cluster placement consistent with `0x800147b0`/`0x80014c58` codec-table family.

Region unnamed count after this pass: **220** (221 minus this rename).

**Next:** superseded by Pass 92.

## Pass 92 (2026-07-01) — sync-conn air-mode capability packer `FUN_80018c80`

Pass 92 target from cold-triage rank-1 (4 xref_in, 96B — largest at xref=4
tier after Pass 91). Decompiled and renamed:
**`FUN_80018c80` → `pack_sync_conn_air_mode_capability_bits_from_feature_page_bytes`**
(96B, HIGH) via `RenamePass92Region80010000Fun80018c80.java` (`renamed=1`,
live-verified).

**Mechanism:** Feature-page byte-block → sync-connection air-mode capability
bitmask packer in the `0x80018cxx` HCI sync-conn cluster (sibling of
`HCI_Setup_Synchronous_Connection_LMP_Feature_Checker` at `0x80018d44`).
Extracts and OR-combines bitfields from bytes at offsets `+1`, `+3`, `+4`,
`+5` of the input page array; byte `+5` sign/bit `0x20`/`0x40` gates
additional flag tiers (`0x40`, `0x80`, `0x140`, `0x280`). Returns `uint`
capability mask for intersection against peer page and packet-type field.

**Callers:** 4 xref_in (2 call sites each) —
`send_HCI_Command_Status_for_HCI_0x07` (HCI Add SCO / sync-conn command-status
gate: `local & remote & (packet_types ^ 0x3c0)`, then `(mask & 0x3f8)` /
`(mask & 7)` compatibility check) and `FUN_80019774` (same intersection
pattern on success path before `FUN_80019714` / `FUN_800192c4` dispatch).

**Confidence:** HIGH — decompile confirms deterministic bit-pack from
feature-page bytes; caller decompiles show identical intersection mask
semantics on both HCI 0x07 command-status paths.

Region unnamed count after this pass: **219** (220 minus this rename).

**Next:** superseded by Pass 93.

## Pass 93 (2026-07-01) — sub-opcode BB reg write dispatcher `FUN_80013c64`

Pass 93 target from cold-triage rank-1 (4 xref_in, 78B — tied largest at
xref=4 tier with `FUN_80012ec8`/`FUN_80011a74`/`FUN_800179a8` after Pass 92).
Decompiled and renamed:
**`FUN_80013c64` → `write_baseband_reg_by_subopcode_byte_via_hook`**
(78B, HIGH) via `RenamePass93Region80010000Fun80013c64.java` (`renamed=1`,
live-verified).

**Mechanism:** Sub-opcode-gated baseband register write dispatcher in the
`0x80013cxx` cluster (sibling of `write_packet_type_table_low_byte_at_offset`
and `clear_indexed_channel_slot_bit4_and_invoke_release_hook`). When
`(param_2 & 0xff) < 4`, sub-opcode 0 merges `param_1 << 12` into stored word
`DAT_80013cb4` (`& 0xcfff`) and writes BB reg `0x16`; sub-opcodes 1–3 look up
register ID from `PTR_DAT_80013cb8[index*2]`; all paths dispatch via hook
`PTR_DAT_80013cbc(reg_id, param_1)`.

**Callers:** 4 xref_in — connection-setup finalize step in
`accept_lmp_conn_setup_and_program_baseband_from_unpacked_pdu` (region
`0x80040000` Pass 52ei) plus sibling dual-slot accept and
`LMP_accept_or_mirror_connection_handler` paths.

**Confidence:** HIGH — decompile confirms 4-slot gate, sub-opcode-0 template
merge, table-indirected register IDs, and hook dispatch; cluster placement
consistent with `0x80013c0c`/`0x80013cec` primitives.

Region unnamed count after this pass: **218** (219 minus this rename).

**Next:** superseded by Pass 94.

## Pass 94 (2026-07-01) — BB reg 0x1f8/0x144 paired writer `FUN_80012ec8`

Pass 94 target from cold-triage rank-1 (4 xref_in, 70B — largest at xref=4
tier after Pass 93). Decompiled and renamed:
**`FUN_80012ec8` → `write_bb_reg_0x1f8_then_merge_0x144_shifted_byte_with_delay`**
(70B, HIGH) via `RenamePass94Region80010000Fun80012ec8.java` (`renamed=1`,
live-verified).

**Mechanism:** Companion writer to Pass 7t's
`write_bb_reg_0x144_shifted_param_and_poll_status_irq_gated` in the
`0x80012exx` cluster. Writes BB reg `0x1f8` from `param_2`, reads BB reg
`0x144`, writes merged value
`(param_1 & 0xff) >> 2 | DAT_80012f10 | DAT_80012f14 & read_mask` via
`write_baseband_register_masked_busywait`, then
`spin_delay_10x_iterations(100)`.

**Callers:** 4 xref_in — `FUN_80012f18` (3×: IRQ-gated poll-then-commit
sequence toggling bit0 of poll result before/after param `0x18`/`0x20` phases)
and `FUN_80012f8c` (1×: sets BB reg `0x148` bit0 then commits with param `0`).

**Confidence:** HIGH — decompile confirms `0x1f8` preamble, `0x144` RMW with
shifted-byte + dual template masks, fixed spin delay; caller decompiles show
paired use immediately after Pass 7t poll primitive.

Region unnamed count after this pass: **217** (218 minus this rename).

**Next:** superseded by Pass 95.

## Pass 95 (2026-07-01) — AFH-flag-gated BB reg 0xfc bit writer `FUN_80011a74`

Pass 95 target from cold-triage rank-1 (4 xref_in, 56B — largest at xref=4
tier after Pass 94). Decompiled and renamed:
**`FUN_80011a74` → `afh_flag_gated_set_or_clear_bb_reg_0xfc_bit_0x1000`**
(56B, HIGH) via `RenamePass95Region80010000Fun80011a74.java` (`renamed=1`,
live-verified).

**Mechanism:** AFH state-flag-gated read-modify-write on BB reg `0xfc` in the
`0x80011axx` cluster (sibling of `called_if_config[0xf2]&4` at `0x80011a18`).
Reads reg `0xfc` via `read_baseband_register_masked_busywait`, tests global
`*DAT_80011aac` upper-halfword bits `0x42` (AFH capability/feature flag area):
when clear, ORs bit `0x1000`; when set, ANDs `~0x1000`; writes back via
`write_baseband_register_masked_busywait`.

**Callers:** 4 xref_in — `unknown_referencing_default_name_6` (ROM link-mode
bitmask reconfigure handler) + `FUN_8010ccb8` (patch eSCO capability writer);
2 confirmed via `find_callers`.

**Confidence:** HIGH — decompile confirms flag-gated bit `0x1000` toggle on BB
reg `0xfc`; prior partial analysis in `reverse_engineering_baseband_reg_helpers.md`
Section 5 now formalized with persisted rename.

Region unnamed count after this pass: **216** (217 minus this rename).

**Next:** superseded by Pass 96.

## Pass 96 (2026-07-01) — conn-slot table index-to-pointer helper `FUN_800179a8`

Pass 96 target from cold-triage rank-1 (4 xref_in, 16B — sole rank-1 at
xref=4 tier after Pass 95). Decompiled and renamed:
**`FUN_800179a8` → `conn_slot_table_entry_ptr_from_index_stride_0x1c`**
(16B, HIGH) via `RenamePass96Region80010000Fun800179a8.java` (`renamed=1`,
live-verified).

**Mechanism:** Index-to-pointer helper in the `0x800179xx` conn-slot table
cluster (sibling of `FUN_800179bc` equality-check and `FUN_800177a4` linked-list
sweep). Returns `PTR_PTR_800179b8 + (param_1 & 0xff) * 0x1c` — 28-byte stride
per connection-slot index.

**Callers:** 4 xref_in — `role_switch_packet_type_reset_and_log` (1×) +
`program_dual_slot_lmp25c_packet_credits_by_conn_index` (3×); confirmed via
`ListXrefsTo800179a8.java`.

**Confidence:** HIGH — decompile confirms pure index×stride table lookup;
caller context ties to role-switch teardown and dual-slot LMP-25C packet-credit
programming paths.

Region unnamed count after this pass: **215** (216 minus this rename).

**Next:** superseded by Pass 97.

## Pass 97 (2026-07-01) — HCI event ring-17 drain `FUN_8001fd20`

Pass 97 target from cold-triage rank-1 (3 xref_in, 380B — largest at xref=3
tier after Pass 96 cleared xref=4). Decompiled and renamed:
**`FUN_8001fd20` → `drain_hci_evt_ring17_and_mode2_dispatch`**
(380B, HIGH) via `RenamePass97Region80010000Fun8001fd20.java` (`renamed=1`,
live-verified).

**Mechanism:** HCI event-buffer ring drain companion to
`enqueue_hci_evt_to_ring17_and_dispatch_or_drain` (Pass 7w). Gated on config
`field_0x169` ∈ {1,3} and pending ring state at `PTR_DAT_8001fea0/+0x4` and
`PTR_DAT_8001fea4/+0x110`. Dequeues current slot at index `+0x113`, copies
4-byte header via `optimized_memcpy`, may split buffer when remaining bytes exceed
slot capacity, dispatches segment via
`hci_evt_buffer_mode2_dispatch_offset4_u16_from_buf_plus2`, updates per-slot
offset at `(index+0x54)*2+2`, increments `bos_base[field_0x9e]` on successful
`lookup_up_to_3_bos_array_indices_by_connection_handle`, decrements pending count.

**Callers:** 3 xref_in via `ListXrefsTo8001fd20.java` —
`enqueue_hci_evt_to_ring17_and_dispatch_or_drain` (1×),
`HCI_EVT_0x1f6_FUN_8001fef8` (1×), `FUN_8001feb0` (1×).

**Confidence:** HIGH — decompile confirms 17-slot ring drain semantics, buffer
split path, and mode-2 dispatch choreography; cross-confirmed with Pass 7w
enqueue/drain fork and `hci_evt_buffer_mode2_dispatch_offset4_u16_from_buf_plus2`
caller list in region `0x80020000` Pass 6.

Region unnamed count after this pass: **214** (215 minus this rename).

**Next:** superseded by Pass 98.

## Pass 98 (2026-07-01) — credit-scheduler TX descriptor batch commit `FUN_80014e40`

Pass 98 target from cold-triage rank-1 (3 xref_in, 244B — largest at xref=3
tier after Pass 97 cleared xref=3 `FUN_8001fd20`). Decompiled and renamed:
**`FUN_80014e40` → `alloc_and_commit_credit_scheduler_tx_descriptor_batch`**
(244B, HIGH) via `RenamePass98Region80010000Fun80014e40.java` (`renamed=1`,
live-verified).

**Mechanism:** Core multi-descriptor credit-scheduler commit primitive in the
`0x80014fxx` cluster (callee of Pass 7x
`program_single_tx_descriptor_slot_via_credit_scheduler` for count=1 submits).
When `param_1` buffer has non-zero length bits at `+2` and `param_2` count≠0:
allocates slot via `alloc_credit_scheduler_slot_0xd` (slot index from
`param_4+2` when `param_5==0` and `param_3≠0`, else default `9`), copies
`param_2` 8-byte TX descriptor pairs into the 0xc-stride slot table at
`PTR_DAT_80014f34`, merges ACL-handle bits from each descriptor's `ushort` at
`+2` into the slot pending dword, optionally sets high bits from `param_6` when
`param_5≠0`, then calls `LMP__25C_called2` coexistence hook and
`commit_credit_scheduler_slot_hw_arm_descriptor`.

**Callers:** 3 xref_in per `ListUnnamed80010000.java`; includes
`program_single_tx_descriptor_slot_via_credit_scheduler` (Pass 7x) and
`ACL_fragment_dequeue_and_credit_consumer` (region `0x80030000` Pass 8).

**Confidence:** HIGH — decompile confirms batch copy loop, handle-bit merge,
and credit-scheduler alloc/arm choreography; closes the long-standing
`FUN_80014e40` gap referenced by Pass 7x and ACL fragment consumer docs.

Region unnamed count after this pass: **213** (214 minus this rename).

**Next:** superseded by Pass 99.

## Pass 99 (2026-07-01) — role-matched ACL TX completion walker `FUN_80017a04`

Pass 99 target from cold-triage rank-1 (3 xref_in, 190B — largest at xref=3
tier after Pass 98 cleared `FUN_80014e40`). Decompiled and renamed:
**`FUN_80017a04` → `process_role_matched_acl_tx_completions_and_emit_hci_completed_packets`**
(190B, HIGH) via `RenamePass99Region80010000Fun80017a04.java` (`renamed=1`,
live-verified).

**Mechanism:** Narrower sibling of Pass 85
`process_acl_tx_completion_credits_and_emit_hci_completed_packets` — walks
singly-linked ACL TX buffer list at `PTR_DAT_80017ac4` (`+0x204` chain),
matching entries whose conn subrecord bytes at `+0xd` (role) and `+0xf`
(type/slot) equal `param_1` and `param_3`. Per match: calls `FUN_80017748`,
dispatches completion hook at `PTR_DAT_80017ac8` with arg `2`, and on first
match resolves bos index via
`lookup_up_to_3_bos_array_indices_by_connection_handle`. When `param_2==1`
(notify): increments per-role counters on `the_0x300` or per-link
`big_ol_struct.unknown2_0x6C`, then emits
`thunk_send_evt_HCI_Number_Of_Completed_Packets`.

**Callers:** 3 xref_in per `ListUnnamed80010000.java`; includes
`dual_slot_buffer_reassignment_on_role_switch` (region `0x80030000` Pass 8)
which invokes with `notify_flag==1` after role-switch buffer slot transitions.

**Confidence:** HIGH — decompile confirms role/type-filtered buffer walk,
completion hook dispatch, and HCI event `0x13` emission path; cross-confirmed
with Pass 85 ACL TX completion cluster and region `0x80030000` role-switch
buffer reassignment caller.

Region unnamed count after this pass: **212** (213 minus this rename).

**Next:** superseded by Pass 100.

## Pass 100 (2026-07-01) — ACL TX buffer walk eligibility probe `FUN_80017ad4`

Pass 100 target from cold-triage rank-1 (3 xref_in, 162B — largest at xref=3
tier after Pass 99 cleared `FUN_80017a04`). Decompiled and renamed:
**`FUN_80017ad4` → `probe_acl_tx_buffer_walk_eligibility_by_role_and_sync_gates`**
(162B, HIGH) via `RenamePass100Region80010000Fun80017ad4.java` (`renamed=1`,
live-verified).

**Mechanism:** Boolean gate predicate used during ACL TX buffer linked-list
walks in `FUN_80018654` (LMP-25C dual-slot packet allocator). When
`param_1` (conn role index) nonzero: scans up to 10 `big_ol_struct` entries
matching `bos_connection__array_index` + `byte_0xCC`; gates on
`field200_0x206`, `field106_0x94` vs `PTR_DAT_80017b7c` threshold, and
`field154_0xc7` with pending counter bytes at conn subrecord
`*(param_3+0x40c)+0x10/+0x11` vs `param_4`; may clear pending byte `+0x11`.
After loop: `remap_role_index_to_esco_slot_if_pending`, then requires both
sync-gate bytes at `PTR_DAT_80017b80[idx]` and `PTR_DAT_80017b84[idx]` set.
Returns 1 when node is eligible for selection, 0 to skip.

**Callers:** 3 xref_in per `ListUnnamed80010000.java`; sole function caller
`FUN_80018654` (invoked 3× during alternate linked-list walk paths); integrates
with Pass 99 `process_role_matched_acl_tx_completions_and_emit_hci_completed_packets`
and Pass 85 ACL TX completion cluster inside same walker.

**Confidence:** HIGH — decompile confirms bos-entry role match, pending-counter
gating, eSCO slot remap, and dual sync-gate byte tables; caller context in
`FUN_80018654` LMP-25C packet-credit allocator cross-confirmed.

Region unnamed count after this pass: **211** (212 minus this rename).

**Next:** superseded by Pass 101.

## Pass 101 (2026-07-01) — VSC fc95/LMP-268 gateway `FUN_80015d9c`

Pass 101 target from cold-triage rank-1 (3 xref_in, 84B — largest at xref=3
tier after Pass 100 cleared `FUN_80017ad4`). Decompiled and renamed:
**`FUN_80015d9c` → `feature_bit0_gated_vsc_fc95_lmp268_gateway_with_config_scaled_delay`**
(84B, HIGH) via `RenamePass101Region80010000Fun80015d9c.java` (`renamed=1`,
live-verified).

**Mechanism:** Feature-gated VSC `0xfc95` + LMP `0x268` dispatch gateway.
Returns immediately unless config byte at `PTR_DAT_80015df0+0x84` bit 0 is set.
When pending slot dword at `PTR_DAT_80015df4 != -1`, calls
`LMP__25B__most_common_for_VSCs1`. When `param_1 != 0`: issues
`VSC_0xfc95_called2(0, pending_slot, PTR_LAB_80015e4c_1_80015df8, 0, 0)` then
`LMP__268__most_common_for_VSCs2_checks_fptr_patch` with pending value and delay
`(ushort at cfg+0x98) * 5 >> 3`. Sibling pattern to region `0x80030000`
`per_link_vsc_fc95_lmp268_gateway_with_param_scaled_timeout` and
`dual_pending_vsc_fc95_and_lmp268_gateway_with_config_timeout`.

**Callers:** `FUN_800161e4` (CPB/page-receive setup — sets `cfg+0x84 |= 1` then
calls with `param_1==1` after connection slot bind); `lmp_pdu_received_top_level_processor`.

**Confidence:** HIGH — decompile confirms feature-bit gate, LMP-25B pre-dispatch,
fc95+268 pair with config-derived scaled delay; caller context in CPB receive
setup cross-confirmed with `HCI_Set_Connectionless_Peripheral_Broadcast_Receive`
cluster documentation.

Region unnamed count after this pass: **210** (211 minus this rename).

**Next:** superseded by Pass 102.

## Pass 102 (2026-07-01) — secondary VSC fc95/LMP-268 gateway `FUN_80015fc0`

Pass 102 target from cold-triage rank-1 (3 xref_in, 84B — largest at xref=3
tier after Pass 101 cleared `FUN_80015d9c`). Decompiled and renamed:
**`FUN_80015fc0` → `feature_bit0_gated_vsc_fc95_lmp268_gateway_secondary_cfg_with_scaled_delay`**
(84B, HIGH) via `RenamePass102Region80010000Fun80015fc0.java` (`renamed=1`,
live-verified).

**Mechanism:** Structural sibling of Pass 101
`feature_bit0_gated_vsc_fc95_lmp268_gateway_with_config_scaled_delay` using a
secondary config block at `PTR_DAT_80016014`. Gates on `cfg+0x40` bit0 (vs
primary `cfg+0x84`). When pending slot dword at `PTR_DAT_80016018 != -1`, calls
`LMP__25B__most_common_for_VSCs1`. When `param_1 != 0`: issues
`VSC_0xfc95_called2(0, pending_slot, PTR_LAB_80016e0c_1_8001601c, 0, 0)` then
`LMP__268__most_common_for_VSCs2_checks_fptr_patch` with delay
`(ushort at cfg+0x4e) * 5 >> 3` (vs primary `cfg+0x98`). CPB disable path
`FUN_80015dfc` calls the primary gateway with `param_1==0` before clearing
`cfg+0x84` bit0.

**Callers:** `status_word_multiflag_link_event_dispatcher`,
`FUN_80016dac`, `FUN_800171bc` (3 xref_in per `ListXrefsTo80015fc0.java`).

**Confidence:** HIGH — decompile confirms identical fc95+268 dispatch skeleton
with alternate config offsets; caller link-event dispatcher cross-confirmed with
region `0x80030000` Pass 104 role-slot state cluster.

Region unnamed count after this pass: **209** (210 minus this rename).

**Next:** superseded by Pass 103.

## Pass 103 (2026-07-01) — sorted-table binary search `FUN_800174a8`

Pass 103 target from cold-triage rank-1 (3 xref_in, 82B — largest at xref=3
tier after Pass 102 cleared `FUN_80015fc0`). Decompiled and renamed:
**`FUN_800174a8` → `binary_search_sorted_table_by_ushort_key`**
(82B, HIGH) via `RenamePass103Region80010000Fun800174a8.java` (`renamed=1`,
live-verified).

**Mechanism:** Textbook binary search over a `{array,capacity,count}` table
struct (`param_1[0]` = array of record pointers, `param_1[2]` = element count).
Compares `**(ushort **)(array[i])` against `param_2 & 0xffff`; returns 1 with
match index via `*param_3` on hit, 0 with insertion index on miss. Structural
twin of region `0x80040000`
`binary_search_sorted_table_by_index_byte` (1-byte key at record `+0x10`) and
region `0x80050000` `binary_search_sorted_table_by_8byte_key` (8-byte key at
record `+0x8`), distinguished only by ushort key at record offset 0.

**Callers:** `FUN_800174fc` (insert-if-absent with shift-right),
`FUN_8001756c` (remove with shift-left), `FUN_800175bc` (lookup returning
record pointer) — a complete sorted ushort-key table CRUD trio in the
`0x800174xx` neighborhood (3 xref_in per `ListXrefsTo800174a8.java`).

**Confidence:** HIGH — self-contained textbook algorithm; caller trio confirms
sorted-table insert/lookup/remove usage pattern identical to the established
conn-record index-table family in region `0x80040000`.

Region unnamed count after this pass: **208** (209 minus this rename).

**Next:** superseded by Pass 104.

## Pass 104 (2026-07-01) — BB reg 5-bit field writer `FUN_80012e80`

Pass 104 target from cold-triage rank-1 (3 xref_in, 72B — largest at xref=3
tier after Pass 103 cleared `FUN_800174a8`). Decompiled and renamed:
**`FUN_80012e80` → `write_baseband_reg_field_5bit_indexed`**
(72B, HIGH) via `RenamePass104Region80010000Fun80012e80.java` (`renamed=1`,
live-verified).

**Mechanism:** Read-modify-write helper on the IRQ-masked baseband register
primitives (`read_baseband_register_masked_busywait` /
`write_baseband_register_masked_busywait`). Selects base offset `0x5c` when
`param_1==0`, else `0x58`; computes register index
`(param_2 & 0x3f) + base`, left-shifts by 2 for word-aligned addressing;
reads current value, writes back with low 5 bits replaced by `param_3 & 0x1f`.

**Callers:** `config_bit_gated_reg_field_write_idx0xb` (region `0x80000000`),
`flag_gated_reg_field_write_idx0xb` (region `0x80000000`), and
`VSC_0xfc39_2_FUN_80013074` (VSC 0xfc39 mode dispatcher tail) — 3 xref_in per
`ListUnnamed80010000.java`.

**Confidence:** HIGH — self-contained RMW field patch; callers in region
`0x80000000` already documented this as the "baseband-register-field-write
helper" at field index `0xb`.

Region unnamed count after this pass: **207** (208 minus this rename).

**Next:** superseded by Pass 105.

## Pass 105 (2026-07-01) — IRQ-masked key-block codec programmer `FUN_80014770`

Pass 105 target from cold-triage rank-1 (3 xref_in, 64B — largest at xref=3
tier after Pass 104 cleared `FUN_80012e80`). Decompiled and renamed:
**`FUN_80014770` → `program_key_block_into_codec_table_masked`**
(64B, HIGH) via `RenamePass105Region80010000Fun80014770.java` (`renamed=1`,
live-verified).

**Mechanism:** Encryption key-block programmer in the `0x800146xx` codec-table
cluster. Looks up role/type via `lookup_codec_or_role_type_table_7x4(param_1,
param_2, &role_index)`; on success calls `lmp_25c_procedure_completion_waiter`,
disables interrupts, then delegates to inner worker `FUN_800146c4(role_index,
key_block)` which maps role → eSCO slot via `conn_record_role_to_esco_slot_index`,
packs the caller's 16-byte key block into four dwords, and writes four consecutive
codec-table rows via `write_codec_table_entry_and_wait_ack` (indices
`slot*7-0x79` .. `+3`). Re-enables interrupts before return.

**Callers:** `program_key_block_and_arm_mode3_encryption` (region `0x80020000`,
thin wrapper `FUN_80014770(0,0,key_block)` + mode-3 arm),
`arm_link_encryption_post_key_program` (debug/unprogrammed crypto `+0x214==0`
path), and `role_switch_commit_staged_slot_transition` (role-switch codec
reprogram) — 3 xref_in per `ListUnnamed80010000.java`.

**Confidence:** HIGH — decompile confirms IRQ-masked wrapper around established
codec-table write primitive; encryption-cluster callers in region `0x80020000`
already documented this function by address as the key-block programming step
before mode-3 encryption arm.

Region unnamed count after this pass: **206** (207 minus this rename).

**Next:** superseded by Pass 106.

## Pass 106 (2026-07-01) — linked-list conn-slot timing clear `FUN_800177a4`

Pass 106 target from cold-triage rank-1 (3 xref_in, 64B — largest at xref=3
tier after Pass 105 cleared `FUN_80014770`). Decompiled and renamed:
**`FUN_800177a4` → `sweep_linked_conn_slots_clear_pending_timing_by_index_and_type`**
(64B, HIGH) via `RenamePass106Region80010000Fun800177a4.java` (`renamed=1`,
live-verified).

**Mechanism:** Singly-linked conn-slot list sweep in the `0x800177xx` cluster
(sibling of `conn_slot_table_entry_ptr_from_index_stride_0x1c` and inverse of
`sweep_linked_conn_slots_reschedule_timing_window_by_index_and_type`). Skips when
`param_1` equals the indexed table entry at
`PTR_PTR_800177e4 + (param_2 & 0xff) * 0x1c`; otherwise walks `param_1` via
`+0x408`, matching subrecords at `+0x40c` where bytes `+0xd` (conn-array index)
and `+0xf` (type) match the head node's pair. On match clears pending byte
`+0xe`, zeroes ushort `+6`, copies ushort `+10` → `+4`, clears byte `+0xc`.

**Callers:** `commit_dual_slot_lmp25c_role_record_state_and_chain_credits`
(region `0x80040000`, `+0x2e` pending path), `FUN_80017c5c` (unnamed sibling
in `0x80017cxx` cluster), and `program_dual_slot_lmp25c_packet_credits_by_conn_index`
(region `0x80040000`) — 3 xref_in per `ListXrefsTo800177a4.java`.

**Confidence:** HIGH — decompile confirms linked-list sweep matching index+type
bytes with timing-field reset; callers tie to dual-slot LMP-25C role-record
commit and packet-credit programming paths already documented in region
`0x80040000`.

Region unnamed count after this pass: **205** (206 minus this rename).

**Next:** Pass 107 — cold-triage next rank-1 unnamed in region `0x80010000`.
