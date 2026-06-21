# Reverse Engineering: `FUN_80047c50` — VSC Parameter Validator and Connection Record Writer

**Runtime address**: `0x80047c50` (ROM — fixed in chip silicon)  
**Memory block**: ROM (no block name from Ghidra, base `0x80000000`)  
**Size**: 700 bytes  
**Reachable via**: `DAT_8010bc64` in the patch's runtime data section → called by `FUN_8010bba4` (the LMP VSC gateway)  
**Source**: Kovah `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`, decompiled via Ghidra 12.1.2 headless  
**Tool used**: `DecompileAddr.java` with `-noanalysis` flag, address `0x80047c50`

---

## Pointer Chain Resolved

`DAT_8010bc64` is at runtime address `0x8010bc64` in the patch's `data [80100000-8013ffff]` block. Raw bytes there: `51 7c 04 80` — MIPS little-endian 32-bit = `0x80047c51`, stripping the MIPS16e ISA mode bit → **`0x80047c50`** in ROM.

This function was NOT installed dynamically by `thing_that_calls_thing_that_installs_LMP_Patch`. It is a **pre-initialized pointer in the patch firmware's static data section** — the patch binary shipped with `0x80047c50` already baked into its data table at offset `0x0000bc64` from the patch base. The patch's `FUN_8010bba4` (the LMP VSC gateway) calls through this table, giving Realtek a single place to swap ROM function pointers per firmware version.

The adjacent pointer slots in the same table:

| Patch data addr | Raw bytes | Resolved address | Kovah name |
|----------------|-----------|-----------------|-----------|
| `0x8010bc64` | `51 7c 04 80` | `0x80047c50` | `FUN_80047c50` (this function) |
| `0x8010bc68` | `05 03 05 80` | `0x80050304` | unknown ROM function |
| `0x8010bc6c` | `65 45 04 80` | `0x80044564` | unknown ROM function |
| `0x8010bc70` | `71 d0 01 80` | `0x8001d070` | **`hci_event_sender`** ← LMP response sent via HCI infra |

The `hci_event_sender` at `0x8001d070` being used for LMP responses confirms that RTL8761BU routes all outbound PDUs through a single packet send path regardless of transport type.

---

## Summary

`FUN_80047c50` is a **28-byte PDU parser, validator, and connection record writer** for the VSC path. It is in ROM — not patched — meaning this logic is fixed in silicon. The patch only wraps it with the gateway (`FUN_8010bba4`).

The function:
1. Reads up to 28 bytes from the incoming LMP/VSC PDU
2. Validates each field against range constraints (returning error code `0x12` on failure)
3. Either finds an existing connection record or allocates a new one
4. Writes validated parameters into ~15 fields of the connection record
5. Optionally calls a secondary hook (`PTR_DAT_80047f18`) for further processing
6. Returns `0x00` on success, `0x07` or `0x0c` or `0x12` on error

The PDU layout being parsed matches an **extended synchronous (eSCO/SCO) connection configuration command** — specifically the kind of parameter block sent during `LMP_eSCO_link_req` negotiation or a Realtek VSC that configures synchronous audio channels.

---

## Annotated Decompilation

```c
byte FUN_80047c50(int param_1, undefined1 *param_2)
// param_1  = pointer to incoming LMP VSC PDU (byte array, at least 28 bytes)
// param_2  = output buffer (1 byte); filled with conn_rec[0x3d] on success
// returns  = status code: 0x00=OK, 0x07=alloc fail, 0x0c=busy, 0x12=invalid param
{
  // ── Locals derived from PDU fields ──────────────────────────────────────
  uint local_38;  // PDU[3]       = LMP transaction handle / link identifier
  uint local_44;  // flags & 0x10 = eSCO-mode flag
  uint local_48;  // flags & 0x20 = extended-eSCO flag
  uint local_58;  // flags & 0x04 = SCO-only flag
  uint local_2c;  // PDU[12]      = non-zero packet count / slots (must be > 0)
  uint local_54;  // PDU[13]      = voice coding / retransmission setting (≤3 unless ext-eSCO)
  uint local_3c;  // PDU[14]      = negotiation mode (≤1)
  uint local_40;  // PDU[21]      = parameter A (≤3 unless SCO-only)
  uint local_24;  // PDU[22]      = signed: power level / attenuation target
  uint local_4c;  // PDU[23] - 1  = parameter B in [0,2] → raw byte in [1,3]
  uint local_28;  // PDU[24]      = parameter C
  uint local_50;  // PDU[25] - 1  = parameter D in [0,2]
  uint local_30;  // PDU[26]      = parameter E (4 bits, ≤15)
  uint local_34;  // PDU[27]      = parameter F (1 bit, ≤1)
  ushort local_20; // PDU[4..5]   = 16-bit flags/options word
  byte local_60[8];  // output buffer (only [0] used for status)

  // ── Phase 1: initialise default error code and zero output ───────────────
  local_60[0] = 0x12;   // default = 18 ("Connection Rejected / Limited Resources")
  *param_2 = 0;

  // ── Phase 2: extract LMP handle and find connection record ───────────────
  local_38 = (uint)PDU[3];  // LMP transaction handle
  int iVar4 = FUN_8004e2d0();
  // FUN_8004e2d0() takes no args — reads from a global "current connection"
  // pointer that FUN_8010bba4 injected into conn_rec[+0x2c] before calling us.
  // Returns: pointer to the per-link connection record, or NULL.

  // ── Phase 3: check "synchronous connection already active" flag ──────────
  if (iVar4 != NULL && (conn_rec[0x1d] & 2) != 0) {
    return 0x0c;  // 12 = "Command Disallowed" — link already configured
  }

  // ── Phase 4: parse 16-bit flags word (PDU bytes 4-5) ────────────────────
  local_20 = *(uint16_t *)(PDU + 4);
  uint flags_lo = local_20 & 0xff;  // low byte of flags
  local_44 = local_20 & 0x10;       // bit 4 = eSCO mode
  local_48 = flags_lo & 0x20;       // bit 5 = extended eSCO
  local_58 = flags_lo & 0x04;       // bit 2 = SCO-only / no BD_ADDR copy

  // ── Phase 5: optional eSCO pre-validation ────────────────────────────────
  if ((local_44 != 0) && (FUN_80044730(PDU, NULL) != 0)) {
    return local_60[0];  // eSCO already rejected by FUN_80044730
  }

  // ── Phase 6: validate 3-byte bandwidth / timing window fields ────────────
  // PDU bytes 6-8 → uVar8 (3-byte LE value, e.g. Tx timing window)
  // PDU bytes 9-11 → uVar7 (3-byte LE value, e.g. Rx timing window)
  uVar8 = PDU[6] | (PDU[7] << 8) | (PDU[8] << 16);
  uVar7 = PDU[9] | (PDU[10] << 8) | (PDU[11] << 16);
  uint max_bw = DAT_80047f0c;   // ROM constant: hardware bandwidth ceiling
  if (uVar7 < uVar8 - 0x20)  return local_60[0];  // Rx window too small vs Tx
  if (uVar7 < 0x20)           return local_60[0];  // Rx below absolute minimum
  if (uVar7 < uVar8)          return local_60[0];  // Rx must be >= Tx

  // ── Phase 7: validate scalar parameters ──────────────────────────────────
  local_2c = PDU[12];
  if (local_2c == 0) return local_60[0];            // packet count must be > 0

  local_54 = PDU[13];   // voice coding / retransmission effort
  if (local_48 == 0 && local_54 > 3) return local_60[0];  // ≤3 unless extended-eSCO

  // PDU[14] = negotiation_mode (complex conditional; must be ≤ 1 in most paths)
  if (... local_3c > 1) return local_60[0];

  local_40 = PDU[21];   // parameter A
  if (local_58 == 0 && local_40 > 3) return local_60[0];

  local_4c = (PDU[23] - 1) & 0xff;
  if (local_4c > 2) return local_60[0];   // PDU[23] must be in range [1, 3]

  local_50 = (PDU[25] - 1) & 0xff;
  if (local_50 > 2) return local_60[0];

  local_30 = PDU[26];
  if (local_30 > 15) return local_60[0];  // 4-bit field

  local_34 = PDU[27];
  if (local_34 > 1) return local_60[0];   // 1-bit field

  // ── Phase 8: find or allocate connection record ───────────────────────────
  if (iVar4 == NULL) {
    // No existing record — allocate a fresh one for this LMP handle
    iVar4 = FUN_8005058c(local_38, local_60);
    // On failure: local_60[0] ≠ 0, so will return error
    if (local_60[0] != 0) return local_60[0];
  } else if (local_44 != 0) {
    // eSCO mode with existing record — check pending state
    if (conn_rec[0x2c] != 0 && (flags_lo & 2) != 0) return local_60[0];
    if (conn_rec[0x2e] != 0) {
      flags_lo &= 1;
      if (flags_lo != 0) return local_60[0];
    }
  }

  // ── Phase 9: write validated parameters into connection record ────────────
  uint mask = DAT_80047f10;   // ROM bitmask for clearing reserved fields in 32-bit words

  conn_rec[0x10] = (byte)local_38;             // LMP handle
  conn_rec[0x11] = (byte)local_2c;             // packet count / slots
  conn_rec[0x14] = (conn_rec[0x14] & mask) | uVar8;  // Tx bandwidth/window (24-bit packed)
  conn_rec[0x18] = (conn_rec[0x18] & mask) | uVar7;  // Rx bandwidth/window
  conn_rec[0x20] = local_20;                   // flags word (16-bit)

  if (local_48 == 0) {
    // Non-extended: pack voice coding into low 3 bits of conn_rec[0x1c]
    conn_rec[0x1c] = (conn_rec[0x1c] & 0xf8) | (local_54 & 7);
  }

  if (local_58 != 0 || local_54 > 1) {
    // SCO-only or multi-packet: copy BD_ADDR from PDU[15..20] into conn_rec[0x46..0x4b]
    optimized_memcpy(conn_rec + 0x46, PDU + 15, 6);
    // Pack negotiation mode into bits [5:3] of conn_rec[0x1c]
    conn_rec[0x1c] = (conn_rec[0x1c] & 0xC7) | ((local_3c & 7) << 3);
  }

  // Pack local_40 into bit 7 and bit 0 of conn_rec[0x1c] and conn_rec[0x1d]:
  conn_rec[0x1c] = (conn_rec[0x1c] & 0x7f) | ((local_40 & 1) << 7);
  conn_rec[0x1d] = (conn_rec[0x1d] & 0xfe) | ((local_40 >> 1) & 1);

  // ── Phase 10: clamp signed power level PDU[22] to ROM maximum ────────────
  char max_power = *PTR_DAT_80047f14;   // ROM pointer to hardware max Tx power byte
  char pdu_power = (char)PDU[22];
  if (pdu_power == 0x7f) {
    conn_rec[0x3d] = max_power;         // 0x7f = "use maximum"
  } else {
    conn_rec[0x3d] = (pdu_power < max_power) ? pdu_power : max_power; // clamp
  }

  // ── Phase 11: optional secondary hook ────────────────────────────────────
  code *hook = *(code **)PTR_DAT_80047f18;
  if (hook != NULL) {
    int hook_result = hook(iVar4, pdu_power, param_2, local_60);
    if (hook_result != 0) {
      // Hook filled in param_2 and local_60[0] itself — use those
      return local_60[0];
    }
  }

  // ── Phase 12: write remaining fields + finalize ───────────────────────────
  *param_2 = conn_rec[0x3d];                        // output = Tx power level
  conn_rec[0x08] = (conn_rec[0x08] & 0x1f) | ((local_4c & 7) << 5);  // pack param B
  conn_rec[0x12] = (byte)local_28;                  // param C
  conn_rec[0x1d] = (conn_rec[0x1d] & 0xe3) | ((local_50 & 7) << 2); // pack param D
  conn_rec[0x3f] = (conn_rec[0x3f] & 0x0f) | (local_30 << 4);       // pack 4-bit param E
  conn_rec[0x1c] = (conn_rec[0x1c] & 0xbf) | ((local_34 & 1) << 6); // pack 1-bit param F

  // ── Phase 13: finalize or re-use eSCO ────────────────────────────────────
  if ((conn_rec[0x20] & 0x10) == 0) {
    // Non-eSCO path: call finalize function
    int rc = FUN_800509b0(iVar4);
    if (rc != 0) {
      local_60[0] = 7;          // 7 = "allocation failure / hardware reject"
      FUN_8004fcec(iVar4);      // cleanup: free the conn record
      return local_60[0];
    }
  } else {
    // eSCO path: re-run FUN_80044730 with the now-populated record
    FUN_80044730(PDU, iVar4);
  }

  local_60[0] = 0;  // success
  return local_60[0];
}
```

---

## PDU Layout Decoded

The function parses a 28-byte extended PDU. Field names are inferred from BT spec LMP synchronous connection parameters:

| Byte(s) | Variable | Constraint | Interpretation |
|---------|----------|-----------|----------------|
| `[3]` | `local_38` | — | LMP transaction handle / link identifier |
| `[4..5]` | `local_20` | — | 16-bit flags word: bit4=eSCO, bit5=ext-eSCO, bit2=SCO-only |
| `[6..8]` | `uVar8` | `≥ 0x20`, `uVar7 ≥ uVar8` | Tx timing window or bandwidth (24-bit LE) |
| `[9..11]` | `uVar7` | `≥ uVar8`, `≥ 0x20`, `≤ max_bw` | Rx timing window or bandwidth (24-bit LE) |
| `[12]` | `local_2c` | `> 0` | Packet slots / count |
| `[13]` | `local_54` | `≤ 3` (non-ext) | Voice coding / retransmission effort |
| `[14]` | `local_3c` | `≤ 1` | Negotiation state |
| `[15..20]` | — | 6 bytes | **BD_ADDR** of remote device (copied to `conn_rec[0x46]`) |
| `[21]` | `local_40` | `≤ 3` (non-SCO) | Codec mode / parameter A |
| `[22]` | `local_24` | clamped to max | Signed Tx power level / attenuation target |
| `[23]` | `local_4c` | raw in `[1,3]` | Parameter B (1-indexed, 2-bit) |
| `[24]` | `local_28` | — | Parameter C |
| `[25]` | `local_50` | raw in `[1,3]` | Parameter D (1-indexed, 2-bit) |
| `[26]` | `local_30` | `≤ 15` | Parameter E (4-bit) |
| `[27]` | `local_34` | `≤ 1` | Parameter F (1-bit) |

The BD_ADDR copy at `conn_rec[0x46]` and the synchronous connection parameters strongly suggest this is an **eSCO/SCO connection configuration command** — either `LMP_eSCO_link_req` (extended LMP opcode 0x3F) or a Realtek vendor-specific variant that uses the same parameter layout.

---

## Connection Record Field Map

Fields written by this function, relative to `iVar4` (the per-link connection state record):

| Offset | Width | Content |
|--------|-------|---------|
| `+0x08` | byte | bits `[7:5]` = param B (`local_4c`) |
| `+0x10` | byte | LMP transaction handle |
| `+0x11` | byte | packet count / slots |
| `+0x12` | byte | param C (`local_28`) |
| `+0x14` | uint32 | Tx bandwidth (masked write with `DAT_80047f10`) |
| `+0x18` | uint32 | Rx bandwidth (masked write) |
| `+0x1c` | byte | packed field: bits[2:0]=voice, bits[5:3]=negotiation, bit[7]=codec-A-hi, bit[6]=param-F |
| `+0x1d` | byte | bits[1:0]=codec-A-lo, bits[4:2]=param-D, bit[1]="already active" (checked, not written here) |
| `+0x20` | uint16 | flags word (direct copy of PDU[4..5]) |
| `+0x3d` | byte | Tx power level (clamped to `*PTR_DAT_80047f14`) |
| `+0x3f` | byte | bits `[7:4]` = param E (`local_30`) |
| `+0x46..+0x4b` | 6 bytes | BD_ADDR of remote device |

---

## ROM Constants and Hooks

| Address | Symbol | Role |
|---------|--------|------|
| `0x80047f0c` | `DAT_80047f0c` | Max bandwidth ceiling constant |
| `0x80047f10` | `DAT_80047f10` | Bitmask for clearing reserved bits in 32-bit word writes |
| `0x80047f14` | `PTR_DAT_80047f14` | Pointer to hardware max Tx power byte |
| `0x80047f18` | `PTR_DAT_80047f18` | Optional secondary hook pointer: `fn(conn_rec, power, out_buf, status_buf)` |

`PTR_DAT_80047f18` is the most interesting: if non-NULL, it intercepts the power-level logic and can override `param_2` and the return code. This is a ROM-level extension point — if the patch wanted to override this specific behavior without replacing the whole function, it would write a handler address here.

---

## Supporting ROM Functions Called

| Address | Call site | Inferred role |
|---------|-----------|--------------|
| `FUN_8004e2d0` | Phase 2 | Get current connection record from global (reads injected ptr from `FUN_8010bba4`) |
| `FUN_80044730` | Phase 5, 13 | eSCO pre-validation / eSCO state update |
| `FUN_8005058c` | Phase 8 | Allocate fresh per-link connection record for `local_38` handle |
| `optimized_memcpy` (`0x8000e85c`) | Phase 9 | Copy BD_ADDR from PDU into conn record |
| `FUN_800509b0` | Phase 13 | Finalize connection (hardware commit); returns non-zero on HW reject |
| `FUN_8004fcec` | Phase 13 | Cleanup / free connection record on failure |

---

## Error Code Summary

| Code | Meaning | Condition |
|------|---------|-----------|
| `0x07` | Hardware reject | `FUN_800509b0` returned non-zero |
| `0x0c` | Command Disallowed | Link already active (`conn_rec[0x1d] & 2`) |
| `0x12` | Connection Rejected | Any parameter validation failure (default) |
| `0x00` | Success | All validations passed, conn record written |

---

## Full VSC Call Chain — Completed

```
ROM: LMP__268__most_common_for_VSCs2_checks_fptr_patch (0x80009a6c)
  └─ checks fptr at bos_base+0xd8
       └─ FUN_8010bba4 (0x8010bba4) [PATCH — gateway]
            ├─ Resource alloc     via *DAT_8010bc54  (0x80050304 ROM)
            ├─ Connection lookup  via *DAT_8010bc5c  (0x80050304 ROM)
            ├─ Link-record lookup via *DAT_8010bc60  (0x80044564 ROM)
            ├─ VSC dispatch       via *DAT_8010bc64 ──► FUN_80047c50 (0x80047c50 ROM) [THIS DOC]
            │     ├─ Conn record lookup   FUN_8004e2d0 (ROM)
            │     ├─ eSCO validation      FUN_80044730 (ROM)
            │     ├─ Conn record alloc    FUN_8005058c (ROM)
            │     ├─ BD_ADDR copy         optimized_memcpy (0x8000e85c ROM)
            │     ├─ HW finalize          FUN_800509b0 (ROM)
            │     ├─ Conn record free     FUN_8004fcec (ROM)
            │     └─ Secondary hook       *PTR_DAT_80047f18 (optional)
            ├─ Post-dispatch cleanup via *DAT_8010bc68  (ROM)
            ├─ Response PDU build   via *DAT_8010bc6c  (0x80044564 ROM)
            └─ LMP PDU send         via *DAT_8010bc70 = hci_event_sender (0x8001d070 ROM)
```

---

## Implications for the Libre Replacement

**Good news**: `FUN_80047c50` is entirely in ROM and handles all the hard validation and connection state writing. A libre patch does **not** need to re-implement this function — it just needs to call it (the patch data table at `0x8010bc64` already points to it).

**What the libre patch must implement**:
1. `FUN_8010bba4` (LMP VSC gateway) — 176 bytes, documented in `reverse_engineering_lmp_vsc_hook.md`
2. The pre-initialized data table at `0x8010bc54`–`0x8010bc74` with the correct ROM function pointers for the target chip version

**Connection record layout**: at minimum offsets `+0x08`, `+0x10`–`+0x12`, `+0x14`, `+0x18`, `+0x1c`–`+0x1d`, `+0x20`, `+0x3d`, `+0x3f`, `+0x46`–`+0x4b` are actively written. The `+0x1d` bit 1 ("already active") flag must be properly maintained to prevent double-configuration.

**The BD_ADDR at `conn_rec+0x46`** is written from the PDU only when `local_58 != 0 || local_54 > 1`. Other code paths may leave it stale from a previous connection — a libre implementation of the connection allocator (`FUN_8005058c`) needs to zero-initialize the record including `+0x46`.

**The `PTR_DAT_80047f18` hook** at ROM address `0x80047f18` is currently NULL in Kovah's analysis. If it is populated in a future firmware version, it intercepts power-level clamping — worth watching.

**Opcode routing**: This function is the *only* target of `DAT_8010bc64`; it does not dispatch by opcode. Vendor LMP opcode **0x268** reaches it via `LMP__268` → `FUN_8010bba4` → `*DAT_8010bc64`. Full LMP + HCI VSC opcode tables: `reverse_engineering_lmp_vsc_opcode_map.md`.

**Next steps**: decompile `FUN_80044730` (eSCO validation, called twice) and `FUN_8005058c` (connection record allocator) to complete the picture of synchronous connection setup.
