# Connection Record Subsystem — Reverse Engineering Notes

**Scope:** ROM functions managing the eSCO/SCO connection record pool.
Covers allocation, registration, lookup, hardware commit, and free.
These are the leaf helpers called by `FUN_80047c50` (VSC dispatcher) and `FUN_8005058c`.

---

## Function Index

| Address | Size | Role |
|---------|------|------|
| `0x80044730` | 102 B | `FUN_80044730` — eSCO packet-type validator + conn_rec field writer |
| `0x8005058c` | 54 B | `FUN_8005058c` — public allocator (allocate + set handle) |
| `0x800504e8` | 146 B | `FUN_800504e8` — intrusive free-list allocator (zeroes slot, extra init) |
| `0x8004e298` | 26 B | `FUN_8004e298` — write LMP handle + register in handle table |
| `0x8004e2b8` | 20 B | `FUN_8004e2b8` — deregister from handle table (reads handle from conn_rec) |
| `0x8004e100` | 84 B | `FUN_8004e100` — sorted-array insert (handle table registration) |
| `0x8004e190` | 52 B | `FUN_8004e190` — sorted-array search (handle table lookup) |
| `0x8004e0b0` | ? B | `FUN_8004e0b0` — binary search core (shared by e100 and e190) |
| `0x8004e2d0` | 30 B | `FUN_8004e2d0` — look up conn_rec by LMP handle |
| `0x800509b0` | 58 B | `FUN_800509b0` — hardware finalize / baseband commit |
| `0x8004fcb8` | 46 B | `FUN_8004fcb8` — free precondition check (conn_rec[0x08]&7 == 0?) |
| `0x8004fcec` | 60 B | `FUN_8004fcec` — release conn_rec back to free pool |
| `0x8004e5ac` | 188 B | `FUN_8004e5ac` — 3-level hardware sub-object hierarchy teardown |

---

## Connection Record Struct Layout

Size: **0x54 bytes (84 bytes)**, confirmed by `memset(slot, 0, 0x54)` in `FUN_800504e8`.

| Offset | Size | Field | Source / Notes |
|--------|------|-------|----------------|
| `+0x00` | 4 | `next_free` | Intrusive free-list next-ptr when slot is unallocated |
| `+0x08` | 1 | `pkt_modes` | bits`[2:0]` = conn type (0–3); bits`[5:3]` = mode flags; must be 0 before free |
| `+0x09` | 1 | `pkt_interval` | Computed T_eSCO interval; written by `FUN_80050810` |
| `+0x0a` | 1 | `flags_0a` | bit 0 copied from global `*PTR_DAT_80050584` on alloc |
| `+0x0b` | 1 | `pkt_type_bitmask` | 7-bit bitmask; each set bit ORs in an interval increment via `PTR_DAT_800508f4` |
| `+0x10` | 1 | `lmp_handle` | LMP connection handle — set by `FUN_8004e298` |
| `+0x11` | 1 | `pdu_slots` | PDU byte 8 — eSCO slot count |
| `+0x12` | 1 | `pdu_param_c` | PDU byte 9 — param C |
| `+0x13` | 1 | `field_13` | Zeroed on alloc; purpose unclear |
| `+0x14` | 4 | `tx_bw` / `sco_subobj` | **Dual-use**: Tx bandwidth from PDU (when active); sub-object ptr when freeing |
| `+0x18` | 4 | `rx_bw` | Rx bandwidth from PDU |
| `+0x1c` | 1 | `pkt_mode_hi` | High byte of packed mode word |
| `+0x1d` | 1 | `pkt_type_flags` | bits`[7:5]` = 3-bit eSCO packet-type code (written by `FUN_80044730`) |
| `+0x20` | 2 | `flags_word` | State flags; tested in `FUN_80047c50` |
| `+0x2c` | ? | `state_saved_1` | Temporarily overwritten by VSC gateway `FUN_8010bba4` |
| `+0x2e` | ? | `state_saved_2` | Temporarily overwritten by VSC gateway `FUN_8010bba4` |
| `+0x3d` | 1 | `tx_power` | Tx power level from PDU byte 27 |
| `+0x3f` | 1 | `param_e` | Low nibble masked on alloc; source unclear |
| `+0x46` | 6 | `bd_addr` | BD_ADDR (little-endian) from PDU bytes 14–19 |
| `+0x4c` | 4 | `default_vtable` | Set to `*PTR_DAT_80050588` on alloc; likely vtable or default-state ptr |
| `+0x50` | 4 | `hw_resource` | Pointer to hardware sub-object; walked by `FUN_800509b0` |

---

## 1 — `FUN_80044730`: eSCO Packet-Type Validator

**Address:** `0x80044730` (ROM) | **Size:** 102 bytes

### Purpose

Maps a 16-bit field from the LMP PDU to a 3-bit packet-type code, then writes it into `conn_rec[+0x1d][7:5]`.  
Returns `0x00` for a valid type, `0x12` for an unknown type.

### Signature

```c
char FUN_80044730(uint8_t *pdu, conn_rec_t *conn_rec);
// param_1 = PDU pointer
// param_2 = conn_rec pointer (may be NULL for pure validation)
// returns: 0x00 = valid, 0x12 = unknown type
```

### Algorithm

```c
char FUN_80044730(int param_1, int param_2)
{
    uint  idx  = (ushort)(*(short *)(param_1 + 4) - 0x10);  // PDU[4..5] − 0x10
    char  code, value;

    if (idx < 14) {                            // valid range: PDU[4..5] in [0x10, 0x1d]
        code  = PTR_PTR_80044798[idx];         // table A: 0 = OK, non-zero = error code
        value = PTR_DAT_8004479c[idx];         // table B: 3-bit packet-type value [0..7]
    } else {
        value = -1;                            // 0xFF (invalid)
        code  = 0x12;                          // BT error: Connection Rejected, Unknown Type
    }

    if (param_2 != NULL) {
        if (code == 0) {
            // pack value into bits[7:5] of conn_rec[0x1d]
            conn_rec[0x1d] = (conn_rec[0x1d] & 0x1f) | (value << 5);
        } else {
            // log: FUN_80074fa8(2, 0xcb, &DAT_000014a6, 0xd2e, 2, pdu, code, PDU[4..5])
            possible_logging_function(2, 0xcb, &DAT_000014a6, 0xd2e, 2,
                                      puVar1, cVar4, *(short*)(param_1+4));
        }
    }
    return code;
}
```

### Lookup Tables

Two 14-entry ROM tables indexed by `(PDU[4..5] − 0x10)`:

| Index | PDU[4..5] | `PTR_PTR_80044798` (status) | `PTR_DAT_8004479c` (3-bit value) | eSCO type (est.) |
|-------|-----------|-----------------------------|------------------------------------|-----------------|
| 0 | `0x0010` | 0 (OK) | TBD | HV1 / EV3 |
| 1 | `0x0011` | 0 / non-0 | TBD | HV2 / EV4 |
| … | … | … | … | … |
| 13 | `0x001d` | 0 / non-0 | TBD | 3-EV5 |
| ≥14 | any other | 0x12 | 0xFF | unknown |

### Dual-call Pattern

- Called with `param_2 = NULL` in **Phase 5** of `FUN_80047c50`: pure validation, no write.
- Called with `param_2 = conn_rec` in **Phase 13** of `FUN_80047c50`: validation + `conn_rec[0x1d]` write.

### Logger Call

- `FUN_80074fa8` at `0x80074fa8` — variadic logger.
- Error code `0xcb` (203), string ref `DAT_000014a6`, source line `0xd2e` (3374).

---

## 2 — `FUN_8005058c`: Public Connection Record Allocator

**Address:** `0x8005058c` (ROM) | **Size:** 54 bytes

### Purpose

Entry point for new SCO/eSCO connection record allocation.  
Allocates a free slot, stores the LMP handle, and reports success/failure via an output byte.

### Signature

```c
conn_rec_t *FUN_8005058c(uint8_t lmp_handle, uint8_t *status_out);
// param_1 = LMP handle byte
// param_2 = output status pointer; 0 = success, 7 = no free slot
// returns: conn_rec pointer on success, NULL (0) on failure
```

### Body

```c
conn_rec_t *FUN_8005058c(uint8_t param_1, uint8_t *param_2)
{
    conn_rec_t *slot = FUN_800504e8(0);    // allocate from free list, type=0
    if (slot == NULL) {
        *param_2 = 7;                      // error 7 = no free resources
    } else {
        FUN_8004e298(slot, param_1);       // set handle + register in lookup table
        *param_2 = 0;                      // success
    }
    return slot;
}
```

### Error Code

- `7` = "Insufficient Resources" — maps directly to the BT HCI `0x07` error.
- This is the same `local_60[0] = 7` seen in `FUN_80047c50` Phase 10 before calling `FUN_8004fcec`.

---

## 3 — `FUN_800504e8`: Intrusive Free-List Slot Allocator

**Address:** `0x800504e8` (ROM) | **Size:** 146 bytes

### Purpose

Pops the first entry from the connection record free list, zeroes it, and optionally applies field initialisation for eSCO slots (`param_1 == 0`).

### Free List

- Head pointer stored at `*PTR_PTR_8005057c` (ROM global, double-pointer).
- When a slot is on the free list, its first 4 bytes (`conn_rec[+0x00]`) hold the next-pointer.
- `FUN_800504e8` pops: `free_list_head = slot->next; memset(slot, 0, 0x54)`.

### Signature

```c
conn_rec_t *FUN_800504e8(uint8_t type);
// type = 0: allocate eSCO slot with full init
// type ≠ 0: allocate generic slot (zeroed only, no extra init)
// returns: slot pointer or NULL if pool exhausted
```

### Body (annotated)

```c
conn_rec_t *FUN_800504e8(char param_1)
{
    conn_rec_t **head_ptr = PTR_PTR_8005057c;   // global free list head
    conn_rec_t  *slot     = *head_ptr;           // first free slot

    if (slot == NULL) {
        // pool exhausted — log + panic
        possible_logging_function(2, 0xd2, 0x11f, 0xd3c, 1,
                                  PTR_unknown_dat_ref_by_logger_80050580, param_1);
        FUN_800504b4();   // panic / assert abort
    } else {
        *head_ptr = *(conn_rec_t **)slot;   // pop: head = slot->next
        memset(slot, 0, 0x54);              // zero entire 84-byte slot

        if (param_1 == 0) {                 // eSCO type init
            slot[0x08] &= 0xf8;            // clear pkt_modes bits[2:0]
            slot[0x1d] &= 0xfd;            // clear bit 1 of pkt_type_flags
            slot[0x13]  = 0;               // zero field_13 (already zero, defensive)
            slot[0x0a]  = (slot[0x0a] & 0xfe)
                          | (*PTR_DAT_80050584 & 1);   // copy global config bit 0
            slot[0x3f] &= 0x0f;            // mask param_e to lower nibble
            slot[0x1d] &= 0x1d;            // final mask on pkt_type_flags
            *(void**)(slot + 0x4c) = PTR_DAT_80050588; // store default vtable/state ptr
        }
    }
    return slot;
}
```

### Notes

- Steps 1–6 in the `param_1 == 0` branch are no-ops after `memset` but reflect the canonical initialisation for each field.
- The only meaningful work in extra-init is step 7: setting `conn_rec[0x4c] = PTR_DAT_80050588`.
- `PTR_DAT_80050584` is a single-byte global config flag whose bit 0 seeds `conn_rec[0x0a]`.
- `PTR_DAT_80050588` is a 4-byte ROM pointer (likely a vtable or default-state descriptor) stored at conn_rec offset 0x4c.
- Error path: `FUN_800504b4` is called unconditionally after the logger — likely a no-return panic / assertion trap.

---

## 4 — `FUN_8004e298`: LMP Handle Setter + Table Registration

**Address:** `0x8004e298` (ROM) | **Size:** 26 bytes

### Purpose

Writes the LMP handle into `conn_rec[+0x10]` and registers the (handle → conn_rec) mapping in a ROM lookup table.

### Body

```c
void FUN_8004e298(conn_rec_t *conn_rec, uint8_t lmp_handle)
{
    conn_rec[0x10] = lmp_handle & 0xff;           // store handle
    FUN_8004e100(*PTR_PTR_8004e2b4,               // table pointer
                 lmp_handle,                       // key
                 conn_rec);                        // value
}
```

### Sub-functions

- `FUN_8004e100` — **registration** into the handle→conn_rec table.
  - `*PTR_PTR_8004e2b4` is a ROM global pointing to the table base.
  - After this call, `FUN_8004e2d0` can find the conn_rec by handle.

---

## 5 — `FUN_8004e2d0`: Handle-to-conn_rec Lookup

**Address:** `0x8004e2d0` (ROM) | **Size:** 30 bytes

### Purpose

Looks up a connection record by LMP handle. Returns the conn_rec pointer, or 0 if not found.

### Body

```c
conn_rec_t *FUN_8004e2d0(uint8_t lmp_handle)
{
    conn_rec_t *result = 0;
    uint32_t    local[3];

    int found = FUN_8004e190(*PTR_PTR_8004e2f0,   // table pointer
                              lmp_handle & 0xff,   // key
                              local);              // output buffer

    if (found != 0) {
        result = (conn_rec_t *)local[0];           // out-param[0] = conn_rec pointer
    }
    return result;
}
```

### Sub-functions

- `FUN_8004e190` — **search** in the handle→conn_rec table.
  - `*PTR_PTR_8004e2f0` — table pointer; likely the same table as in `FUN_8004e298`.
  - Returns non-zero = found; output written into caller-provided buffer.

### Usage in the VSC Call Chain

Called by `FUN_80047c50` (VSC dispatcher) Phase 7 with the handle stored at `conn_rec_existing[+0x10]`, to retrieve the active connection record for parameter updates.

---

## 6 — `FUN_800509b0`: Hardware Sub-Object Commit Chain

**Address:** `0x800509b0` (ROM) | **Size:** 58 bytes

### Purpose

Commits the hardware sub-object tree at `conn_rec[+0x50]` to the baseband. Calls `FUN_80050994` on the root and each child pointer.

### Body

```c
uint FUN_800509b0(conn_rec_t *conn_rec)
{
    int err = FUN_80050810(conn_rec[0x50]);  // type-dispatch + interval compute for root
    if (err != 0) return 5;

    hw_obj_t *hw = conn_rec[0x50];
    if (hw != NULL) {
        FUN_80050994(hw);               // prep + commit root hw object

        if (hw[0x24] != NULL)
            FUN_80050994(hw[0x24]);     // prep + commit secondary hw object

        if (hw[0x20] != NULL)
            FUN_80050994(hw[0x20]);     // prep + commit tertiary hw object
    }
    return 0;
}
```

### `FUN_80050994` — Prep + Commit Pair (26 bytes)

```c
void FUN_80050994(hw_obj_t *obj)
{
    FUN_80050810(obj);   // type dispatch: compute interval, init by type
    FUN_8004f824(obj);   // write to baseband hardware registers
}
```

- `FUN_8004f824` is the **actual hardware write** — not yet decompiled.

### `FUN_80050810` — Connection Type Dispatcher (218 bytes)

Not a readiness check — it is a **full connection-type dispatcher** with interval computation:

```c
uint8_t FUN_80050810(conn_rec_t *param_1)
{
    // Optional patch hook: PTR_DAT_800508ec may hold a function pointer
    if (*PTR_DAT_800508ec != NULL) {
        uint8_t result[8];
        if ((*PTR_DAT_800508ec)(param_1, result) != 0)
            return result[0];   // hook handled it
    }

    // Dispatch on conn_rec[0x08] bits[2:0] = connection type
    switch (param_1[0x08] & 7) {
        case 0: if (FUN_800506ac(param_1) != 0) return 5; break;  // type 0 init
        case 1: FUN_8004e670(param_1); break;   // type 1 init
        case 2: FUN_8004e6f4(param_1); break;   // type 2 init
        case 3: FUN_8004e76c(param_1); break;   // type 3 init
        default: log_error(conn_type); break;   // types 4–7: unsupported
    }

    // Compute packet interval from conn_rec[0x0b] bitmask
    uint8_t pkt_interval = 1;
    for (int bit = 0; bit < 7; bit++) {
        if ((param_1[0x0b] >> bit) & 1)
            pkt_interval += PTR_DAT_800508f4[bit];  // per-bit interval table
    }
    param_1[0x09] = pkt_interval;   // store T_eSCO / polling interval

    // Log: (conn_type, pkt_mode_bits[5:3], conn_rec[0x0b], pkt_interval)
    log(4, 0xd2, &DAT_00001469, 0xd36, 4, ...,
        param_1[0x08] & 7, (param_1[0x08] >> 3) & 3, param_1[0x0b], pkt_interval);
    return 0;
}
```

**New conn_rec fields discovered:**

| Offset | Field | Description |
|--------|-------|-------------|
| `+0x09` | `pkt_interval` | Computed T_eSCO / polling interval — written here |
| `+0x0b` | `pkt_type_bitmask` | 7-bit bitmask; each set bit contributes to interval via `PTR_DAT_800508f4` |

**Connection types (conn_rec[0x08] bits[2:0]):**

| Value | Handler | BT Type (estimated) |
|-------|---------|---------------------|
| 0 | `FUN_800506ac` | SCO / HV1-HV3 |
| 1 | `FUN_8004e670` | eSCO EV3/EV4/EV5 |
| 2 | `FUN_8004e6f4` | 2-EV3/2-EV5 EDR |
| 3 | `FUN_8004e76c` | 3-EV3/3-EV5 EDR |
| 4–7 | log+error | unsupported |

**PTR_DAT_800508f4** — 7-entry byte table mapping each bit position in `pkt_type_bitmask` to an interval increment. Values unknown; derives T_eSCO from negotiated packet types.

**PTR_DAT_800508ec** — optional patch hook pointer (double-pointer); if non-null, the pointed-to function may override the entire prep operation.

### Notes

- Called by `FUN_80047c50` Phase 15 after all PDU fields are written.
- Error code `5` propagates back as the HCI status byte in the VSC response.
- `conn_rec[0x50]` is allocated separately; set up during eSCO parameter write flow, not by `FUN_800504e8`.

---

## 7 — `FUN_8004fcec`: Connection Record Release

**Address:** `0x8004fcec` (ROM) | **Size:** 60 bytes

### Purpose

Validates the conn_rec state, deregisters from the handle table, optionally releases a SCO sub-resource, then pushes the slot back onto the free list.

### Body

```c
void FUN_8004fcec(conn_rec_t *conn_rec)
{
    if (!FUN_8004fcb8()) return;    // validity/state check — 0 = not freeable

    FUN_8004e5ac(conn_rec);         // deregister from handle→conn_rec table
    FUN_8004e2b8(conn_rec);         // additional cleanup (timers? event notifications?)

    // if eSCO packet type bits are set, release the SCO sub-resource too
    if ((conn_rec[0x08] & 0x7) != 0) {
        // push conn_rec[0x14] to SCO sub-resource list at PTR_PTR_8004fd28
        void **sco_head   = PTR_PTR_8004fd28;
        void  *sco_subobj = conn_rec[0x14];
        sco_subobj->next  = *sco_head;
        *sco_head         = sco_subobj;
    }

    // push conn_rec to main free list at PTR_PTR_8004fd2c
    void **free_head = PTR_PTR_8004fd2c;
    conn_rec[0x00]   = *free_head;   // conn_rec->next = old_head
    *free_head       = conn_rec;     // head = conn_rec
}
```

### Sub-functions

| Address | Role |
|---------|------|
| `FUN_8004fcb8` | Validity check; returns 0 = skip free (not in freeable state) |
| `FUN_8004e5ac` | Deregister from handle table (inverse of `FUN_8004e100`) |
| `FUN_8004e2b8` | Unknown cleanup — possibly timer/event cancellation |

### Two Free Lists

| Global Pointer | Usage |
|----------------|-------|
| `PTR_PTR_8004fd28` | SCO sub-resource pool (populated when `conn_rec[0x08] & 7 != 0`) |
| `PTR_PTR_8004fd2c` | Main conn_rec free list (allocator head) |

**Note:** `PTR_PTR_8004fd2c` (freer) and `PTR_PTR_8005057c` (allocator) are two different PC-relative literal pool entries. They likely hold the same runtime address (same global variable), confirmed only if their literal values match — not yet verified.

### Dual-use of `conn_rec[+0x14]`

When the conn_rec is **free**: `conn_rec[0x00]` = next-ptr in free list.  
When the conn_rec is **active**: `conn_rec[0x14]` = Tx bandwidth (PDU) **or** SCO sub-object pointer, depending on which layer last wrote it. `FUN_80047c50` writes the raw PDU bandwidth; `FUN_800509b0`'s commit path transforms that into the hardware sub-object pointer stored at `conn_rec[0x50]`. The exact ordering needs further tracing.

---

## Updated Call Chain Diagram

```
ROM VSC hook (0x80009a6c)
  └─► FUN_8010bba4 [patch gateway]
        ├─ FUN_8004e2d0(handle) → conn_rec           [lookup existing]
        │    └─ FUN_8004e190(table, handle, out)
        └─► FUN_80047c50 [VSC dispatcher, 700 B]
              │
              ├─ Phase 5:  FUN_80044730(pdu, NULL)   [validate pkt type only]
              ├─ Phase 10: FUN_8005058c(handle, &st) [allocate new conn_rec]
              │             └─ FUN_800504e8(0)        [pop free list + zero + init]
              │             └─ FUN_8004e298(slot, h)  [set handle + register]
              │                  └─ FUN_8004e100(tbl, h, slot)
              │
              ├─ Phase 11–12: (write PDU fields to conn_rec)
              │                   → conn_rec[0x10..0x46] written
              │
              ├─ Phase 13: FUN_80044730(pdu, conn_rec) [validate + write pkt type bits]
              │                   → conn_rec[0x1d][7:5] written
              │
              ├─ Phase 14: FUN_8004e2d0 / optimized_memcpy (BD_ADDR copy)
              │
              ├─ Phase 15: FUN_800509b0(conn_rec)    [hardware finalize]
              │             ├─ FUN_80050810()         [hw readiness]
              │             └─ FUN_80050994(hw_obj)   [baseband commit x1-3]
              │
              └─ On error: FUN_8004fcec(conn_rec)    [release slot]
                            ├─ FUN_8004fcb8()         [validity check]
                            ├─ FUN_8004e5ac(slot)     [deregister]
                            ├─ FUN_8004e2b8(slot)     [cleanup]
                            └─ push to free list
```

---

## 8 — `FUN_8004e100` + `FUN_8004e190`: Sorted-Array Handle Table

**Addresses:** `0x8004e100` (insert, 84 B) / `0x8004e190` (search, 52 B) — ROM

### Data Structure

The handle→conn_rec mapping is a **sorted array of conn_rec pointers**, sorted by the LMP handle stored at `conn_rec[+0x10]`.

**Table descriptor** (3-word struct, pointed to by `*PTR_PTR_8004e2b4`):

| Word | Offset | Field | Description |
|------|--------|-------|-------------|
| 0 | `+0x00` | `array_base` | Pointer to the conn_rec pointer array |
| 1 | `+0x04` | `capacity` | Maximum entries |
| 2 | `+0x08` | `count` | Current entry count |

The elements of `array_base[]` are `conn_rec *` pointers, kept sorted by `conn_rec[0x10]` (LMP handle, 1 byte, unsigned).

### `FUN_8004e100` — Insert

```c
int FUN_8004e100(table_t *table, uint8_t handle, conn_rec_t *conn_rec)
// returns 1 on success, 0 if key already exists or table full
{
    int insert_idx;
    int found = FUN_8004e0b0(table, handle, &insert_idx);  // binary search
    if (!found && table->count < table->capacity) {
        // shift elements right from end down to insert_idx
        for (int i = table->count; i > insert_idx; i--)
            table->array_base[i] = table->array_base[i-1];
        table->array_base[insert_idx] = conn_rec;
        table->count++;
        return 1;
    }
    return 0;
}
```

### `FUN_8004e190` — Lookup

```c
bool FUN_8004e190(table_t *table, uint8_t handle, conn_rec_t **out)
// returns true if found; writes conn_rec pointer to *out
{
    int found_idx;
    bool found = FUN_8004e0b0(table, handle, &found_idx);
    if (found)
        *out = table->array_base[found_idx];
    return found;
}
```

### `FUN_8004e0b0` — Binary Search Core

Shared by both `FUN_8004e100` and `FUN_8004e190`. Returns 1 (found) or 0 (not found); writes the found/insertion index into the caller's output buffer. The sort key is `conn_rec[0x10]` — dereferenced from each pointer in the array.

### `FUN_8004e0b0` — Binary Search Core (Confirmed)

```c
// Standard iterative binary search; sort key = conn_rec[+0x10] = LMP handle
int FUN_8004e0b0(table_t *table, uint8_t key, int *out_idx)
{
    int lo = 0, hi = table->count - 1;
    while (lo <= hi) {
        int mid = (lo + hi) / 2;
        int diff = (uint8_t)(table->array_base[mid]->lmp_handle) - key;
        if (diff < 0)  lo = mid + 1;
        else if (diff > 0) hi = mid - 1;
        else { *out_idx = mid; return 1; }   // found
    }
    *out_idx = lo;   // insertion point
    return 0;        // not found
}
```

The critical line: `lbu s0, 0x10(s0)` — dereferences the conn_rec pointer and loads byte at offset `0x10` = LMP handle. **Sort key = `conn_rec[+0x10]` confirmed.**

### `FUN_8004e154` — Sorted Delete (Confirmed)

```c
void FUN_8004e154(table_t *table, uint8_t key)
{
    int idx;
    if (FUN_8004e0b0(table, key, &idx)) {
        // shift elements left from idx+1 to count-1
        while (idx < table->count - 1) {
            table->array_base[idx] = table->array_base[idx+1];
            idx++;
        }
        table->count--;
    }
}
```

Exact inverse of `FUN_8004e100`'s insert: finds position, shifts left, decrements count.

### Notes

- Array is trivially small (BT Classic max ~3 eSCO links), so O(n) shift is fine.
- All three globals `PTR_PTR_8004e2b4` (register), `PTR_PTR_8004e2f0` (search), and `PTR_PTR_8004e2cc` (deregister) are PC-relative literal pool entries pointing to the **same** underlying table descriptor global.

---

## 9 — `FUN_8004e2b8`: Handle Deregistration

**Address:** `0x8004e2b8` (ROM) | **Size:** 20 bytes

Reads the LMP handle from `conn_rec[+0x10]` and calls `FUN_8004e154(table, handle)` to remove it from the sorted array.

```c
void FUN_8004e2b8(conn_rec_t *conn_rec)
{
    FUN_8004e154(*PTR_PTR_8004e2cc, conn_rec[0x10]);
    // FUN_8004e154 = sorted-array delete (shift left)
}
```

This is the direct inverse of `FUN_8004e298` — together they maintain the sorted table invariant.

---

## 10 — `FUN_8004fcb8`: Free Precondition Check

**Address:** `0x8004fcb8` (ROM) | **Size:** 46 bytes

```c
bool FUN_8004fcb8(conn_rec_t *conn_rec)
// returns true = freeable, false = still active (logs error)
{
    bool ok = (conn_rec[0x08] & 7) == 0;
    if (!ok)
        possible_logging_function(2, 0xd2, 0x1ed, 0xd39, 1,
                                  PTR_unknown_dat_ref_by_logger_8004fce8,
                                  conn_rec[0x08] & 7);
    return ok;
}
```

- Returns `true` only if `pkt_modes[2:0]` = `0b000` — i.e., the connection type has been cleared.
- The **caller** (LMP teardown path) must zero `conn_rec[0x08] bits[2:0]` before invoking `FUN_8004fcec`.
- Logger error code `0xd2`, source line `0xd39`, format string at `PTR_unknown_dat_ref_by_logger_8004fce8`.
- The subsequent `if (conn_rec[0x08] & 7)` check inside `FUN_8004fcec` is dead code when reached through the normal free path.

---

## 11 — `FUN_8004e5ac`: Hardware Sub-Object Hierarchy Teardown

**Address:** `0x8004e5ac` (ROM) | **Size:** 188 bytes

### Purpose

Releases the entire hardware sub-object tree rooted at `conn_rec[+0x50]` back to the hardware object pools. Clears `conn_rec[+0x50] = NULL` when done.

### Object Hierarchy

```
conn_rec
└─ [+0x50]: hw_resource  (struct with ≥10 fields)
    ├─ [0x20]: singly-linked list of sub_obj_A (up to 11 items)
    │    each sub_obj_A:
    │      [0x08] bits[2:0] = type flags
    │      [0x14] = sco_subobj ptr (freed to PTR_PTR_8004e668 if type != 0)
    │      [0x00] = next ptr (intrusive linked list)
    └─ [0x24]: sub_obj_B (head of a similar chain via sub_obj_B[0x20])
         each item follows same pattern as sub_obj_A
```

### Algorithm

```c
void FUN_8004e5ac(conn_rec_t *param_1)
{
    hw_resource_t *hw = conn_rec[0x50];
    if (hw == NULL) return;

    hw[0x1c] = 0;   // hw_resource[7] = 0 — clear active state

    // Release chain at hw[0x24] (sub_obj_B and its sub-list)
    sub_obj_t *chain_b = hw[0x24];
    if (chain_b != NULL) {
        sub_obj_t *node = chain_b[0x20];  // chain_b[8] = linked list start
        for (int i = 0; i < 11 && node != NULL; i++) {
            sub_obj_t *next = node[0x20];
            if (node[0x08] & 7)            // has SCO sub-resource
                push(PTR_PTR_8004e668, node[0x14]);
            push(PTR_PTR_8004e66c, node);  // free node to hw pool
            node = next;
        }
        if (chain_b[0x08] & 7)
            push(PTR_PTR_8004e668, chain_b[0x14]);
        push(PTR_PTR_8004e66c, chain_b);
        hw[0x24] = NULL;
    }

    // Release chain at hw[0x20] (sub_obj_A list)
    sub_obj_t *chain_a = hw[0x20];
    if (chain_a != NULL) {
        sub_obj_t *node = chain_a;
        for (int i = 0; i < 11 && node != NULL; i++) {
            sub_obj_t *next = node[0x20];
            if (node[0x08] & 7)
                push(PTR_PTR_8004e668, node[0x14]);
            push(PTR_PTR_8004e66c, node);
            node = next;
        }
        hw[0x20] = NULL;
    }

    // Release hw_resource itself
    if (hw[0x08] & 7)
        push(PTR_PTR_8004e668, hw[0x14]);
    push(PTR_PTR_8004e66c, hw);
    conn_rec[0x50] = NULL;
}
```

### Pools Referenced

| Global | Pool |
|--------|------|
| `PTR_PTR_8004e668` | SCO sub-resource pool (freed when type bits set) |
| `PTR_PTR_8004e66c` | Hardware object pool (hw_resource + sub-objects) |

These are **distinct** from the conn_rec pool (`PTR_PTR_8004fd2c` / `PTR_PTR_8005057c`).

### Implications

- `conn_rec[+0x50]` is a **separate allocation** from the conn_rec itself, pointing to a hardware object tree.
- The tree has at most 1 + 1 + 11 + 11 = 24 nodes (hw_resource + chain_b + chain_b_sub-list + chain_a_sub-list). In practice BT Classic eSCO is far smaller.
- The `[0x14]` field in sub-objects is a pointer to a further sub-resource (e.g., SCO slot descriptor), freed to a separate pool.

---

## ROM Globals Summary

| Literal Pool Address | Name | Description |
|----------------------|------|-------------|
| `*PTR_PTR_8005057c` | `conn_rec_free_head` | Head of conn_rec free list (allocator side) |
| `*PTR_PTR_8004fd2c` | `conn_rec_free_head` | Same global (freer side — different literal pool entry) |
| `*PTR_PTR_8004fd28` | `sco_subobj_free_head` | (appears dead at free; see `FUN_8004e5ac`) |
| `*PTR_PTR_8004e2b4` | `handle_table_desc` | Table descriptor: register side |
| `*PTR_PTR_8004e2f0` | `handle_table_desc` | Same table descriptor: search side |
| `*PTR_PTR_8004e2cc` | `handle_table_desc` | Same table descriptor: deregister side |
| `*PTR_PTR_8004e668` | `sco_subobj_pool` | SCO sub-resource pool (freed by `FUN_8004e5ac`) |
| `*PTR_PTR_8004e66c` | `hw_obj_pool` | Hardware object pool (freed by `FUN_8004e5ac`) |
| `*PTR_DAT_80050584` | `config_flag_0` | Single byte; bit 0 seeds `conn_rec[0x0a]` on alloc |
| `*PTR_DAT_80050588` | `default_vtable` | 4-byte ptr stored at `conn_rec[0x4c]` on alloc |
| `PTR_PTR_80044798` | `esco_status_table` | 14-entry ROM array: eSCO type → error code |
| `PTR_DAT_8004479c` | `esco_value_table` | 14-entry ROM array: eSCO type → 3-bit packet-type value |

---

## Implications for Libre Replacement

### What must be re-implemented

1. **Connection record pool** — 84-byte (`0x54`) structs in a singly-linked free list.
   - Pool must be in writable RAM; size TBD from `FUN_800504b4` panic path (how many entries before exhaustion?).
   - `conn_rec[0x4c]` must be initialised to a valid default-state descriptor on every alloc.

2. **eSCO packet-type lookup tables** — the two 14-entry tables at `PTR_PTR_80044798` / `PTR_DAT_8004479c` are ROM constants that must be reproduced exactly. The table values (error codes and 3-bit packet-type mappings) are not yet fully resolved; decompiling is blocked until the raw bytes of those ROM addresses are read.

3. **Handle→conn_rec registration** — a small table (array or hash) mapping the 8-bit LMP handle to a conn_rec pointer. `FUN_8004e100` (register) and `FUN_8004e190` (search) can be called directly from the libre patch; no reimplementation needed for these ROM utilities.

### What can call ROM directly

- `FUN_8004e100` / `FUN_8004e190` — handle table operations (pure ROM, no state in the patch)
- `FUN_80050810` / `FUN_80050994` — hardware commit primitives (hardware-specific, ROM only)
- `FUN_80074fa8` — logger
- `FUN_8000e98c` — memset

### Resolved since initial draft

| Item | Resolution |
|------|------------|
| Handle-table structure | Sorted array of conn_rec ptrs; descriptor = `[base, capacity, count]` |
| `FUN_8004e100` / `FUN_8004e190` | Insert / lookup via binary search `FUN_8004e0b0` |
| `FUN_8004e5ac` | 3-level hw hierarchy teardown; uses `PTR_PTR_8004e668` / `PTR_PTR_8004e66c` |
| `FUN_8004e2b8` | Deregistration via `FUN_8004e154(table, handle)` |
| `FUN_8004fcb8` | Checks `conn_rec[0x08] & 7 == 0`; must be cleared before free |

### Resolved since second update

| Item | Resolution |
|------|------------|
| `FUN_8004e0b0` | Standard binary search; sort key = `conn_rec[+0x10]` (LMP handle) confirmed |
| `FUN_8004e154` | Sorted delete via shift-left + count-- |
| `FUN_80050810` | Connection type dispatcher (types 0–3) + packet interval compute → `conn_rec[+0x09]` |
| `FUN_80050994` | `FUN_80050810(obj) + FUN_8004f824(obj)` — type prep then hardware write |

### Resolved — hardware layer (see `reverse_engineering_hardware_layer.md`)

| Item | Resolution |
|------|------------|
| `FUN_8004f824` | Slot budget validator + hook; actual HW writes via hook at `0x801212e4` |
| `PTR_DAT_800508f4` / `DAT_8007abd8` | 7-byte table `{6,6,1,2,3,18,1}`; interval = 1 + Σ table[i] for set bits |
| Hook RAM addresses | `0x801212e0` (FUN_80050810 override) and `0x801212e4` (HW write), in patch data area |

### Still unknown

| Item | Needed for |
|------|------------|
| Non-free hook at `0x801212e4` — what registers it writes | **Critical** for libre HW implementation |
| `FUN_800506ac` / `FUN_8004e670` / `FUN_8004e6f4` / `FUN_8004e76c` | Type 0–3 hardware init |
| Raw bytes of `PTR_PTR_80044798` + `PTR_DAT_8004479c` | Complete eSCO packet-type table values |
| Confirm `PTR_PTR_8005057c` == `PTR_PTR_8004fd2c` | Verify single vs dual free list |
| hw_resource struct layout | `FUN_8004e5ac` accesses `[0x08]`, `[0x14]`, `[0x1c]`, `[0x20]`, `[0x24]` — full struct TBD |
