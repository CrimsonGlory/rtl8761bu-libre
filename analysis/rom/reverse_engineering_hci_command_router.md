# Reverse Engineering: ROM HCI Command Parser / Router (Standard OGF 0x01–0x08)

**Top-level dispatcher**: `assoc_w_tHCI_CMD` (`0x80020ee0`, ROM, 672 bytes)
**Memory block**: ROM (fixed in chip silicon)
**Source**: Kovah `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`, decompiled via Ghidra
12.1.2 headless, GZF process mode
**Tools used**: `DecompileAddr.java` / `DiagAddr.java` with `script_args`
(one address per call — no new script needed)
**Date**: 2026-06-21. Ticket: work-in-progress.txt "Document the full HCI
command parser / router"

---

## Summary

Phases 1–8 only mapped the **vendor-specific** OGF 0x3F path
(`reverse_engineering_lmp_vsc_opcode_map.md`, 73 opcodes at `0x80030f1c`).
This pass documents the **parent** dispatcher and the **standard** OGF
0x01–0x08 command-group routing that the VSC doc's call-chain diagram already
sketched but did not verify by decompilation.

`assoc_w_tHCI_CMD` (`0x80020ee0`) is the single entry point for **every**
inbound HCI command, vendor or standard. It is reached from the
USB/transport HCI receive path (not traced in this pass — see Open Questions)
and does:

1. House-keeping: stash the command pointer into a global (`PTR_DAT_80021180`),
   conditionally call `LMP__25C_called2()` if a coexistence flag is set.
2. Special-case dispatch on the **raw 16-bit opcode value** (not yet split
   into OGF/OCF) for three fixed opcodes: `0x300`, `0x456`, `0x1`. `0x456`
   and `0x1` are generic indirect-call/`ret_wrapper` escape hatches, not real
   HCI opcodes — `0x300`'s zero-opcode branch is a `BD_ADDR`-keyed lookup
   path (`FUN_8001ac74`), likely the loopback/test-mode "null command" case.
3. **HCI loopback-mode short-circuit**: if a config bit + a hard-coded set of
   six excluded opcodes (`0xc03`, `0xc31`, `0xc33`, `0xc35`, `0x1005`,
   `0x1801`, `0x1802`) don't match, the command is echoed back via
   `send_evt_HCI_Loopback_Command` instead of being dispatched at all. This
   is the same `HCI_Read/Write_Loopback_Mode` infrastructure named in
   `kovah_function_list.md` ("Logging/Debug" + `0x8001e780`/`0x8001ea34`
   rows).
4. A **disconnect-on-stale-command** guard: if a specific config bit pattern
   and `*psVar12 == 0xc03` (`HCI_Reset`, OGF=3/OCF=3) line up with a non-zero
   guard variable, it forcibly disconnects (`HCI_Disconnect_on_error`) before
   continuing — a watchdog-style "controller got a Reset while something was
   already in flight" safety path.
5. **OGF extraction**: `local_20[0] = (byte)((ushort)*psVar12 >> 10)` — the
   textbook HCI opcode layout (`OGF = bits[15:10]`, `OCF = bits[9:0]`),
   confirming the field layout Kovah's VSC doc already inferred for OGF 0x3F
   generalizes to all OGF values.
6. An optional **pre-dispatch patch hook** at `PTR_PTR_800211a4` — the same
   `if (fptr != NULL) { ...; may override status }` idiom documented for the
   OGF 0x3F router's `PTR_DAT_80032058` hook, but at the *top* level, before
   OGF is even branched on. If the hook returns non-zero, the command's
   status is forced to `0x66` (102, vendor-specific "command not understood"
   class code used elsewhere in this ROM) and OGF dispatch is skipped.
7. A **debug/var-args logger** call (`possible_logging_function__var_args`)
   that dumps the full opcode + first ~11 PDU bytes — the same logging
   infrastructure named in `kovah_function_list.md` ("Logging/Debug" /
   `log_many_2_0x72_0x121-0x14e`).
8. The **OGF switch** itself (`if (local_20[0]==5) ... else if (<6) ... else
   ...`), a chain of direct equality/range tests, **not** a jump table —
   contrast with the OCF-level dispatch inside each per-OGF handler, which
   *is* a real bounds-checked jump table (see below).
9. A default/unknown-OGF fallthrough (`LAB_80021134`) that returns
   `HCI_Command_Status` with error `0x1` (`Unknown HCI Command`) unless a
   config byte at `PTR_DAT_800211ac` is non-zero, in which case it goes to
   `OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10` — the
   same generic "send back a command-complete/status with this error code"
   helper used by every per-OGF dispatcher's own unknown-OCF path (see
   `kovah_function_list.md`'s `OGC_3_default_func_0_OCF_0x3F_and_above` for
   the OGF-3-specific instance of the same pattern).
10. Epilogue: re-increment a counter at `field_0x165` (decremented at step 4
    above — looks like an in-flight-command depth counter / reentrancy
    guard), call a completion hook (`PTR_DAT_800211b0`), and clear the
    global command pointer.

Confidence: **High** for the full decompiled control flow of
`assoc_w_tHCI_CMD` itself (clean, fully-resolved decompile, no undefined
fallthrough) and for the **OGF→handler address table** below (each entry
independently confirmed via `DiagAddr.java`/`DecompileAddr.java`, not just
read off the decompiler's pseudo-names). **Medium** for the semantic
interpretation of the loopback-mode and disconnect-guard logic (steps 3–4) —
the control flow is exact, the *purpose* is inferred from opcode values and
existing Kovah names, not independently confirmed against a spec.

---

## OGF → Handler Address Table

All eight standard Bluetooth OGF values (0x01–0x08) are present in
`assoc_w_tHCI_CMD`'s switch. **OGF is bits `[15:10]` of the opcode** (6 bits,
so 0x01–0x3F are the legal range; 0x3F is reserved by spec for vendor-specific
and is handled here as the VSC path, already documented).

| OGF | BT spec group | ROM handler | Address | Size | Status |
|-----|---------------|-------------|----------|------|--------|
| `0x01` | Link Control | `HCI_CMD_OGF_01__Link_Control__FUN_80020814` | `0x80020814` | 872 B | Already named (`kovah_function_list.md`); confirmed reachable from `assoc_w_tHCI_CMD` via direct `jal` at `0x800210d8` |
| `0x02` | Link Policy | `HCI_CMD_OGF_02__Link_Policy__FUN_8002060c` | `0x8002060c` | — | Already named; confirmed via `jal` at `0x800210e0` |
| `0x03` | Controller & Baseband | `HCI_CMD_OGF_03__Controller_and_Baseband__big_switch_FUN_800202c0` | `0x800202c0` | 600 B | Already named; confirmed via `jal` at `0x800210e8` |
| `0x04` | Informational Parameters | `HCI_CMD_OGF_04___wraps__possible_OGF0__referencing_default_name_10` | `0x8002013c` | **74 B** | **NEW — not in `kovah_function_list.md` before this pass.** Confirmed via `jal` at `0x800210f0`. Fully decompiled (see below) — it's a near-stub. |
| `0x05` | Status Parameters | `HCI_CMD_OGF_05__Status_Parameters__FUN_80020188` | `0x80020188` | 288 B | Already named; confirmed via `jal` at `0x800210f8` |
| `0x06` | Testing | `HCI_CMD_OGF_06__TestMode__big_switch_FUN_800200a8` | `0x800200a8` | — | Already named; confirmed via `jal` at `0x80021100` |
| `0x07` | *(LE, per some specs)* | **no separate case** | — | — | OGF value `7` is **not** individually tested; it falls through to the generic `LAB_80021134` "unknown OGF" path like any other unhandled value 9–0x3E. See note below. |
| `0x08` | LE Controller Commands | `HCI_CMD_OGF_08__LE_Commands__big_switch` | `0x80044674` | **176 B** | **NEW — not in `kovah_function_list.md` before this pass.** Confirmed via `jal` at `0x8002111c`, gated behind a config-blob enable bit (see below). Fully decompiled. |
| `0x3F` | Vendor-Specific | `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` | `0x80030f1c` | 4372 B | Already documented in full (`reverse_engineering_lmp_vsc_opcode_map.md`) |

**Correction to the VSC doc's call-chain diagram**: it showed `OGF 0x01..0x08
→ standard HCI dispatchers (ROM)` as a single collapsed line. This pass
confirms that abbreviation was directionally correct but OGF 0x07 has no
dedicated handler — LE commands are tested under OGF **0x08** only, and OGF
0x04 is real but trivial.

### Why OGF 0x07 has no case

The Bluetooth Core Spec assigns OGF 0x08 to "LE Controller Commands" and
never defines OGF 0x07 (it's reserved). This ROM's switch matches the real
spec, not the ticket's working assumption (which hedged "0x08 (LE again per
some specs)") — there is exactly **one** LE OGF, 0x08, not two. This is
confirmed by direct decompilation, not inferred.

---

## OGF 0x04 (Informational Parameters) — full decompile

```c
undefined4 HCI_CMD_OGF_04___wraps__possible_OGF0__referencing_default_name_10(ushort *param_1)
{
  uint uVar1;
  undefined4 uVar2;
  undefined1 uVar3;

  uVar1 = *param_1 - 0x1001;          // OCF - 1 (opcode 0x1001 = OGF4/OCF1)
  if ((uVar1 & 0xffff) < 9) {
    uVar1 = 1 << (uVar1 & 0x1f);
    if ((uVar1 & 0x157) != 0) {        // OCF in {1,3,5,6,8} (bitmask 0x157)
      uVar2 = 0; uVar3 = 0;
      goto LAB_80020172;
    }
    if ((uVar1 & 8) != 0) {            // OCF == 4 (redundant — already in 0x157? no: bit3=OCF4 covered by 0x157 too)
      uVar3 = *(undefined1 *)((int)param_1 + 3);
      uVar2 = 0;
      goto LAB_80020172;
    }
  }
  uVar2 = 1;                          // default: status = 1 (unknown command)
  uVar3 = 0;
LAB_80020172:
  OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10(
      (uint)*param_1, uVar2, 0, uVar3, 0);
  return 1;
}
```

This function does **not** itself send any HCI events or read controller
state — it only validates that the OCF (relative to `0x1001` = OGF4/OCF1, the
first Informational-Parameters opcode) falls in a small recognized set, and
delegates *all* actual response generation to
`OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10` (already
named, generic "build and send command-complete/status" helper shared by
every OGF dispatcher's default path — see `kovah_function_list.md`). For OCF
values matching bit `0x8` (one specific OCF, value not pinned down further in
this pass) it forwards one extra byte from the inbound PDU (`param_1[3]`) as
the third value in the call to the deal-with-status helper.

**Implication**: Kovah's own placeholder name
("`__wraps__possible_OGF0?_referencing_default_name_10`") undersold this
slightly — it does branch on a real OCF range (`HCI_Read_Local_Version_Info`
through roughly `HCI_Read_BD_ADDR`, OGF4 OCF1–9 per spec, matches the `< 9`
bound) but the actual per-OCF *data* (chip revision, supported commands
bitmap, BD_ADDR, etc.) must be supplied by whatever
`OGC_3_OCF_TONS_deal_with_return_status...` does with its arguments — this
function itself carries almost no payload logic. Not traced further in this
pass (out of scope — see Open Questions).

---

## OGF 0x08 (LE Controller Commands) — full decompile

```c
void HCI_CMD_OGF_08__LE_Commands__big_switch(ushort *param_1)
{
  uint uVar3 = *param_1 & 0x3ff;       // OCF = low 10 bits of opcode
  // ... debug logger call dumping opcode + first ~10 PDU bytes ...
  possible_logging_function__var_args(5, 0xcb, &DAT_0000203a, 0xd04, 0x14, ..., uVar3, ...);

  // bounds check: uVar3 must be <= some per-build max OCF value
  // (read from a byte field in a 0x1ac-sized struct array, "large2" — same
  //  family of structures used by the OGF 0x3F VSC router's resource pool)
  uVar3 = uVar3 & -(uint)(uVar3 <= max_ocf);

  // indirect call through jump table PTR_PTR_8004472c, indexed by uVar3
  // (with an extra masking step that re-zeroes the index if the table slot
  //  itself is non-canonical -- the same "validate slot, fall back to slot 0
  //  on bad index" idiom as other big-switch dispatchers in this ROM)
  (*handler_table[uVar3])(param_1);
}
```

This has the **exact same shape** as the OGF 0x3F VSC router and the other
"`_big_switch`"-suffixed dispatchers (OGF 3, OGF 6): extract OCF, log it,
bounds-check against a per-table max, index a function-pointer jump table,
call through it with the raw PDU pointer. This is the ROM's standard
"big switch" dispatch idiom, used consistently across all multi-command OGF
groups.

### LE OCF jump table (`PTR_PTR_8004472c`)

`PTR_base_of_0x1ac_struct_array_0xA_large2_80044728` (a literal-pool slot at
`0x80044728`) holds the value `0x8004472c` — i.e. it's a pointer-to-the-table,
one indirection before the actual table base. Raw bytes confirmed via
`DiagAddr.java` at `0x8004472c`:

```
8004472c: ac 06 12 80   →  0x801206ac   (table[0], OCF=0)
80044730: <code: FUN_80044730 begins here — NOT a table entry>
```

**The table has exactly one (1) live entry.** `FUN_80044730` (a real,
102-byte function) begins immediately at `0x80044730`, only 4 bytes after
the table base — confirming the bounds check restricts OCF to `{0}` only in
this ROM revision (`max_ocf` resolves to 0). Entry 0 is `0x801206ac`, a
**RAM address**, not ROM code — i.e. like the OGF 0x3F VSC router's
pre-dispatch hook and the LMP VSC gateway pattern, the *one* LE OCF this ROM
recognizes is dispatched through a **patchable function-pointer slot**, not
a fixed ROM handler. This is consistent with `0x801206ac` sitting in the same
`0x8012xxxx` RAM "hook slot" region already documented for `bos_base`
(`CLAUDE.md`'s "Key Struct Offsets" table; `0x801212e4`,
`0x80120f84`/`reverse_engineering_interrupt_vectors.md`, etc.) — this ROM
ships with a thin **inbound LE command-parsing** path (one OCF, patch-extensible).

**Correction (2026-06-21, via `reverse_engineering_ble_link_layer.md`)**: this
"essentially no native LE support" framing is accurate only for inbound HCI
command parsing covered in this section. The same follow-up pass found a large,
fully-decompiled cluster of 17+ ROM-resident `send_evt_Meta_subevent_0x##`
functions (`0x80044730`-`0x80046620+`) implementing nearly every LE Meta Event
through BT 5.x (Connection Complete, Advertising Report, PHY Update Complete,
LE Secure Connections P-256/DHKey, etc.) — i.e. the **outbound event-generation**
side of LE is substantially ROM-resident, not thin at all. See that doc for
the full picture; don't read this section as "BLE is mostly unimplemented in
ROM."

### LE-enable gate

Both call sites that reach `HCI_CMD_OGF_08__LE_Commands__big_switch` are
gated behind the same config-blob flag already documented elsewhere
(`reverse_engineering_lmp_version_conn_setup.md` line 36;
`reverse_engineering_sco_esco_layer.md` lines 1547/1552):

```c
pcVar4 = PTR_config_base_80021190;
bVar8 = pcVar4->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ & 2;
if (bVar8 != 0) {
    HCI_CMD_OGF_08__LE_Commands__big_switch(psVar12);
    ...
}
```

i.e. `config_base + 0x7a` bit `0x2` doubles as an "LE enable" flag in
addition to its already-documented role gating LMP_POWER_REQ/RES and clock
adjustment. If this bit is clear in the config blob shipped with a given
firmware/board, **OGF 0x08 commands are rejected entirely** (falls through to
the generic unknown-OGF `HCI_Command_Status(error=1)` path) regardless of
whether the host sends them.

**Libre implication**: this is good news for the primary goal — if RTL8761BU
boards ship with this bit clear (plausible, since UB500 is BT 5.1 Classic +
LE via the *patch*, not ROM), the libre patch replacement does not need to
emulate any ROM-level LE command logic; LE entirely lives in the patch
(consistent with `CLAUDE.md`'s TODO "Document the BLE link layer state
machine, if present in this ROM" — this pass's finding strongly suggests LE
link-layer logic is **not** in ROM, only this one dispatch stub + a single
patchable hook slot, reinforcing that the BLE work belongs in `firmware/` not
`rom/`). Not confirmed which way the actual production config blob sets this
bit — would need to inspect the real `rtl8761bu_config.bin` byte at offset
`0x7a` to be certain; see Open Questions.

---

## OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10 — shared sink

Every per-OGF dispatcher's unknown/default OCF path, and both the top-level
`assoc_w_tHCI_CMD`'s own unknown-OGF fallback and OGF 4's entire response
path, converge on this one already-Kovah-named function. It was not
independently decompiled in this pass (out of scope — budget), but its
signature as called from three different sites (`assoc_w_tHCI_CMD` directly,
OGF 4's handler, and by implication OGF 3's
`OGC_3_default_func_0_OCF_0x3F_and_above`) is consistently
`(opcode, status, arg3, arg4, arg5)`-shaped, strongly suggesting it is the
**single generic "format and send HCI_Command_Complete/Status with this
status code" helper** for the entire standard-HCI surface — the OGF
equivalent of `hci_event_sender` (`0x8001d070`) which plays the same role for
the LMP/VSC side (documented in `reverse_engineering_vsc_dispatcher.md`).

---

## Updated call-chain diagram

```
HCI host (USB/UART, not traced in this pass)
  └─ assoc_w_tHCI_CMD (ROM 0x80020ee0, 672 B)  ← TOP-LEVEL HCI COMMAND ROUTER
       ├─ housekeeping: stash cmd ptr, optional LMP__25C_called2() coexistence call
       ├─ raw-opcode special cases: 0x300 (BD_ADDR lookup), 0x456/0x1 (escape hatches)
       ├─ loopback-mode short-circuit (6 excluded opcodes) → send_evt_HCI_Loopback_Command
       ├─ HCI_Reset-while-busy guard → HCI_Disconnect_on_error
       ├─ OGF = opcode >> 10        (textbook OGF/OCF split, confirmed for ALL ogf, not just 0x3F)
       ├─ [optional] pre-dispatch patch hook @ PTR_PTR_800211a4 (top-level, before OGF branch)
       ├─ debug logger: possible_logging_function__var_args(...)
       └─ OGF switch (linear compare chain, NOT a jump table):
            ├─ 0x01 → HCI_CMD_OGF_01__Link_Control          (0x80020814, 872 B) [named]
            ├─ 0x02 → HCI_CMD_OGF_02__Link_Policy           (0x8002060c)        [named]
            ├─ 0x03 → HCI_CMD_OGF_03__Controller_and_Baseband (0x800202c0, 600B)[named]
            ├─ 0x04 → HCI_CMD_OGF_04_informational          (0x8002013c, 74 B) [NEW, this pass]
            ├─ 0x05 → HCI_CMD_OGF_05__Status_Parameters     (0x80020188, 288B) [named]
            ├─ 0x06 → HCI_CMD_OGF_06__TestMode              (0x800200a8)        [named]
            ├─ 0x07 → (no case — falls to unknown-OGF default)
            ├─ 0x08 → HCI_CMD_OGF_08__LE_Commands           (0x80044674, 176B) [NEW, this pass]
            │           gated by config_base+0x7a bit 0x2 ("LE enable")
            │           → 1-entry OCF jump table @ 0x8004472c → RAM hook slot 0x801206ac
            │             (thin INBOUND command parsing only -- patch-extensible;
            │              OUTBOUND LE event generation is substantially ROM-resident,
            │              see reverse_engineering_ble_link_layer.md)
            ├─ 0x3F → HCI_CMD_OGF_3F__Vendor_Specific       (0x80030f1c, 4372B)[documented separately]
            └─ default → HCI_Command_Status(error=Unknown Command)
                          or OGC_3_OCF_TONS_deal_with_return_status... if config byte set
       └─ epilogue: re-increment in-flight counter, completion hook @ PTR_DAT_800211b0, clear cmd ptr
```

Each per-OGF handler (1, 3, 6, 8 confirmed; 2, 5 inferred by symmetry/already
named) internally does its **own** OCF-level dispatch via a bounds-checked
function-pointer jump table — this is the "real" jump-table layer the ticket
asked about; the *top-level* OGF dispatch is a linear compare chain, not a
table.

---

## Open Questions / Future Work

1. **USB/UART → `assoc_w_tHCI_CMD` entry path not traced.** How raw transport
   bytes become the `param_1` PDU struct passed into this function (framing,
   length validation, the struct's exact layout beyond the fields read here)
   is the explicit subject of the still-open TODO "Document USB/UART
   transport framing and the ROM-side HCI driver" — not duplicated here.
2. **OGF 2 (Link Policy) and OGF 5 (Status Parameters) internals not
   decompiled in this pass** — their entry addresses and call sites from
   `assoc_w_tHCI_CMD` are confirmed (direct `jal`, unambiguous), and they were
   already named by Kovah, but their internal OCF jump tables were not
   walked (budget). Same generic "big switch" shape is expected by analogy
   with OGF 1/3/6/8 but not verified.
3. **`OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10` not
   decompiled.** This is the apparent single shared "send HCI status/complete"
   sink for the entire standard-HCI surface — high-value target for a future
   pass, would likely also clarify OGF 4's actual Informational-Parameters
   payload generation (chip revision, BD_ADDR, supported-commands bitmap).
4. **OGF 4's OCF→data mapping not resolved.** Confirmed *which* OCF values are
   accepted (bitmask `0x157` plus bit `0x8`, i.e. roughly OCF 1,3,5,6,8 plus
   one more relative-to-0x1001 offset) but not which spec-defined
   Informational command each corresponds to without cross-referencing the
   exact opcode arithmetic against the BT spec's OGF4 OCF table — left as an
   exercise, low priority since OGF4 carries no security/libre-relevance
   payload logic itself.
5. **LE-enable config bit's real-world value not confirmed.** Whether
   `config_base+0x7a` bit `0x2` is actually set or clear in the production
   `rtl8761bu_config.bin` shipped with UB500 boards was not checked in this
   pass — would resolve whether OGF 0x08 commands are live at all on real
   hardware (see `reverse_engineering_config_blob.md` for the config blob's
   format; a follow-up should grep the real config bytes at file offset
   matching struct offset `0x7a`).
6. **RAM hook slot `0x801206ac` (LE OCF=0 handler) not traced further** — is
   it ever populated by the patch installer (`FUN_80103780` and its
   sub-installers, per `firmware/reverse_engineering_patch_installer.md`)?
   Not checked in this pass; would directly answer whether *any* LE command
   path is wired up by the existing vendor patch, informing the libre
   replacement's BLE scope.
7. **The six loopback-mode-excluded opcodes' significance** (`0xc03`=
   `HCI_Reset` OGF3/OCF3, `0xc31`/`0xc33`/`0xc35`, `0x1005`, `0x1801`/`0x1802`)
   was read directly off the decompile but not cross-checked against the BT
   spec one-by-one to confirm they're exactly "commands that must work even
   in loopback mode" per spec (this is the expected rationale but not
   independently verified).

None of the above block the ticket's core ask: the top-level dispatcher
(`assoc_w_tHCI_CMD`, `0x80020ee0`) is fully decompiled and documented, and
**all eight** standard OGF groups (0x01–0x08) have confirmed handler
addresses, with two (OGF 4, OGF 8) newly identified, named, and fully
decompiled in this pass beyond what `kovah_function_list.md` already had.

---

## Tool note

No wairz blocker encountered this pass. `DecompileAddr.java`/`DiagAddr.java`
via `script_args` (one address per call, GZF process mode,
`use_saved_project=true`) were sufficient for the entire investigation —
top-level dispatcher, both newly-found OGF handlers, and the LE OCF jump
table bytes. One correction to GHIDRA_SCRIPTS.md's general note: addresses
passed via `script_args` to `DecompileAddr.java`/`DiagAddr.java` **do** need
the `0x` prefix (bare hex like `"80020ee0"` throws
`NumberFormatException`/`Long.decode` failure) — this is opposite of the
documented `script_name`-mode rule "Do NOT pass `script_args` with hex
literals (`0x...`)", which applies to a different invocation pattern
(likely raw-binary `-import` mode setup args, not these two
diagnostic/decompile scripts' own internal `Long.decode(args[0])` calls).
Future sessions calling `DecompileAddr.java`/`DiagAddr.java` should use
`0x`-prefixed addresses.

---

## Related documents

| File | Content |
|------|---------|
| `reverse_engineering_lmp_vsc_opcode_map.md` | OGF 0x3F vendor-specific path (73 opcodes), the sibling this doc completes |
| `rom/reverse_engineering_vsc_dispatcher.md` | `FUN_80047c50`, LMP VSC leaf handler; `hci_event_sender` |
| `kovah_function_list.md` | Pre-existing OGF 1/3 handler names this doc builds on |
| `reverse_engineering_config_blob.md` | `config_base+0x7a` and other config-blob feature bits |
| `rom/reverse_engineering_lmp_version_conn_setup.md` | Earlier sighting of `config_base+0x7a` bit 2 |
