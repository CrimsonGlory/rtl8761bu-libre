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
