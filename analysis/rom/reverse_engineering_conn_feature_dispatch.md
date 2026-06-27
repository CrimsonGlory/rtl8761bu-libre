# ROM Connection Feature/Parameter Negotiation Cluster (`FUN_80052c64` family)

Source: GZF process-mode decompiles, 2026-06-21 (Phase 9 consolidation).

---

## Overview

This cluster of ten ROM functions all funnel into (or are called by) a common
caller, `FUN_80052c64`, which was not itself in the consolidation list but
appears repeatedly as the calling context for every function below — strongly
suggesting `FUN_80052c64` is a connection-parameter / feature-negotiation
dispatcher and these are its leaf helpers and a generic list/refcounting
substrate it shares with other subsystems.

---

## 1. `FUN_80056988` — eSCO/SCO link-parameter negotiation + logging

`0x80056988`, 738 bytes — the largest function in this batch. Callers:
`FUN_8004cb48`, patch dispatch slot (`0x8010b906`), `FUN_8004bde8`,
`FUN_80052c64`.

This function is a long sequence of config-flag-gated checks and conditional
logging (`possible_logging_function__var_args`) around what is clearly an LMP
parameter-negotiation acceptance path: it inspects bits of an incoming PDU
buffer (`param_1`) for eSCO/SCO type fields (LMP PDU opcode in low nibble of
byte 0; flags in bytes 2-3), validates against feature-page-derived state
(`base_of_0x1ac_struct_array[10]`), and — when conditions are met — writes
3 staged 16-bit values into a per-connection sub-structure at computed offsets
`+0x28`/`+0x2a`/`+0x2c` or `+0x2e`/`+0x30`/`+0x32` (selected by whether the PDU
opcode is one of `{1,3,5}`, i.e. the "TX" vs "RX" or "initiator" vs
"responder" parameter set), each value sourced from a fixed global constant
(`DAT_80056c90`..`9c`..`a0`..`a4`).

**Purpose:** validates and stages eSCO/SCO connection parameters (timing
offsets/windows, by the `0x2e..0x34`-range struct offsets matching the eSCO
connection-record layout from `reverse_engineering_conn_record_subsystem.md`)
into the per-connection record, gated by negotiated LMP PDU flags and feature
bits, with extensive debug logging of the decision path. Returns a status
byte (read from a global flag byte) regardless of which path is taken.

## 2. `FUN_8004f25c` — Connection-record hash-bucket lookup/remove

`0x8004f25c`, 186 bytes. Callers: patch dispatch slot (`0x8010b94a`),
`FUN_80052c64`.

```c
int *FUN_8004f25c(int key, ushort *opt_match)
{
    // iterate over 2 hash buckets (uVar6 = 0, 1)
    for (bucket = 0; bucket < 2; bucket++) {
        for (node = bucket_head[bucket]; node != NULL; node = node->next) {
            delta = (key - node->field_0xc) & DAT_8003b1...;   // signed-distance hash compare
            if (delta exceeds half-range) continue;             // not a match, keep walking
            if (opt_match == NULL || node->field5 matches *opt_match (12-bit company-id-shifted field))
            {
                unlink node from its bucket list;
                node->next = 0;
                return node;                                     // found: unlink and return
            }
        }
    }
    return NULL;   // not found in either bucket
}
```

**Purpose:** a generic 2-bucket hash-table lookup-and-remove primitive keyed
by a distance/delta comparison (rather than exact equality) — i.e. it finds
the bucket entry whose key is "close enough" to the target, optionally also
matching a secondary 12-bit field, and removes it from the list. This is a
free-list/pending-queue pop operation, structurally similar to
`assign_pointer_to_0x1AC_offset_0x134` (append) documented in
`reverse_engineering_lmp_version_conn_setup.md` but operating on a different
(hash-bucketed) list structure — likely a pending-timer or pending-request
queue used by the eSCO/SCO negotiation path.

## 3. `FUN_800511b8` — Refcount increment

`0x800511b8`, 36 bytes. Callers: `FUN_80007af0`, `FUN_80007654`,
patch `FUN_8010c63c`, patch dispatch slot (`0x8010b990`), raw data caller
(`0x5eb8`), `FUN_80052c64`.

```c
void FUN_800511b8(int obj)
{
    if (obj != 0) {
        disable_interrupts...();
        obj->field_0x10 += 1;
        enable_interrupts...();
    }
}
```

**Purpose:** simple atomic (interrupt-disabled) refcount increment at offset
`+0x10` of a generic object. Companion to `FUN_80051124` below (decrement +
free-on-zero).

## 4. `FUN_80050b2c` — Connection BD_ADDR/codec-parameter commit with mismatch logging

`0x80050b2c`, 470 bytes. Callers: patch dispatch slot (`0x8010b9b4`),
`FUN_80052c64`.

```c
undefined4 FUN_80050b2c(byte *param_1, int param_2, uint param_3, undefined4 *param_4, int param_5)
{
    record = FUN_8004e820(param_5);    // resolve connection record from handle/index
    if (record == NULL) return 0;
    new_bdaddr = *param_4;
    if (new_bdaddr == NULL) {
        if ((*(param_5+8) & 2) && *(param_5+0x1d) == 0) record->field_0xe |= 4;
    } else {
        // resolve "negotiated codec/role byte" either from a per-connection-type table
        // (indexed by param_3, looked up in base_of_0x1ac_struct_array[10].field319_0x14c)
        // or falls back to deriving it from param_1's bit 6.
        if (record's state == 6 && sub-state == 2 && memcmp(record->bdaddr, new_bdaddr, 6) != 0)
            log_mismatch(...);    // BD_ADDR changed mid-negotiation: log it
        record->some_role_byte = derived_role;
        memcpy(record->bdaddr_field, derived_codec_param, 6);
        record->field_0xe = (record->field_0xe & ~3) | derived_role;
        memcpy(record->bdaddr2_field, new_bdaddr, 6);
    }
    if (param_4[3] != 0) {
        // negotiate/validate a "channel" field (12-bit) against param_4[3]'s structure,
        // logging on first-time-set vs. on mismatch with previously-set value.
    }
    return 1;
}
```

**Purpose:** commits negotiated connection parameters (BD_ADDR and a
role/codec selector byte) into the connection record, with consistency
checking against previously-committed values and verbose mismatch logging —
this is the actual "apply" step that `FUN_80056988`'s staged values
(case above) ultimately feed into via the shared caller `FUN_80052c64`.

## 5. `FUN_80050ff8` — Conditional connection-parameter re-validation trigger

`0x80050ff8`, 222 bytes. Callers: patch dispatch slot (`0x8010b9c2`),
`FUN_80052c64`.

```c
undefined4 FUN_80050ff8(undefined4 param_1, int *param_2, int param_3)
{
    flags = PTR_PTR_800510d8;
    record = *(param_3 + 0x14);
    if (((flags[5]>>2 & 3) is odd) && !(flags[5]&0x40) && (record->field_0xe & 4))
        return 0;   // short-circuit: feature disabled / connection flagged "no-renegotiate"
    if ((flags[5] & 3) && !(record->field_0xe & 4) && (*param_2 != 0 || !(param_3->field8 & 1))) {
        sub = FUN_80050ef8(record + 4, &local_buf);
        // merge/compare a "channel" 12-bit field similarly to FUN_80050b2c above
        result = FUN_8004f328(param_1, param_2, param_3, record);
        if (result != 0) sub->field_0x11 |= 0x10;   // mark "needs re-sync"
    }
    return 1;
}
```

**Purpose:** decides — based on global feature flags and the connection's
current "no-renegotiate" flag — whether to re-run channel/parameter
validation (`FUN_8004f328`) and, if it determines a mismatch, marks the
sub-record dirty for re-sync. This is a guarded re-validation entry point
invoked when connection parameters might have drifted (e.g. after an LMP
parameter-update procedure).

## 6. `FUN_8004f374` — eSCO/SCO negotiation diagnostic logger

`0x8004f374`, 368 bytes. Callers: patch dispatch slot (`0x8010baa6`),
`FUN_80052c64`.

Almost entirely composed of `possible_logging_function__var_args` calls
formatting fields of `param_1` (an LMP PDU buffer) and `param_2` (an array of
4 sub-record pointers) — dumping packet-type/window/offset fields for up to
4 related sub-records plus a trailing variable-length region. No state
mutation.

**Purpose:** pure diagnostic/trace logger for eSCO/SCO negotiation PDUs and
their associated sub-records — almost certainly compiled out or gated behind
a debug build flag in production, included here only because it's reachable
from the same `FUN_80052c64` dispatcher as the functional code above.

## 7. `FUN_80052c1c` — Negotiation-result post-processing dispatcher

`0x80052c1c`, 72 bytes. Callers: patch dispatch slot (`0x8010bac4`),
`FUN_80052c64` (self — `FUN_80052c64` calls this, consistent with it being
the umbrella dispatcher for this whole cluster).

```c
void FUN_80052c1c(p1, p2, p3, conn_record, p5)
{
    state = FUN_8004e820(conn_record);
    if (state != NULL && state->field0 == 0) {
        FUN_80052a38(p1, p2, p3, conn_record, p5, state);   // apply negotiated params
        FUN_80052774(conn_record, p5, state);                // follow-up / notify step
    }
}
```

**Purpose:** thin gate-and-dispatch wrapper — only proceeds if the connection
state's first field is clear (i.e. "not already finalized"), then calls two
further (out-of-scope-for-this-ticket) functions to apply parameters and
trigger a follow-up notification. `FUN_80052a38`/`FUN_80052774` were not in
the original consolidation list and remain undocumented; flagged here as a
natural follow-up target for future ROM RE work.

## 8. `FUN_8004e808` — Linked-list push (LIFO)

`0x8004e808`, 14 bytes. Caller: patch dispatch slot (`0x8010bad4`) only.

```c
void FUN_8004e808(undefined4 *node)
{
    if (node != NULL) {
        *node = *PTR_PTR_8004e818;     // node->next = current head
        *PTR_PTR_8004e818 = node;      // head = node
    }
}
```

**Purpose:** trivial singly-linked-list LIFO push onto a global free-list
head — the allocation-side counterpart to whatever consumes this list
(not traced further; out of scope here, but the pattern matches a simple
object free-list/pool allocator, consistent with this whole cluster managing
pooled per-negotiation sub-records).

## 9. `FUN_80051124` — Refcount decrement with free-on-zero

`0x80051124`, 66 bytes. Callers: patch dispatch slot (`0x8010bae2`),
`FUN_80051368`, `assoc_w_tLC_RX`, `FUN_8004ce70`, `FUN_80052f8c`,
`FUN_80051170`, `FUN_80051194`, `FUN_80052c64`.

```c
void FUN_80051124(int *obj)
{
    if (obj == NULL) return;
    disable_interrupts...();
    if (obj[4] != 0) obj[4]--;                  // decrement refcount at +0x10
    if (obj[4] == 0) {
        sub = obj[5];                            // +0x14: linked sub-object, if any
        if (sub != NULL) { *sub = *free_list_head_2; *free_list_head_2 = sub; }  // free sub-object
        obj[5] = 0;
        *obj = *free_list_head_1; *free_list_head_1 = obj;   // free obj itself
    }
    enable_interrupts...();
}
```

**Purpose:** the decrement-and-free counterpart to `FUN_800511b8`'s increment
— when the refcount at `+0x10` reaches zero, both the object and an optional
linked sub-object (`+0x14`) are returned to their respective free lists. This
confirms the cluster manages a refcounted pool of negotiation sub-records,
consistent with `FUN_8004e808`'s push primitive above and `FUN_8004f25c`'s
hash-bucket pop.

## 10. `FUN_8004e2d0` — Connection-record field accessor (4-byte)

`0x8004e2d0`, 30 bytes. Callers: this is the most widely-shared leaf in the
batch — called from `FUN_8010dd1c` (patch), `FUN_8010bba4` (the documented
**LMP VSC hook entry point**, see `firmware/reverse_engineering_lmp_vsc_hook.md`),
`FUN_80047c50` (the documented **VSC dispatcher**, see
`reverse_engineering_vsc_dispatcher.md`), `FUN_8004cb48` (×2),
`FUN_8010bb54` (patch), `FUN_80052c64` (self),
`send_evt_Meta_subevent_0x01_or_0x0a_HCI_LE_Connection_Complete_or_HCI_LE_Enhanced_Connection_Complete`,
`FUN_8004727c`, `FUN_80047304`, `FUN_80047628`, `FUN_80047980`, `FUN_80047bdc`.

```c
undefined4 FUN_8004e2d0(undefined1 conn_handle)
{
    iVar2 = FUN_8004e190(PTR_PTR_8004e2f0, conn_handle, &local_10);
    return (iVar2 != 0) ? local_10[0] : 0;
}
```

**Purpose:** a generic "look up a 4-byte field for this connection handle"
accessor on top of `FUN_8004e190` (a hash/lookup helper, not in this
consolidation batch). Its caller list — spanning the LMP VSC hook entry point,
the VSC dispatcher, multiple LE connection-complete event senders, and the
`FUN_80052c64` cluster — confirms this is a **core, widely-reused
connection-record field accessor**, not specific to the eSCO/SCO negotiation
cluster; it just happens to also be used there.

---

## Summary Table

| Function | Size | Role |
|----------|------|------|
| `FUN_80056988` | 738 B | eSCO/SCO parameter negotiation + staging (largest in batch) |
| `FUN_8004f25c` | 186 B | 2-bucket hash-table lookup-and-remove (distance-keyed) |
| `FUN_800511b8` | 36 B | Atomic refcount increment (+0x10) |
| `FUN_80050b2c` | 470 B | Commits negotiated BD_ADDR/role/codec params, logs mismatches |
| `FUN_80050ff8` | 222 B | Guarded connection-parameter re-validation trigger |
| `FUN_8004f374` | 368 B | Pure diagnostic logger for eSCO/SCO negotiation PDUs |
| `FUN_80052c1c` | 72 B | Gate-and-dispatch wrapper (finalized-state check) |
| `FUN_8004e808` | 14 B | Singly-linked free-list LIFO push |
| `FUN_80051124` | 66 B | Atomic refcount decrement + free-on-zero (pairs with `FUN_800511b8`) |
| `FUN_8004e2d0` | 30 B | Generic 4-byte connection-record field accessor (widely reused outside this cluster) |

**Architecture inferred:** `FUN_80052c64` (not itself decomposed in this
ticket — candidate for future work) is a connection feature/parameter
negotiation dispatcher that: looks up pending negotiation records from a
refcounted, hash-bucketed pool (`FUN_8004f25c`/`FUN_800511b8`/`FUN_80051124`/
`FUN_8004e808`), validates and stages eSCO/SCO parameters (`FUN_80056988`),
commits them into the connection record (`FUN_80050b2c`), optionally
re-validates (`FUN_80050ff8`) and finalizes (`FUN_80052c1c`), with diagnostic
logging throughout (`FUN_8004f374`). All ROM-only; no libre firmware
reimplementation required for this layer.

---

## Renames Applied (region `0x80050000` Pass 12, 2026-06-27)

This doc's analysis above (2026-06-21, Phase 9 consolidation) was never
actually applied as Ghidra renames — confirmed by a fresh `decompile_function`
round trip on 2026-06-27 (region `0x80050000` Pass 12) showing all 10
functions still resolving under their `FUN_*` names, with decompiled bodies
matching this doc's prose exactly (including `FUN_80050ff8`'s 12-bit channel
merge/compare logic, which this doc only summarized in prose and the fresh
decompile confirmed in full). Applied via `RenameEscoNegotiationCluster.java`
(`run_ghidra_headless`, `use_saved_project=true`, `script_file_id`).
Script's own per-address check: `renamed=10 alreadyOk=0 missing=0 failed=0`.
Independently re-verified in a separate `batch_decompile_functions` round
trip for 5 of the 10 — all resolve correctly under their new names.

| # | Old Name | New Name |
|---|----------|----------|
| 1 | `FUN_80056988` | `esco_sco_param_negotiate_and_stage` |
| 2 | `FUN_8004f25c` | `pending_negotiation_hash_pop_by_distance` |
| 3 | `FUN_800511b8` | `refcount_increment_atomic` |
| 4 | `FUN_80050b2c` | `conn_param_commit_bdaddr_and_role` |
| 5 | `FUN_80050ff8` | `conn_param_revalidate_if_dirty` |
| 6 | `FUN_8004f374` | `esco_sco_negotiation_diagnostic_logger` |
| 7 | `FUN_80052c1c` | `conn_negotiation_finalize_gate_dispatch` |
| 8 | `FUN_8004e808` | `free_list_lifo_push` |
| 9 | `FUN_80051124` | `refcount_decrement_and_free` |
| 10 | `FUN_8004e2d0` | `conn_record_get_4byte_field_by_handle` |

See `reverse_engineering_region_0x80050000.md` "Pass 12" for the full
per-function rationale and region-wide unnamed-count impact.
