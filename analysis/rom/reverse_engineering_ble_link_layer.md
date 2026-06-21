# Reverse Engineering: BLE Link Layer (Advertising / Scanning / Connection State Machine)

**Memory block:** ROM (fixed in chip silicon)
**Source:** Kovah `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`, decompiled via Ghidra
12.1.2 headless, GZF process mode
**Tools used:** `DiagAddr.java` / `DecompileAddr.java` with 0x-prefixed `script_args`
(one address per call); `FindStringRefs.java` (confirmed non-functional on this
binary — see Tool Note); `FindXrefsTo.java` (confirmed stale/non-repurposable —
known wairz bug, see Tool Note)
**Date:** 2026-06-21. Ticket: work-in-progress.txt "Document the BLE link
layer state machine, if present in this ROM"

---

## Summary — correction to the prior doc's conclusion

`reverse_engineering_hci_command_router.md` (completed immediately before this
pass) concluded that OGF 0x08 (LE Commands) "ships essentially no native LE
command support" in this ROM, based on the OCF jump table at `0x8004472c`
having exactly one live entry (OCF=0) pointing at RAM slot `0x801206ac`. That
finding is **correct as far as it goes** — re-confirmed in this pass (see
Part 1) — but it only examined the **inbound command-parsing** side of OGF 8.

This pass looked at the **outbound event-generation** side (`HCI_LE_Meta_Event`,
opcode `0x3e`, the standard way the LE controller reports asynchronous
link-layer events to the host) and found something materially different:

**This ROM contains a large, fully-named, fully-decompiled cluster of at
least 17 distinct `send_evt_Meta_subevent_0x##` functions at
`0x80044730`–`0x80046620+`, covering nearly every LE Meta Event subevent
defined through Bluetooth 5.x — including LE Connection Complete, LE
Advertising Report, LE Connection Update Complete, LE Long Term Key Request,
LE Remote Connection Parameter Request, LE Data Length Change, LE PHY Update
Complete, LE Channel Selection Algorithm, and the LE Secure Connections P-256/
DHKey pair.** These are not stubs: the largest (`send_evt_Meta_subevent_0x01_or_0x0a`,
1032 bytes) fully assembles a real `HCI_LE_Connection_Complete` /
`HCI_LE_Enhanced_Connection_Complete` payload — connection handle, role, peer
address + address type, connection interval, peripheral latency, supervision
timeout, master clock accuracy, channel-selection-algorithm bit — by reading
the **same shared per-connection-record struct array**
(`PTR_base_of_0x1ac_struct_array_0xA_large2`) already documented for BT
Classic SCO/eSCO in `reverse_engineering_conn_record_subsystem.md`. The LE
Advertising Report sender (`send_evt_Meta_subevent_0x2_or_0x0b`, 332 bytes)
loops over a report count, copying 6-byte BD_ADDRs, RSSI, address-type, and
AD-payload bytes into the spec-shaped report array.

**Conclusion: BLE link-layer *event reporting* genuinely lives in this ROM**
— it is not a patch-only or "ROM ships nothing" situation for that side of
the stack. What was *not* found in this pass, despite a real search effort
(detailed in Part 3), is a single, named, monolithic "BLE link-layer state
machine" *driver* function — i.e. the code that runs the actual RF-level
advertising/scanning/connection-event timing loop and *decides* when to call
these senders. That driver was not located; whether it exists elsewhere in
ROM (unnamed, unexplored 8000xxxx region) or is itself patch/RAM-resident is
an open question (Part 4), but the **event-formatting half of the BLE link
layer is unambiguously ROM-resident**, contradicting the previous doc's
broader "essentially no native LE support" framing, which was accurate only
for HCI **command parsing**, not event generation.

Confidence: **High** that the Meta-event sender cluster is real ROM code,
not patch-installed (it is part of the static `rom` memory block at
`0x80044730`–`0x80046620`, addresses fixed in silicon, named with full Kovah
annotations, several fully decompiled with clean payload-construction logic
matching BT spec field layouts). **Medium-low** on whether a separate,
dedicated link-layer *driver*/state-machine function exists elsewhere in ROM
— not found, but ROM is large (only ~17% of functions are named per
`rom_coverage_baseline.md`) and this pass's search was necessarily bounded.

---

## Part 1 — Re-confirmation: OGF 0x08 command-parsing IS thin (prior doc correct on this point)

Re-decompiled `HCI_CMD_OGF_08__LE_Commands__big_switch` (`0x80044674`, 176
bytes) directly in this pass to double check the prior doc's table-size claim
before extending it:

```c
void HCI_CMD_OGF_08__LE_Commands__big_switch(ushort *param_1)
{
  uint uVar3 = *param_1 & 0x3ff;     // OCF = low 10 bits of opcode
  possible_logging_function__var_args(5, 0xcb, &DAT_0000203a, 0xd04, 0x14, ...);
  p0Var2 = PTR_base_of_0x1ac_struct_array_0xA_large2_80044728;
  puVar1 = PTR_PTR_8004472c;
  uVar3 = uVar3 & -(uint)(uVar3 <= *(byte *)((int)&p0Var2[0xb].field96_0x60 + 1));
  (**(code **)(puVar1 + (uVar3 & (...)>>0x1f) * 4))(param_1);   // jump table call
}
```

Confirmed: the bounds-check max-OCF value (read from the connection-record
struct array, not a fixed literal) clamps the effective index, and the table
at `0x8004472c` has exactly one live entry (raw bytes `a9 54 04 80` =
`0x800454a9`, the odd/MIPS16e-call form of `0x800454a8`). **This part of the
prior doc's finding stands**: native LE *command handling* in this ROM is a
single dispatch slot, not a full OCF table.

### Correction to the prior doc's "unfilled RAM hook slot" framing

The prior doc described `0x801206ac` (table[0]) as "RAM address, not ROM
code" / "an unfilled RAM hook slot." Re-checked directly in this pass via
`DiagAddr.java`:

```
Target: 801206ac
MemBlock: data [80100000 - 8013ffff] execute=true
SYM PTR_FUN_800454a8+1_801206ac   type=Label
RawBytes: a9 54 04 80 ...   ->  0x800454a9 (odd = MIPS16e call form of 0x800454a8)
```

The slot **is** a patchable RAM pointer (consistent with the prior doc's
broader "hook slot" architecture), but its *default/as-shipped* content is
**not null/empty** — it already points at a real ROM function,
`FUN_800454a8` (58 bytes):

```c
undefined4 FUN_800454a8(short *param_1)
{
  // if OCF param is 0, status=0; else look up a per-connection
  // status byte (field_0x165) and use that as the status code
  ...
  hci_event_sender(0xe, &cStack_10, 4);   // 0xe = HCI_Command_Status event
  return 1;
}
```

This is a generic "send back `HCI_Command_Status` with whatever this
connection-record's stored status byte says" stub — the same pattern as
other unimplemented-OCF defaults elsewhere in this ROM (e.g.
`OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10` for the
standard-OGF defaults). It is **not** itself link-layer logic; it is a
"return current status, don't actually do anything new" placeholder. The
prior doc's directional conclusion (OGF8 command path ships ~no functional
LE command implementation) holds, but "unfilled"/"empty" overstated it —
the slot has ROM-supplied default content, just inert content. A patch (or
later ROM revision) could override this single slot, but as-shipped in this
ROM it resolves to a status-echo stub regardless of which (the one) OCF
value reaches it.

---

## Part 2 — The BLE Meta-Event sender cluster (the actual new finding)

Immediately *adjacent in memory* to the OGF8 command stub (`0x800454a8`,
itself sitting right after the unrelated function-cluster starting at
`0x80044730`) is a large, contiguous, fully-Kovah-named block of LE Meta
Event senders. All confirmed via `DiagAddr.java`/`DecompileAddr.java` this
pass (not inferred from names alone — several fully decompiled):

| Address | Name | Size | LE Meta subevent (BT spec) |
|---------|------|------|------------------------------|
| `0x80044730` | `FUN_80044730` | 102 B | (eSCO/air-mode table function — **not** part of the LE cluster; documented separately in `reverse_engineering_conn_type_dispatch_and_esco.md`; included here only because it sits at the OCF-table's single entry's *neighbor* address, easily confused with the LE cluster) |
| `0x800447a4` | `send_evt_Meta_subevent_0x13` | 90 B | 0x13 = LE Advertising Set Terminated (BT5.0 extended adv) |
| `0x80044804` | `send_evt_Meta_subevent_0x11` | 26 B | 0x11 = LE Scan Request Received (BT5.0 extended adv) |
| `0x80044820` | `send_evt_Meta_subevent_0x14_HCI_LE_Channel_Selection_Algorithm` | 36 B | 0x14 = LE Channel Selection Algorithm |
| `0x80044844` | `send_evt_Meta_subevent_0x07` | 74 B | 0x07 = LE Remote Connection Parameter Request |
| `0x80044890` | `send_evt_Meta_subevent_0x04` | 50 B | 0x04 = LE Read Remote Features Complete |
| `0x800448c4` | `send_evt_Meta_subevent_0x03_HCI_LE_Connection_Update_Complete` | 46 B | 0x03 = LE Connection Update Complete |
| `0x80044990` | `send_evt_Meta_subevent_0x16` | 148 B | 0x16 = LE Data Length Change |
| `0x80044a24` | `send_evt_Meta_subevent_0x17` | 38 B | 0x17 = LE Directed Advertising Report (or similar 5.x addition) |
| `0x80044c7c` | `send_evt_Meta_subevent_0_or_1` | 594 B | dispatcher/wrapper distinguishing subevent 0 vs 1 — likely the "is this Connection Complete or something else" entry shim feeding `0x80045e8c` |
| `0x800454a8` | `FUN_800454a8` | 58 B | (OGF8 OCF=0 command-status stub — see Part 1; not a Meta-event sender despite sitting in this address range) |
| `0x800454e8` | `send_evt_Meta_subevent_0x12` | 74 B | 0x12 = LE Channel Selection Algorithm (duplicate-looking label vs 0x14 — Kovah likely tentative on one of the two; not resolved this pass) |
| `0x80045534` | `send_evt_Meta_subevent_0x0c_HCI_LE_PHY_Update_Complete` | 66 B | 0x0c = LE PHY Update Complete |
| `0x80045578` | `send_evt_Meta_subevent_0x09_HCI_LE_Generate_DHKey_Complete` | 56 B | 0x09 = LE Generate DHKey Complete (LE Secure Connections) |
| `0x800455b8` | `send_evt_Meta_subevent_0x08_HCI_LE_Read_Local_P-256_Public_Key_Complete` | 58 B | 0x08 = LE Read Local P-256 Public Key Complete (LE Secure Connections) |
| `0x800455f4` | `send_evt_Meta_subevent_0x2_or_0x0b` | 332 B | 0x02 = LE Advertising Report; 0x0b = LE Direct Advertising Report — **fully decompiled, see below** |
| `0x80045c00` | `send_evt_Meta_subevent_0x05_HCI_LE_Long_Term_Key_Request` | 112 B | 0x05 = LE Long Term Key Request (link-layer encryption start) |
| `0x80045e8c` | `send_evt_Meta_subevent_0x01_or_0x0a_HCI_LE_Connection_Complete_or_HCI_LE_Enhanced_Connection_Complete` | **1032 B** | 0x01 = LE Connection Complete; 0x0a = LE Enhanced Connection Complete — **fully decompiled, see below**, by far the largest function in the cluster |

This is **not** a partial/stub cluster — sizes range up to 1032 bytes with
real conditional logic, struct field reads at spec-correct byte offsets, and
calls into `hci_event_sender`/`possible_logging_function__var_args` with the
correct LE Meta Event opcode (`0x3e`) and per-subevent code byte. Compare to
the genuinely thin OGF4 Informational-Parameters stub (74 bytes, "near
zero payload logic," per the prior doc) — these LE senders are an order of
magnitude more substantial and structurally indistinguishable from the
already-trusted BT-Classic event senders (`send_evt_HCI_Read_Remote_Version_Information_Complete`,
etc.) documented in `reverse_engineering_lmp_version_conn_setup.md`.

### `send_evt_Meta_subevent_0x2_or_0x0b` (LE Advertising Report) — full decompile

```c
void send_evt_Meta_subevent_0x2_or_0x0b(byte *param_1)
{
  local_127 = *param_1;                     // report count
  local_28 = (uint)local_127;
  if (local_28 != 0) {
    pbVar3 = PTR_base_of_0x1ac_struct_array_0xA_large2_0__field76_0x4c_80045740;
    local_128 = 2;                          // subevent 0x02 = LE Advertising Report
    if (param_1 != pbVar3) local_128 = 0xb;  // else 0x0b = LE Direct Advertising Report
    ...
    while (uVar4 < local_28) {               // for each report in this event...
      local_126[uVar6++] = event_type_lookup_table[param_1[uVar4+1] & 7];
      local_126[uVar6++] = param_1[uVar4+5]; // address type
      optimized_memcpy(local_24+uVar6, param_1+local_1c+9, 6);  // 6-byte BD_ADDR
      uVar6 += 6;
      ... // AD-data length + AD-data payload bytes, branch on direct-vs-undirected
      local_126[uVar6++] = pbVar3[uVar5+0xa5]; // RSSI byte
    }
    *param_1 = 0;
    if (PTR_DAT_80045748 hook != NULL) (*hook)(param_1);   // optional post-report hook
    hci_event_sender(0x3e, &local_128, uVar6+2 & 0xff);    // 0x3e = HCI_LE_Meta_Event
  }
}
```

This is genuine BLE advertising-report formatting: per-report event type
(`ADV_IND`/`ADV_DIRECT_IND`/etc., via a lookup table — table contents not
dumped this pass), address type, BD_ADDR, variable-length AD payload, and
RSSI — exactly the BT spec's `HCI_LE_Advertising_Report` array-of-reports
layout. The existence of an "optional post-report hook" (`PTR_DAT_80045748`)
before sending is the same patch-extensibility idiom documented elsewhere in
this ROM (e.g. the pre-dispatch hook in `assoc_w_tHCI_CMD`) — ROM generates
the report but a patch can intercept/augment it first.

### `send_evt_Meta_subevent_0x01_or_0x0a` (LE Connection Complete) — key excerpts

```c
void send_evt_Meta_subevent_0x01_or_0x0a_HCI_LE_Connection_Complete_or_HCI_LE_Enhanced_Connection_Complete
       (char param_1, undefined2 *param_2, char param_3, uint param_4, uint param_5)
{
  ...
  uVar9 = (local_res8[0]=='\0') ? 1 : 10;   // subevent: 1=Connection Complete, 10=Enhanced
  *local_18 = uVar9;
  local_18[1] = local_res0[0];              // status
  *(undefined2*)(local_18+2) = *param_2;    // connection handle
  local_18[4] = (peer-address-type-derived role/own-addr-type bit)
  local_18[5] = peer address type;
  *(undefined2*)(local_18+6..10) = peer BD_ADDR (3x uint16, i.e. 6 bytes)
  ...
  // conn interval / peripheral latency / supervision timeout pulled from
  // the connection-record struct array (PTR_base_of_0x1ac_struct_array_0xA_large2)
  // at offsets matching the eSCO/SCO struct's general layout, +0x28/0x2a/0x2c
  // region (same struct family as BT Classic, confirming LE shares the pool)
  ...
}
```

Confirms LE connections are tracked in the **same per-connection-record
struct array** (`large2`, the `0x1ac`-sized struct documented in
`reverse_engineering_conn_record_subsystem.md`) used for BT Classic SCO/eSCO
— there is no separate "BLE connection table" in this ROM; BLE connections
are just another record type/role flag in the shared pool.

---

## Part 3 — What was NOT found: searched, came up empty

Per the ticket's checklist, the following were searched for directly and
**not found** in this ROM:

1. **BLE-specific string constants.** `FindStringRefs.java` was run with
   multiple regex patterns covering advertising PDU type names
   (`ADV_IND`/`ADV_DIRECT_IND`/etc.), `whiten`/whitening, `access address`,
   `CRC.?24`, channel numbers, and generic `BLE`/`LE_`/`LL_` prefixes — **zero
   matches for all of them**, including a trivial control query
   (`"LMP"`, a string-fragment known to appear in this ROM's own Kovah
   labels) and even the maximally permissive `".+"` (match-anything) pattern,
   which also returned **zero results**. This confirms the script's
   non-executable-block ASCII scanner finds **no extractable string data at
   all** in this binary — this ROM/GZF is string-poor by construction (no
   embedded log-message or constant-name string table), not specifically
   missing BLE strings. This is a scanner/binary-content limitation, not
   evidence about BLE; treat the "no BLE strings found" result as
   uninformative rather than as negative evidence.
2. **A registered ISR slot identifiably tied to a BLE/LE radio source**, as
   distinct from BT-Classic-radio. Per
   `reverse_engineering_interrupt_vectors.md`, this ROM has exactly **one**
   generic ISR dispatch slot (`PTR_DAT_80033d1c`, default `0x80120f84`) with
   no per-source vector table and no read of any cause/status register
   before dispatch — i.e. there is no architectural hook point in the
   interrupt layer that could be labeled "the BLE radio ISR" vs "the BT
   Classic radio ISR" even in principle; that doc's own conclusion ("single
   ISR slot vs. true per-source vector table not disambiguated") already
   covers this, and nothing in this pass added BLE-specific information to
   it. Not re-investigated further here — would require a live/RAM trace
   per that doc's Open Questions, out of scope for a ROM-only ticket.
3. **A single, monolithic, named "BLE link-layer state machine" function**
   analogous to e.g. a textbook `ll_driver_task()`. None of the 17 Meta-event
   senders *triggers itself* — they are all leaf functions that format and
   send an event given already-resolved connection-record state; none
   contain the actual radio-timing loop (anchor point computation, channel
   index/hop calculation, whitening, CRC-24) that would constitute the
   low-level link-layer engine. `FindXrefsTo.java` (the only available
   generic xref tool) is **confirmed stale** per `GHIDRA_SCRIPTS.md` — it
   was rewritten for a different (boot-sequence) probe in an earlier
   session and the `save_ghidra_script` overwrite did not propagate to
   subsequent `run_ghidra_headless` calls — so it could not be used to find
   callers of `send_evt_Meta_subevent_0x01_or_0x0a` or
   `send_evt_Meta_subevent_0x2_or_0x0b` to trace upward into whatever drives
   them. This is the one place this pass hit a **real tool limitation**
   (see Tool Note) rather than a confirmed absence — the driver function may
   exist elsewhere in this ROM's large unexplored region (`rom_coverage_baseline.md`:
   only 16.84% of functions named) but could not be located without a
   working xrefs-to tool.

---

## Part 4 — Architectural picture and what remains open

Putting Parts 1–3 together, the BLE picture in this ROM looks like:

```
                     ┌─────────────────────────────────────┐
HCI host  ──cmd──►   │ HCI_CMD_OGF_08__LE_Commands (0x80044674) │  ← thin, 1 live OCF slot,
                     └─────────────────────────────────────┘     default = status-echo stub
                                      (Part 1 — confirmed thin, prior doc correct)

  [ unknown / not located this pass: the actual advertising/scanning/
    connection-event radio-timing engine — anchor points, channel
    selection, whitening, CRC-24, PDU TX/RX at the link-layer level ]
                                      │
                                      │ (would call into ↓ when an event occurs)
                                      ▼
                     ┌─────────────────────────────────────────────┐
ROM  ──evt──►        │ send_evt_Meta_subevent_0x01/0x02/0x03/.../0x17 │  ← rich, 17+ functions,
                     │ (0x80044730 – 0x80046620+)                    │     fully decompiled,
                     └─────────────────────────────────────────────┘     real payload logic
                                      │
                                      ▼
                     hci_event_sender(0x3e, payload, len)   ← HCI_LE_Meta_Event to host
```

The **bottom half** (event formatting + the shared connection-record pool
that both BLE and BT Classic draw from) is unambiguously ROM-resident and
richly implemented — this is the part the ticket asked about that turned out
to have a clear **positive** finding, correcting the prior doc's blanket
"essentially no native LE support" statement.

The **top half** (actual link-layer RF timing/state machine that decides
*when* to call these senders) was searched for via the available means
(string search, ISR-slot inspection, struct/cluster proximity reading) and
**not located** in this pass. Three non-exclusive possibilities, none
confirmed:

1. It exists somewhere in this ROM's large unexplored ~83% (per
   `rom_coverage_baseline.md`), reachable only via xrefs-to-the-senders,
   which needs a working `FindXrefsTo.java` (currently stale/unusable per
   the known wairz overwrite bug) or a fresh batch script (currently
   blocked — both wairz script-storage pools are reportedly at their cap
   per other recent docs' Tool Notes; not independently re-verified this
   pass since no new script was attempted).
2. It is genuinely **not** in ROM at all — i.e. unlike the event-formatting
   layer, the actual RF/timing engine is patch-resident (in the `data` block,
   `0x80100000`–`0x8013ffff`) or even partially hardware-sequenced (a
   baseband state machine implemented in silicon logic rather than firmware
   instructions, invisible to Ghidra static analysis entirely — plausible
   for the most timing-critical parts of any real BLE controller, since
   sub-instruction-cycle anchor-point timing is hard to do in software on a
   MIPS16e core this size).
3. Some mix of both: low-level RF sequencing in hardware/microcode, with a
   thin ROM glue layer (not yet identified) connecting hardware-detected
   events to the senders documented here, similar to how
   `reverse_engineering_hardware_layer.md` describes the eSCO/SCO hardware
   commit path being mostly a hook dispatch with the real register access
   happening in patch-installed runtime-generated code.

This was **not chased further** — the RAM hook slot check the ticket
specifically asked for (whether the patch's `data` block ever writes
`0x801206ac`) was checked and found **not applicable as originally framed**:
that slot already holds ROM-default content (`0x800454a8`, the status-echo
stub) and is unrelated to the Meta-event cluster found in Part 2, which has
its own separate, not-yet-traced trigger path. Re-scoping the RAM-write
check to the *actual* relevant question — does the patch ever overwrite any
function pointer leading into the Meta-event senders — was not done this
pass; it would require first finding the as-yet-unlocated caller(s) of those
senders (Part 3's blocker) before knowing which RAM slot(s), if any, to
check for patch writes. Flagged as the natural next step, not done here to
avoid guessing at addresses with no caller-graph evidence backing them.

---

## Tool note (wairz limitations encountered, not worked around)

1. **`FindStringRefs.java` returns zero results for every pattern tried on
   this binary, including the maximally permissive `.+`.** This was
   cross-checked against a deliberately trivial control pattern (`"LMP"`,
   a substring known to appear throughout this ROM's own Kovah-applied
   function names) to rule out a regex-escaping mistake on this pass's
   side — it also returned zero. The script's non-executable-block
   ASCII-string scanner appears to find no string data at all in this
   particular GZF/binary (consistent with this being a stripped/annotation-
   only build with no embedded log-string table, as opposed to a script bug
   — not independently confirmed which). This is the same class of
   "tool genuinely cannot answer this for this binary" situation
   CLAUDE.md asks to flag rather than silently work around; flagging here.
   Does not block this ticket's conclusion (Part 2's finding did not depend
   on string evidence) but blocks any *future* ticket that wants to use
   string search as a primary technique on this ROM/GZF.
2. **`FindXrefsTo.java` is confirmed still stale** (hardcoded to dump xrefs
   to `0x801212e4`, the eSCO hardware-write hook, regardless of the actual
   need) — this is the same known bug already documented in
   `GHIDRA_SCRIPTS.md` and `reverse_engineering_boot_reset_sequence.md`'s
   Tool Note (a `save_ghidra_script` overwrite of an existing filename did
   not take effect on subsequent `run_ghidra_headless` runs). Re-confirmed
   here by running it and observing the same `0x801212e4`-only output as
   before. This directly blocked Part 3's attempt to find callers of the
   Meta-event sender cluster — flagged per CLAUDE.md's "tell the user, don't
   work around" rule, not re-attempted with a new script (both research-file
   and scripts-directory pools were reported at their 50/50 caps in the most
   recent prior session that touched this area;not independently re-verified
   in this pass since no new script was attempted, given the existing
   documented blocker).

Both limitations are already-known, not new discoveries — surfaced here only
because they concretely bounded what this specific ticket could conclude
(Part 3/4's open questions trace directly back to these two tool gaps).

---

## Related documents

| File | Relationship |
|------|--------------|
| `rom/reverse_engineering_hci_command_router.md` | Source of the "OGF8 ships ~no LE command support" finding this doc partially corrects/extends (command-parsing side confirmed thin; event-generation side shown to be rich) |
| `rom/reverse_engineering_conn_record_subsystem.md` | Documents the shared `0x1ac`-sized per-connection struct array (`large2`) that this doc's LE Connection Complete sender reads from — same pool used by BT Classic SCO/eSCO |
| `rom/reverse_engineering_interrupt_vectors.md` | Single-ISR-slot architecture referenced in Part 3's "no BLE-specific ISR found" sub-finding |
| `rom/reverse_engineering_lc_lmp_state_machine.md` | Establishes the BT-Classic analogy this doc draws on ("no single state-machine dispatcher, shared record + shared primitive + per-procedure drivers") — BLE's event-formatting side fits the same architectural pattern, though its trigger/driver side remains unlocated (unlike BT Classic's, which this doc fully traced) |
| `GHIDRA_SCRIPTS.md` | `FindXrefsTo.java` row documents the stale-overwrite bug blocking Part 3/4 |
