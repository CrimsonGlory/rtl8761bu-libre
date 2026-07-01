# Reverse Engineering: Link Controller (LC) / LMP Procedure State Machine

**Scope:** ROM baseband procedures beyond eSCO/SCO/AFH — inquiry, paging,
role switch, sniff/QoS power-mode hooks. Authentication/pairing's *trigger*
path is already documented (`FUN_80025b68` / `rom/reverse_engineering_hardware_layer.md`
section 9); this doc adds the inquiry/page/role-switch LMP procedures that
were previously only sketched as "ROM's LMP PDU state machine" without trace.

**Memory block:** ROM (fixed in chip silicon)
**Source:** Kovah `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`, decompiled via Ghidra
12.1.2 headless, GZF process mode
**Tools used:** `DecompileAddr.java` / `DiagAddr.java` with 0x-prefixed `script_args`
(one address per call — no new script needed; `xrefs_to`/`xrefs_from` MCP
tools do **not** work against this GZF, see Tool Note)
**Date:** 2026-06-21. Ticket: work-in-progress.txt "Document the link
controller (LC) / LMP procedure state machine"

---

## Summary

Starting from the OGF 0x01 (Link Control, `0x80020814`) and OGF 0x02 (Link
Policy, `0x8002060c`) HCI handlers documented in
`reverse_engineering_hci_command_router.md`, this pass traces **past** the
HCI entry points into the actual baseband procedure logic: connection-record
allocation, the shared procedure-start dispatcher, low-level baseband
register programming for paging, and the LMP PDU transmit primitive used by
sniff and role-switch.

Three sub-procedures were traced with concrete addresses and decompiled
logic:

1. **Inquiry** (OGF1/OCF1 `HCI_Inquiry`) — trigger chain only; the actual
   inquiry train baseband logic lives behind a still-unexplored function.
2. **Paging / Create Connection** (OGF1/OCF5 `HCI_Create_Connection`) — fully
   traced from HCI handler through connection-record allocation to the raw
   baseband register-programming function that arms the page train.
3. **Role switch** (OGF2/OCF11 `HCI_Switch_Role`) — fully traced from HCI
   handler through feasibility validation to the LMP PDU that kicks off the
   procedure (`LMP_SLOT_OFFSET`, opcode `0x13`), confirming this ROM drives
   role switch via the same generic LMP-PDU-send primitive as every other
   LMP procedure.

A fourth, **sniff mode** (OGF2/OCF3/0x11 `HCI_Sniff_Subrating`-adjacent), is
covered partially: the *unsniff* request path is fully decompiled and
confirms the shared `send_LMP_pkt` primitive, but `HCI_Sniff_Mode`/
`HCI_Exit_Sniff_Mode` themselves were not reached this pass (see Open
Questions). Hold mode and Park mode were not reached at all.

**Headline architectural finding**: there is no separate, monolithic "LC/LMP
state machine" function. Instead, the ROM implements each baseband procedure
as its own small function chain (HCI handler → validate/lookup → program
hardware and/or send one LMP PDU via the shared `send_LMP_pkt` primitive),
all operating on the same per-connection record array (`bos_base`,
`PTR_big_ol_struct_*` in the decompiler's naming, documented struct layout in
`reverse_engineering_conn_record_subsystem.md`). "State" is tracked as a
status byte at connection-record offset `+0xb2` (`_xb2_byte_minus_4_used_as_status_array_index`,
already named) plus several procedure-specific flag/byte fields
(`+0x171`, `+0x16f`, `+0x179`, `+0xd0`, `+0x216`) that this pass newly
characterizes. The "state machine" is best understood as **one shared
connection-record + one shared LMP-PDU-send primitive, with N small
per-procedure driver functions**, not a single switch-on-state dispatcher.

Confidence: **High** for all decompiled control flow quoted below (clean,
fully-resolved Ghidra decompiles, no undefined fallthrough). **Medium** for
semantic interpretation of status byte values (e.g. "state `0xe` = role-switch
pending") — these are read directly off magic-number comparisons and cross-
checked against BT spec LMP opcode numbers, not independently confirmed
against a reference trace.

---

## 1. Shared infrastructure

### 1.1 `send_LMP_pkt` (`0x800611e4`, 720 bytes) — generic LMP PDU transmitter

Already named by Kovah. Confirmed (this pass) as the **single shared
transmit primitive** for LMP procedures outside the encryption/SSP cluster
already documented in `reverse_engineering_hardware_layer.md`. Signature
recovered from two call sites:

```c
send_LMP_pkt(uint conn_idx, void *pdu_buf, int len, int lmp_tid_or_type,
             int timeout_ticks, int flags);
```

Observed call sites this pass:

| Caller | PDU buf[0] (LMP opcode) | len | arg4 | timeout | flags |
|--------|--------------------------|-----|------|---------|-------|
| `LMP_0x18_LMP_UNSNIFF_REQ` (`0x8001af9c`) | `0x18` (`LMP_UNSNIFF_REQ`) | 2 | 3 | 100 | 0 |
| `LMP_QUALITY_OF_SERVICE_REQ_0x2A` (`0x8001aa3c`) | `0x2a` (`LMP_QUALITY_OF_SERVICE_REQ`) | 5 | 3 | 100 | 0 |
| `FUN_8001ab44` (role-switch kickoff) | `0x13` (`LMP_SLOT_OFFSET`) | 6 | `bdaddr_random^1` | 0xe (14) | 0 |

The opcode byte is always `pdu_buf[0]`; callers stack-allocate a small buffer,
write the LMP opcode into byte 0, then the PDU payload into the following
bytes, and call `send_LMP_pkt`. This is the **same uniform pattern** used by
every named `LMP_*_0x##` function in `kovah_function_list.md` — confirming
those ~25 already-named LMP handlers (`LMP_AU_RAND_0x0B`, `LMP_SRES_0x0C`,
etc.) are almost certainly all thin wrappers around this one primitive,
differing only in PDU payload construction and the `arg4`/timeout/flags
tuning per procedure.

### 1.2 Connection-record status byte (`+0xb2`, `_xb2_byte_minus_4_used_as_status_array_index`)

Already named (read-only references) in `reverse_engineering_conn_record_subsystem.md`
and `reverse_engineering_lmp_version_conn_setup.md`. This pass adds two new
confirmed status-value meanings from the role-switch chain:

| Value | Meaning (confirmed this pass / earlier docs) |
|-------|-----------------------------------------------|
| `0` | Free / idle |
| `2` (set by OGF2 case 0/2/4 path in `HCI_CMD_OGF_02__Link_Policy`) | Generic "operation pending" — exact semantics not pinned down |
| `4` | "Connected" / steady-state — required by `FUN_8002143c` (role-switch feasibility check) and `fHCI_Create_Connection_0x05`'s busy-loop scan before allowing a new outbound connection |
| `0xe` (14) | **Role-switch pending** — set by `FUN_8001ac74` when role-switch is deferred rather than kicked off immediately (new, this pass) |

### 1.3 Other per-connection-record state fields touched this pass

| Offset | Field name (decompiler) | Role (this pass's finding) |
|--------|--------------------------|------------------------------|
| `+0x179` (global `the_0x300` struct, not per-conn) | `field_0x179` | Inquiry/paging "busy" mode: `2` = inquiry-with-scan-active (triggers a busy-loop check over 10 conn slots before allowing `HCI_Create_Connection`); `1`/`3` = unconditionally reject new connections (`return 0xc`, Connection Rejected) |
| `+0x171` (global) | `field_0x171` | Added into role-switch backoff timer calc (`local_14 = local_14 + field_0x171`, clamped `<2`) and into paging retry-count selection (`cVar7 = bVar6 + ptVar1->field_0x171`) — looks like a global retry/attempt counter shared across procedures |
| `+0x16f` (global) | `byte_0x16f` | Paging mode selector: combined with per-slot scan results to choose among page-timeout values `0x100`/`0x200`/`0x300` (see §3 below) |
| `+0xd0` (per-conn) | (unnamed) | Boolean: gates whether `FUN_8001ab44` reuses an existing slot-offset calculation or recomputes (`if (pbVar2[param_1].field_0xd0 != 0) ...`) |
| `+0x216` (per-conn) | `field_0x216` | Role-switch "already linked to another op" deferred-request flag, set by `FUN_8001acd8`, read by `FUN_8001ac74` |
| `+0x16a` (global) | `byte_0x16a` | Triggers `FUN_8004090c()` call from the shared connect-procedure dispatcher (`called_by_fHCI_Remote_Name_Request_5`) — purpose not traced further this pass |

---

## 2. Inquiry (`HCI_Inquiry`, OGF1/OCF1, opcode `0x401`)

### HCI handler → trigger (`0x80020814` case 0 → `fHCI_Inquiry_0x01`)

`HCI_CMD_OGF_01__Link_Control__FUN_80020814` case 0 (already documented in
`reverse_engineering_hci_command_router.md`) gates on a single byte at
`PTR_DAT_80020bc0` (busy flag — "is inquiry/page/connect already active")
and calls `fHCI_Inquiry_0x01` (`0x8001bfa0`, 50 bytes):

```c
int fHCI_Inquiry_0x01(int param_1)
{
  int iVar2 = validate_hci_inquiry_length_and_lap_range();  // validate inquiry length + LAP range
  if (iVar2 == 0) {
    *PTR_DAT_8001bfd4 = param_1[7];   // stash num_responses
    iVar2 = called_by_fHCI_Remote_Name_Request_5(param_1);
    if (iVar2 == 0) send_evt_HCI_Command_Status(0x401, 0);
  }
  return iVar2;
}
```

`validate_hci_inquiry_length_and_lap_range` (`0x8002155c`, 66 bytes) is a pure parameter validator: checks
a "coexistence busy" bit (`bit_test__bit_index_at_offset_0x16f__within__short_at_offset_0x24_`)
and that the inquiry length byte is in range `[1, 0x30]` and the 3-byte LAP
value plus a global offset stays `< 0x40`. Returns `0xc` (Connection
Rejected) or `0x12` (Invalid Parameters) on failure, `0` on success.

### Shared trigger dispatcher (`called_by_fHCI_Remote_Name_Request_5`, `0x80036df8`, 316 bytes)

This function is the **common entry point for both inquiry start and paging
start** (and Remote Name Request, per its Kovah-assigned name) — it is the
function called by both `fHCI_Inquiry_0x01` (opcode `0x401`) and
`fHCI_Create_Connection_0x05` (opcode `0x405`):

```c
undefined4 called_by_fHCI_Remote_Name_Request_5(ushort *param_1)
{
  if (*PTR_DAT_80036f34 == 1) {                  // periodic-inquiry-mode flag set?
    if (fHCI_Exit_Periodic_Inquiry_Mode_0x04() == 0) { /* clear EIR/inquiry state */ }
    *PTR_DAT_80036f34 = 0;
    possible_logging_function__var_args(...);
  }
  if (PTR_struct_of_at_least_0x300_size_80036f38->int_0x10 & 3) {
    possible_logging_function__var_args(...);    // already busy
    return 0xc;
  }
  LMP__25C_called2();                              // coexistence hook
  if (the_0x300->byte_0x16a != 0) FUN_8004090c();  // conditional side-effect, not traced
  switch (*param_1) {                              // opcode-keyed dispatch
    case 0x401:  /* HCI_Inquiry */
    case 0x419:  /* HCI_Create_Connection_Cancel_? (see note) */
    case 0x43f:  /* HCI_Setup_Synchronous_Connection-adjacent */
      pcVar9 = config_base->field208_0xd8 & 0x80 ? coexistence_pre_hook(...) : fptr_DAT_80036f5c;
      uVar8 = (*pcVar9)(param_1);
      break;
    case 0x405:  /* HCI_Create_Connection */
      pcVar9 = config_base->field208_0xd8 & 0x80 ? coexistence_pre_hook(...) : fptr_DAT_80036f54;
      uVar8 = (*pcVar9)(param_1);
      break;
    default:
      uVar8 = 0x1f;                                 // Invalid OGF/OCF Parameters
  }
  return uVar8;
}
```

This confirms a **single shared opcode-keyed jump** (not a state-machine
switch, just a small literal-value dispatch) that routes inquiry and paging
to two distinct baseband-programming functions via function pointers held at
`fptr_DAT_80036f54` (opcode `0x405`) and `fptr_DAT_80036f5c` (opcodes `0x401`/
`0x419`/`0x43f`):

| Opcode | fptr slot | Resolves to | Procedure |
|--------|-----------|--------------|-----------|
| `0x405` (`HCI_Create_Connection`) | `fptr_DAT_80036f54` → `0x80041900` | `FUN_80041900` | Paging |
| `0x401` (`HCI_Inquiry`) / `0x419` / `0x43f` | `fptr_DAT_80036f5c` → `0x8004147c` | `FUN_8004147c` | Inquiry-train baseband programming / role-related setup |

**Note**: opcode `0x419` is `HCI_Create_Connection_Cancel` per spec layout
(`0x400 + 0x19`) and `0x43f` is `HCI_Setup_Synchronous_Connection` (OGF1/OCF
`0x3f`, the SCO/eSCO path already documented elsewhere) — both routed through
the *same* fptr slot as plain `HCI_Inquiry`. This grouping was not expected
going in; it suggests `FUN_8004147c` is a more general "baseband-procedure
register-programming" function shared across inquiry/cancel/SCO-setup rather
than an inquiry-specific function. Not fully disambiguated this pass — see
Open Questions.

`FUN_8004147c` (934 bytes) was decompiled but is dense, register-programming
heavy code (writes ~15 baseband registers via the `PTR_DAT_80041838`
indirect-call vtable: BD_ADDR halves, clock offset, role/AM_ADDR encode at
register `0xaa` with bit `0x2000` conditionally set when `local_34==0x43f`,
packet-type register `0x2c`, etc.) — consistent with arming either a page
train or an SCO/eSCO baseband schedule depending on caller opcode, but the
function itself does not visibly branch into a separate "inquiry scan"
sub-procedure. **The actual inquiry-train/inquiry-scan logic (FHS PDU
broadcast, response collection) was not located in this pass** — it likely
lives in a function not yet reached from this call chain (see Open
Questions).

---

## 3. Paging / Create Connection (`HCI_Create_Connection`, OGF1/OCF5, opcode `0x405`)

### HCI handler → connection-record allocation (`fHCI_Create_Connection_0x05`, `0x8001bd38`, 512 bytes)

Already named. Full decompile (this pass):

```c
int fHCI_Create_Connection_0x05(int param_1)
{
  // busy-state guard keyed on global mode byte (field_0x179):
  //   == 2 -> scan all 10 conn-record slots for in-progress ops, reject (0xc) if any busy
  //   == 1 or 3 -> reject unconditionally (0xc)
  if (FUN_800214f4(param_1) == 0) {                  // parameter validation
    slot = return_big_ol_array_offset(...);           // allocate a free conn-record slot
    if (slot == 0) {
      memcpy(&local_34, param_1+3, 6);                 // BD_ADDR from PDU
      record = bos_base[slot_idx];
      record.BDADDR              = local_34;            // copy BD_ADDR into record
      record.HCI_Create_Connection_PacketType            = pdu->packet_type;
      record.HCI_CMD_Multiple__PSRM_byte                  = pdu->page_scan_repetition_mode;
      record.HCI_Create_Connection_Allow_Role_Switch_Byte = pdu->allow_role_switch;
      record.HCI_Create_Connection_RFU_Byte               = pdu->reserved;
      record.bos_connection__array_index                 = slot_idx;
      record._x256_link_type = 1;                         // mark as ACL link
      FUN_80036420(slot_idx);                              // (not traced; side-effect init)
      record.field185_0x100 = (byte)return_big_ol_array_offset_result;
      // clock-offset valid bit handling (HCI clock offset bit 15):
      record.HCI_Create_Connection_Clock_Offset = (clock_offset_valid)
          ? (clock_offset & 0x7fff) : return_big_ol_array_offset_result;

      err = called_by_fHCI_Remote_Name_Request_5(param_1); // -> FUN_80041900 (see below)
      if (err == 0) {
        set_bos_bosi__0xb2_index_arg2(slot_idx, 1);          // status = 1 ("page pending")
        send_evt_HCI_Command_Status(0x405, 0);
      } else {
        called_by_fHCI_Remote_Name_Request_6_nop_if_not_patched_(...); // patch hook, no-op in ROM
        zero_initialize_6_bytes_at_param1(&local_34);          // roll back BD_ADDR copy
      }
    }
  }
  return result;
}
```

This confirms `HCI_Create_Connection` allocates a connection-record slot
*before* attempting to page, populates it with all the per-link parameters
from the HCI command (BD_ADDR, packet type, page-scan-repetition-mode,
allow-role-switch flag, clock offset), and only then hands off to the shared
dispatcher (§2) which resolves to `FUN_80041900` for opcode `0x405`.

### Page-train baseband programming (`FUN_80041900`, `0x80041900`, 376 bytes)

```c
undefined1 FUN_80041900(int param_1)
{
  FUN_8003785c(the_0x300->field_0x185);              // (not traced; pre-page setup)
  (*hw_write)(0x14, pdu->bdaddr_lo16);                 // program BD_ADDR halves into HW
  FUN_80013c0c(0x16, pdu->bdaddr_byte5);
  if (DAT_80041a80 == local_18 /* bdaddr lo24 */) {
    (*hw_write)(0x10, &DAT_00006e1e);                  // default sync word A
    (*hw_write)(0x12, &DAT_000088d6);                  // default sync word B
  } else {
    compute_access_code_sync_word_from_bdaddr(bdaddr, &sync_word_buf);  // derive access-code sync word from BD_ADDR
    (*hw_write)(0x10, sync_word_buf[0]);
    (*hw_write)(0x12, sync_word_buf[1]);
  }
  FUN_80013c3c(sync_word_flags);
  (*hw_write)(0x2e, (pdu->clock_offset_high6 & 0x3f) << 10); // clock-offset high bits -> reg 0x2e

  // Busy/abort check identical in shape to the trigger-dispatcher's guard:
  if (the_0x300->ushort_0x24 == 0x20 && the_0x300->byte_0x16f != 0) return 0xc;

  // Scan up to 3 connection slots; if any slot is status==4 (connected) and
  // not yet "bdaddr_random" (role-switch-eligible?) and byte_0x16f==1, log
  // and bump byte_0x16f to 2 ("collision with active link" state).
  for (i = 0; i < 3; i++) { ... }

  // Select page timeout class based on (byte_0x16f + field_0x171):
  //   sum == 1 -> 0x100 or 0x300 depending on extra conditions
  //   sum == 0 -> 0x100
  //   sum == 2 -> 0x300
  //   else     -> abort (0xc)
  (*hw_write)(0x2c, page_timeout_class);                // arm page-timeout register

  if (optional_patch_hook == NULL || hook(param_1, &status) == 0) {
    (*hw_write)(0, 1);                                   // kick off paging (register 0 = "start" trigger?)
    the_0x300->int_0x10 = 1;                              // mark "paging in progress" (busy bit checked by fHCI_Create_Connection's sibling guard)
    FUN_800362b4();                                       // (not traced; likely arms a page timer/watchdog)
    status = 0;
  }
  return status;
}
```

This is the clearest baseband-procedure trace obtained this pass: programs
BD_ADDR-derived access-code sync words, clock offset, and a page-timeout
class into hardware registers via the `PTR_DAT_80041a7c` HW-write vtable,
then writes `1` to register `0`, which is almost certainly the actual
"start paging" trigger bit, and sets a busy flag checked by the *next*
`HCI_Create_Connection` call's slot-budget guard (§2's `field_0x179`/busy
loop). `FUN_800362b4` (uncalled-into this pass) is the best candidate for
"arms the page-timeout watchdog timer that eventually produces
`HCI_Connection_Complete` or page-timeout failure" — not traced further.

**What was not found**: the actual page-train transmit loop (the repeated
ID-packet broadcast across the 32 page-scan hop frequencies) and the
page-response handling (FHS reception → `HCI_Connection_Complete` event).
These almost certainly live in interrupt-driven or timer-driven code outside
this synchronous HCI-command call chain (consistent with `FUN_800362b4`
arming a timer rather than looping synchronously) — out of scope for what's
reachable via straight-line decompilation of the HCI command path. See Open
Questions.

---

## 4. Role switch (`HCI_Switch_Role`, OGF2/OCF `0xb`, opcode `0x80b`)

This sub-procedure was the most completely traced — three functions, full
chain from HCI handler to LMP PDU transmission, all attributable to existing
named functions in `reverse_engineering_lmp_version_conn_setup.md` §5 (this
pass adds the final link, `FUN_8001ab44`, which that doc left undecompiled).

### Chain

```
HCI_CMD_OGF_02__Link_Policy__FUN_8002060c   (0x8002060c, case 0xa, OCF=0xb)
  └─ FUN_8001acd8   (0x8001acd8, 104 B) — slot lookup + in-progress check
       └─ FUN_8002143c  (0x8002143c, 172 B) — feasibility validation
            • role byte sanity (bits [7:1] must be 0)
            • resolve BD_ADDR -> connection slot
            • local + remote feature-page bit 0x20 ("role switch" LMP feature) must be set
            • connection status (+0xb2) must == 4 ("connected")
       └─ FUN_8001ac74  (0x8001ac74, 96 B) — kickoff / defer decision
            • if record.int_0x50 == -1 (no op already linked):
                 - precondition check (FUN_80021f6c)
                 - if "fast path" condition: FUN_8001ab44(conn_idx)   <- send LMP_SLOT_OFFSET
                 - else: range-check (FUN_800220fc), else status = 0xe (deferred)
            • else: record.field_0x216 = 1  (flag pending request on already-busy link)
            └─ FUN_8001ab44  (0x8001ab44, 288 B) — THIS PASS: full decompile
                 • computes a slot-offset/backoff value from per-conn timing fields
                   (+0x82 field90, role-switch retry table via FUN_8006ae20)
                 • clamps to a config-gated minimum (800 if config_base+0xd8 bit 0x20 set)
                 • builds 4-byte slot-offset payload (copy_bytes_in_LSB_order)
                 • send_LMP_pkt(conn_idx, [0x13, ...4-byte-offset...], len=6,
                                arg4=bdaddr_random^1, timeout=0xe, flags=0)
                     -- LMP opcode 0x13 = LMP_SLOT_OFFSET (BT spec)
                 • if bdaddr_random==1: ORs a status bit via
                   get_status_bits_by_LMP_Opcode(0x34, 0) into the conn record
                     -- LMP opcode 0x34 = LMP_FEATURES_RES (tracks pending
                        features exchange alongside the switch)
```

### Key decompiled logic — `FUN_8001ab44` (role-switch LMP kickoff)

```c
undefined4 FUN_8001ab44(uint conn_idx)
{
  record = bos_base[conn_idx];
  FUN_80017bac(record.bos_connection__array_index, record.byte_0xCC, 4);
  retry_count = (FUN_8006ae20(record.byte_0xCC) + 1) & 0xff;
  backoff = (retry_count + 4) * record.field90_0x82;
  if (backoff < 0xee) backoff = 0xee;

  if (calls_fptr_down_LMP__47E_path(...) in [1, 0x31]) {
    // re-derive retry_count by summing FUN_8006ae20(0..3)
    backoff = (retry_count + 10) * record.field90_0x82;
    if (backoff < 0x140) backoff = 0x140;
  }
  if (config_base->field208_0xd8 & 0x20 && backoff < 800) backoff = 800;

  FUN_80034a24(&slot_offset_raw, record.byte_0xCC);
  slot_offset = (backoff + (slot_offset_raw >> 1)) & DAT_8001ac6c;
  record.field_0x18 = slot_offset;   // stash computed slot offset in conn record

  if (record.bdaddr_random == 0) {
    FUN_800721a0(conn_idx, 1);
    record.field_0x205 = *PTR_DAT_8001ac70;
  }

  pdu[0] = 0x13;                                 // LMP_SLOT_OFFSET
  copy_bytes_in_LSB_order(pdu+1, slot_offset, 4); // 4-byte slot offset payload
  send_LMP_pkt(conn_idx, pdu, 6, record.bdaddr_random ^ 1, 0xe, 0);

  if (record.bdaddr_random == 1) {
    record._x30_status_byte |= get_status_bits_by_LMP_Opcode(0x34, 0);
  }
  return 0;
}
```

**Interpretation**: this ROM's role-switch implementation sends
`LMP_SLOT_OFFSET` (a real BT Core Spec LMP PDU sent by the future-slave
device ahead of `LMP_SWITCH_REQ`, supplying clock-offset timing info the new
master will need) as its first/only directly-observed action. The
`LMP_SWITCH_REQ` PDU itself was not located in this 96-byte function — either
it's sent by a caller of `FUN_8001ab44` not traced this pass, or it's
triggered asynchronously once the slot-offset exchange completes (consistent
with role switch being a multi-PDU LMP procedure, not a single fire-and-forget
message). The `bdaddr_random` field doubles as a "are we already the master"
role flag here (controls both the LMP transaction-ID-ish `arg4` passed to
`send_LMP_pkt` and whether `FUN_800721a0` — not traced — runs first).

This confirms the **deferred/pending mechanism**: when a role-switch request
arrives while another procedure already holds `record.int_0x50` linked,
`FUN_8001ac74` just sets `field_0x216=1` rather than calling `FUN_8001ab44`
immediately — i.e. there is a real (if minimal) "pending operation" state
per connection record, and role-switch requests queue behind whatever
already-in-flight procedure that pointer represents. What clears `int_0x50`
or re-checks `field_0x216` to actually fire the deferred switch was **not**
traced this pass.

---

## 5. Sniff mode (partial — `LMP_UNSNIFF_REQ` path only)

### `LMP_0x18_LMP_UNSNIFF_REQ` (`0x8001af9c`, 114 bytes)

Reached from `HCI_CMD_OGF_02__Link_Policy__FUN_8002060c` case 3 (OCF
`0x801+3 = 0x804` = `HCI_Exit_Sniff_Mode`... **correction**: tracing the
switch arithmetic, OGF2 base opcode is `0x801`, and case index 3 in the
*second* switch (`*param_1 - 0x801`) routes here — i.e. this is the
**`HCI_Exit_Sniff_Mode` (OCF 0x4)** handler, not a raw OCF-3 command; Kovah's
function name (`LMP_0x18...`) names it by the **LMP opcode it sends**
(`0x18` = `LMP_UNSNIFF_REQ`), not the triggering HCI command):

```c
undefined4 LMP_0x18_LMP_UNSNIFF_REQ(uint conn_idx)
{
  record = bos_base[conn_idx];
  if (record.bdaddr_random == 1) {           // currently the "non-master" side of sniff?
    FUN_80033d28(conn_idx);                    // (not traced; sniff-state cleanup)
    record.field_0x205 = *PTR_DAT_8001b014;
    possible_logging_function__var_args(...);
  }
  send_LMP_pkt(conn_idx, [0x18], len=2, arg4=3, timeout=100, flags=0);
  return 0;
}
```

This confirms `LMP_UNSNIFF_REQ` (opcode `0x18`) is sent unconditionally to
exit sniff mode, with an extra conditional cleanup step
(`FUN_80033d28`, untraced) when the local role flag (`bdaddr_random`) is set.
This is a 2-byte PDU (opcode only, no parameters) consistent with the BT
spec's `LMP_UNSNIFF_REQ` having no payload.

**Not traced this pass**: `HCI_Sniff_Mode` (entering sniff — would send
`LMP_SNIFF_REQ`, opcode `0x17`) and the actual periodic sniff-interval
scheduler/wake logic. OGF2 case 0xc/0xe (`FUN_8001a9f0`/`FUN_8001aa20`,
visible in the OGF2 decompile in `reverse_engineering_hci_command_router.md`'s
companion trace but not followed here) are the likely `HCI_Sniff_Mode`/
related handlers — see Open Questions.

---

## What was NOT reached this pass (explicit unexplored list)

This is a broad ticket; the following sub-procedures remain **undocumented**
and are legitimate future-work targets, not failures:

1. **Inquiry train / inquiry scan logic itself.** `FUN_8004147c` (the
   function inquiry's trigger dispatches to) was decompiled but is shared
   with `HCI_Create_Connection_Cancel`/SCO-setup opcodes and does not
   visibly contain an FHS-broadcast loop — the actual inquiry hop/transmit
   logic and inquiry-response (FHS PDU) collection were not located.
2. **Page-train transmit loop and page-response handling.** `FUN_80041900`
   arms hardware registers and sets a "paging in progress" flag, then calls
   untraced `FUN_800362b4` (likely a timer arm). The actual repeated ID-packet
   transmission across page-scan hop frequencies, and FHS-reception →
   `HCI_Connection_Complete` event generation, were not found — almost
   certainly timer/interrupt-driven, outside the synchronous HCI-command call
   chain this pass followed.
3. **`HCI_Sniff_Mode` (entering sniff)** — likely `FUN_8001a9f0` or
   `FUN_8001aa20` (OGF2 cases 0xc/0xe per the router doc's decompile) but not
   decompiled this pass.
4. **Hold mode and Park mode entirely** — not reached. OGF2's decompile (in
   `reverse_engineering_hci_command_router.md`) shows cases mapping to
   `FUN_8001ad44` (case 6) and others, but none were specifically identified
   as Hold/Park handlers this pass; the OGF2 OCF range for
   `HCI_Hold_Mode`/`HCI_Park_State`/`HCI_Exit_Park_State` was not
   cross-checked against the case table.
5. **Authentication/pairing state machine driving `LMP_encryption_opcode_handlers`'s
   callers.** `reverse_engineering_hardware_layer.md` §9 already documents
   that `LMP_encryption_opcode_handlers` (`0x80028264`) is "dispatched by the
   ROM's LMP PDU state machine" with state values `0x15`/`0x1d`/`0x1e`
   triggering specific sub-handlers (`FUN_80029364`, `FUN_800293f0`,
   `FUN_80023d14`) — but the dispatcher itself (what reads incoming LMP PDUs
   and routes by opcode to `LMP_*` handlers) was not located in this pass or
   the earlier one. This is the single highest-value remaining target: it is
   almost certainly the true "LMP receive-side state machine" the ticket is
   ultimately asking about, complementing the transmit-side `send_LMP_pkt`
   primitive this pass characterized.
6. **What clears `record.int_0x50` / re-evaluates `record.field_0x216`** to
   fire a deferred role-switch request once the blocking procedure
   completes — not traced.
7. **`LMP_SWITCH_REQ` PDU transmission itself** — only `LMP_SLOT_OFFSET`
   (the precursor) was found in `FUN_8001ab44`; the actual switch-request PDU
   send is presumably triggered later in the procedure (possibly from the
   LMP-receive dispatcher in item 5, once `LMP_SLOT_OFFSET` is acknowledged).
8. **`FUN_8004090c`, `FUN_800362b4`, `FUN_80033d28`, `FUN_800721a0`,
   `calls_fptr_down_LMP__47E_path`** — five small functions touched by the
   chains above whose names suggest relevance (timer arming, sniff cleanup,
   role-flag side-effects) but were not independently decompiled this pass.

---

## Tool note

No wairz blocker encountered for the work actually completed.
`DecompileAddr.java`/`DiagAddr.java` via `script_args` (0x-prefixed hex, GZF
process mode, `use_saved_project=true`) were sufficient for every decompile
in this doc, consistent with the router doc's note from the same day.

One **new** finding: `mcp__wairz__xrefs_to` (and presumably `xrefs_from`)
**does not work against the GZF filename** — it expects a different
binary_path format (attempted
`2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf` and got `Binary not found:
/data/firmware/projects/.../firmware/.../<name>.gzf`, i.e. it's looking in a
plain-ELF firmware-binary store, not the Ghidra-project GZF store used by
`run_ghidra_headless`'s GZF process mode). This is consistent with
CLAUDE.md's existing guidance to use `run_ghidra_headless` for this GZF
rather than the binary-analysis-oriented tools — not a new blocker, just
confirms those MCP tools are scoped to a different binary representation and
were correctly avoided in favor of Ghidra scripts for this investigation.
Not asking for a wairz change here since `run_ghidra_headless` covered
everything needed; noting it so a future session doesn't re-attempt
`xrefs_to`/`xrefs_from` against this GZF expecting it to work.

---

## Related documents

| File | Content |
|------|---------|
| `reverse_engineering_hci_command_router.md` | OGF 0x01/0x02 HCI handler addresses and full per-OCF case decompiles this doc traces *past* |
| `rom/reverse_engineering_hardware_layer.md` | §9: SSP/encryption trigger path (`FUN_80025b68`), and the existing "ROM's LMP PDU state machine" reference this doc's Open Question #5 follows up on |
| `rom/reverse_engineering_lmp_version_conn_setup.md` | §5: role-switch validation chain (`FUN_8002143c`/`FUN_8001acd8`/`FUN_8001ac74`) this doc completes with `FUN_8001ab44`'s decompile |
| `rom/reverse_engineering_conn_record_subsystem.md` | Connection-record struct layout (`bos_base`/`big_ol_struct`) referenced throughout |
| `kovah_function_list.md` | Pre-existing LMP/HCI handler names this doc builds on (`LMP_0x18_LMP_UNSNIFF_REQ`, `LMP_QUALITY_OF_SERVICE_REQ_0x2A`, `fHCI_Create_Connection_0x05`, `fHCI_Inquiry_0x01`, etc.) |
