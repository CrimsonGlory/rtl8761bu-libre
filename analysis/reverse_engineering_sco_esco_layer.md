# RTL8761BU — SCO/eSCO Layer & Slot Scheduler (2026-06-08)

Covers 15 functions from the DATA block decompiled in the `[NEXT]` pass:
- **Group A** (0xa000 region helpers): 10 functions near the patch entry point
- **Group B** (Appendix D hook targets): 5 functions installed as first-batch hooks

All decompiled via `DecompileSmallFunctions.java`, `DecompileSmallFunctions2.java`,
`DecompileSmallFunctions3.java`, `DecompileHookTargets.java`, and `DecompileC63c.java`
against GZF `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`.

---

## Summary Table

| Function | Size | Classification | Description |
|----------|------|----------------|-------------|
| `FUN_8010a410` | 86B | **REAL** | HW register 0x11C — eSCO codec mode bits |
| `FUN_8010a49c` | 10B | trivial | Clear byte at 0x8012b803 |
| `FUN_8010a4ac` | 68B | **REAL** | eSCO readiness gate (6-condition check) |
| `FUN_8010a550` | 54B | **REAL** | Set "eSCO active" flag |
| `FUN_8010a594` | 14B | trivial | MMIO write 0x80 → 0xb6001080 |
| `FUN_8010a5ac` | 36B | **REAL** | Timing compensation adder |
| `FUN_8010a5d8` | 94B | **REAL** | Codec register 0x1FE configurator |
| `FUN_8010a6ec` | 180B | **REAL** | eSCO slot buffer allocator |
| `FUN_8010a84c` | 450B | **REAL** | SCO/eSCO connection response handler |
| `FUN_8010aa58` | 96B | **REAL** | SCO connection continuation handler |
| `FUN_8010b118` | 82B | **REAL** | Slot interval allocator (ROM API wrapper) |
| `FUN_8010b3d8` | 206B | **REAL** | ACL slot scheduler (interrupt-protected) |
| `FUN_8010b0a4` | 108B | **REAL** | ACL packet type flag corrector |
| `FUN_8010c780` | 34B | **REAL** | Subsystem initializer (3 fn calls + flag) |
| `FUN_8010c63c` | 278B | **REAL** | Retransmission counter / ACL packet handler |

Only 2 of 15 functions are trivial stubs. The remaining 13 require real implementation.

---

## Group A — 0xa000 Region Helpers

### FUN_8010a410 (86B) — HW Register 0x11C: eSCO Codec Mode

**Decompile:**
```c
void FUN_8010a410(void) {
    if (PTR_struct[0x16e] != '\0') {              // guard: eSCO mode active
        uint16 reg = *DAT_b600011c;               // read MMIO 0xb600011c (reg 0x11C)
        if (PTR_DAT[0x30] == '\0') {              // if flag at +0x30 clear
            reg = (reg | 0x40) | (high << 2);     // set bit 6 + bit 1 in high byte
        } else {
            reg = (reg & ~0x40) & ~(high << 2);   // clear those bits
        }
        (*hw_reg_write_fn)(0x11c, reg);           // write register 0x11C
    }
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010a468` | `0x801259ec` | struct ptr (bos_base + something) |
| `0x8010a46c` | `0xb600011c` | MMIO address of reg 0x11C |
| `0x8010a470` | `0x8012b764` | another struct ptr (flag at +0x30) |
| `0x8010a474` | `0x8012048c` | fn-ptr slot → HW register write fn |

**Notes:** Configures bits 6 and 1 of codec control register 0x11C. The `0xb600011c`
MMIO address is a second MMIO bank at physical `0x16000000` (distinct from the main
baseband MMIO at `0xb000a0bc`). Flag at struct+0x16e is the eSCO/codec activation
gating flag also seen in `FUN_8010a4ac`.

**Libre:** Must implement — programs real hardware register.

---

### FUN_8010a49c (10B) — Clear Flag Byte

```c
undefined4 FUN_8010a49c(void) {
    *(0x8012b803) = 0;
    return 0;
}
```

**Libre:** Trivial — one `sb zero, 0(ptr)` + `jr ra`.

---

### FUN_8010a4ac (68B) — eSCO Readiness Gate

```c
undefined4 FUN_8010a4ac(byte *param_1, undefined4 param_2, undefined1 *param_3) {
    // All 6 conditions must pass to return 1:
    if (conn_table[*param_1 * 0x2b8 + 0xb2] == 0x0f &&   // conn state = active
        conn_table[*param_1 * 0x2b8 + 0xc1] == 0 &&       // eSCO not busy
        struct[0x171] == 0 &&                               // no pending setup
        (byte)struct[0x16e] >= 2 &&                        // mode >= 2
        DAT_8012b764[0x30] != 0 &&                         // config flag set
        *(0x8012b803) == 0) {                               // reset flag clear
        *param_3 = 0;
        return 1;
    }
    return 0;
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010a4f0` | `0x8012dc50` | Connection record table base (stride 0x2b8) |
| `0x8010a4f4` | `0x801259ec` | bos_base / main BT struct |
| `0x8010a4f8` | `0x8012b764` | config struct ptr |
| `0x8010a4fc` | `0x8012b803` | reset/busy flag byte |

**Notes:** The stride 0x2b8 = 696 decimal is the connection record size used in
`FUN_8010a84c` as well. The state value `0x0f` at +0xb2 likely means "eSCO active".

**Libre:** Must implement — reads connection record fields and struct flags.

---

### FUN_8010a550 (54B) — Set eSCO Active Flag

```c
undefined4 FUN_8010a550(uint *param_1) {
    if (*(0x80120ccd) == 0 &&                   // not already active
        (*(0x80122fc4) & 0x80) == 0 &&          // MMIO flag clear
        (*(0x8012194e) & 8) != 0 &&             // capability bit 3 set
        (*param_1 >> 0x10 & 2) != 0) {          // param bit 17 set
        *(0x80120ccd) = 1;                       // set eSCO-active flag
    }
    return 0;
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010a588` | `0x80120ccd` | eSCO active flag byte |
| `0x8010a58c` | `0x80122fc4` | MMIO/status byte (bit 7) |
| `0x8010a590` | `0x8012194c` | capability word (bit 3 at +2) |

**Libre:** Must implement — sets runtime state flag.

---

### FUN_8010a594 (14B) — MMIO Reset Trigger

```c
undefined4 FUN_8010a594(void) {
    *(uint16*)0xb6001080 = 0x80;    // write to MMIO b6001080
    return 0;
}
```

**Notes:** `0xb6001080` = physical `0x16001080`. Writes constant 0x80, likely a
codec/audio reset signal. The same MMIO bank as `0xb600011c` seen in FUN_8010a410.

**Libre:** Trivial — one `sh 0x80, 0(ptr)` + `jr ra`.

---

### FUN_8010a5ac (36B) — Timing Counter Increment

```c
void FUN_8010a5ac(short *param_1, uint param_2) {
    // Access stride-0x1ac struct array at 0x8012382c, field +0x21a
    if (struct_array[param_2 & 0xff * 0x1ac + 0x21a] != 0) {
        *param_1 += (byte)*(0x8012081b);    // add adjustment value
    }
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010a5d0` | `0x8012382c` | struct array base (stride 0x1ac) |
| `0x8010a5d4` | `0x8012081b` | timing adjustment byte |

**Notes:** The `0x8012382c` struct array with stride 0x1ac appears in multiple functions
(also `FUN_8010b3d8`, `FUN_8010c63c`). Field `+0x21a` is a 16-bit value; if nonzero,
adds a byte offset to a counter. This adjusts timing for SCO retransmission windows.

**Libre:** Must implement — reads runtime connection structs.

---

### FUN_8010a5d8 (94B) — Codec Register 0x1FE Configurator

```c
undefined8 FUN_8010a5d8(short param_1, char param_2) {
    *(uint32*)0xb000a030 |= 0x40;              // set bit 6 in MMIO
    if (param_2 == 0) {
        (*hw_reg_write)(0x1fe, param_1);       // write reg 0x1FE = param_1
    } else {
        (*hw_reg_write)(0x1fe, 0x24);          // write reg 0x1FE = 0x24
        *(0xb60010ce) = (param_1 << 8) | (*(0xb60010ce) & 0xff);  // update high byte
    }
    return (uint64)(*(uint32*)0xb000a028 << 32) | *(uint32*)0xb000a028;
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010a638` | `0xb000a030` | MMIO register (same bank as baseband at 0xb000a0bc) |
| `0x8010a63c` | `0x80000000` | mask (sign bit) |
| `0x8010a640` | `0x8012048c` | fn-ptr → HW reg write |
| `0x8010a644` | `0xb60010ce` | MMIO in 0xb600xxxx bank |
| `0x8010a648` | `0xb000a028` | MMIO return value |

**Notes:** Programs baseband register 0x1FE (likely codec frequency / sample rate).
Mode 0: direct value; Mode 1: fixed 0x24 + update separate MMIO field. The return
value is the same MMIO address duplicated — this is a Ghidra decompile artifact from
the `CONCAT44` operation; the real intent is just to return the MMIO value.

**Libre:** Must implement — programs hardware registers.

---

### FUN_8010a6ec (180B) — eSCO Slot Buffer Allocator

```c
void FUN_8010a6ec(int param_1, uint *param_2, uint param_3) {
    uint cap_mask = *(byte*)(param_1 + 2) & 0x3f;
    if (cap_mask == 0) {
        memset(param_2, 0, 7 * sizeof(uint));   // clear all 7 slots
        return;
    }
    byte *data = (byte*)(param_1 + 4);
    byte bitmask = *(byte*)(param_1 + 3);       // which sub-slots enabled
    for (int i = 0; i < 7; i++) {
        if ((bitmask >> i) & 1 == 0) {
            param_2[i] = 0;
        } else {
            // Slot offsets for each slot type:
            static const uint offsets[] = {0, 6, 0xc, 0xe, 0x10, 0x13, 0x25};
            uint slot_ptr = param_3 + offsets[i];
            if (slot_ptr != 0) {
                byte size = ROM_slot_table_8007abd8[i];   // size from ROM table
                memcpy(slot_ptr, data, size);              // copy capability data
                param_2[i] = slot_ptr;
                data += size;
            }
        }
    }
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010a7b0` | `0x8007abd8` | ROM slot interval table (7 bytes: sizes per slot type) |
| `0x8010a7b4` | `0x8000e85d` | ROM `optimized_memcpy` (MIPS16e) |

**Notes:** Uses the same ROM table `0x8007abd8` (slot interval table) that was already
documented in `reverse_engineering_hardware_layer.md` Section 11. The slot type offsets
`[0, +6, +0xC, +0xE, +0x10, +0x13, +0x25]` correspond to the eSCO packet type offsets
(HV1, HV2, HV3, EV3, EV4, EV5, eSCO-extended).

**Libre:** Must implement — allocates eSCO capability sub-buffers using ROM table.

---

### FUN_8010a84c (450B) — SCO/eSCO Connection Response Handler

This is the primary incoming SCO/eSCO connection handler.

**Decompile (condensed):**
```c
void FUN_8010a84c(void) {
    if (*pending_flag_8012b943 != 1) goto clear_and_return;

    int rc = (*FUN_80060dd9)(local_buf, &conn_id);   // read pending request
    if (rc == 0) {
        // Success path — accept connection
        memcpy(local_cap6, &DAT_8012b843, 6);         // copy capability set
        debug_log(4, 0xfa, 0x4fde, 0xcea, 9, ...);   // via FUN_80074fa9
        // Populate connection record fields:
        memcpy(conn_rec + conn_id*0x2b8 + 0xd1, local_cap6, 6);
        conn_rec[conn_id * 0x2b8 + 0x254] = timing_val;
        conn_rec[conn_id * 0x2b8 + 0xde] = pkt_type;
        conn_rec[conn_id * 0x2b8 + 0xe0] = codec_type;
        conn_rec[conn_id * 0x2b8 + 0xe1] = codec2;
        conn_rec[conn_id * 0x2b8 + 0xd7] = cap_byte;
        conn_rec[conn_id * 0x2b8 + 0x256] = 1;       // accept flag
        (*FUN_80036421)(conn_id);                     // allocate connection handle
        conn_rec[conn_id * 0x2b8 + 0x100] = 0;
        conn_rec[conn_id * 0x2b8 + 0x78] = bandwidth_val;
        int r2 = (*FUN_80036df9)(pending_data_ptr);   // state machine advance
        if (r2 != 0) {
            (*FUN_80060d0d)(cap_byte, conn_rec[...+0xcc]);  // on partial success
            (*FUN_80060cfd)(local_cap6_ptr);
            goto error_path;
        }
        (*FUN_80071b85)(conn_id, 1);
        bos_struct[4] = 1;                            // connection active
        *DAT_80120dd4 = 1;
        *PTR_8012b83c = 2;
    } else {
error_path:
        // Reject — send LMP disconnect PDU
        // reject_pdu[0]=2, [1..6]=bd_addr, [7]=1, [8]=0
        (*FUN_8001d071)(3, reject_buf, 0xb);          // send reject PDU
        *PTR_8012b83c = 0;
    }
    (*memset_8000e98d)(pending_data_8012b840, 0, 0x102);  // clear buffer
clear_and_return:
    *pending_flag_8012b943 = 0;
}
```

**Key literal pool values:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010aa10` | `0x8012b943` | Pending request flag (byte) |
| `0x8010aa14` | `0x80060dd9` | ROM: read pending SCO request data |
| `0x8010aa18` | `0x8000e85d` | ROM `optimized_memcpy` |
| `0x8010aa1c` | `0x8012dc50` | Connection record table (stride 0x2b8) |
| `0x8010aa20` | `0x8012b843` | Capability set source |
| `0x8010aa24` | `0x80111068` | eSCO cap table (patch DATA) |
| `0x8010aa28` | `0x80074fa9` | ROM debug/trace logger |
| `0x8010aa2c` | `0x8012b840` | Pending request buffer |
| `0x8010aa30` | `0x80036421` | ROM: allocate connection handle |
| `0x8010aa34` | `0x80036df9` | ROM: SCO state machine (`FUN_called_by_fHCI_Remote_Name_Request_5_1`) |
| `0x8010aa38` | `0x80060d0d` | ROM: partial-success callback |
| `0x8010aa3c` | `0x80060cfd` | ROM: cleanup callback |
| `0x8010aa40` | `0x80071b85` | ROM: connection-accepted notifier |
| `0x8010aa44` | `0x801259ec` | bos_base struct |
| `0x8010aa48` | `0x80120dd4` | Connection active flag |
| `0x8010aa4c` | `0x8012b83c` | State byte |
| `0x8010aa50` | `0x8001d071` | ROM: send LMP PDU |
| `0x8010aa54` | `0x8000e98d` | ROM MIPS16e `memset` thunk |

**Libre:** Critical — must implement fully. Handles SCO/eSCO connection negotiation.
Calls 10+ ROM functions at well-known addresses. All ROM calls can be made by address.

---

### FUN_8010aa58 (96B) — SCO Connection Continuation

```c
void FUN_8010aa58(void) {
    if (*pending_flag_8012b942 != 1) goto clear_and_return;

    int rc = (*FUN_80036df9)(pending_buf_8012b840);    // SCO state machine
    *(0x80126372) = pending_buf[7];                    // copy BD_ADDR byte
    if (rc == 0) {
        bos_struct[4] = 3;                             // set state 3
        *DAT_80120dd4 = 1;
        *PTR_8012b83c = 1;                             // state = accepted
    } else {
        (*FUN_8001da0d)(0xc);                          // send disconnect (error 0x0c)
        *PTR_8012b83c = 0;
    }
    (*memset_8000e98d)(pending_buf_8012b840, 0, 0x102); // clear buffer

clear_and_return:
    *pending_flag_8012b942 = 0;
}
```

**Key literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010aab8` | `0x8012b942` | Pending flag (byte; different from 0x8012b943) |
| `0x8010aabc` | `0x8012b840` | Pending request buffer |
| `0x8010aac0` | `0x80036df9` | ROM SCO state machine |
| `0x8010aac4` | `0x80126372` | BD_ADDR storage byte |
| `0x8010aac8` | `0x8012b83c` | State byte |
| `0x8010aacc` | `0x801259ec` | bos_base struct |
| `0x8010aad0` | `0x80120dd4` | Connection active flag |
| `0x8010aad4` | `0x8001da0d` | ROM: send disconnect |
| `0x8010aad8` | `0x8000e98d` | ROM memset thunk |

**Notes:** Nearly identical structure to `FUN_8010a84c` — uses the same ROM state
machine `0x80036df9`, same buffer `0x8012b840`, but checks flag `0x8012b942` (vs
`0x8012b943` in a84c) and sets state=1 instead of 2 on success. These two functions
handle the two phases of an SCO/eSCO connection negotiation.

**Libre:** Must implement — complement to FUN_8010a84c.

---

## Group B — Appendix D Hook Targets

### FUN_8010b118 (82B) — Slot Interval Allocator

```c
bool FUN_8010b118(char *param_1, char param_2) {
    uint result;
    if (param_2 == 0) {
        // Long-form slot allocation
        result = (*ROM_FUN_80056661)((*param_1 - 8) * 0x1e + 1);
    } else {
        // Short-form: allocate two slots
        char idx = *param_1 * 0x14;
        result = (*ROM_FUN_80056609)(idx + 1);
        (*ROM_FUN_80056609)(idx);
    }
    return (result >> 16) == 0;    // success if upper 16 bits = 0
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010b16c` | `0x80056609` | ROM: BT slot allocator (short-form) |
| `0x8010b170` | `0x80056661` | ROM: BT slot allocator (long-form) |

**Notes:** Two ROM slot allocation functions with different stride formulas.
Short-form: `idx * 0x14` (stride 20) — likely for 2-slot ACL packets.
Long-form: `(idx-8) * 0x1e + 1` (stride 30) — for 3-slot ACL packets (3 × 10 = 30).

**Libre:** Must implement — wraps ROM scheduling APIs.

---

### FUN_8010b3d8 (206B) — ACL Slot Scheduler (Interrupt-Protected)

This is the most complex of the slot-scheduling functions.

```c
uint FUN_8010b3d8(uint param_1) {
    param_1 &= 0xff;
    uint saved_sr = (*disable_interrupts_80009104)();    // save+disable IRQ

    uint result;
    if (param_1 < 8) {
        // Simple case: fixed-stride allocation
        result = (*ROM_80056609)((param_1 * 5 + 1) * 4) >> 16;
    } else {
        // Complex case
        uint idx = param_1 - 8;
        undefined *conn = struct_array_80123a20 + param_1 * 0x1ac;
        uint frame_slots = idx * 0x1e;

        // Call ROM allocator twice (for two slot positions)
        uint hi = (*ROM_80056661)(frame_slots + 0x16) & 0x0003ffff;
        uint lo = (*ROM_80056661)(frame_slots + 4) >> 16;

        // Get connection handle
        uint handle = (*DAT_ROM_801208a0)(idx);

        // Check for residual: read field +0x216 from stride-0x1ac struct
        undefined *conn2 = struct_array_8012382c;
        if (conn != NULL &&
            param_1 != conn2[((conn[3] >> 3) & 3) + 0x1da] &&
            ((*ROM_80056291)(hi, handle) & 0x00020000) == 0) {
            uint timing = *(uint16*)(conn2 + param_1 * 0x1ac + 0x216);
            uint residual = (timing == 0) ? 0 : (hi >> 1) / timing;
            lo -= residual;
        }
        result = lo & 0xffff;
    }

    (*enable_interrupts_80009120)(saved_sr);            // restore IRQ
    return result;
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010b4a8` | `0x80009104` | ROM: `disable_interrupts` (MIPS CP0 Status clear) |
| `0x8010b4ac` | `0x80056609` | ROM: slot allocator A |
| `0x8010b4b0` | `0x80123a20` | RAM: stride-0x1ac connection struct (second array) |
| `0x8010b4b4` | `0x80056661` | ROM: slot allocator B |
| `0x8010b4b8` | `0x0003ffff` | Mask (18 bits) |
| `0x8010b4bc` | `0x801208a0` | RAM fn-ptr → connection handle getter |
| `0x8010b4c0` | `0x8012382c` | RAM: stride-0x1ac connection struct (first array) |
| `0x8010b4c4` | `0x80056291` | ROM: slot availability check |
| `0x8010b4c8` | `0x00020000` | Flag bit 17 |
| `0x8010b4cc` | `0x80009120` | ROM: `enable_interrupts` (MIPS CP0 Status restore) |

**Notes:** The Kovah-annotated function names `PTR_disable_interrupts__clear_LSBit_of_CP0_Status_Register__8010b4a8`
and `PTR_enable_interrupts__set_CP0_Status_to_arg__8010b4cc` confirm these are standard
MIPS interrupt enable/disable wrappers. This function implements **safe BT slot timing
calculation** for ACL links. The "residual" computation avoids over-allocating slots
already used by eSCO connections.

Two runtime struct arrays found:
- `0x80123a20` — second stride-0x1ac array (may be a parallel/backup array)
- `0x8012382c` — primary stride-0x1ac array (seen in multiple functions)

**Libre:** Critical — must implement with proper interrupt protection.

---

### FUN_8010b0a4 (108B) — ACL Packet Type Flag Corrector

```c
undefined4 FUN_8010b0a4(ushort *param_1) {
    uint idx = (*(byte*)((int)param_1 + 1) >> 2);    // bits[3:2] of byte 1
    if ((*param_1 & 0x380) == 0x200) {               // bits[9:7] = 0x200 (ACL 3-slot)
        ushort *entry = (ushort*)(idx * 2 + 0xb6001214);  // MMIO table entry
        ushort val = *entry;
        uint check = (*ROM_80056609)(idx * 0x14 & 0xff) & 0xf;
        if (check == 3 && (val >> 8) & 0xffc0) != 0) {
            *entry = val & 0x3fff;                    // clear top 2 bits
        }
    }
    return 0;
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010b110` | `0xb6001214` | MMIO base (table of uint16 slot entries) |
| `0x8010b114` | `0x80056609` | ROM: slot allocator A |

**Notes:** Detects when an ACL connection has 3-slot packet configuration
(bits[9:7] == 0x200) and clears stale "wide" bits in the MMIO slot table
when the slot allocator returns code 3 (some capacity state). This is a
**packet type coercion** — prevents 3-slot ACL packets from being scheduled
when the link cannot support them.

**Libre:** Must implement — MMIO manipulation + ROM scheduling API.

---

### FUN_8010c780 (34B) — Subsystem Initializer

```c
void FUN_8010c780(void) {
    (*FUN_80110869)();     // patch fn: unknown (to be analyzed)
    (*ROM_800083ed)();     // ROM fn
    (*ROM_80007af1)();     // ROM fn
    *(0x8012133b) = 1;    // set "initialized" flag
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010c7a4` | `0x80110869` | **UNKNOWN** patch DATA fn (needs analysis) |
| `0x8010c7a8` | `0x800083ed` | ROM fn |
| `0x8010c7ac` | `0x80007af1` | ROM fn |
| `0x8010c7b0` | `0x8012133b` | "initialized" flag byte |

**Notes:** Calls 3 setup functions then sets an "initialized" flag at `0x8012133b`.
`FUN_80110869` is a **new unknown function** in the patch DATA block at address `0x80110869`.
Both `0x800083ed` and `0x80007af1` are ROM functions (to be identified).

**Libre:** Implementation is trivial once the 3 callee functions are understood.
**New unknown function identified: `FUN_80110869`** — must be analyzed next.

---

### FUN_8010c63c (278B) — Retransmission Counter / ACL Packet Handler

```c
void FUN_8010c63c(int param_1) {
    undefined *arr = struct_array_8012382c;
    uint16 seq = *(uint16*)(param_1 + 2);             // sequence counter from packet
    uint stored = *(uint32*)(arr + 0x14bc);           // stored reference counter

    if (seq < stored) {
        // Counter wrapped / out of bounds — log and reset
        debug_log(2, 0xfa, 0x2870, 0xc96);            // via FUN_80074fa9
        *(arr + 0x14bc) = 0;
    } else {
        *(arr + 0x14bc) = 0;
        uint delta = seq - stored;
        // Pack into arr[0x19e] bits[6:4] from MMIO 0xb6001176 bits[15:14]
        arr[0x19e] = (arr[0x19e] & ~0x30) |
                     (((*(uint16*)0xb6001176) >> 0xe) << 4);

        if (delta != 0) {
            // Build local LMP-PDU struct in local_38
            local_38 = CONCAT bytes (delta << 11) | 0x200;

            // Get ACL buffer from FUN_8004f241 (ROM)
            int buf = (*ROM_8004f241)();
            if (buf != 0 && (*(byte*)(buf + 8) & 1) != 0) {
                *(byte*)(buf + 0x21) |= 1;             // set flag in buffer
            } else if (buf == 0) {
                // Allocate new buffer via ACL queue
                local_buf.type = 0x2d0;
                local_buf.flags = ...;
                if (*(int*)(DAT_801250d0 + 8) == 0) {
                    local_buf.flags &= ~0x01000000;
                } else {
                    int rbuf = (*ROM_8004f999)();
                    local_buf.priority = *(byte*)(rbuf + 0x10);
                    local_buf.flags |= 0x01000000;
                }
                int rc = (*FUN_800098d9)(*(uint32*)ptr_80122da8,
                                         local_38, buf, local_buf);
                if (rc == 0) {
                    (*ROM_80051119)(buf);
                }
            }
        }
    }
    (*ROM_800519d9)(*(uint16*)(param_1 + 10));    // process next in chain
}
```

**Literal pool:**
| Address | Value | Role |
|---------|-------|------|
| `0x8010c754` | `0x8012382c` | stride-0x1ac struct array |
| `0x8010c758` | `0x80111068` | patch data: eSCO capability table |
| `0x8010c75c` | `0x80074fa9` | ROM debug logger |
| `0x8010c760` | `0xb6001176` | MMIO: sequence counter register |
| `0x8010c764` | `0x8004f241` | ROM: get ACL buffer ptr |
| `0x8010c768` | `0x801250d0` | RAM: ACL queue state |
| `0x8010c76c` | `0x8004f999` | ROM: get buffer from queue |
| `0x8010c770` | `0x80122da8` | RAM: ACL queue base ptr |
| `0x8010c774` | `0x800098d9` | ROM: ACL packet submit |
| `0x8010c778` | `0x80051119` | ROM: ACL post-submit notifier |
| `0x8010c77c` | `0x800519d9` | ROM: process next ACL packet |

**Notes:** This function handles **ACL packet sequence counting and retransmission**.
It reads a sequence counter from an incoming PDU, compares against the stored counter,
packs bits into a connection record field, and if there's a delta (new data), submits
an ACL buffer to the hardware queue. The MMIO `0xb6001176` provides flow-control bits.

Field `0x14bc` in the stride-0x1ac struct is the **sequence counter** for ACL packets.

**Libre:** Must implement — complex ACL flow control. Involves 5+ ROM functions.

---

## New Unknown Functions Identified

| Address | Caller | Notes |
|---------|--------|-------|
| `0x80110869` | `FUN_8010c780` | Patch DATA fn; appears to be a sub-initializer |
| `0x80060dd9` | `FUN_8010a84c` | ROM: read pending SCO request |
| `0x80036421` | `FUN_8010a84c` | ROM: allocate connection handle |
| `0x80036df9` | `FUN_8010a84c`, `FUN_8010aa58` | ROM: SCO state machine (Kovah: `called_by_fHCI_Remote_Name_Request_5_1`) |
| `0x80060d0d` | `FUN_8010a84c` | ROM: partial-success callback |
| `0x80060cfd` | `FUN_8010a84c` | ROM: cleanup callback |
| `0x80071b85` | `FUN_8010a84c` | ROM: connection-accepted notifier |
| `0x8001d071` | `FUN_8010a84c` | ROM: send LMP PDU (reject) |
| `0x8001da0d` | `FUN_8010aa58` | ROM: send disconnect |
| `0x80056609` | `FUN_8010b118`, `FUN_8010b3d8`, `FUN_8010b0a4` | ROM: BT slot allocator A |
| `0x80056661` | `FUN_8010b118`, `FUN_8010b3d8` | ROM: BT slot allocator B |
| `0x80056291` | `FUN_8010b3d8` | ROM: slot availability check |
| `0x800083ed` | `FUN_8010c780` | ROM fn (init) |
| `0x80007af1` | `FUN_8010c780` | ROM fn (init) |
| `0x8004f241` | `FUN_8010c63c` | ROM: get ACL buffer |
| `0x8004f999` | `FUN_8010c63c` | ROM: get buffer from queue |
| `0x800098d9` | `FUN_8010c63c` | ROM: ACL packet submit |
| `0x80051119` | `FUN_8010c63c` | ROM: ACL post-submit notifier |
| `0x800519d9` | `FUN_8010c63c` | ROM: process next ACL packet |

All ROM functions (`0x8000xxxx` – `0x8007xxxx`) are **called by address** from the libre
firmware — no reimplementation needed.

---

## Runtime Data Structures Identified

| Address | Stride | Field | Use |
|---------|--------|-------|-----|
| `0x8012dc50` | 0x2b8 | +0xb2, +0xc1, +0xd1, +0xd7, +0xde, +0xe0, +0xe1 | SCO/eSCO connection records |
| `0x8012382c` | 0x1ac | +0x21a, +0x216, +0x14bc, +0x19e | ACL connection timing records |
| `0x80123a20` | 0x1ac | +0x... | Secondary ACL struct (parallel to 0x8012382c?) |
| `0x8012b840` | — | 0x102 bytes | Pending SCO request buffer |
| `0x8012b842` | — | byte | Pending flag #2 |
| `0x8012b943` | — | byte | Pending flag #1 |
| `0x8012b803` | — | byte | eSCO reset/busy flag |
| `0x80120ccd` | — | byte | eSCO active flag |
| `0x8012133b` | — | byte | Subsystem initialized flag |

---

## Libre Firmware Implementation Matrix

| Function | Lines of MIPS16e | ROM calls | MMIO writes | Priority |
|----------|-----------------|-----------|-------------|----------|
| `FUN_8010a49c` | ~3 | 0 | 0 | LOW (trivial) |
| `FUN_8010a594` | ~3 | 0 | 1 | LOW (trivial) |
| `FUN_8010a410` | ~20 | 1 | 1 | MEDIUM |
| `FUN_8010a4ac` | ~25 | 0 | 0 | MEDIUM (gate fn) |
| `FUN_8010a550` | ~20 | 0 | 0 | MEDIUM |
| `FUN_8010a5ac` | ~15 | 0 | 0 | MEDIUM |
| `FUN_8010a5d8` | ~30 | 1 | 2 | MEDIUM |
| `FUN_8010b118` | ~25 | 2 | 0 | MEDIUM |
| `FUN_8010b0a4` | ~30 | 1 | 1 | MEDIUM |
| `FUN_8010c780` | ~10 | 3 | 0 | MEDIUM |
| `FUN_8010a6ec` | ~60 | 1 | 0 | HIGH (buffer alloc) |
| `FUN_8010aa58` | ~35 | 4 | 0 | HIGH |
| `FUN_8010b3d8` | ~80 | 5 | 0 | HIGH (scheduler) |
| `FUN_8010a84c` | ~150 | 10 | 0 | HIGH (connection) |
| `FUN_8010c63c` | ~100 | 5 | 1 | HIGH (ACL flow ctrl) |

Total: 13 functions need real MIPS16e code (~620 insns estimated).
All ROM calls go to fixed addresses in the 0x80000000–0x8007ffff range.
