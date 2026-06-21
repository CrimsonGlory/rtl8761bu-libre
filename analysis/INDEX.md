# Analysis Findings Index

All reverse-engineering and design findings for the RTL8761BU libre firmware project.
See `/root/rtl8761bu-libre/CLAUDE.md` for project context, tool stack, and goals
(primary: libre firmware replacement; secondary/nice-to-have: ROM documentation).

`analysis/` is split by what's being reverse-engineered:

- **root** (this directory) — cross-cutting design specs, audits, and references
  that span both ROM and firmware, or are project-process docs
- **`firmware/`** — RE notes whose subject is exclusively the patch/firmware blob
  (`0x8010xxxx` runtime / file offset `0x0000`–`0xadc3`) — the primary goal
- **`rom/`** — RE notes whose subject is exclusively the silicon mask ROM
  (`0x8000xxxx`–`0x8007ffff`) — the secondary, nice-to-have goal

## Root — cross-cutting design, audits, references

| File | What it covers |
|------|---------------|
| `kovah_function_list.md` | Full list of Kovah's annotated function/label names (spans patch, data, and ROM blocks) |
| `reverse_engineering_lmp_vsc_opcode_map.md` | LMP / HCI VSC opcode dispatch map (ROM dispatchers + patch-installed handlers) |
| `reverse_engineering_config_blob.md` | `rtl8761bu_config.bin` format, BD_ADDR delivery, ROM/patch consumption (Phase 5) |
| `reverse_engineering_libre_patch_architecture.md` | Master design doc for the libre patch architecture (Phase 5 → Phase 6 implementation reference) |
| `reverse_engineering_libre_patch_layout.md` | Libre patch binary layout design spec |
| `reverse_engineering_mandatory_hooks.md` | Mandatory vs. optional ROM hooks — libre design decisions |
| `reverse_engineering_minimum_feature_set.md` | Minimum vs. full feature set classification for libre implementation |
| `reverse_engineering_stub_implementations.md` | Stub implementations for non-critical functions — design spec |
| `reverse_engineering_p4_hook_audit.md` | P4 completeness audit — 44 `FUN_8010a000` hook installs, IMPL/SHIM/STUB_RET status |
| `reverse_engineering_phase7_hci_bringup.md` | Phase 7 — basic HCI bring-up verification (working BT controller post-FC20 load) |
| `reverse_engineering_linux_libre_compliance.md` | Linux-libre blob policy compliance audit |

## firmware/ — patch/firmware RE (primary goal)

| File | What it covers |
|------|---------------|
| `firmware/reverse_engineering_patch_installer.md` | Master installer `FUN_80103780`: all 50+ fptr installs, 6 sub-installers, phase-by-phase |
| `firmware/reverse_engineering_lmp_vsc_hook.md` | `FUN_8010bba4` — LMP VSC hook entry point |
| `firmware/reverse_engineering_protocol_dispatch_layer.md` | Protocol dispatch layer analysis (string-assoc + remaining new functions) |
| `firmware/reverse_engineering_string_assoc_installer.md` | String-associated function installer (`FUN_8010e27c`) analysis |
| `firmware/reverse_engineering_sub_installers.md` | All 6 sub-installers and the patch boot sequence |
| `firmware/reverse_engineering_sco_esco_layer.md` | SCO/eSCO layer & slot scheduler — 28 functions across 5 analysis passes |
| `firmware/reverse_engineering_patch_entry.md` | `patch_entry` (`FUN_8010a000` region) — libre `init.S`/`bootstrap.S` replacement for vendor prefix |
| `firmware/reverse_engineering_address_pair_table_omit.md` | Address-pair table @ file `0xA0` — intentional OMIT decision for all libre profiles (P1–P4) |
| `firmware/reverse_engineering_late_patch_block_omit.md` | Late-patch block `FUN_80109980`–`80109824` — intentional OMIT decision for all libre profiles (P1–P4) |

## rom/ — silicon ROM RE (secondary, nice-to-have goal)

The ROM is fixed in chip silicon and can never be reflashed — this RE has no
shipping deliverable, it exists purely to document the chip for research value.

| File | What it covers |
|------|---------------|
| `rom/reverse_engineering_hardware_layer.md` | `FUN_8004f824` slot-budget validator + 7-entry interval table; hook-based HW abstraction architecture |
| `rom/reverse_engineering_vsc_dispatcher.md` | `FUN_80047c50` — ROM VSC parameter validator and connection record writer |
| `rom/reverse_engineering_conn_record_subsystem.md` | ROM eSCO/SCO connection record pool: allocation, registration, lookup, hardware commit, free |
| `rom/reverse_engineering_rom_regs.md` | ROM HW register r/w protocol (`0x8001136c`/`9c`, MMIO `0xb000a0bc`) |
| `rom/rom_coverage_baseline.md` | Phase 9 baseline: 2738 fns total, 461 named (16.84%), 95.37% bytes defined / 4.63% undefined, as of 2026-06-21 |
| `rom/reverse_engineering_baseband_reg_helpers.md` | 9 generic baseband register read/write/toggle helper fns (`FUN_80011510` family + `FUN_8000b820`) consolidated from Phases 1-8 |
| `rom/reverse_engineering_lmp_version_conn_setup.md` | 8 fns: LMP version exchange (`send_evt_HCI_Read_Remote_Version_Information_Complete` etc.) + role-switch/link-policy validation chain |
| `rom/reverse_engineering_conn_feature_dispatch.md` | 10 fns in the `FUN_80052c64` eSCO/SCO parameter negotiation cluster: hash-bucket pool, refcounting, commit, re-validation |
| `rom/reverse_engineering_register_script_interpreter.md` | `FUN_8003aea0` — 16-opcode byte-code VM for hardware register-config scripts, called from both ROM init and patch entry |
| `rom/reverse_engineering_conn_type_dispatch_and_esco.md` | `FUN_80050810` (`bos_base+0xe0` hook) + 4 type handlers, plus `FUN_80044730` eSCO packet-type/air-mode table |
| `rom/reverse_engineering_boot_reset_sequence.md` | ROM power-on boot path: 32-bit MIPS preamble @ `0x80000000`–`~0x800000c8` (MMIO/CP0 setup), unresolved MIPS16e-switch gap to `0x80009104`, and the (separate, patch-download-time) call chain to patch entry `0x8010a001` via `0x800109ac` |
| `rom/reverse_engineering_interrupt_vectors.md` | Interrupt/exception context-save trampoline @ `0x80009160` (32-bit MIPS, saves all GPRs+HI/LO/EPC, `jalr`s into MIPS16e dispatcher `0x80033ce8`); single-slot ISR dispatch + default ack path at MMIO `0xb000a0a0`, sibling register to the documented `0xb000a0bc` HW port; resolves boot-reset doc's MIPS16e mode-switch question (`jalx`) and corrects its mis-IDed `disable_interrupts`/`enable_interrupts` as MIPS16e (they're 32-bit MIPS) |
| `rom/reverse_engineering_hci_command_router.md` | Top-level standard-HCI dispatcher `assoc_w_tHCI_CMD` (`0x80020ee0`) fully decompiled; confirms all 8 OGF (0x01-0x08) handler addresses incl. 2 newly-found/decompiled (`OGF4` Informational `0x8002013c` 74B near-stub, `OGF8` LE Commands `0x80044674` 176B with a 1-entry OCF jump table pointing at an unfilled RAM hook slot — ROM ships ~zero native LE support); no OGF 0x07 case exists |
| `rom/reverse_engineering_lc_lmp_state_machine.md` | LC/LMP baseband procedures traced past the OGF1/OGF2 HCI handlers: paging (`fHCI_Create_Connection_0x05` → `FUN_80041900`, full HW register programming) and role switch (`FUN_8001acd8`→`FUN_8001ac74`→`FUN_8001ab44`, sends `LMP_SLOT_OFFSET`) fully traced; inquiry trigger and `LMP_UNSNIFF_REQ` partially traced; confirms `send_LMP_pkt` (`0x800611e4`) as the shared LMP-PDU-TX primitive used by all named `LMP_*` handlers; no single state-machine dispatcher found — architecture is shared conn-record + shared TX primitive + per-procedure driver fns; LMP receive-side dispatcher, inquiry-train/page-train low-level transmit loops, hold/park, and `HCI_Sniff_Mode` remain unexplored |
| `rom/reverse_engineering_ble_link_layer.md` | Corrects/extends the HCI-router doc's "OGF8 ships ~no LE support" claim: command-parsing side confirmed thin (1-entry OCF table → ROM status-echo stub, not actually empty), but found a separate, rich, 17+ function cluster of fully-decompiled `send_evt_Meta_subevent_0x##` ROM functions (`0x80044730`–`0x80046620+`) covering nearly all LE Meta Events (Connection Complete, Advertising Report, PHY Update, LE Secure Connections P-256/DHKey, etc.), sharing the same conn-record pool as BT Classic; the lower-level RF timing/state-machine driver that calls these senders was searched for but not located (blocked by stale `FindXrefsTo.java` + a string-search tool that returns zero results on this binary) — open question, not confirmed patch-resident or ROM-resident |

**Keep this index current**: any time a new file is added to `analysis/`, add a row
to the right section in the same turn — filename (with `firmware/` or `rom/` prefix
if applicable) + one-line summary of its scope/verdict. A new file goes in
`firmware/` if it's exclusively about the patch/firmware blob, `rom/` if it's
exclusively about silicon ROM internals, or stays at the root if it's a design
doc, audit, or reference that spans both. Do this immediately, not as a follow-up.
