# ROM Connection-Type Dispatch Hook & eSCO Packet-Type Selector

Source: GZF process-mode decompiles, 2026-06-21 (Phase 9 consolidation).

---

## Overview

Two related pieces, both already named in `CLAUDE.md`'s key-address table
(`FUN_80050810` is the "type-dispatch hook" installed at `bos_base+0xe0`):

1. **`FUN_80050810` + 4 connection-type handlers** — the dispatcher invoked
   via the `bos_base+0xe0` hook slot, and the four per-type handler functions
   it calls.
2. **`FUN_80044730`** — a small, separate eSCO packet-type-to-parameter
   lookup used during eSCO/SCO connection setup (in the call path of
   `FUN_80047628`/`FUN_80047980`, part of the already-documented SCO/eSCO
   layer — see `firmware/reverse_engineering_sco_esco_layer.md`).

---

## 1. `FUN_80050810` — Connection-type dispatch hook

`0x80050810`, 218 bytes. This is the function the patch installs at
`bos_base+0xe0` (per `CLAUDE.md`'s struct-offset table: `+0xe0` →
"`FUN_80050810` type-dispatch hook"). Callers: `FUN_800509b0`, `FUN_80050610`,
`FUN_800508f8`, `FUN_80050994`, `FUN_80047304` (×2), `FUN_80050a90`,
`FUN_80047628` (×2), `FUN_800509ec`.

```c
undefined1 FUN_80050810(int conn_record)
{
    // optional override hook: if installed and it handles the call, use its result
    if (override_hook_fptr != NULL) {
        if (override_hook_fptr(conn_record, &result) != 0) return result;
    }

    type = conn_record->field_0x8 & 7;     // 3-bit connection-type field
    switch (type) {
    case 0: status = FUN_800506ac(conn_record); if (status) return 5; break;
    case 1: FUN_8004e670(conn_record); break;
    case 2: FUN_8004e6f4(conn_record); break;
    case 3: FUN_8004e76c(conn_record); break;
    default: log_unhandled_type(type); break;
    }

    // After dispatch: compute a simple checksum/parity byte over bits of field_0xb
    // (7-bit field, weighted by table PTR_DAT_800508f4) and stash it at +0x9.
    checksum = 1;
    for (bit = 0; bit < 7; bit++)
        if (field_0xb & (1<<bit)) checksum += PTR_DAT_800508f4[bit];
    conn_record->field_0x9 = checksum;
    log_dispatch_result(type, subtype, field_0xb, checksum);
    return 0;
}
```

**Purpose:** confirmed as the connection-type dispatcher described in
`CLAUDE.md` — reads a 3-bit type field from the connection record and routes
to one of four type-specific setup handlers (types 0-3), with an optional
override hook checked first (consistent with the hook-table architecture
documented in `reverse_engineering_hardware_layer.md`). After dispatch it
computes a weighted-bit checksum over flag byte `+0xb` and stores it at
`+0x9` — this looks like a simple per-connection-type capability/parity
tag used by later code paths to validate the dispatch outcome.

## 2. `FUN_800506ac` — Type-0 connection setup handler

`0x800506ac`, 354 bytes. Only caller: `FUN_80050810`.

```c
undefined4 FUN_800506ac(int conn_record)
{
    flags = conn_record->field_0x20;       // 16-bit negotiated-feature flags
    if (!(flags & 0x10) && (flags & 3) == 3) return 5;   // invalid combination -> error

    conn_record->field_0xb = 0;
    conn_record->field_0x8 &= 0xe7;
    if (flags & 4) conn_record->field_0xb = 2;
    multi_flag = (flags & 3) != 0;

    if ((no linked sub-record) && !multi_flag) {
        conn_record->field_0xb |= 1;
        goto finish;
    }
    conn_record->field_0xb = (conn_record->field_0xb & 0xbc) | 0x18;
    if (flags & 1) conn_record->field_0x8 = (field_0x8 & 0xe7) | 8;
    else if (flags & 2) conn_record->field_0x8 = (field_0x8 & 0xe7) | 0x10;

    sub = conn_record->field_0x50;          // linked sub-record (e.g. paired eSCO link)
    if (sub == 0) {
        if (FUN_80050610(conn_record) != 0) return 5;
        conn_record->field_0x2b = (field_0x2b & 0x87) | 8;
        if (multi_flag) {
            if (FUN_800508f8(conn_record) != 0) return 5;
            conn_record->field_0x2b = (field_0x2b & 0x87) | (((field_0x2b>>3 & 0xf)+1 & 0xf) << 3);
        }
    } else {
        sub->field_0x8 = (sub->field_0x8 & 0x1f) | ((conn_record->field_0x1d >> 2) << 5);
        if (multi_flag) {
            sub2 = sub->field_0x24;
            if (sub2 == 0) { /* same FUN_800508f8 path as above */ }
            else sub2->field_0x8 = (sub2->field_0x8 & 0x1f) | ((conn_record->field_0x1d >> 2) << 5);
        }
    }
finish:
    if (flags & 0x20) conn_record->field_0xb &= 0xfe;
    return 0;
}
```

**Purpose:** the most elaborate of the four type handlers — manages a
**multi-link / linked-sub-record** connection type (likely the "primary +
secondary eSCO" or similar combined link case, given the `field_0x50`/`field_0x24`
sub-record chaining and the `multi_flag` derived from 2 negotiated-feature
bits). Validates the negotiated flags, sets up internal state bits, and
either initializes a fresh sub-record chain (calling `FUN_80050610`/
`FUN_800508f8`) or propagates parameters into an already-linked sub-record.

## 3. `FUN_8004e670` — Type-1 connection setup handler

`0x8004e670`, 130 bytes. Only caller: `FUN_80050810`.

```c
void FUN_8004e670(int conn_record)
{
    parent = conn_record->field_0x1c;
    flags = parent->field_0x20;
    conn_record->field_0xb = parent->field_0xb | 1;
    if (flags & 4)  conn_record->field_0xb |= 2;
    conn_record->field_0xb |= 8;
    conn_record->field_0xb = (parent->field_0x20_int == 0)
        ? (conn_record->field_0xb & 0xef) | 8
        : conn_record->field_0xb | 0x18;
    if (flags & 0x40) conn_record->field_0xb |= 0x40;
    if (flags & 0x20) conn_record->field_0xb &= 0xfe;
    type_bits = (parent->field_0x8 >> 3) & 3;
    conn_record->field_0x8 = (conn_record->field_0x8 & 0xe7) | (type_bits << 3);
    if (type_bits == 1) conn_record->field_0xb &= 0xef;
}
```

**Purpose:** inherits/derives state from a parent connection record
(`field_0x1c`) — propagates feature flags and the parent's type-bits field
into this connection's own state, with one conditional override
(`type_bits==1` clears a flag bit). This is the "child link inherits from
parent link" handler — i.e. type 1 represents a connection that's logically
subordinate to another (e.g. the secondary eSCO leg of a combined link set
up by `FUN_800506ac` above).

## 4. `FUN_8004e6f4` — Type-2 connection setup handler

`0x8004e6f4`, 118 bytes. Only caller: `FUN_80050810`.

```c
void FUN_8004e6f4(int conn_record)
{
    parent = conn_record->field_0x1c;
    if ((parent->field_0x8 & 0x18) == 8) {
        conn_record->field_0xb = 3;       // parent already in a specific sub-state: just set fixed value
    } else {
        flags = parent->field_0xb & 0xf1;
        conn_record->field_0xb = flags | 1;
        conn_record->field_0xb = (conn_record->field_0x20_int == 0) ? (flags | 1) : (flags | 0x11);
        conn_record->field_0xb &= 0xdf;
        if (parent->field_0x20 & 0x40) conn_record->field_0xb |= 0x40;
        if (parent->field_0x20 & 0x20) conn_record->field_0xb &= 0xfe;
    }
    conn_record->field_0x8 &= 0xe7;
}
```

**Purpose:** similar parent-inheritance pattern to `FUN_8004e670`, but with a
short-circuit fast path when the parent is already in sub-state `0x8`/`0x18`
(both bits set, i.e. a specific combined state) — sets a fixed flags value
`3` rather than deriving it. Type 2 likely represents another link role
variant in the same multi-link family.

## 5. `FUN_8004e76c` — Type-3 connection setup handler

`0x8004e76c`, 72 bytes — simplest of the four. Only caller: `FUN_80050810`.

```c
void FUN_8004e76c(int conn_record)
{
    parent = conn_record->field_0x1c;
    flags_b = parent->field_0xb & 0xf8;
    flags_20 = parent->field_0x20;
    conn_record->field_0xb = flags_b | 8;
    conn_record->field_0xb = (conn_record->field_0x20_int == 0) ? (flags_b & 0xe8) | 8 : (flags_b | 0x18);
    if (flags_20 & 0x40) conn_record->field_0xb |= 0x40;
    conn_record->field_0x8 &= 0xe7;
}
```

**Purpose:** the simplest variant of the same parent-inheritance pattern,
without the parent-substate fast-path branch that types 1/2 have. Likely the
default/simple case of a subordinate link with no special-casing needed.

---

## 6. `FUN_80044730` — eSCO packet-type-to-air-mode parameter table lookup

`0x80044730`, 102 bytes. Already partially explored by prior-art script
`ReadEscoPacketTables.java`; re-confirmed and documented here for the
consolidation ticket. Callers: `FUN_80047ca6`, `FUN_80047edc` — both inside
the already-documented SCO/eSCO layer
(`firmware/reverse_engineering_sco_esco_layer.md`).

```c
char FUN_80044730(int param_1, int conn_record)
{
    index = (ushort)(*(short*)(param_1 + 4) - 0x10);    // packet-type code, rebased from 0x10
    if (index < 0xe) {
        result_status = PTR_PTR_80044798[index];        // status/validity byte table (64 B @ 0x8007abb0)
        air_mode      = PTR_DAT_8004479c[index];          // air-mode/coding byte table (64 B @ 0x8007abc0)
    } else {
        air_mode = -1;
        result_status = 0x12;                             // HCI error: invalid packet type
    }
    if (conn_record != NULL) {
        if (result_status == 0) {
            conn_record->field_0x1d = (field_0x1d & 0x1f) | (air_mode << 5);  // store air-mode in top 3 bits
        } else {
            log_rejected_packet_type(result_status, *(short*)(param_1+4));
        }
    }
    return result_status;
}
```

Raw table contents (from the prior-art script dump):

```
PTR_PTR_80044798 -> 0x8007abb0 (status/validity per packet-type index 0..13):
  00 12 00 00 12 00 12 12 12 12 12 12 12 00

PTR_DAT_8004479c -> 0x8007abc0 (air-mode/coding per packet-type index 0..13):
  03 ff 02 00 ff 04 ff ff ff ff ff ff ff 01
```

Status `0x00` = accepted, `0x12` = rejected (HCI "Invalid HCI Command
Parameters"). Indices 0, 2, 3, 13 (and only those) are accepted, with air-mode
values `3`, `2`, `0`, `1` respectively — these correspond to the 4 valid eSCO
packet types in the Bluetooth spec (HV1/HV3/EV3/EV5-class encodings), matching
the `0x10`-based rebasing (eSCO packet type codes in HCI start at `0x10`/`0x20`
ranges depending on class).

**Purpose:** validates a requested eSCO packet type code against a fixed
14-entry table and, if valid, stores the corresponding air-mode/coding value
into the connection record's `field_0x1d` top 3 bits (used later to configure
the codec — tying into the codec-template pipeline already documented in
`reverse_engineering_hardware_layer.md` Section 9). Rejects (with HCI error
`0x12`) packet-type codes outside the valid range or falling on table entries
marked invalid (`0xff` air-mode sentinel, `0x12` status for index 1/5-12).

---

## Summary Table

| Function | Size | Role |
|----------|------|------|
| `FUN_80050810` | 218 B | `bos_base+0xe0` connection-type dispatch hook; routes to 4 handlers by 3-bit type field, computes a post-dispatch checksum |
| `FUN_800506ac` | 354 B | Type-0 handler: multi-link/linked-sub-record connection setup |
| `FUN_8004e670` | 130 B | Type-1 handler: inherits state from parent link |
| `FUN_8004e6f4` | 118 B | Type-2 handler: inherits state from parent link (with fast-path) |
| `FUN_8004e76c` | 72 B | Type-3 handler: inherits state from parent link (simplest variant) |
| `FUN_80044730` | 102 B | eSCO packet-type validation + air-mode lookup (14-entry table) |

**Libre firmware implication:** `FUN_80050810` is the documented hook target
for `bos_base+0xe0` — the libre patch must install this same ROM function (or
an equivalent) at that slot for any connection type other than the absolute
minimum to work; all four type handlers and the eSCO table are pure ROM, so
no patch-side reimplementation is needed for the dispatch logic itself, only
for installing the hook pointer (already covered by the existing patch
installer documentation in `firmware/reverse_engineering_patch_installer.md`).
