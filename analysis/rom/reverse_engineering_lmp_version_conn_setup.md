# ROM LMP Version Exchange & Connection-Setup Validation

Source: GZF process-mode decompiles, 2026-06-21 (Phase 9 consolidation).

---

## Overview

These ROM functions implement the LMP version-information exchange (the
`LMP_VERSION_REQ`/`LMP_VERSION_RES` PDU pair and its HCI surface,
`HCI_Read_Remote_Version_Information`) and the connection-record validation
path used when accepting/initiating ACL connections. Several of these already
carry Kovah-given descriptive names in the GZF, which is strong independent
confirmation of the purpose inferred from decompilation here.

---

## 1. `send_evt_HCI_Read_Remote_Version_Information_Complete` (`FUN_8001d4a0`)

`0x8001d4a0`, 134 bytes. Kovah-named. Callers: an LMP dispatch slot
(`0x8010b528`), patch `FUN_8010b64c`, ROM `0x800604e0`,
`fHCI_Read_Remote_Version_Information_0x1D_send_LMP_VERSION_REQ_0x25`,
`LMP_VERSION_RES_0x26`, and `FUN_8005e2c4`.

```c
void send_evt_HCI_Read_Remote_Version_Information_Complete
        (undefined1 status, uint conn_handle, uint param_3)
{
    if (param_3 < 10) {
        // param_3 indexes a small (<10-entry) array of cached remote LMP_VERSION_RES records
        entry = big_ol_struct_8001d530[param_3];
        version    = entry.LMP_VERSION_REQ_Version;
        company_id = entry.LMP_VERSION_REQ_Company_ID;
        subversion = entry.LMP_VERSION_REQ_Subversion;
    } else {
        if ((config_base->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ & 2) == 0) return;
        // param_3 >= 10 indexes into a second, larger per-connection (0x1ac-stride) struct array
        idx = param_3 - 10;
        entry2 = base_of_0x1ac_struct_array[1 + idx*0x1ac /* ... */];
        version    = entry2.field60_0x3c;
        company_id = entry2.field62_0x3e;
        subversion = entry2.field64_0x40;
    }
    hci_event_sender(0xc /* HCI_Read_Remote_Version_Information_Complete */,
                      {status, conn_handle_lo, conn_handle_hi, version, company_id_lo,
                       company_id_hi, subversion_lo, subversion_hi}, 8);
}
```

**Purpose:** formats and sends the `HCI_Read_Remote_Version_Information_Complete`
event (event code `0xc`). The remote version data is sourced from one of two
places depending on the connection index: a small fixed-size cache for the
first 10 connections/slots, or the per-connection `0x1ac`-stride structure
array (the same connection-record array documented elsewhere as
`base_of_0x1ac_struct_array`) for connection indices ≥10. Gated additionally by
a config feature bit for the second path.

## 2. `access_config_at_0xa5_and_0x1ac_stuct_stuff` (`FUN_8005e23c`)

`0x8005e23c`, 118 bytes. Kovah-named. Callers: LMP dispatch slot (`0x8010b576`),
patch `FUN_8010b5d8`, raw data caller (`0x4d70`),
`c_by_fHCI_Read_Remote_Version_Information_various_0x1ac_manip`, `FUN_8005e2c4`.

```c
int access_config_at_0xa5_and_0x1ac_stuct_stuff(void)
{
    iVar5 = FUN_8005d438(0xc, local_10);    // look up connection record for handle 0xc (placeholder param)
    if (iVar5 == 0xff) return 0;
    entry = base_of_0x1ac_struct_array[10];          // fixed slot index 10 — likely "self"/local device record
    local_buf[1] = entry.field0_0x0;
    local_buf[2] = (byte)config_base->_xa4_ushort_lmp_company_id;
    local_buf[3] = (byte)(config_base->_xa4_ushort_lmp_company_id >> 8);
    local_buf[4] = (byte)*DAT_8005e2bc;
    local_buf[5] = (byte)(*DAT_8005e2bc >> 8);
    possible_logging_function__var_args(...);  // logs the assembled record
    return local_buf;  // (returns ptr/value, used by caller to build LMP_VERSION_RES)
}
```

**Purpose:** assembles the **local** device's LMP version-info record (LMP
version field + company ID from `config_base->_xa4_ushort_lmp_company_id` +
a sub-version word) — i.e. this builds the *outgoing* `LMP_VERSION_RES` payload,
as opposed to `send_evt_HCI_Read_Remote_Version_Information_Complete` which
reports *remote* version info already received. The function name in the GZF
("config at 0xa5 and 0x1ac struct stuff") matches: it reads config blob offset
`0xa4`/`0xa5` (LMP company ID) and the local-device slot of the `0x1ac`-stride
connection array.

## 3. `assign_pointer_to_0x1AC_offset_0x134` (`FUN_8005d26c`)

`0x8005d26c`, 88 bytes. Kovah-named. **By far the most-called function in this
entire consolidation batch** — over 30 callers spanning nearly the whole
`0x8005dxxx`-`0x8006xxxx` LMP/connection-management region, plus patch
`FUN_8010b5d8`, plus a connection-establishment call site at `0x800493ce`.

```c
void assign_pointer_to_0x1AC_offset_0x134(undefined1 *ptr, uint slot_index)
{
    slot_index &= 0xff;
    if (ptr == NULL) return;
    uVar3 = disable_interrupts...();
    struct_array = base_of_0x1ac_struct_array_0xA_large2_1;
    head = &struct_array.field301_0x134[slot_index * 0x6b];   // linked-list head per slot
    if (head == NULL) {
        head = ptr;                              // list was empty: ptr becomes the head
    } else {
        // list non-empty: walk to existing tail (via struct_array._x138 "tail ptr" field)
        *(tail_node + 0x18) = ptr;               // append ptr after current tail
    }
    refcount = struct_array.field303_0x13c[slot_index * 0x1ac];
    struct_array._x138_tail_ptr[slot_index*0x6b] = ptr;        // update tail pointer
    struct_array.field303_0x13c[slot_index*0x1ac] = refcount + 1;  // bump refcount
    enable_interrupts...(uVar3);
    struct_array.field134_0x8d[slot_index*0x1ac] = *ptr;       // cache first byte of new entry
}
```

**Purpose:** generic intrusive-linked-list **append** operation onto a
per-slot list embedded in the connection-record (`0x1ac`-stride) structure
array at offset `0x134`, with refcounting at offset `0x13c` and a
first-byte cache at offset `0x8d`. Given the sheer breadth of callers (LMP
encryption/authentication/role-switch/feature-exchange handlers across
`0x8005dxxx`–`0x8006xxxx`), this is almost certainly the backing store for
**queued/pending LMP PDU records per connection** — e.g. a pending-procedure
queue, feature-request queue, or similar per-connection work list that many
independent LMP procedures append entries to.

## 4. `FUN_8005ca00` — Connection slot-budget OR/mask bit packer

`0x8005ca00`, 48 bytes. Callers: LMP dispatch slot (`0x8010b588`), patch
`FUN_8010b5d8`, raw data caller (`0x4dac`).

```c
void FUN_8005ca00(int conn_record, uint bit_index, uint value)
{
    if (conn_record->field0x7c != 0 || conn_record->field0x78 != 0) {  // only if either budget field set
        mask = 1 << (bit_index & 0x1f);
        conn_record->field0x84 |= mask;                                 // set "valid" bitmap bit
        conn_record->field0x88 = (conn_record->field0x88 & ~mask)
                                | ((value >> 1) << bit_index);          // set value bit (shifted)
    }
}
```

**Purpose:** conditional bitmap-and-value packer guarded by two "budget" fields
(`+0x78`/`+0x7c`, matching the slot-budget terminology already established in
`reverse_engineering_hardware_layer.md` for `FUN_8004f824`). Sets one bit of a
"valid" bitmap (`+0x84`) and the corresponding value bit in a parallel bitfield
(`+0x88`) — a compact per-connection boolean-array-with-presence-flag idiom,
likely tracking per-slot eSCO/SCO parameter validity (consistent with this
being part of the connection-record subsystem documented in
`reverse_engineering_conn_record_subsystem.md`).

## 5. `FUN_8001acd8` / `FUN_8002143c` / `FUN_8001ac74` — Link-policy connection acceptance validation

These three form a single validation chain used by the `HCI_CMD_OGF_02
(Link Policy)` command group, specifically reachable from
`HCI_CMD_OGF_02__Link_Policy__FUN_8002060c`.

### `FUN_8002143c` (172 bytes) — feasibility/role check

```c
uint FUN_8002143c(int bdaddr_and_role)
{
    role_byte = *(byte*)(bdaddr_and_role + 6);
    if ((role_byte & 0xfe) != 0) return 0x12;          // HCI error: invalid params (bad role byte)
    copy bdaddr (6 bytes);
    slot = look_for_non_matching_bdaddr_bos_index_i_e__free_connection_slot(bdaddr, &slot_out);
    if (slot != 0) return 2;                            // HCI error: unknown connection / no such device
    if ((*some_feature_page_base & 0x20) == 0) return 0x11;  // HCI error: unsupported feature (local)
    record = big_ol_struct[slot_out];
    if ((record._xe3_features_pages_array[0] & 0x20) == 0) return 0x1a;  // unsupported (remote)
    if (record._xb2_byte_minus_4_used_as_status_array_index != 4) return 0x21;  // wrong connection state
    // role/bdaddr_random consistency check, else fall through to:
    uVar6 = something_using_LMP_features();
    return (sign-extend trick) & 0x1f;                  // pass-through result from feature check
    // (else) logging + return 0xc (HCI error: connection rejected — unsupported feature)
}
```

**Purpose:** validates that a role-switch (or similar link-policy) request is
feasible: checks the requested role byte is sane, resolves the BD_ADDR to a
connection slot, confirms both local and remote feature pages advertise the
relevant capability bit (`0x20` — almost certainly the "role switch" LMP
feature bit), and checks the connection is in the expected state (status index
`4`). Returns standard HCI error codes (`0x02` Unknown Connection,
`0x11`/`0x1a` Unsupported Feature, `0x12` Invalid HCI Command Parameters,
`0x21` Command Disallowed, `0xc` Connection Rejected) or a feature-check
pass-through value on success.

### `FUN_8001acd8` (104 bytes) — connection-slot lookup + commit

```c
int FUN_8001acd8(int param_1, ushort *out_slot)
{
    copy bdaddr (6 bytes) from param_1+3;
    err = FUN_8002143c(param_1 + 3);             // run the validation above first
    if (err != 0) return err;
    look_for_non_matching_bdaddr_bos_index_i_e__free_connection_slot(bdaddr, &slot);
    if (slot == 0xff) return 2;                   // no matching connection
    record = big_ol_struct[slot];
    if (record._x58_crypto_struct[1] == 0) {
        *out_slot = slot;
        record.field_0x216 = 0;
        return FUN_8001ac74();                    // commit / kick off the role-switch procedure
    }
    return 0xc;                                    // already in progress -> Command Disallowed
}
```

**Purpose:** wraps `FUN_8002143c`'s validation with the actual slot lookup and
in-progress check, then commits by clearing a flag and invoking
`FUN_8001ac74`. Callers: patch `FUN_8010b7d4` and
`HCI_CMD_OGF_02__Link_Policy__FUN_8002060c` directly — i.e. this is the
backend for an `HCI_CMD_OGF_02` (Link Policy) vendor or standard command,
most plausibly `HCI_Switch_Role`.

### `FUN_8001ac74` (96 bytes) — role-switch state-machine kickoff

```c
undefined4 FUN_8001ac74(uint conn_idx)
{
    record = big_ol_struct[conn_idx & 0xffff];
    if (record.int_0x50 == -1) {
        if (FUN_80021f6c(conn_idx) != 0) return 0xc;     // pre-condition failed -> Command Disallowed
        if (get_byte_0x26_in_unknown_ptr_0x58_points_to_struct_at_least_0x27_big(conn_idx) == 0)
            return FUN_8001ab44(conn_idx);                 // fast path: kick off directly
        if (FUN_800220fc(conn_idx) == 0) return 0x1f;      // HCI error: parameter out of range
        set_bos_bosi__0xb2_index_arg2(conn_idx, 0xe);       // mark connection state = 0xe (role-switch pending)
    } else {
        record.field_0x216 = 1;                             // already linked to another op: just flag it
    }
    return 0;
}
```

**Purpose:** the actual role-switch (or equivalent link-policy procedure)
state-machine entry point — either starts the LMP procedure immediately via
`FUN_8001ab44`, defers it by setting connection state `0xe`, or simply flags
that a switch is requested if one is already linked/in-flight via `int_0x50`.

## 6. `FUN_8000c3f4` — Power-class / link-supervision config aggregator

`0x8000c3f4`, 414 bytes — the largest function in this batch. Callers:
`FUN_8010ce0c` (patch AFH/init chain), `FUN_8000e764`, `FUN_800122fc`.

```c
void FUN_8000c3f4(void)
{
    flags1 = config_base->field56_0x3e;
    if (optional_hook_fptr != NULL && optional_hook_fptr(0) != 0) return;  // bail if hook vetoes

    // Decide whether to apply an "extended" config word (local_14) based on a mix of:
    //  - a feature-page bit (DAT_8000c5ac & 1)
    //  - config flags (flags1 & 8 / & 1 / & 0x10)
    //  - a hardware feature register bit (*puVar5 >> 0x1e / >> 0x18)
    // ... sets local_14 |= 0x800000 and/or 0x200000 based on the above, tracked via bVar11.

    if (logging table not yet initialized) { initialize it; log_call(...); }
    if (bVar11) unknown_referencing_default_name_6(local_14);   // apply the aggregated config word

    flags2 = config_base->field57_0x3f;
    if (flags2 & 2) {
        if (local_14 & 0x200000) { reset 3 related state globals to 0/0/1; }
        if (flags2 & 4) { *state = FUN_8000bd78(); }            // refresh a derived state byte
        log/apply another derived flag (flags2 & 0x20 selects raw vs. boolean-normalized value);
    }
    if (post_hook_fptr != NULL) post_hook_fptr(&local_18);       // optional post-processing hook
}
```

**Purpose:** a config-blob-driven aggregator that decides, from a combination
of config flags and a hardware feature register, whether to apply an
"extended" baseband/link configuration word, then optionally calls a
pre-registered hook before and after. The exact semantics of the assembled
flags (`0x800000`, `0x200000`) aren't fully resolved, but the structure —
config-gated decision, optional pre/post hook function pointers, logging —
matches other "feature aggregator" functions in this ROM that gate
optional baseband behavior on the config blob (compare to
`reverse_engineering_config_blob.md`). Given its caller `FUN_8010ce0c` is part
of the AFH init chain, this likely participates in AFH or power-control
feature negotiation at connection bring-up.

---

## Summary Table

| Function | Size | Role |
|----------|------|------|
| `send_evt_HCI_Read_Remote_Version_Information_Complete` (`FUN_8001d4a0`) | 134 B | Sends `HCI_Read_Remote_Version_Information_Complete` event from cached remote version data |
| `access_config_at_0xa5_and_0x1ac_stuct_stuff` (`FUN_8005e23c`) | 118 B | Builds local LMP_VERSION_RES payload from config blob |
| `assign_pointer_to_0x1AC_offset_0x134` (`FUN_8005d26c`) | 88 B | Generic per-connection intrusive linked-list append (30+ callers) |
| `FUN_8005ca00` | 48 B | Budget-gated bitmap+value packer on connection record |
| `FUN_8002143c` | 172 B | Role-switch feasibility/feature validation |
| `FUN_8001acd8` | 104 B | Role-switch slot lookup + commit wrapper around `FUN_8002143c` |
| `FUN_8001ac74` | 96 B | Role-switch state-machine kickoff/defer |
| `FUN_8000c3f4` | 414 B | Config-blob-driven power/link config aggregator with pre/post hooks |

All eight functions are pure ROM with no patch-side reimplementation needed;
the libre firmware relies on the ROM's existing LMP/HCI state machine to reach
them and requires no new code at this layer.
