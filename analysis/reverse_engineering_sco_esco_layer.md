# RTL8761BU — SCO/eSCO Layer & Slot Scheduler (2026-06-08)

Covers 26 functions from the DATA block across four analysis passes:
- **Group A** (0xa000 region helpers): 10 functions near the patch entry point
- **Group B** (Appendix D hook targets): 5 functions installed as first-batch hooks
- **Group C** (2026-06-08 second pass): 9 functions — `FUN_80110868` + 8 from `FUN_8010a84c` pool
- **Group D** (2026-06-08 third pass): 2 PATCH functions from `FUN_8010ce0c` AFH init chain
- **Tail-call resolution** (2026-06-08): `FUN_8010b4d0` fully resolved; `jr a3` = normal return

All decompiled via various `Decompile*.java` scripts and `DecompileNewHookFns.java` /
`DecompileHookFnsBatch.java` / `AnalyzeB4D0Tail.java` against GZF
`2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`.

---

## Summary Table

### Group A — 0xa000 Region Helpers

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

### Group B — Appendix D Hook Targets

| Function | Size | Classification | Description |
|----------|------|----------------|-------------|
| `FUN_8010b118` | 82B | **REAL** | Slot interval allocator (ROM API wrapper) |
| `FUN_8010b3d8` | 206B | **REAL** | ACL slot scheduler (interrupt-protected) |
| `FUN_8010b0a4` | 108B | **REAL** | ACL packet type flag corrector |
| `FUN_8010c780` | 34B | **REAL** | Subsystem initializer (3 fn calls + flag) |
| `FUN_8010c63c` | 278B | **REAL** | Retransmission counter / ACL packet handler |

### Group C — Second-Pass Hook Targets (2026-06-08)

| Function | Size | Classification | Description |
|----------|------|----------------|-------------|
| `FUN_80110868` | 322B | **REAL** | eSCO codec frame ring-buffer scheduler |
| `FUN_8010ce0c` | 728B | **REAL** | AFH capability mapper + HW init chain |
| `FUN_8010fa34` | 184B | **REAL** | AFH 79-channel map merger (AND + min-20) |
| `FUN_8010f950` | 174B | **REAL** | AFH channel classification + VSC fc95 trigger |
| `FUN_8010fb08` | 292B | **REAL** | BLE/narrow-band AFH 5-byte channel aggregator |
| `FUN_8010e350` | 1174B | **REAL** | AFH quality ranking engine (79 channels) |
| `FUN_8010bda0` | 114B | **REAL** | SCO/eSCO negotiation acceptance validator |
| `FUN_8010c09c` | 76B | **REAL** | Baseband register capability gate |
| `FUN_8010b4d0` | 76B | **REAL** | eSCO slot allocation trigger (FULLY RESOLVED 2026-06-08) |

### Group D — AFH Init Chain PATCH Functions (2026-06-08)

| Function | Size | Classification | Description |
|----------|------|----------------|-------------|
| `FUN_8010ad88` | 40B | **REAL** | BB reg 0x104 bit extractor: returns 4-bit AFH cap mode |
| `FUN_8010ccb8` | 264B | **REAL** | AFH HW reg configurator: programs BB regs 0x15c/0x1fc via masks |

Only 2 of 26 functions are trivial stubs. The remaining 24 require real implementation.

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

## Group C — Second-Pass Hook Targets (2026-06-08)

Decompiled via `DecompileNewHookFns.java` and `DecompileHookFnsBatch.java`.

---

### FUN_80110868 (322B) — eSCO Codec Frame Ring-Buffer Scheduler

Called by `FUN_8010c780` (the subsystem initializer) as its first action.
MIPS16e odd pointer `0x80110869` → entry at even address `0x80110868`.

**Role:** Flushes pending eSCO codec frame descriptors into an 8-slot, 0x10-byte-per-slot ring
buffer. Runs as an infinite loop until no more pending frames exist.

**Decompile (condensed):**
```c
void FUN_80110868(void) {
    ushort *ring_base = DAT_801109ac;           // ring state struct
    ushort uVar1 = *DAT_801109ac;               // capability word A
    ushort uVar2 = *DAT_801109b0;               // capability word B

    // Sanity checks — log and clear overflow bits
    if ((short)uVar1 < 0)                       // bit 15 set = overflow flag
        (*log_fn)(2,0xf8,0x1d7,0xe05,2,...);
        *ring_base = uVar1 & 0x7fff;            // clear overflow bit
    if (uVar2 & 0x10)                           // bit 4 = another overflow flag
        (*log_fn)(2,0xf8,0x1de,0xe05,...);
        (*fn_ptr_bc)(0xac, uVar2 & 0xffef);     // clear bit 4, pass opcode 0xac

    PTR_DAT_801109c0[0x83] = 0;                 // clear mode flag byte

    do {
        byte *slot = base + base[0x80] * 0x10;  // current ring slot (stride 16)

        if ((*DAT_801109c4 & 1) == 0) {         // mode 0 (e.g. SCO encode path)
            if ((*DAT_801109e0 & 1) == 0) return; // no pending → done
            slot[6] = *DAT_801109e4;             // fill frame field +6
            slot[8] = *DAT_801109e8;             // fill frame field +8
            slot[10] = *DAT_801109ac;            // fill frame field +10 (cap word A)
            uVar3 = *DAT_801109ec; uVar7 = *DAT_801109f0;
            *slot |= 1;                          // mark slot valid
            PTR_DAT_801109f4[0x164] &= 0xfe;    // clear pending bit at struct+0x164
        } else {                                 // mode 1 (e.g. SCO decode path)
            if ((*DAT_801109c8 & 1) == 0) return;
            slot[0xc] = *DAT_801109cc;
            slot[0xe] = *DAT_801109d0;
            uVar3 = *DAT_801109d4; slot[10] = *DAT_801109d8; uVar7 = *DAT_801109dc;
            *slot &= 0xfe;                       // clear valid bit (mode 1 uses inverse)
            base[0x83] = 1;                      // set mode flag
        }
        slot[4] = uVar7;  slot[2] = uVar3;       // fill frame fields +2, +4
        base[0x80] = (base[0x80] + 1) & 7;       // advance ring head (mod 8)
        base[0x82]++;                             // increment total frame count
    } while (true);                              // loop until no pending data
}
```

**Ring buffer layout** (stride 16 bytes, 8 slots, head at `base[0x80]`):

| Offset | Content |
|--------|---------|
| `+0` | mode/valid flag byte |
| `+2` | frame field A (from `*DAT_801109ec` or `*DAT_801109d4`) |
| `+4` | frame field B (from `*DAT_801109f0` or `*DAT_801109dc`) |
| `+6` | field C (mode 0 only, from `*DAT_801109e4`) |
| `+8` | field D (mode 0 only, from `*DAT_801109e8`) |
| `+a` | capability word A (`*DAT_801109ac`) |
| `+c` | field E (mode 1 only, from `*DAT_801109cc`) |
| `+e` | field F (mode 1 only, from `*DAT_801109d0`) |

**Key literal pool addresses:**

| Symbol | Value | Role |
|--------|-------|------|
| `DAT_801109ac` | runtime RAM | capability word A (16-bit) / ring state base |
| `DAT_801109b0` | runtime RAM | capability word B (16-bit) |
| `DAT_801109b8` | fn ptr | debug/log function |
| `PTR_DAT_801109bc` | fn ptr | handler for opcode 0xac |
| `PTR_DAT_801109c0` | RAM struct | ring buffer state struct |
| `DAT_801109c4/c8` | RAM | mode-0 / mode-1 "pending" flags |
| `DAT_801109e0/e4/e8` | RAM | mode-0 source data |
| `DAT_801109c8/cc/d0` | RAM | mode-1 source data |
| `PTR_DAT_801109f4` | RAM struct | external state struct (pending bit at +0x164) |

**Libre:** Must implement — manages the codec frame ring buffer that drives eSCO audio.
The mode selector and two data paths correspond to two codec directions (encode/decode or
two different codec configurations). The `& 7` wrap gives an 8-slot circular buffer.

---

### FUN_8010ce0c (728B) — AFH Capability Mapper + Hardware Init Chain

**Role:** Reads the 16-bit BT AFH host channel classification word from `config_base+0x44`,
maps individual bits into hardware register space, programs baseband registers 0x15c and
0x1fc, then calls a chain of 4–5 initialization functions to arm the AFH engine.

**Decompile (condensed):**
```c
void FUN_8010ce0c(void) {
    // If chip type == 6: enable register-4 bit 0x200 (mode gating)
    if ((*PTR_DAT_8010d0e4 & 0x1e) == 6) {
        uint r4 = (*hw_read)(4);
        (*hw_write)(4, r4 | 0x200, 1);
    }

    uint32 cap_reg = *DAT_8010d0f0;                    // 32-bit capability register
    uint16 ch_class = *(uint16*)(config_base + 0x44);  // host channel classification

    if (config_base[0x46] & 8) {                       // if enhanced AFH capable
        byte codec_byte = config_base[0xec];
        uint16 codec2 = *(uint16*)(config_base + 0xd2);

        // Map ch_class bits [0..9] → cap_reg bits [0x10..0x19]
        cap_reg = scatter_bits(cap_reg, ch_class, masks);

        // Read and reprogram registers 0x15c and 0x1fc
        uint r15c = (*hw_read)(0x15c, 2);
        uint r1fc = (*hw_read)(0x1fc, 2);
        uint new_15c = remap_codec_bits(r15c, codec_byte, codec2[0..3], codec2[4..7]) & mask;
        uint new_1fc = remap_codec_bits(r1fc, codec_byte, codec2[8..11], codec2[12..]) & mask;
        (*hw_write)(0x15c, new_15c | extra_bits, 2);
        (*hw_write)(0x1fc, new_1fc | extra_bits, 2);
        *DAT_8010d128 = (*DAT_8010d128 & mask) | ((ch_class >> 13 & 1) << 0x1d);
    }

    if ((*PTR_DAT_8010d0e4 & 0x1e) == 2 && !(config_base[0x3f] & 0x10))
        cap_reg |= DAT_8010d130;
    *DAT_8010d0f0 = cap_reg;

    if (PTR_DAT_8010d134[0x10b] == 0)                  // debug gate
        (*log_fn)(4,0xfa,0x2a4_line,...,ch_class);

    // AFH init chain
    (*DAT_8010d140)();              // AFH module init 1
    (*DAT_8010d144)();              // AFH module init 2
    *(PTR_DAT_8010d148 + 0x40) = 0; // clear a struct field
    (*DAT_8010d14c)(1);             // trigger AFH with arg=1
    (*DAT_8010d150)();              // AFH module init 3
}
```

The **bit-scatter** pattern maps each bit i (0–9) of `ch_class` into bit position `0x10+i` of
`cap_reg`, using per-bit mask constants. This is the standard HCI-to-HW AFH channel
classification encoding.

Registers 0x15c and 0x1fc are baseband codec capability registers — their bit fields
correspond to specific codec configurations (µ-law, A-law, CVSD, transparent, wideband).

**AFH init chain — RESOLVED (2026-06-08):**

| Pool addr | Value | Function | Block | Size | Role |
|-----------|-------|----------|-------|------|------|
| `DAT_8010d140` | `0x800117a5` | `FUN_800117a4` | ROM | 14B | ORs `0xfc00` into global — enables AFH channel mask bits [15:10] |
| `DAT_8010d144` | `0x8000c3f5` | `FUN_8000c3f4` | ROM | 414B | AFH channel map updater: checks config flags, updates maps, calls classifier |
| `DAT_8010d148` | `0x80121908` | `DAT_80121908` | RAM | — | Data pointer (struct field to zero), NOT a function call |
| `DAT_8010d14c` | `0x800122b9` | `FUN_800122b8` | ROM | 64B | AFH cap-param extractor: reads config[0x46-0x47], extracts bits[11:8] (arg=1) |
| `DAT_8010d150` | `0x8010ccb9` | `FUN_8010ccb8` | **PATCH** | 264B | **AFH HW register configurator** — programs BB regs 0x15c and 0x1fc |

`*(PTR_DAT_8010d148 + 0x40) = 0` — the d148 entry is a struct base address; the code
zeros a field at struct+0x40, not a function call.

**Libre:** Must implement — core of the AFH initialization path. The three ROM tail-calls
are called by address; only `FUN_8010ccb8` (PATCH, 264B) requires libre implementation.

---

### FUN_8010fa34 (184B) — AFH 79-Channel Map Merger

**Role:** Merges a remote channel map (10 bytes = 79 BT channels) with the local map using
bitwise AND (intersection), enforces the BT-spec minimum of 20 usable channels, and copies
the result to an output buffer.

**Decompile (condensed):**
```c
undefined4 FUN_8010fa34(undefined4 dst, undefined4 p2, char trigger) {
    if (*init_flag == 0) {
        // First call: copy reference map to both local and working map
        memcpy(local_map, ref_map_10bytes, 10);
        memcpy(working_map, ref_map_10bytes, 10);
        init_flag[2] = 1;  init_flag[4] = 1;
    } else {
        // Merge: working = ref AND local AND mask
        memcpy(working_map, ref_map_10bytes, 10);
        working_map &= local_map;                    // AND with local
        working_map &= PTR_DAT_8010faf8;             // AND with allowed mask

        // Enforce minimum 20 channels
        uint good = (*popcount_fn)(working_map, 0);
        if (good < 20)
            memcpy(working_map, local_map, 10);      // fall back to unfiltered local
    }
    // Copy result to destination
    (*memcpy_fn)(dst, working_map, 10);
    *init_flag = 1;
    if (trigger == 1) (*notify_fn)();
    return 1;
}
```

The two state paths (first-call init vs. merge) and the `< 20` fallback implement
the AFH channel map update procedure per Bluetooth Core Spec §4.2.27.

**Key data:**
- `PTR_PTR_8010faf0` → dual pointers to local map (`+0`) and working map (`+0xc`)
- `PTR_DAT_8010faf4` → 10-byte reference channel map (default allow-all or classification result)
- `PTR_DAT_8010faf8` → 10-byte hardware-forbidden-channel mask

**Libre:** Must implement — standard BT AFH channel map management. Logic is well-defined
by the BT specification.

---

### FUN_8010f950 (174B) — AFH Channel Classification Handler + VSC fc95 Trigger

**Role:** Called when new channel quality data arrives. Logs the 10-byte channel map,
runs a classification step, and if a scan has not yet been started, triggers the chip's
"start AFH scan" command (via `PTR_VSC_0xfc95_called2_1_8010fa2c`).

**Decompile (condensed):**
```c
undefined4 FUN_8010f950(char *mode_flag) {
    (*reset_or_ack_fn)();                                    // acknowledge / reset trigger
    (*log_fn)(3,0xf7,0x2a4,0x688, 10, hdr,                 // log 10-byte channel map
              ch_map[0],...,ch_map[9]);

    undefined4 tmp = 0;  byte tmp2 = 0;
    (*classify_fn)(&tmp, working_map_ptr);                   // classify quality
    (*memcpy5)(&tmp, working_map_ptr, 5);                   // copy 5-byte result

    // Handle mode transitions
    if (*mode_flag == 1 || *mode_flag == 0)
        (*transition_fn)();                                  // trigger mode transition

    // Start AFH scan if not already running (sentinel = -1)
    if (*(int*)scan_started_ptr == -1) {
        (*PTR_VSC_0xfc95_called2_1_8010fa2c)
              (0, scan_started_ptr, PTR_LAB_80066d9c, 0, *(undefined4*)notify_ptr);
        (*schedule_fn)(*(undefined4*)scan_started_ptr, *(undefined4*)notify_ptr);
    }
    return 1;
}
```

`PTR_VSC_0xfc95_called2_1_8010fa2c` is Kovah's annotation for the VSC 0xfc95 "start scan"
call — this is the chip's vendor command to initiate channel quality measurement.

**Libre:** Must implement — triggers the chip's AFH scan when channel quality data is ready.
The VSC 0xfc95 command is sent via a function pointer installed at runtime; libre firmware
must install the same pointer (to ROM's VSC dispatch or a stub that calls the ROM fn).

---

### FUN_8010fb08 (292B) — 5-Byte (BLE/Narrow-Band) Channel Map Aggregator

**Role:** Aggregates a 5-byte channel map by ANDing three source maps (local, peer, and
an additional mask), enforces a minimum threshold of 2 usable channels, and tracks
consecutive failure count. The 5-byte = 40-channel format matches **BLE channel maps**.

**Decompile (condensed):**
```c
undefined4 FUN_8010fb08(void) {
    if (state[1] == 0) {
        // First-time init: copy default map to two arrays
        memcpy(map_a, ref_map_5bytes, 5);  // into array at struct+0x14
        memcpy(map_b, ref_map_5bytes, 5);  // into array at struct+0x20
        state[2] = 1; state[4] = 1;
    } else {
        // Merge: map_b = ref AND map_a AND map_c
        memcpy(map_b, ref_map_5bytes, 5);
        map_b &= map_a;                     // AND with local (struct+0x14)
        map_b &= map_c;                     // AND with extra mask (struct+0x18)
        map_b &= map_d;                     // AND with peer map (struct+0x1c)

        uint good = (*popcount_fn)(map_b, 1);
        if (good < 2)                       // minimum 2 channels (BLE spec)
            (*memcpy5)(map_b, map_c, 5);    // fall back to map_c
    }

    if (state[1] != 0) {
        // Check if map changed
        if ((*compare_fn)(ref_map, map_b, 5) == 0) {
            // No change: increment failure counter
            struct[0x3e]++;
            (*log_fn)(5,0xf7,0x261,...,struct[0x3e]);
        } else {
            // Map changed: reset counter, update
            struct[0x3e] = 0;
            (*log_fn)(5,0xf7,0x24f,...,0);
            if (struct_array[0x1478] & 1)
                (*notify_fn)(map_b, 2);      // notify change
            (*memcpy5)(ref_map, map_b, 5);   // save as new reference
        }
    }
    state[1] = 1;
    return 1;
}
```

**Key observations:**
- 5-byte maps = 40-bit BLE channel map (channels 0-39 per BLE spec)
- Minimum threshold of 2 (BLE Core Spec requires ≥ 2 data channels)
- Three-source AND mirrors the BLE channel map merge: local + peer + HCI-mandated
- The `struct_array[0x1478]` flag (with `PTR_base_of_0x1ac_struct_array_0xA_large2_8010fc50`)
  indicates this is embedded within the per-connection eSCO/ACL structure

**Libre:** Must implement — BLE channel map aggregation logic. The BLE channel map exchange
happens via LL_CHANNEL_MAP_IND (BLE v4.0+).

---

### FUN_8010e350 (1174B) — AFH Channel Quality Ranking Engine

**Role:** The core AFH algorithm. Takes an 80-element quality array, applies weighted
combination with connection-tracking history, ranks all 79 usable Bluetooth channels by
quality, selects the best N (minimum 20), applies hardware forbidden-channel mask, and
outputs the final 10-byte AFH channel bitmap.

**Decompile (condensed):**
```c
undefined4 FUN_8010e350(int conn, short target_count, undefined4 quality_src,
                         int output_map, int *selected_count) {
    if (*(int*)(conn + 0x1c) < 9) {         // too few samples
        memset(output_map, 0xff, 10);        // output: all channels
        output_map[9] = 0x7f;               // 79 channels, top bit clear
        (conn+0x18) = 0; (conn+0x14) = 0x20;
        *selected_count = 0x4f;             // all 79
        return 1;
    }

    // Compute thresholds from connection record
    int upper_thresh = *(char*)(conn + conn[10] + 4) << 7;  // signed, scaled
    int lower_thresh = *(char*)(conn + 0x10) * -0x10000 >> 10;  // negative limit
    int limit = *(char*)(conn + 0x11);                           // override limit

    // Step 1: decode + weight current measurements
    short quality[80];
    (*decode_fn)(quality_src, quality);          // decode from packed format
    bool use_history = *(char*)(conn + 0xd);
    for (int i = 0; i < 80; i++)
        quality[i] = use_history * 0x80 * quality[i] + *(short*)(conn + (i+12)*2 + 8);

    // Step 2: rank channels (sort quality array, keep track of original indices)
    short sorted[80];  short idx[80];
    (*rank_fn)(quality, sorted, 0, 78);

    // Step 3: select top N (scan from highest to find threshold crossing)
    int N = 79;  // default: all channels
    for (int i = 78; i >= 0; i--)
        if (upper_thresh < target_count * 0x80 - sorted[i]) { N = i+1; break; }

    // Step 4: secondary threshold (lower quality floor)
    for (int i = 78; i >= 0; i--)
        if (sorted[i] < lower_thresh) {
            // adjust N
            if (i+1 < 20 || -limit < total_good) goto use_20;
            N = (i+1 < N) ? i-4 : N-5;
            break;
        }
use_20:
    if (N < 20) N = 20;                        // enforce BT minimum
    if (*(char*)(conn + 0xe) != 0) N = *(char*)(conn + 0xe);  // hard override

    // Step 5: build output bitmap from top-N channel indices
    byte enable[80] = {0};
    for (int i = 0; i < N; i++) enable[idx[i]] = 1;
    (*bitmap_fn)(enable, output_map);           // convert channel list → 10-byte bitmap

    // Step 6: apply hardware forbidden-channel mask
    for (int i = 0; i < 10; i++) output_map[i] &= PTR_DAT_8010e814[i];

    // Step 7: enforce minimum again after masking
    byte good = (*popcount_fn)(output_map);
    if (good < 20) {
        // Add channels from sorted list that are not forbidden
        int need = 20 - good;
        for (int i = 0; i < 80 && need > 0; i++) {
            int ch = idx[i];
            if (ch is in forbidden mask AND not yet set) { enable[ch]=1; need--; }
        }
        (*bitmap_fn)(enable, output_map);
        (*log_fn)(..., need_added);
    }

    *selected_count = (*popcount_fn)(output_map);
    return 1;
}
```

**Key function pointers (from literal pool):**

| Symbol | Role |
|--------|------|
| `DAT_8010e7f8` | Decode packed quality measurements |
| `DAT_8010e7fc` | Some transform (possibly FFT/Hadamard for channel quality) |
| `DAT_8010e800` | Sort channels by quality (produces ranked index array) |
| `DAT_8010e804` | Count channels above threshold |
| `DAT_8010e808` | Validate + adjust N against connection record |
| `DAT_8010e810` | Convert channel enable-list to 10-byte bitmap |
| `DAT_8010e814` | Hardware forbidden-channel mask (10 bytes, ROM or RAM) |
| `DAT_8010e818` | Popcount of 10-byte bitmap |
| `DAT_8010e800` | Channel ranking sort |

**Libre:** Critical — this IS the AFH algorithm. Must be implemented in full.
The connection record (`conn`) provides per-link quality history at `+8 + (i+12)*2`,
thresholds at `+0x10, +0x11, +0xd`, and sample count at `+0x1c`.
The forbidden-channel mask at `DAT_8010e814` must match the hardware's RF constraints.

---

### FUN_8010bda0 (114B) — SCO/eSCO Negotiation Acceptance Validator

**Role:** Called when an incoming SCO/eSCO connection setup request arrives. Validates
whether the local state allows accepting it; rejects (LMP_NOT_ACCEPTED) or accepts and
updates the connection record.

**Decompile (condensed):**
```c
undefined4 FUN_8010bda0(int conn, undefined2 *pdu, undefined1 *result) {
    uint type = *(byte*)(pdu + 2);  // connection type field from PDU
    bool can_accept = (*(byte*)(conn + 0x20) & 2) != 0;  // SCO-enable flag
    bool esco_exception = (type == 3 && *(char*)(pdu + 3) != 0);

    if (!can_accept && !esco_exception) {
        // Reject: set error + send LMP_NOT_ACCEPTED (opcode 0xe)
        *result = 0x12;                        // error: Unsupported Feature or Similar
        (*lmp_reject_fn)(*pdu, tmp_buf);       // build reject PDU using first 2 bytes
        tmp_buf[0] = *result;
        (*lmp_send_fn)(0xe, tmp_buf, 4);       // send LMP PDU, opcode 0xe, 4 bytes
        return 1;                              // handled (rejected)
    }

    // Accept: update connection record
    if (*(int*)(conn + 0x50) != 0 && *(int*)(*(int*)(conn+0x50) + 0x24) != 0) {
        int link = (*get_link_fn)(conn);       // get linked record
        if ((*(byte*)(conn + 0x20) & 0x10) && type == 3)
            *(undefined1*)(link + 0x11) = 0;  // clear field for type-3 eSCO
    }
    return 0;                                  // handled (accepted)
}
```

The `0x12` error reason is HCI_ERROR_UNSUPPORTED_FEATURE_OR_PARAMETER_VALUE.
LMP opcode `0xe` is `LMP_MAX_SLOTS` — this may be a Realtek-specific rejection encoding,
or there's a sub-function `lmp_reject_fn` that builds the actual LMP_NOT_ACCEPTED PDU.

**Libre:** Must implement — gateway for SCO/eSCO acceptance. Logic is straightforward once
the connection-state flag semantics are understood.

---

### FUN_8010c09c (76B) — Baseband Per-Link Feature Gate

**Role:** If a specific capability is not declared in config (config+0x7a bit 5 clear),
clears certain feature bits from baseband hardware register 0x0f at a given connection
index. Acts as a capability downgrade for connections that don't support the feature.

**Decompile:**
```c
undefined4 FUN_8010c09c(int param_1) {
    if ((PTR_config_base[0x7a] & 0x20) == 0) {  // if capability bit NOT set
        uint val = (*hw_read)(*(byte*)(param_1+2), 0xf);  // read reg 0x0f at conn index
        (*hw_write)(*(byte*)(param_1+2), 0xf, val & 0xffffce03);  // clear bits 2–8, 12–13
        // Mask 0xce03 = 1100 1110 0000 0011 → clears bits 2,3,4,5,6,7,8,12,13
    }
    return 0;
}
```

Bits 2–8 and 12–13 of register 0x0f (per-link) control enhanced data rate, SCO power
control, and retransmission features. Clearing them disables those features for the link.

**Libre:** Must implement — programs per-link feature bits. Logic is simple once
`hw_read`/`hw_write` fn-ptrs are wired.

---

### FUN_8010b4d0 (76B) — eSCO Slot Allocation Trigger ✓ FULLY RESOLVED (2026-06-08)

**Role:** Receives incoming eSCO capability data (packet type fields from an LMP PDU),
stores it in the per-connection struct, and triggers slot allocation if the connection
is not yet active. Operates on the 0x80123a20-based stride-0x1ac struct array.

**Resolution of "UNRECOVERED_JUMPTABLE":** The Ghidra warning at `0x8010b5b4` is a
false alarm. The bytes `00 ef` = `jr a3` — a standard MIPS16e function return.
The function prologue saved `ra` to `sp+0x24`; the epilogue restores it into `a3` and
returns via `jr a3`. Ghidra lost track that `a3 = saved_ra`.

Full epilogue (MIPS16e):
```
8010b5ae: lw  a3, 0x24(sp)   ; restore saved ra into a3
8010b5b0: lw  s1, 0x20(sp)   ; restore s1
8010b5b2: lw  s0, 0x1c(sp)   ; restore s0
8010b5b4: jr  a3             ; RETURN (jr a3, not jr ra)
8010b5b6: addiu sp, 0x28     ; delay-slot: restore stack pointer
```

**Full decompile (from Ghidra via AnalyzeB4D0Tail.java, 2026-06-08):**
```c
void FUN_8010b4d0(int cap_pdu, undefined4 p2, uint conn_idx) {
    conn_idx &= 0xff;
    int offset = conn_idx * 0x1ac;

    // conn = &struct2_array[conn_idx]  (0x80123a20-based, stride 0x1ac)
    undefined2 *conn = (undefined2 *)(0x80123a20 + offset);

    // Store capability fields from incoming LMP PDU into connection struct
    // (byte offsets: conn+0x3c, conn+0x3e, conn+0x40)
    *(byte  *)(conn + 0x1e) = *(byte  *)(cap_pdu + 1);    // +0x3c: pkt-type byte
    conn[0x1f]              = *(ushort *)(cap_pdu + 2);    // +0x3e: field A (u16)
    conn[0x20]              = *(ushort *)(cap_pdu + 4);    // +0x40: field B (u16)
    *(byte  *)(conn + 0x48) |= 2;                          // +0x90: set "pending" bit 1

    // Check cap-flags (struct1 array, 0x8012382c-based) for pending timer
    ushort *cap_base = (ushort *)0x8012382c;
    if ((*(ushort *)(cap_base + offset/2 + 0x12a) & 2) != 0) {  // struct1[conn]+0x254
        // Cancel timer: ROM FUN_8001d4a0 (send_HCI_Read_Remote_Version_Info_Complete)
        (*FUN_8001d4a0)(0, *conn, conn_idx + 10);
        *(ushort *)(cap_base + offset/2 + 0x12a) &= 0xfffd;     // clear bit 1
    }

    if ((*(byte *)(conn + 0x48) & 1) == 0) {    // not yet active (bit 0 of +0x90 clear)
        if ((*(int *)(conn + 0x3e) == 0) &&      // +0x7c: no timer B
            (*(int *)(conn + 0x3c) == 0)) {      // +0x78: no timer A
            *(int *)(conn + 0x3e) = 0x20;        // +0x7c = 0x20 (slot count)
            (**(code **)0x8012082c)(conn_idx, 6);// indirect: connection trigger (type 6)
            *(byte *)(conn + 0x48) |= 1;         // +0x90: mark active (bit 0)
            uint ts = (*FUN_8005e23c)();          // ROM: get connection config struct
            (*FUN_8005d26c)(ts, conn_idx);        // ROM: insert ptr at struct+0x134
            (*FUN_8005ca00)(conn, 0xc, 2);        // ROM: set cap bit at struct+0x84/0x88
            if ((*(byte *)((int)conn + 0x8f) & 0x10) == 0)
                return;                           // bit 4 clear: done
            *(byte *)((int)conn + 0x8f) &= 0xef; // clear bit 4
        } else {
            *(byte *)((int)conn + 0x8f) |= 0x10; // set bit 4 (deferred)
        }
    } else {
        (**(code **)0x80120958)(conn, 0x20, 1);  // indirect: update active record
    }
    // Standard MIPS16e epilogue: lw a3,0x24(sp); lw s1; lw s0; jr a3; addiu sp,0x28
}
```

**Struct fields (byte offsets within 0x1ac-stride array at 0x80123a20):**

| Offset | Size | Content |
|--------|------|---------|
| `+0x3c` | byte | Packet-type byte from LMP PDU |
| `+0x3e` | u16 | PDU field A |
| `+0x40` | u16 | PDU field B |
| `+0x78` | u32 | Timer A (0 = no timer) |
| `+0x7c` | u32 | Timer B (set to 0x20 on trigger) |
| `+0x8f` | byte | State flag byte (bit 4 = deferred) |
| `+0x90` | byte | Status flags (bit 0 = active, bit 1 = pending) |

`+0x254` (in the **0x8012382c**-based array) = capability/timer flags halfword (bit 1 = cancel pending).

**Full literal pool:**

| Pool addr | Value | Role |
|-----------|-------|------|
| `0x8010b5b8` | `0x80123a20` | Base of struct2 array (stride 0x1ac, 10 elements) — `[1].field0` offset |
| `0x8010b5bc` | `0x8012382c` | Base of struct1 array (stride 0x1ac) — for cap-flags at +0x254 |
| `0x8010b5c0` | `0x8001d4a1` | ROM `FUN_8001d4a0` (odd = MIPS16e): send HCI_Read_Remote_Version_Info_Complete |
| `0x8010b5c4` | `0x8012082c` | RAM fn-ptr slot — double-indirect call: `(**0x8012082c)(conn_idx, 6)` |
| `0x8010b5c8` | `0x8005e23d` | ROM `FUN_8005e23c` (odd): access config at 0xa5 / copy LMP company ID |
| `0x8010b5cc` | `0x8005d26d` | ROM `FUN_8005d26c` (odd): insert ptr into 0x1ac struct at offset +0x134 |
| `0x8010b5d0` | `0x8005ca01` | ROM `FUN_8005ca00` (odd): set bit in capacity bitmask at +0x84/+0x88 |
| `0x8010b5d4` | `0x80120958` | RAM fn-ptr slot — double-indirect call: `(**0x80120958)(conn, 0x20, 1)` |

The two RAM fn-ptr slots (`0x8012082c`, `0x80120958`) are installed by the master installer
and point to ROM or patch functions for connection trigger and active-record update.

**New function discovered:** `FUN_8010b5d8` starts at `0x8010b5d8` (immediately after this
function's literal pool). Prologue: `addiu sp,-0x20 / sw ra,0x1c(sp) / sw s1 / sw s0`.

**Libre:** Must implement. All 3 ROM calls go to fixed addresses. The 2 double-indirect
calls need the fn-ptr slots (`0x8012082c`, `0x80120958`) populated by the installer.
Function semantics are fully clear from the decompile.

---

## New Unknown Functions Identified

### ROM functions (called by address — no reimplementation needed)

| Address | Caller | Notes |
|---------|--------|-------|
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
| `0x800083ed` | `FUN_8010c780` | ROM fn (init #2 in subsystem init) |
| `0x80007af1` | `FUN_8010c780` | ROM fn (init #3 in subsystem init) |
| `0x80011510` | `FUN_8010ccb8`, `FUN_8010ad88`, `FUN_80011a74` | ROM: BB reg-read (new interface; polls MMIO) |
| `0x80011608` | `FUN_8010ccb8`, `FUN_80012e38` | ROM: BB reg-write (interrupt-protected; MMIO latched) |
| `0x80011a74` | `FUN_8010ccb8` | ROM: BB reg 0xfc mode-bit setter/clearer |
| `0x800122b8` | `FUN_8010ce0c` (via d14c) | ROM: AFH cap param extractor → FUN_80012e38 |
| `0x80012e38` | `FUN_800122b8` | ROM: clear bits[7:6] in BB regs 0x170/0x174/0x178/0x17c |
| `0x800117a4` | `FUN_8010ce0c` (via d140) | ROM: OR `0xfc00` into AFH global |
| `0x8000c3f4` | `FUN_8010ce0c` (via d144) | ROM: AFH channel map updater (414B) |
| `0x8001d4a0` | `FUN_8010b4d0` | ROM: send HCI_Read_Remote_Version_Info_Complete (event 0x0c) |
| `0x8005e23c` | `FUN_8010b4d0` | ROM: get ACL config struct (returns ptr, copies LMP company ID) |
| `0x8005d26c` | `FUN_8010b4d0` | ROM: insert ptr into 0x1ac struct at offset +0x134 (linked list) |
| `0x8005ca00` | `FUN_8010b4d0` | ROM: set bit in capacity bitmask at struct+0x84/+0x88 |
| `0x8004f241` | `FUN_8010c63c` | ROM: get ACL buffer |
| `0x8004f999` | `FUN_8010c63c` | ROM: get buffer from queue |
| `0x800098d9` | `FUN_8010c63c` | ROM: ACL packet submit |
| `0x80051119` | `FUN_8010c63c` | ROM: ACL post-submit notifier |
| `0x800519d9` | `FUN_8010c63c` | ROM: process next ACL packet |

### Patch DATA functions still needing identification

| Address | Caller | Notes |
|---------|--------|-------|
| `DAT_8010d140` = `FUN_800117a4` | `FUN_8010ce0c` | **RESOLVED** — ROM 14B; ORs `0xfc00` into global |
| `DAT_8010d144` = `FUN_8000c3f4` | `FUN_8010ce0c` | **RESOLVED** — ROM 414B; AFH channel map updater |
| `DAT_8010d14c` = `FUN_800122b8` | `FUN_8010ce0c` | **RESOLVED** — ROM 64B; extracts AFH cap param (arg=1) |
| `DAT_8010d150` = `FUN_8010ccb8` | `FUN_8010ce0c` | **RESOLVED** — PATCH 264B; AFH HW reg configurator (see Group D) |
| `jr a3` epilogue | `FUN_8010b4d0` | **RESOLVED (2026-06-08)** — `jr a3` = normal MIPS16e return (a3 = saved ra); NOT a jump table |

### RAM function-pointer slots (double-indirect, identity TBD)

| RAM slot addr | Caller | Call prototype | Status |
|---------------|--------|----------------|--------|
| `0x8012082c` | `FUN_8010b4d0` | `fn(conn_idx, 6)` — connection trigger type-6 | Installed by master installer; target unknown |
| `0x80120958` | `FUN_8010b4d0` | `fn(conn_rec, 0x20, 1)` — update active record | Installed by master installer; target unknown |

### Newly discovered PATCH functions (not yet analyzed)

| Address | Discovered from | Prologue | Notes |
|---------|-----------------|----------|-------|
| `FUN_8010b5d8` | After `FUN_8010b4d0` literal pool | `addiu sp,-0x20 / sw ra,0x1c / sw s1 / sw s0` | Size unknown; full function |

All ROM functions (`0x8000xxxx` – `0x8007xxxx`) are **called by address** from the libre
firmware — no reimplementation needed.

---

## Group D — AFH Init Chain PATCH Functions (2026-06-08)

Discovered by resolving the `FUN_8010ce0c` literal pool (DAT_8010d140–d150). Two of the
four tail-calls are PATCH functions requiring libre implementation.

---

### FUN_8010ad88 (40B, PATCH) — BB Register 0x104 AFH Capability Extractor

**Role:** Reads baseband register 0x104 and extracts 4 bits that represent the current
AFH channel capability mode. Called by `FUN_8010ccb8` to get a 4-bit selector that
gets embedded into BB regs 0x15c / 0x1fc.

**Decompile:**
```c
uint FUN_8010ad88(void) {
    uint uVar1 = (*DAT_8010adb0)(0x104, 2);  // read BB reg 0x104 via FUN_80011510
    return ((0x80000000 & uVar1) >> 0x1c) | ((uVar1 & 0xe00) >> 9);
    //  = {bit[31], bits[11:9]} of BB reg 0x104 → 4-bit AFH capability code
}
```

**Literal pool:**

| Address | Value | Role |
|---------|-------|------|
| `0x8010adb0` | `0x80011511` | → `FUN_80011510` (ROM, 98B) = BB reg-read fn |
| `0x8010adb4` | `0x80000000` | mask = bit 31 (Ghidra mislabels as `PTR_FUN_8010adb4`) |

**Bit extraction:** `{bit[31], bit[11], bit[10], bit[9]}` of BB reg 0x104.
These 4 bits are an AFH channel classification mode selector that tells FUN_8010ccb8
what region of reg 0x15c to populate.

**Libre:** Must implement. Two-instruction body: call ROM FUN_80011510(0x104, 2), then
compute `(result >> 28 & 1) | (result >> 9 & 7)`.

---

### FUN_8010ccb8 (264B, PATCH) — AFH Hardware Register Configurator

**Role:** The AFH hardware initialization function. Reads two MMIO registers and two
baseband registers, applies a cascade of AND masks and OR values to configure AFH channel
capability bitfields, then writes the results back. Finally calls ROM FUN_80011a74 to
set/clear a mode bit in BB reg 0xfc.

**Decompile:**
```c
void FUN_8010ccb8(void) {
    // Read current state
    uint mmio1 = *(uint*)0xb000a050;    // MMIO AFH capability state 1
    uint mmio2 = *(uint*)0xb000a05c;    // MMIO AFH capability state 2 (read; not modified)
    uint reg15c = FUN_80011510(0x15c, 2);  // read BB reg 0x15c
    uint reg1fc = FUN_80011510(0x1fc, 2);  // read BB reg 0x1fc

    // Compute new MMIO1 value (clears bits 20, 18, 17, 16, 12)
    uint new_mmio1 = mmio1 & 0xffffefff & 0xffefffff & 0xfffbffff & 0xfffdffff & 0xfffeffff;

    // Configure BB reg 0x15c:
    //   Clear bits [11:8] and bit[5]; set bits 11,9,8,4,2,1 (= | 0xb16)
    //   Then clear bits [23:20], set bits 23,21,20 (= | 0x00b00000)
    //   Insert 4-bit AFH cap from FUN_8010ad88 into bits [15:12]
    //   Then clear bits [25:24]
    uint cap4 = FUN_8010ad88();
    uint new_15c = ((reg15c & 0xfffff0df | 0xb16) & 0xff0fffff | 0x00b00000);
    new_15c = (new_15c & 0xffff0fff | ((cap4 & 0xf) << 12)) & 0xfcffffff;
    new_15c |= 0x01000000;  // set bit 24

    // MMIO1: apply 5 more masks
    new_mmio1 &= 0xfdffffff & 0xffdfffff & 0xffbfffff & 0xff7fffff;

    // Configure BB reg 0x1fc (same mask pattern as 0x15c but no cap4 insertion)
    uint new_1fc = (reg1fc & 0xfffff0df | 0xb16) & 0xff0fffff | 0x00b00000;

    // Commit
    *(uint*)0xb000af04 = 0xff00;            // write AFH bitmap clear
    *(uint*)0xb000a050 = new_mmio1;         // write MMIO AFH state 1
    *(uint*)0xb000a05c = mmio2;             // restore MMIO AFH state 2 (unchanged)
    FUN_80011608(0x15c, new_15c, 2);        // write BB reg 0x15c
    FUN_80011608(0x1fc, new_1fc, 2);        // write BB reg 0x1fc
    FUN_80011a74();                          // set/clear bit[12] of BB reg 0xfc
}
```

**Literal pool summary:**

| Pool addr | Value | Role |
|-----------|-------|------|
| `0x8010cdc0` | `0xb000a050` | MMIO: AFH capability state register 1 |
| `0x8010cdc4` | `0xb000a05c` | MMIO: AFH capability state register 2 |
| `0x8010cdc8` | `0x80011511` | → `FUN_80011510` (ROM, 98B) = BB reg-read fn |
| `0x8010cdcc` | `0xffefffff` | AND mask: clears bit 20 of MMIO1 |
| `0x8010cdd0` | `0xfffbffff` | AND mask: clears bit 18 |
| `0x8010cdd4` | `0xfffdffff` | AND mask: clears bit 17 |
| `0x8010cdd8` | `0xfffeffff` | AND mask: clears bit 16 |
| `0x8010cddc` | `0xff0fffff` | AND mask: clears bits [23:20] of reg |
| `0x8010cde0` | `0x00b00000` | OR mask: sets bits 23, 21, 20 |
| `0x8010cde4` | `0x8010ad89` | → `FUN_8010ad88` (PATCH, 40B) = BB reg 0x104 extractor |
| `0x8010cde8` | `0xfcffffff` | AND mask: clears bits [25:24] |
| `0x8010cdec` | `0xfdffffff` | AND mask: clears bit 25 of MMIO1 |
| `0x8010cdf0` | `0xffdfffff` | AND mask: clears bit 21 |
| `0x8010cdf4` | `0xffbfffff` | AND mask: clears bit 22 |
| `0x8010cdf8` | `0xff7fffff` | AND mask: clears bit 23 |
| `0x8010cdfc` | `0xb000af04` | MMIO: AFH bitmap clear register |
| `0x8010ce00` | `0x01000000` | constant: bit 24 |
| `0x8010ce04` | `0x80011609` | → `FUN_80011608` (ROM, 110B) = BB reg-write fn |
| `0x8010ce08` | `0x80011a75` | → `FUN_80011a74` (ROM, 56B) = BB reg 0xfc mode bit |

**New ROM functions identified:**

| Function | Block | Size | Role |
|----------|-------|------|------|
| `FUN_80011510` | ROM | 98B | BB reg-read: encodes `(param_2 << 27) \| param_1`; polls MMIO until ready |
| `FUN_80011608` | ROM | 110B | BB reg-write: interrupt-protected; polls MMIO; handles 8-bit alignment |
| `FUN_80011a74` | ROM | 56B | Reads BB reg 0xfc; conditionally sets/clears bit[12] based on global state |
| `FUN_800122b8` | ROM | 64B | AFH cap extractor: reads config[0x46-0x47]; extracts 4 bits → `FUN_80012e38` |
| `FUN_80012e38` | ROM | 70B | Clears bits[7:6] of BB regs 0x170, 0x174, 0x178, 0x17c |
| `FUN_800117a4` | ROM | 14B | ORs `0xfc00` into global (AFH upper-channel mask enable) |
| `FUN_8000c3f4` | ROM | 414B | AFH channel map update: checks config flags, updates local/working maps |

All seven are ROM functions — **zero libre reimplementation needed**.

**Libre:** Must implement both `FUN_8010ad88` and `FUN_8010ccb8`.
All mask constants are hardware config data — safe to copy verbatim from the binary.
ROM calls: `FUN_80011510` (read), `FUN_80011608` (write), `FUN_80011a74` (final), `FUN_8010ad88` (4-bit extractor).

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

### Group A + B (15 functions, previously assessed)

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

### Group C (9 functions, newly analyzed 2026-06-08)

| Function | Lines of MIPS16e | ROM calls | MMIO writes | Priority |
|----------|-----------------|-----------|-------------|----------|
| `FUN_8010c09c` | ~15 | 2 | 0 | MEDIUM (capability gate) |
| `FUN_8010bda0` | ~40 | 2 | 0 | MEDIUM (SCO validator) |
| `FUN_8010b4d0` | ~50 | 5 | 0 | MEDIUM (slot trigger; fully resolved) |
| `FUN_80110868` | ~120 | 1 | 0 | HIGH (codec ring buffer) |
| `FUN_8010fa34` | ~60 | 3 | 0 | HIGH (AFH 79-ch merger) |
| `FUN_8010fb08` | ~100 | 4 | 0 | HIGH (BLE 40-ch aggregator) |
| `FUN_8010f950` | ~60 | 5 | 0 | HIGH (AFH trigger) |
| `FUN_8010ce0c` | ~250 | 6 | 3 | HIGH (AFH HW init) |
| `FUN_8010e350` | ~450 | 8 | 0 | CRITICAL (AFH engine) |

### Group D (2 PATCH functions from AFH init chain, 2026-06-08)

| Function | Lines of MIPS16e | ROM calls | MMIO writes | Priority |
|----------|-----------------|-----------|-------------|----------|
| `FUN_8010ad88` | ~15 | 1 | 0 | HIGH (AFH cap bit extractor) |
| `FUN_8010ccb8` | ~100 | 3 | 3 | HIGH (AFH HW reg configurator) |

**Grand total: 24 functions need real MIPS16e code (~1850 insns estimated across all groups).**
All ROM calls go to fixed addresses in the 0x80000000–0x8007ffff range.
