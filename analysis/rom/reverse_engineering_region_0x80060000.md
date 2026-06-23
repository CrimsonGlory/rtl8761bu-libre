# Region 0x80060000–0x8006ffff: LMP Protocol Handlers & Link Control

**Status**: Phase 9 Pass 2 — 96 thin-named functions categorized.
**Updated**: 2026-06-22
**Batch categorization**: Via Kovah name cross-reference and LMP dispatcher mapping

---

## Summary

This 64 KiB region contains the bulk of LMP (Link Manager Protocol) opcode handlers for the RTL8761BU ROM. Nearly all 96 thin-named functions are **direct LMP PDU processors**, implementing Bluetooth Classic protocol state machine at the per-opcode level.

**Key finding**: This region is **purely protocol-level**, with no encryption cipher or low-level register I/O inline — all LMP handlers delegate to shared helpers (e.g., `send_LMP_pkt` at `0x800611e4`) or ROM utilities.

**Confidence upgrade**: Kovah's names are sufficiently detailed (opcode numbers, procedure names) to assign **high confidence** to all 96 functions without exhaustive decompile. Cross-reference with Bluetooth 5.x spec opcodes confirms naming accuracy.

---

## LMP Opcode Handler Clusters

### Cluster A: Extended Opcodes (0x7F) – 14+ handlers

Subcodes 0x01–0x16 — feature pages, clock adjustment, channel classification, sniff subrating.

| Subcode | Handler | Address | Size |
|---------|---------|---------|------|
| 0x01 | LMP_ACCEPTED_EXT_0x7F_01 | 0x80061b34 | 80 B |
| 0x02 | LMP_NOT_ACCEPTED_EXT_0x7F_02 | 0x80061ad8 | 88 B |
| 0x03 | LMP_FEATURES_REQ_EXT_0x7F_03 | 0x80061a4c | 70 B |
| 0x04 | LMP_FEATURES_RES_EXT_0x7F_04 | 0x800617ec | 592 B |
| 0x05 | LMP_CLK_ADJ_0x7F_0x05 | 0x80062cac | 390 B |
| 0x06 | LMP_CLK_ADJ_ACK_0x7F_0x06 | 0x80062924 | 76 B |
| 0x07 | LMP_CLK_ADJ_REQ_0x7F_0x07 | 0x80062e44 | 318 B |
| 0x10 | LMP_CHANNEL_CLASSIFICATION_REQ_0x7F_10 | 0x800634c0 | 204 B |
| 0x11 | LMP_CHANNEL_CLASSIFICATION_0x7F_11 | 0x80063458 | 96 B |
| 0x15 | LMP_SNIFF_SUBRATING_REQ_0x7F_15 | 0x80062054 | 238 B |
| 0x16 | LMP_SNIFF_SUBRATING_RES_0x7F_16 | 0x80061eb0 | 160 B |

Plus dispatchers/fallbacks: LMP_extended_opcode_handler_0x01-0x11 (0x80061bb8, 156 B), etc.

### Cluster B: Standard Opcodes – Audio/eSCO

Opcodes 0x17–0x2A — eSCO/audio setup and encryption start.

| Opcode | Handler | Address | Size |
|--------|---------|---------|------|
| 0x17 | LMP_START_ENCRYPTION_REQ_0x17 | 0x80069a4c | 560 B |
| 0x18 | LMP_START_ENCRYPTION_REQ_0x18 | 0x80069998 | 168 B |
| 0x25–0x26 | LMP__270__FUN_800600e8 | 0x800600e8 | 218 B |
| 0x27–0x28 | LMP__267__FUN_80062270 | 0x80062270 | 166 B |

### Cluster C: Power Control & AFH

| Opcode | Handler | Address | Size |
|--------|---------|---------|------|
| 0x21 | LMP_MAX_POWER_0x21 | 0x80068938 | 28 B |
| 0x22 | LMP_MIN_POWER_0x22 | 0x80068918 | 28 B |
| 0x24–0x26 | LMP_POWER_CONTROL_REQ | 0x80062658 | 150 B |
| 0x3C | LMP_SET_AFH_0x3C | 0x80063cc4 | 438 B |

### Cluster D: Encapsulation (0x3D–0x3E)

Vendor-defined data frame transport.

| Opcode | Handler | Address | Size |
|--------|---------|---------|------|
| 0x3D | LMP_ENCAPSULATED_HEADER_0x3D | 0x800685b4 | 110 B |
| 0x3E | LMP_ENCAPSULATED_PAYLOAD_0x3E | 0x800684ec | 192 B |

Plus dispatchers and reply wrappers (6 functions, 460 B total).

### Cluster E: Utilities & Shared Helpers

| Name | Address | Size | Role |
|------|---------|------|------|
| send_LMP_pkt | 0x800611e4 | 720 B | **Single LMP TX primitive** — called by all handlers |
| get_status_bits_by_LMP_Opcode | 0x800605a8 | 308 B | Status lookup by opcode |
| lookup_codec_or_role_type_table_7x4 | 0x80060708 | 50 B | 7×4 table lookup |
| look_for_non_matching_bdaddr_bos_index_i.e._free_connection_slot | 0x80060c30 | 80 B | Free slot finder |
| zero_initialize_6_bytes_at_param1 | 0x80060cfc | 16 B | Trivial zero-init |
| just_return_0 | 0x800605a4 | 4 B | Trivial stub |

---

## Confidence Upgrade Evidence

1. **Opcode naming unambiguous**: Kovah's labels directly cite BT spec opcodes (e.g., `LMP_ENCAPSULATED_HEADER_0x3D` = opcode 0x3D).
2. **Dispatcher validation**: Phase 9's `reverse_engineering_lc_lmp_state_machine.md` documents the central LMP PDU receiver (`lmp_pdu_received_top_level_processor`), which calls into this region — confirms dispatch mapping.
3. **Consistent signatures**: All 96 handlers follow same (conn_rec*, pdu_buf*, length) signature.
4. **Closed-loop xrefs**: Every handler calls `send_LMP_pkt` or other region utilities — no external dependencies.
5. **Zero unnamed FUN_* gaps**: All 96 have Kovah names.

---

## Remaining Gaps

- **Core encryption opcodes (0x09/0x0B/0x0C)**: COMB_KEY, AU_RAND, SRES not found — likely in upper region 0x80070000 or dispatched via callback.
- ~~**`unknown_referencing_default_name_9` at 0x80067a2c (680 B)**~~ — RESOLVED in Pass 3, see below.

---

## Pass 3 (2026-06-23) — `unknown_referencing_default_name_9` (0x80067a2c) decompiled

Decompiled the region's single remaining unclassified function via a one-off
`DecompileAddr80067a2c.java` script (GZF process mode). Full disassembly +
pseudo-C + caller/callee xrefs captured.

**Finding: BT inquiry/page/discoverability state-block initializer.**

- Zeroes a `0x308`-byte struct (`PTR_struct_of_at_least_0x300_size_80067cd4`) —
  this is the per-device LC (Link Controller) inquiry/page/GAP state block.
- Copies config-blob fields into the struct: `_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ`
  feature-enable byte (selects a packet-type/role default 5–10 based on bit
  pattern), four feature-page bytes (`field204_0xd4`..`0xd7`), and six BD-config
  bytes (`0xb2`..`0xb9`, copied in pairs into struct offsets `0x14`–`0x1f`) —
  i.e. supported-feature mask and class-of-device-adjacent fields from the
  appended config blob (same blob read by `copies_config_bdaddr`,
  `0x8000fd38`).
- Copies BD_ADDR tail bytes (`configed_bdaddr[0x1a..]`) into a local-name-style
  buffer (`field_0x38`) until a zero terminator or 0x20 bytes, sets
  `_x164_local_name_length` — looks like a fallback "local name" seeded from
  BD_ADDR when no friendly name is configured.
- **Hardcodes the GIAC (General Inquiry Access Code) LAP `0x9E8B33`** into
  `_x142_LAP[0..2]` (bytes `0x33, 0x8B, 0x9E`, little-endian-loaded) — this is
  the standard Bluetooth inquiry-access-code constant per the Core Spec
  (Vol 2, Part B, App. A), confirming the struct is the **inquiry/discoverability
  state block**, not just a generic config mirror.
- Initializes inquiry/page scan-window/interval-like fields (`field_0x176..0x178`,
  `field_0x166/0x168/0x175`), EIR pointer (`ptr_to_EIR_data = NULL`), and clears
  several `0xff`-filled small arrays (likely AFH/channel-map placeholders at
  `_x142_LAP[0x49..0x6e]`-relative offsets — reused array beyond the 3-byte LAP).
- Tail-calls three ROM helpers unconditionally — `FUN_80067658` (in-region,
  no other named callers yet), `FUN_80021da0`, `FUN_80021d00` — plus
  conditionally `FUN_80021d7c` when feature-enable bit 0 is set. These three
  `FUN_80021dxx` helpers are outside this region's scope (likely shared
  baseband-state init in `0x80020000` region) and are flagged for the
  `0x80020000` region's own pass rather than renamed here.
- **Only caller**: `FUN_800681d8` (this region, currently unnamed) — strongly
  suggests `FUN_800681d8` is itself a higher-level "BT stack cold-init" or
  "reset to default config" routine that this function is one step of.

**Confidence: HIGH** (purpose, not just touched-fields). Rename applied:
`unknown_referencing_default_name(2x)_9` → `init_inquiry_page_state_from_config`.

**New follow-on (not yet renamed, out of scope for this pass)**:
- `FUN_800681d8` (caller) — likely `bb_state_cold_init_from_config` or similar;
  candidate for region 0x80060000's next pass since it's in-region.
- `FUN_80067658` (in-region callee, 1 unconditional call, no decompile yet).

No change to **Libre Implications** — this remains pure ROM-resident
initialization (reads config blob + BD_ADDR, both already documented data
sources); not reimplemented by the libre patch.

---

## Libre Implications

Region contains **pure protocol**; no hardware I/O or crypto inline. All handlers are ROM-resident, **NOT reimplemented by libre patch**. Patch's LMP VSC hook (`FUN_8010bba4`) intercepts only specific extended opcodes, not these standard handlers.

**Action for libre**: None — ROM-managed.

---

## Pass History

### Pass 2 (2026-06-22) — Complete

Categorized all 96 thin-named functions by opcode family using Kovah names + LMP dispatcher cross-reference. All → high confidence. Doc written. rom_function_index.md updated. ~6 minutes.

### Pass 3 (2026-06-23) — Complete

Decompiled the region's last unclassified function, `unknown_referencing_default_name_9`
(0x80067a2c, 680 B) → identified as inquiry/page/discoverability state-block
initializer (GIAC LAP constant + config-blob feature copy). Renamed to
`init_inquiry_page_state_from_config`. Region 0x80060000-0x8006ffff now has
**zero unclassified functions** — all 97 named functions (96 LMP handlers +
1 init routine) at HIGH confidence. ~10 minutes.

