# Hardware Layer — `FUN_8004f824` and the Interval Table

**Scope:** The bottom two layers of the eSCO commit chain: `FUN_8004f824` (slot
budget validator) and the 7-entry interval table at `DAT_8007abd8`.  Also documents
the hook-based hardware abstraction architecture that is essential for the libre
replacement.

---

## 1 — `FUN_8004f824`: Slot Budget Validator + Hardware Hook

**Address:** `0x8004f824` (ROM) | **Size:** 106 bytes

### What it is NOT

Despite being called "hardware commit" in earlier notes, `FUN_8004f824` does **not**
write any baseband registers itself.  Its primary path is an optional hook call; the
fallback is a slot-budget validation.  The actual register writes live in the hook.

### Signature

```c
uint8_t FUN_8004f824(hw_obj_t *obj);
// obj = hardware sub-object (rooted at conn_rec[+0x50])
// returns: 0 on success, 5 on error (type not set)
```

### Logic

```c
uint8_t FUN_8004f824(hw_obj_t *obj)
{
    // ── Hook path ──────────────────────────────────────────────────────────
    code *hook = *(code **)0x801212e4;   // RAM variable; patch installs a fn here
    if (hook != NULL) {
        uint8_t result[8];
        if (hook(obj, result) != 0)
            return result[0];   // hook handled it completely → use its return value
        // hook returned 0 = "not handled" → fall through to ROM fallback
    }

    // ── ROM fallback ───────────────────────────────────────────────────────
    if ((obj[0x08] & 7) == 0) {
        // conn type must be non-zero; if zero, something set up conn_rec wrong
        log(2, 0xd2, &DAT_000014a7, 0xd39, 1, ..., obj[0x08] & 7);
        return 5;
    }

    // Slot budget clamp: pkt_interval + pdu_slots + 1 must fit in 8 bits
    if ((uint)(obj[0x09] + obj[0x11] + 1) > 0xFF) {
        obj[0x11] = 0xFE - obj[0x09];   // clamp: slots = 0xFE − interval
        //  → interval + clamped_slots + 1 = T + (0xFE − T) + 1 = 0xFF ✓
    }
    return 0;
}
```

### Slot Budget Constraint

| Field | Offset | Content |
|-------|--------|---------|
| `pkt_interval` | `obj[+0x09]` | T_eSCO in slots, computed by `FUN_80050810` |
| `pdu_slots` | `obj[+0x11]` | W_eSCO window width in slots, from PDU |

Invariant enforced: **`pkt_interval + pdu_slots + 1 ≤ 0xFF`**

If violated, `pdu_slots` is reduced to `0xFE − pkt_interval` so the sum is exactly `0xFF`.
This matches the Bluetooth spec constraint that `T_eSCO > W_eSCO` and both fit in the
LMP_eSCO_link_req encoding.

### Hardware Hook Variable

- **Location:** `0x801212e4` (RAM)
- **Type:** `uint8_t (*)(hw_obj_t *obj, uint8_t *result_buf)`
- **Installed by:** nothing in analyzed firmware (stays NULL); see Section 12 for the
  separate per-connection hook installed by ROM `FUN_80025b68` during SSP pairing
- **ROM default:** `NULL` until `FUN_80025b68` runs
- **Effect if NULL:** ROM fallback runs (validation only; no hardware registers written)
- **Effect if set:** called with `(obj, result_buf)`; executes runtime-generated MIPS16e code

**Architecture — runtime code generation (confirmed 2026-06-08):**

ROM `FUN_80025b68` (called at connection setup with a connection handle) does:
```c
void FUN_80025b68(uint handle) {
    sub_struct *s = conn_array[handle]._x58;    // per-connection sub-struct
    s[0xe0] = FUN_800240a4(handle);             // codec type byte
    s[0xe1] = 1; s[0xe2] = 1 or 2;             // capability flags
    *(void **)(s + 0xe4) = s + 0x13e;           // INSTALL: hook → code buffer
    s[0xe3] = (s[0x214] ? 0x40 : 0x30);        // packet window
    // copy codec template (reversed bytes) from ROM table into s+0x13e:
    if (s[0x1f1] == 6)
        memcpy_reversed(s+0x13e, ROM_codec6_table + handle*0x30, 0x30);
    else if (s[0x1f1] == 8)
        memcpy_reversed(s+0x13e, ROM_codec8_table + handle*0x40, 0x40);
    // ... then calls FUN_80024218, FUN_80068428/800688b0, set_arg1_1_to_arg2
}
```

`s + 0x13e` is a **RAM buffer that receives pre-built MIPS16e machine code** from ROM
lookup tables (`PTR_DAT_80025ca8` for codec-type-6, `PTR_DAT_80025cac` for type-8`).
The bytes are stored reversed in the ROM table and un-reversed on copy.
`FUN_8004f824` then calls this generated code as `fn(obj, result_buf)`.

**This is where the actual hardware write lives.**  The ROM's fallback does not write
any hardware registers; it only validates and clamps the slot budget.  For eSCO
connections to work at all, `FUN_80025b68` must be reachable and called correctly.

---

## 2 — `FUN_80050810` Hook Variable

**Location:** `0x801212e0` (RAM, patch data region; 4 bytes before the `FUN_8004f824` hook)

- **Type:** `uint8_t (*)(hw_obj_t *obj, uint8_t *result_buf)`
- Checked at the very start of `FUN_80050810` (connection type dispatcher).
- If non-null and returns non-zero, the hook's result is used instead of the dispatcher.
- Allows the patch to override the entire type-dispatch + interval computation.

---

## 3 — Hook Table in RAM

Two adjacent 4-byte hook variables in the patch data area:

| RAM Address | Checked by | Purpose |
|-------------|------------|---------|
| `0x801212e0` | `FUN_80050810` | Override type dispatch + interval computation |
| `0x801212e4` | `FUN_8004f824` | Override / implement baseband register write |

Both use the same **double-pointer** access pattern:

```asm
lw  v0, PTR_DAT_xxxxx    ; load address of RAM slot from ROM literal pool
lw  v0, 0(v0)            ; dereference: load actual hook function pointer
beqz v0, skip            ; if NULL, skip hook
jalr v0                  ; call hook(obj, result_buf)
```

This is the mechanism by which ROM exposes all hardware-sensitive operations to
the patch firmware without hard-wiring patch addresses into ROM.  The libre
replacement must install at minimum the function at `0x801212e4`.

---

## 4 — Interval Table `DAT_8007abd8`

**ROM address:** `0x8007abd8`  
**Referenced via:** `PTR_DAT_800508f4` → pointer word `0x8007abd8` at pool `0x800508f4`  
**Size:** 7 bytes

### Raw Bytes

```
Addr       +0    +1    +2    +3    +4    +5    +6
8007abd8   0x06  0x06  0x01  0x02  0x03  0x12  0x01
           = 6    = 6    = 1    = 2    = 3    = 18   = 1
```

### How it is used (from `FUN_80050810`)

```c
uint8_t pkt_interval = 1;
for (int bit = 0; bit < 7; bit++) {
    if ((conn_rec[0x0b] >> bit) & 1)
        pkt_interval += DAT_8007abd8[bit];
}
conn_rec[0x09] = pkt_interval;
```

`conn_rec[0x0b]` is a 7-bit bitmask; each set bit contributes its table entry to the
accumulated interval.

### Bit → Interval Contribution Mapping

| Bit | Table value | Running total (if only this bit set) | BT type (estimated) |
|-----|-------------|--------------------------------------|---------------------|
| 0 | 6  | 1 + 6 = **7**  | HV3 / EV3 (T_eSCO = 6 slots) |
| 1 | 6  | 1 + 6 = **7**  | EV3 (duplicate or alternate mode) |
| 2 | 1  | 1 + 1 = **2**  | HV1 (T_SCO = 2 slots) |
| 3 | 2  | 1 + 2 = **3**  | HV2 (T_SCO = 4 slots?) |
| 4 | 3  | 1 + 3 = **4**  | EV4 / 2-EV3 |
| 5 | 18 | 1 + 18 = **19** | 3-EV5 / 2-EV5 (high-bandwidth, long T) |
| 6 | 1  | 1 + 1 = **2**  | Reserved / HV1-equivalent |

The `pkt_interval` result in `conn_rec[0x09]` is what `FUN_8004f824`'s budget check
tests: `pkt_interval + pdu_slots + 1 ≤ 0xFF`.

### Interpretation note

The bit→type mapping is an estimate.  `conn_rec[0x0b]` likely encodes the negotiated
eSCO packet types differently from the HCI 16-bit `packet_type` bitmask.
The large value of **18 at bit 5** is consistent with 2-EV5/3-EV5 (BT EDR eSCO at
2 Mbps/3 Mbps), which has T_eSCO = 12 slots in the spec.  The field stores a
firmware-internal representation, not the raw HCI bitmask.

### Reproduction requirement

The libre patch must embed this exact 7-byte table (or compute equivalent values):

```c
// Interval increment table — ROM DAT_8007abd8
static const uint8_t esco_interval_table[7] = {
    0x06, 0x06, 0x01, 0x02, 0x03, 0x12, 0x01
};
```

The table is in ROM and can be called via ROM without copying, but the libre patch
may need it for its own interval computations if it bypasses ROM.

---

## 5 — Literal Pool Cross-Reference

Addresses extracted from the literal pools adjacent to `FUN_80050810` and `FUN_8004f824`:

| Pool address | Bytes (LE) | Resolved value | Content |
|--------------|-----------|----------------|---------|
| `0x800508ec` | `e0 12 12 80` | `0x801212e0` | FUN_80050810 hook RAM var |
| `0x800508f0` | `00 9b 07 80` | `0x80079b00` | Unknown ROM constant (string?) |
| `0x800508f4` | `d8 ab 07 80` | `0x8007abd8` | Interval table base |
| `0x8004f890` | `e4 12 12 80` | `0x801212e4` | FUN_8004f824 hook RAM var |
| `0x8004f894` | `00 9b 07 80` | `0x80079b00` | Same ROM constant (shared) |

---

## 6 — Revised `FUN_80050994` Understanding

```c
void FUN_80050994(hw_obj_t *obj)
{
    FUN_80050810(obj);   // (1) type dispatch, compute interval → obj[0x09]
    FUN_8004f824(obj);   // (2) call HW hook or clamp slot budget
}
```

Step (1) may itself call a hook at `0x801212e0`.  
Step (2) calls the hardware write hook at `0x801212e4`.

In a system with no patch loaded:
- (1) runs the type dispatch (types 0–3 via ROM init functions) and computes the interval
- (2) validates conn type is set and clamps the budget — **no hardware register writes**

**Conclusion: no actual baseband hardware configuration happens without a patch hook.**

---

## 7 — Implications for Libre Replacement

### Critical (revised 2026-06-08)

The global `0x801212e4` hook is **never populated** in the analyzed firmware (see
Section 12).  Per-connection JIT code lives at `crypto_struct+0x13e`, installed by
ROM `FUN_80025b68` during SSP pairing.  The libre firmware does **not** need to
write either hook.  What matters is:

1. The SSP pairing path must reach ROM `FUN_80025b68` (automatic via ROM LMP state machine).
2. `FUN_80025b68` must find codec tables in ROM (fixed addresses, initialized by `FUN_800225a8`).
3. Per-connection `crypto_struct+0x13e` buffer (≥ 0x40 bytes) must exist — ROM allocates it.

**Resolved (2026-06-08):** the ROM codec templates at `PTR_DAT_80025ca8` /
`PTR_DAT_80025cac` are fully initialized and executed by ROM — the libre firmware
provides zero implementation for them.  See Section 9 for the complete codec
template pipeline.

### Interval computation can call ROM

The interval table and `FUN_80050810` are pure ROM and require no patch reimplementation.
The libre can call `FUN_80050810` directly.  It must populate `conn_rec[0x0b]`
correctly before calling `FUN_80050994`.

### The hook at `0x801212e0` is optional

`FUN_80050810`'s hook override (`0x801212e0`) can be left NULL.  The ROM dispatcher
handles types 0–3.

### Remaining unknowns at the hardware boundary

| Unknown | Impact |
|---------|--------|
| ~~Type-init functions `FUN_800506ac` / `FUN_8004e670` / `FUN_8004e6f4` / `FUN_8004e76c`~~ | **Resolved 2026-06-08 — see Section 10** |
| ~~What invokes `FUN_80025b68` in the connection-setup chain?~~ | **Resolved 2026-06-08 — see Section 9** |
| Exact hardware registers written by the codec templates | Low priority — ROM manages all of this |

---

## 8 — Complete Call Graph to Hardware

```
FUN_80047c50 (VSC dispatcher)
└─ FUN_800509b0 (commit chain)
    ├─ FUN_80050994(hw_resource)           ← root object
    │   ├─ FUN_80050810(hw_resource)
    │   │   ├─ [hook 0x801212e0?]          ← optional patch override
    │   │   ├─ FUN_800506ac / e670 / e6f4 / e76c   (type init, by conn_rec[0x08]&7)
    │   │   ├─ interval = 1 + Σ DAT_8007abd8[i] for set bits in obj[0x0b]
    │   │   └─ obj[0x09] = interval
    │   └─ FUN_8004f824(hw_resource)
    │       ├─ [hook 0x801212e4]           ← ACTUAL HW REGISTER WRITE (patch only)
    │       └─ clamp: obj[0x11] = 0xFE − obj[0x09]  (if hook NULL or returns 0)
    │
    ├─ FUN_80050994(hw_resource[0x24])     ← secondary sub-object (if non-null)
    │   └─ … (same two-step as above)
    └─ FUN_80050994(hw_resource[0x20])     ← tertiary sub-object (if non-null)
        └─ … (same two-step as above)
```

The **only** path to actual baseband registers is through the function installed at
`0x801212e4`.  All other ROM code performs data management, validation, and logging.

---

## 9 — Codec Template Pipeline (ROM-Managed, 2026-06-08)

### Overview

When `FUN_8004f824` calls the hook at `bos_base+0xe4`, that hook points to
dynamically-generated MIPS16e code at `conn_record._x58+0x13e`.  This code is
**not written by the patch** — it is written during **Bluetooth SSP (Secure Simple
Pairing) IO capability exchange** by ROM function `FUN_80025b68`, which copies and
un-scrambles pre-built byte sequences from ROM-initialized RAM staging areas.

### Initialization call chain

```
ROM FUN_800614fc                     ← system BT init (single call site)
  └─ ROM FUN_800225a8                ← populates codec staging tables in RAM
        ├─ FUN_8002c31c              : writes 0xc4000003 to 4 global RAM locations
        │                              (0x80120ed8/dc/e0/e4)
        ├─ FUN_8002c2d8(0x801220bc, 0x8012205c)  : codec-6 h2 + h0 staging areas
        ├─ optimized_memcpy(0x8012208c, 0x80079be0, 0x30)  : codec-6 h1 staging area
        ├─ optimized_memcpy(0x801220d4, 0x80079c10, 0x18)  : codec-6 h2 (second half)
        ├─ FUN_8002c2ac(0x8012216c, 0x801220ec)  : codec-8 h2 + h0 staging areas
        ├─ optimized_memcpy(0x8012212c, 0x80079c28, 0x40)  : codec-8 h1 staging area
        └─ optimized_memcpy(0x8012218c, 0x80079c68, 0x20)  : codec-8 h2 (second half)
```

`FUN_800614fc` is the **only** caller of `FUN_800225a8`.

### ROM source addresses for template bytes

| Codec | Handle | Size | ROM source address | RAM staging address |
|-------|--------|------|--------------------|---------------------|
| 6 | h0 | 0x30 | `0x80079f0c` | `0x8012205c` |
| 6 | h1 | 0x30 | `0x80079be0` | `0x8012208c` |
| 6 | h2 first half | 0x18 | `0x80079ef4` | `0x801220bc` |
| 6 | h2 second half | 0x18 | `0x80079c10` | `0x801220d4` |
| 8 | h0 | 0x40 | `0x8007a07c` | `0x801220ec` |
| 8 | h1 | 0x40 | `0x80079c28` | `0x8012212c` |
| 8 | h2 first half | 0x20 | `0x8007a05c` | `0x8012216c` |
| 8 | h2 second half | 0x20 | `0x80079c68` | `0x8012218c` |

Template staging base addresses: `PTR_DAT_80025ca8 → 0x8012205c` (codec-6),
`PTR_DAT_80025cac → 0x801220ec` (codec-8).

### Per-pairing call chain for FUN_80025b68 (traced 2026-06-08)

```
ROM LMP_encryption_opcode_handlers @ 0x80028264   ← dispatched by ROM LMP state machine
    ├─ ROM FUN_80029364 @ 0x80029364  [state==0x15]
    │      send_evt_HCI_IO_Capability_Response(conn_handle, ...)
    │      └─ FUN_80025b68(conn_handle, role_bit)
    └─ ROM FUN_800293f0 @ 0x800293f0  [state==0x1d]
           send_evt_HCI_IO_Capability_Response(conn_handle, ...)
           └─ FUN_80025b68(conn_handle, role_bit)

ROM HCI_Write_Simple_Pairing_Debug_Mode @ 0x800233e8   ← HCI command handler
    └─ ROM continue_ssp_pairing_after_hci_debug_mode_write @ 0x80023d14  [state==0x1e or fallthrough]
           └─ unscramble_codec_jit_template_and_install_hw_hook(conn_handle, uVar5)
```

`role_bit = *(byte *)(lmp_pdu + 4) & 1` — 0 = slave, 1 = master.  This selects
which staging area variant to un-scramble into the per-connection buffer.

All callers are ROM functions; `LMP_encryption_opcode_handlers` is dispatched by
the ROM's LMP PDU state machine.  The libre firmware does not call `FUN_80025b68`
and does not need to install any trigger for it.

### Un-scrambling at pairing time (ROM FUN_80025b68)

ROM `FUN_80025b68(conn_handle, role_bit)` is called after IO capability exchange
completes.  It selects the appropriate staging area entry based on `role_bit`,
un-scrambles the pre-built template bytes into `conn_record._x58 + 0x13e`, and
sets `bos_base+0xe4` to point to that code.

The un-scrambling algorithm is **two independent half-reversals**:

```c
// For codec-6: size=0x30, half=0x18.  For codec-8: size=0x40, half=0x20.
void unscramble(uint8_t *src, uint8_t *dst, int size, int half) {
    for (int i = 0; i < half; i++)
        dst[i] = src[half - 1 - i];          // first half reversed
    for (int i = half; i < size; i++)
        dst[i] = src[size - 1 - (i - half)]; // second half reversed
}
```

This is NOT a full block reversal; each half is independently reversed.

### Template code properties

The un-scrambled bytes at `conn_record._x58+0x13e` decode as valid MIPS16e.
The first 7 instructions of the codec-6 h0 un-scrambled sequence:

```
+00: 0x736f  cmpi    v1, 0x6f
+02: 0xc519  sb      s0, 0x19(a1)
+04: 0xd353  swsp    v1, 332(sp)
+06: 0x48a9  addiu   s0, -0x57
+08: 0x4c54  addiu   a0, 0x54
+0a: 0x57f4  slti    a3, 0xf4
+0c: 0x13ce  b       <far forward target>
```

Ghidra stops at the `b` instruction at +0x0c because the branch target lies far
beyond the 48-byte (0x30) template buffer.  This means the template code is
**position-dependent** — it expects adjacent connection-record memory to follow
immediately.  The code cannot be disassembled in isolation.

The exact hardware registers written by these templates remain unknown, but this
does not affect the libre firmware (see below).

### Verification

ROM source `0x80079f0c` (codec-6 h0) has **identical bytes** to the GZF DATA block
at `0x8012205c`.  This confirms the GZF runtime snapshot was captured with the
vanilla ROM and that both the algorithm and source addresses are correct.

### Libre firmware implication

**The libre firmware needs zero implementation for the codec template system:**

- ROM `FUN_800225a8` (called via `FUN_800614fc`) initializes all staging tables from ROM-internal data.
- ROM `FUN_80025b68` un-scrambles templates at connection setup time.
- No template bytes, no scrambling logic, and no `bos_base+0xe4` hook-write code lives in the patch.

The libre firmware must only ensure that the ROM init chain (`FUN_800614fc`) is
not bypassed.  The connection record must have sufficient buffer space at `+0x13e`
(minimum 0x40 bytes for codec-8), which is allocated by ROM's connection-record
allocator — not by the patch.

---

## 10 — Connection-Type Dispatch: `FUN_80050810` and Type Handlers (2026-06-08)

All five functions in this section are **pure ROM** (addresses `0x8004e670`–`0x80050810`).
The libre firmware needs zero implementation for them.

### Connection-setup parameter struct layout

`FUN_80050810` and its four sub-handlers operate on a "connection parameter object"
(`hw_obj_t *`) passed as `param_1`.  Fields used across the five functions:

| Offset | Type | Description |
|--------|------|-------------|
| `+0x08` | `byte` | conn_type: bits[2:0] = connection type (0–3); bits[4:3] = mode subtype |
| `+0x09` | `byte` | computed slot interval (written by `FUN_80050810` after dispatch) |
| `+0x0b` | `byte` | capability bitmask: bits 0–6 map to SCO/eSCO packet-type flags |
| `+0x11` | `byte` | PDU slot count (clamped by `FUN_8004f824`) |
| `+0x1c` | `int*` | pointer to parent / existing connection record |
| `+0x1d` | `byte` | packed codec field (bits[7:2] copied to parent record) |
| `+0x20` | `ushort` | remote capability flags (0 = no remote caps; non-0 = peer-declared support) |
| `+0x2b` | `byte` | connection sub-slot counter |
| `+0x50` | `int*` | pointer to existing SCO/eSCO link slot (NULL → allocate new) |

### `FUN_80050810` — connection-type dispatcher (218 bytes)

```c
undefined1 FUN_80050810(hw_obj_t *obj)
{
    // Optional patch override at 0x801212e0 (hook RAM var from Section 3)
    code *override = *(code **)0x801212e0;
    if (override != NULL) {
        uint8_t tmp[8];
        if (override(obj, tmp) != 0)
            return tmp[0];   // hook handled it; use its result
    }

    // Dispatch on bits[2:0] of obj[0x08]
    uint8_t conn_type = obj[0x08] & 7;
    switch (conn_type) {
        case 0: if (FUN_800506ac(obj) != 0) return 5; break;  // new init
        case 1: FUN_8004e670(obj); break;                      // accept/copy
        case 2: FUN_8004e6f4(obj); break;                      // renegotiate
        case 3: FUN_8004e76c(obj); break;                      // restore
        default: log_error(..., conn_type); /* type ≥ 4 unsupported */
    }

    // Post-dispatch: accumulate slot interval from capability bitmask
    uint8_t interval = 1;
    for (int bit = 0; bit < 7; bit++)
        if ((obj[0x0b] >> bit) & 1)
            interval += DAT_8007abd8[bit];  // interval table, Section 4
    obj[0x09] = interval;

    // Log: conn_type, mode bits[4:3], capability bitmask, interval
    log(4, 0xd2, ..., obj[0x08]&7, (obj[0x08]>>3)&3, obj[0x0b], interval);
    return 0;
}
```

**10 ROM callers** including `FUN_80050994`, `FUN_80047304`, `FUN_80047628`,
`FUN_800509b0`, `FUN_80050610`, `FUN_800508f8`, `FUN_800509ec`, `FUN_80050a90`.

### Type 0 — new SCO/eSCO initiation (`FUN_800506ac`, 354 bytes)

Validates and initialises a brand-new outgoing/incoming SCO or eSCO link.

```
param_1+0x20 capability bitmask (remote-declared):
  bit 0 — EV3/HV SCO type A
  bit 1 — EV3/HV SCO type B
  bit 2 — EDR / enhanced-rate capability
  bit 4 — voice-channel indicator
  bit 5 — "no SCO basic" override
```

Behaviour:
1. Returns 5 if neither voice nor SCO is supported (bits 0x10 clear and bits 0x03 == 3).
2. Clears `+0xb` and bits[4:3] of `+0x08`.
3. Sets `+0xb` bit 1 if EDR capable (cap bit 2).
4. Sets `+0xb` bits[3:4] (SCO enabled + extended) based on cap bits 0/1.
5. If `+0x50 == NULL`: allocates new link slot via `FUN_80050610`;
   if `+0x50` non-NULL: reuses existing slot (copies codec field from `+0x1d`).
6. If SCO capable (bVar1): allocates secondary slot via `FUN_800508f8`.
7. If cap bit 5 set: clears `+0xb` bit 0 (disables basic SCO).
8. Returns 0 on success, 5 on allocation failure.

### Type 1 — accept / mirror from parent (`FUN_8004e670`, 130 bytes)

Used when the local side is responding to a remote SCO/eSCO request.
Mirrors parent connection record at `*(param_1+0x1c)`:

- Always sets `+0xb` bit 0 (basic SCO) and bit 3 (eSCO enabled).
- If parent cap bit 2: also sets `+0xb` bit 1 (EDR).
- If `+0x20 == 0`: bit 4 cleared; else bit 4 set (extended eSCO).
- If parent `+0x20` bit 6 set: sets `+0xb` bit 6.
- If parent `+0x20` bit 5 set: clears `+0xb` bit 0 (no basic SCO).
- Copies bits[4:3] of parent `+0x08` into own `+0x08` bits[4:3].

### Type 2 — renegotiation (`FUN_8004e6f4`, 118 bytes)

Used when attempting to change parameters on an existing eSCO connection.

Special case: if parent `+0x08` bits[4:3] == `0b01` (SCO-only mode):
  → Forces `+0xb = 3` (bits 0+1 only), clears bits[4:3] of `+0x08`.

Normal case (eSCO renegotiation):
- Copies base caps from parent, clearing bits 1 and 3, keeping bit 0.
- `+0x20 == 0`: bit 4 cleared; else: bit 4 set.
- Always clears bit 5 (no retransmission for renegotiation).
- Propagates parent `+0x20` bits 6 and 5 as above.
- Always clears bits[4:3] of `+0x08`.

### Type 3 — restore / reject-to-base (`FUN_8004e76c`, 72 bytes)

Simplest handler; resets to minimum eSCO-only capability:

- Forces `+0xb` bit 3 (eSCO only); copies other bits from parent with bits[2:0] cleared.
- If `+0x20 == 0`: keeps bit 3 only; else: also sets bit 4 (extended).
- Propagates parent `+0x20` bit 6 to `+0xb` bit 6.
- Clears bits[4:3] of `+0x08`.

### Capability bitmask (`+0x0b`) summary

| Bit | Interval contribution | Estimated BT packet type |
|-----|-----------------------|--------------------------|
| 0 | +6 → T=7 | HV3 / EV3 |
| 1 | +6 → T=7 | EV3 alternate |
| 2 | +1 → T=2 | HV1 |
| 3 | +2 → T=3 | HV2 |
| 4 | +3 → T=4 | 2-EV3 / EV4 |
| 5 | +18 → T=19 | 3-EV5 / 2-EV5 (EDR high-BW) |
| 6 | +1 → T=2 | reserved / secondary HV1 |

### Libre firmware implication

All five functions are ROM code.  The libre firmware requires zero implementation:

- The ROM initializes `bos_base+0xe0` to point to `FUN_80050810` (ROM).
- The override slot at `0x801212e0` defaults to NULL; the libre firmware can leave it NULL.
- Types 0–3 are handled automatically by ROM dispatch whenever the BT stack performs SCO/eSCO setup or renegotiation.
- The only field the libre firmware must correctly set up before any SCO/eSCO connection is `conn_rec[0x0b]` (capability bitmask) so that `FUN_80050810`'s interval computation produces a valid `conn_rec[0x09]` for `FUN_8004f824`'s budget check.
  That field is populated by the type handlers themselves from the remote capability flags, so no manual initialisation is needed.

---

## 11 — eSCO Packet-Type Selector: `FUN_80044730` (2026-06-08)

**Address:** `0x80044730` (ROM) | **Size:** 102 bytes
**Callers:** `FUN_80047ca6`, `FUN_80047edc` (both ROM)

### Decompiled C

```c
char FUN_80044730(int param_1, int param_2)
{
    uint  uVar3 = (uint)(ushort)(*(short *)(param_1 + 4) - 0x10);
    char  cVar4, cVar2;

    if (uVar3 < 0xe) {          // valid index 0..13 → input codes 0x10..0x1d
        cVar4 = DAT_8007abb0[uVar3];   // Table A: 0x00=OK  0x12=unsupported
        cVar2 = DAT_8007abc0[uVar3];   // Table B: codec-type selector (3 bits)
    } else {
        cVar4 = 0x12;           // out-of-range → error
        cVar2 = -1;
    }
    if (param_2 != 0) {
        if (cVar4 == 0) {
            // pack codec type into bits[7:5] of param_2[0x1d]
            *(byte *)(param_2 + 0x1d) =
                (*(byte *)(param_2 + 0x1d) & 0x1f) | (cVar2 << 5);
        } else {
            possible_logging_function__var_args(2, 0xcb, ...);  // log error
        }
    }
    return cVar4;   // 0 = success, 0x12 = unsupported
}
```

### Parameters

| Parameter | Type | Meaning |
|-----------|------|---------|
| `param_1` | `int *` | Pointer to packet-type descriptor; `+4` holds 16-bit packet type code |
| `param_2` | `int *` | Output object; `+0x1d` bits[7:5] receive the 3-bit codec-type |

### Lookup Tables at `0x8007abb0` / `0x8007abc0`

Both tables have 14 entries (indices 0–13), indexed by `(input_code − 0x10)`.

**Table A — validity** (`DAT_8007abb0`, `0x8007abb0`):

| Index | Input code | Table A byte | Result |
|-------|-----------|-------------|--------|
| 0 | 0x10 | 0x00 | OK |
| 1 | 0x11 | 0x12 | unsupported |
| 2 | 0x12 | 0x00 | OK |
| 3 | 0x13 | 0x00 | OK |
| 4 | 0x14 | 0x12 | unsupported |
| 5 | 0x15 | 0x00 | OK |
| 6–12 | 0x16–0x1c | 0x12 | unsupported |
| 13 | 0x1d | 0x00 | OK |

**Table B — codec type** (`DAT_8007abc0`, `0x8007abc0`):

| Index | Input code | Codec type (bits[7:5]) |
|-------|-----------|----------------------|
| 0 | 0x10 | 3 |
| 2 | 0x12 | 2 |
| 3 | 0x13 | 0 |
| 5 | 0x15 | 4 |
| 13 | 0x1d | 1 |

The 3-bit codec-type values (0–4) align with BT air-coding formats:

| Value | Likely format |
|-------|--------------|
| 0 | µ-law / linear PCM |
| 1 | A-law |
| 2 | CVSD |
| 3 | Transparent data |
| 4 | Vendor-specific / wideband |

### ROM data layout at `0x8007ab__`

```
0x8007abb0: 00 12 00 00 12 00 12 12 12 12 12 12 12 00  ← Table A (14 bytes)
            00 00                                       ← 2 padding bytes
0x8007abc0: 03 ff 02 00 ff 04 ff ff ff ff ff ff ff 01  ← Table B (14 bytes)
            00 00                                       ← 2 padding bytes
0x8007abd0: ff ff ff ff                                 ← padding / guard
0x8007abd8: 06 06 01 02 03 12 01 00                    ← DAT_8007abd8 (slot interval
                                                           contributions for FUN_80050810)
```

This confirms that the slot-interval table used by `FUN_80050810` (`DAT_8007abd8`) is
immediately adjacent in ROM memory to the packet-type lookup tables.

### Libre firmware implication

`FUN_80044730` and both tables are 100% ROM.  The callers at `0x80047ca6` and
`0x80047edc` are also ROM functions dispatched by the BT stack's SCO/eSCO setup path.
The libre firmware requires **zero implementation** for this subsystem.

---

## 12 — Hardware-Write Hook: Final Resolution (2026-06-09)

The Phase 3 BLOCKED item ("decompile the hardware-write hook at `0x801212e4`") is
**resolved**.  There is no single patch function to decompile.  Two distinct `+0xe4`
hook mechanisms exist; only the per-connection one carries real generated code.

### 12.1 — Global hook: `0x801212e4` (`bos_base+0xe4`)

**Reader:** ROM `FUN_8004f824` @ `0x8004f824` (106 B), via literal pool
`PTR_DAT_8004f890 → 0x801212e4`.

```c
undefined1 FUN_8004f824(hw_obj_t *obj)
{
    uint8_t result[8];
    code *hook = *(code **)0x801212e4;

    if (hook != NULL) {
        if (hook(obj, result) != 0)
            return result[0];          // hook handled — use its return byte
        // hook returned 0 → fall through to ROM clamp below
    }
    if ((obj[0x08] & 7) == 0)
        return 5;                        // conn type unset
    if (obj[0x09] + obj[0x11] + 1 > 0xFF)
        obj[0x11] = 0xFE - obj[0x09];   // clamp slot budget
    return 0;
}
```

**Prototype (if ever non-NULL):**

```c
/* Returns non-zero if handled; writes status byte to result_buf[0]. */
uint8_t hw_write_hook(hw_obj_t *obj, uint8_t result_buf[8]);
```

**Writer:** none.  Full-GZF `ScanStoreOffsets.java` scan (213,799 insns) found
**zero** `sw` targets to `0x801212e4`.  GZF DATA-block snapshot: `*0x801212e4 = 0`
(NULL).  The non-free patch does not install here either.

**Libre implication:** leave `0x801212e4` NULL.  `FUN_8004f824` then performs
validation/clamping only.  **No stub required.**

### 12.2 — Per-connection hook: `crypto_struct+0xe4` → `+0x13e`

**Installer:** ROM `FUN_80025b68` @ `0x80025b68` (300 B), triggered during **SSP IO
capability exchange** (see Section 9).

```c
void FUN_80025b68(uint conn_idx, /* role_bit in a1 selects template variant */)
{
    crypto_struct *cs = conn_array[conn_idx & 0xffff]._x58;

    cs[0xe0] = FUN_800240a4(conn_idx);   // codec-type byte
    cs[0xe1] = 1;
    cs[0xe2] = (cs[0x214] ? 2 : 1);
    *(void **)(cs + 0xe4) = cs + 0x13e;   // hook → in-struct code buffer
    cs[0xe3] = (cs[0x214] ? 0x40 : 0x30); // template size

    FUN_80024218(conn_idx, role_template_ptr);  // may call LMP__268 gateway

    if (cs[0x50] == 1) {
        // Un-scramble codec template into cs+0x13e (half-reversal, see Section 9)
        if (cs[0x1f1] == 6)  copy_unscramble(PTR_DAT_80025ca8 + role*0x30, cs+0x13e, 0x30);
        if (cs[0x1f1] == 8)  copy_unscramble(PTR_DAT_80025cac + role*0x40, cs+0x13e, 0x40);
        FUN_800688b0(conn_idx, cs+0xe0, ...);   // queue LMP-side dispatch
    } else {
        FUN_80068428(conn_idx, cs+0xe0, ...);   // alternate path
    }
    set_arg1_1_to_arg2(cs, tag);
}
```

This is **not** the same address as global `0x801212e4`.  `FUN_80025b68` writes
`sw v0, 0xe4(s0)` where `s0` is the per-connection `crypto_struct` pointer loaded
from `conn_rec+0x58` — not `bos_base`.

### 12.3 — Template code (the actual "hook body")

| Property | codec-6 | codec-8 |
|----------|---------|---------|
| Template size | `0x30` (48 B) | `0x40` (64 B) |
| Half size | `0x18` | `0x20` |
| Staging base | `PTR_DAT_80025ca8 → 0x8012205c` | `PTR_DAT_80025cac → 0x801220ec` |
| ROM source (h0) | `0x80079f0c` | `0x8007a07c` |
| Role selector | `config[0x47]` byte | same |

**Codec-6 h0 — first MIPS16e instructions after unscramble** (verified against ROM
`0x80079f0c` staging bytes, identical to GZF DATA block):

```
+00: 0x736f  cmpi    v1, 0x6f
+02: 0xc519  sb      s0, 0x19(a1)
+04: 0xd353  swsp    v1, 332(sp)
+06: 0x48a9  addiu   s0, -0x57
+08: 0x4c54  addiu   a0, 0x54
+0a: 0x57f4  slti    a3, 0xf4
+0c: 0x13ce  b       <far target beyond 0x30 buffer>
```

The branch at `+0x0c` targets memory **outside** the 48-byte buffer.  The templates
are **position-dependent**: they rely on conn-record memory immediately following
`+0x13e`.  Ghidra cannot decompile them as standalone functions.

The exact baseband registers programmed remain unknown but are **100% ROM-managed**
via the template/staging pipeline (Section 9).

### 12.4 — Correction to Section 7

Section 7 stated that `FUN_80025b68` installs the hook at `bos_base+0xe4`.  That was
imprecise: it installs `crypto_struct+0xe4 → crypto_struct+0x13e` per connection.
The global `0x801212e4` slot used by `FUN_8004f824` is a separate, currently-unused
indirection point that neither patch nor ROM populates in the analyzed firmware.

### 12.5 — Libre firmware verdict

| Component | Libre action |
|-----------|--------------|
| Global `0x801212e4` | Leave NULL (default) |
| `FUN_80025b68` codec templates | Zero — ROM only |
| Per-connection `+0x13e` buffer | Zero — ROM allocates in `crypto_struct` |
| `FUN_8004f824` / `FUN_80050810` | Call ROM directly |
| Phase 6 "implement hw-write hook" | **Removed — not needed** |
