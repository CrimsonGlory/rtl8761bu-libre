# LMP / HCI VSC Opcode Map

**Source**: `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`  
**Tools**: `DecompilePostDispatch.java` (expanded), `DecompileAddr.java` on `0x80030f1c`  
**Date**: 2026-06-09

---

## Terminology

Realtek uses “VSC” in two related places:

| Layer | Opcode format | Entry | What the patch touches |
|-------|--------------|-------|------------------------|
| **HCI VSC** | 16-bit `0xFCxx` / `0xFDxx` (OGF=0x3F) | `assoc_w_tHCI_CMD` → `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` | Optional pre-hook at router entry; individual handlers mostly ROM |
| **LMP VSC** | Vendor LMP extended opcodes `0x25B`–`0x271` | ROM wrappers `LMP__25B` / `LMP__25C` / `LMP__268` / … | **`bos_base+0xd8` → `FUN_8010bba4`** for opcode **0x268** |

The file `reverse_engineering_vsc_dispatcher.md` documents **`FUN_80047c50`** — the *leaf* handler reached after the patch gateway. It is **not** an opcode router; it parses one fixed 28-byte eSCO/SCO configuration PDU layout.

---

## LMP Vendor Opcode Map (VSC path)

Kovah names these from the **internal LMP extended opcode** embedded in the PDU. All five have ROM entry stubs; only **0x268** is fully intercepted by the patch gateway documented in `reverse_engineering_lmp_vsc_hook.md`.

| LMP opcode | ROM entry | Patchable fptr slot | Patch hook / fallback | Leaf work | Libre |
|------------|-----------|---------------------|----------------------|-----------|-------|
| **0x25B** | `LMP__25B__most_common_for_VSCs1` `0x80009ac8` | `PTR_patchable_fptr2_LMP_25B` `0x80009b18` | If fptr NULL → `LMP__25B_meat` (ROM) under IRQ disable | Pending-flag scheduler; fires from eSCO state machine (`FUN_8005d364`) | **ROM only** unless custom LMP injection needed |
| **0x25C** | `LMP__25C_called1` `0x80009a30` | `PTR_DAT_80009a68` | If fptr NULL → `FUN_80076090` (ROM) | BLE coexistence recovery; also called from `assoc_w_tHCI_CMD` when flag set | **ROM only**; patch uses via `FUN_8010ca20` (type `0x67`) |
| **0x266** | `LMP__266__FUN_80022030` `0x80022030` | — | None | SSP DHKey material copy (`FUN_8002c928` / `get_DHKey_to_3rd_param_`) | **ROM only** |
| **0x268** | `LMP__268__most_common_for_VSCs2_checks_fptr_patch` `0x80009a6c` | `PTR_DAT_80009ac4` = **`bos_base+0xd8`** | If fptr set → **`FUN_8010bba4`** (PATCH); else `FUN_80076110(conn, timeout_ms)` | **eSCO config PDU** via gateway → `FUN_80047c50` | **PATCH: `FUN_8010bba4` + literal pool**; ROM does validation |
| **0x271** | `LMP__271__FUN_80025cb4` `0x80025cb4` | — | If `field_0xb9` set → `possible_logger_called_if_no_patch3` | Crypto-state logging / `set_arg1_1_to_arg2` | **ROM only** |

### `FUN_8010bba4` gateway literal pool (`0x8010bc54`–`0x8010bc70`)

| Slot | Resolved target | Role |
|------|-----------------|------|
| `0x8010bc54` | `0x80050304` ROM | Resource / channel allocator (arg=2) |
| `0x8010bc58` | `0x8012382c` DATA | `large2` connection array base; busy flag at `[0x28]` |
| `0x8010bc5c` | `0x8010bb55` PATCH | Connection lookup from PDU (`FUN_8010bb54` validator lives at `0x8010bb54`) |
| `0x8010bc60` | `0x8004e2d0` ROM | Per-link record lookup by LMP handle (PDU byte 3) |
| `0x8010bc64` | `0x80047c50` ROM | **Leaf VSC handler** — 28-byte eSCO config parser/writer |
| `0x8010bc68` | `0x80050304` ROM | Post-dispatch cleanup / release |
| `0x8010bc6c` | `0x80044564` ROM | LMP response header builder |
| `0x8010bc70` | `0x8001d070` ROM | `hci_event_sender` — 5-byte LMP response TX |

**Libre verdict for 0x268 path**: implement **`FUN_8010bba4` (176 B)** and embed this 8-word pool with ROM addresses unchanged. No need to reimplement `FUN_80047c50`.

### HCI → LMP bridge (fires LMP VSC handlers from host)

| HCI VSC | Calls | Notes |
|---------|-------|-------|
| **0xFC67** | `LMP__25B__most_common_for_VSCs1()` then `LMP__268__(conn, delay×100)` | Delay in param byte 3 × 100 ms; uses `VSC_0xfc95_called2` internally |
| **0xFC95** | `VSC_0xfc95_FUN_800300c4` → may call `VSC_0xfc95_called2` | AFH scan; patch triggers via `PTR_VSC_0xfc95_called2_1` (`FUN_8010f950`) |
| **0xFC97** | `VSC_0xfc97_1/2` (`0x800566f8`, `0x8005770c`) | Channel-quality; no direct LMP hook |

`VSC_0xfc95_called2` (`0x80009b1c`) has its own patchable fptr at `PTR_DAT_80009b78` (same `if (fptr != NULL)` pattern).

---

## HCI Vendor-Specific Command Router

**Function**: `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` (`0x80030f1c`, 4372 B)  
**Reached from**: `assoc_w_tHCI_CMD` (`0x80020ee0`) when `OGF = 0x3F` (extracted as `opcode >> 10`).

### Pre-dispatch patch hook

At entry, the router checks `*(PTR_DAT_80032058)`; if non-NULL, calls  
`fptr(param_1, &local_37, &local_36)` and may return early. This is the HCI-level analogue of the LMP `if (fptr != NULL)` pattern.

### Opcode inventory (73 opcodes in decompiled switch)

Grouped by libre implementation requirement:

#### Critical for firmware load / bring-up

| Opcode | Handler | Impl |
|--------|---------|------|
| `0xFC20` | `VSC_0xfc20__download_patch__FUN_8002fee0` | **ROM** — Linux `btrtl` uses this; patch is payload, not reimplemented |

#### Documented / security-relevant (Kovah + prior RE)

| Opcode | Handler(s) | Impl |
|--------|------------|------|
| `0xFC11` | `VSC_0xfc11_1/2/3` + `FUN_80009680`, `FUN_80012820` | ROM |
| `0xFC39` | `VSC_0xfc39_wrapper` chain (`VSC_common`, `_1`, `_2`) | ROM; patch calls via `FUN_8010ce0c` AFH path |
| `0xFC61` | `VSC_0xfc61_write_to_relevant_data_FUN_80030dd8` | ROM — memory read (GZF / RE) |
| `0xFC6C` | `VSC_0xfc6c_FUN_8000bd04` → `FUN_8000bc38` | ROM — not a direct `cmpi` case; helper exists |
| `0xFC95` | `VSC_0xfc95_FUN_800300c4`, `VSC_0xfc95_called2` | ROM + **patch trigger** (`FUN_8010f950`) |
| `0xFCC0` | `VSC_0xfcc0_FUN_80014054` | ROM |
| `0xFCC2` | `VSC_0xfcc2_FUN_800148f0` | ROM |
| `0xFCC4` | `call_to_multi_VSC_e.g._0xfcc4_unknown` | ROM — multi-arg vendor call |
| `0xFD49` | `VSC_0xfd49` / `FUN_8003bbf0` (patch pool ref) | ROM |

#### Patch-adjacent (trigger LMP VSC or patch state)

| Opcode | Effect | Impl |
|--------|--------|------|
| `0xFC67` | Invokes `LMP__25B` + `LMP__268` | ROM dispatches; **0x268 hits patch if installed** |
| `0xFC68` | Writes `config_base+0x189` | ROM |
| `0xFC8A`–`0xFC99` | AFH / coexistence cluster; includes `VSC_0xfc95` | ROM |
| `0xFCA1`–`0xFCA2` | `VSC_0xfca1_FUN_80077474`, `poll_bb_reg_ready_write_offset_value_poll_complete` | ROM |

#### Full opcode list (alphabetical)

`0xFC10`, `0xFC11`, `0xFC13`, `0xFC14`, `0xFC15`, `0xFC16`, `0xFC17`, `0xFC18`, `0xFC1A`, `0xFC1F`, `0xFC20`, `0xFC22`, `0xFC27`, `0xFC28`, `0xFC35`, `0xFC36`, `0xFC37`, `0xFC39`, `0xFC3A`, `0xFC46`, `0xFC50`, `0xFC55`, `0xFC56`, `0xFC60`, `0xFC61`, `0xFC63`, `0xFC64`, `0xFC65`, `0xFC66`, `0xFC67`, `0xFC68`, `0xFC69`, `0xFC6A`, `0xFC6B`, `0xFC6D`, `0xFC6E`, `0xFC73`, `0xFC74`, `0xFC79`, `0xFC7A`, `0xFC7B`, `0xFC80`, `0xFC81`, `0xFC83`, `0xFC84`, `0xFC8A`, `0xFC8B`, `0xFC8C`, `0xFC93`, `0xFC95`, `0xFC97`, `0xFC98`, `0xFC99`, `0xFCA1`, `0xFCA2`, `0xFCC0`, `0xFCC1`, `0xFCC2`, `0xFCC4`, `0xFCC5`, `0xFCC7`, `0xFCC9`, `0xFCCA`, `0xFCCB`, `0xFCCD`, `0xFCCF`, `0xFCD0`, `0xFCD4`, `0xFCD9`, `0xFCDA`, `0xFCDB`, `0xFCDC`, `0xFCF0`, `0xFD40`, `0xFD42`, `0xFD43`, `0xFD47`, `0xFD49`, `0xFD4B`, `0xFD4D`

Unknown / stub cases return `local_38 = 1` (`HCI_Command_Disallowed`).

---

## Call-chain diagram

```
HCI host
  └─ assoc_w_tHCI_CMD (ROM 0x80020ee0)
       ├─ OGF 0x01..0x08 → standard HCI dispatchers (ROM)
       └─ OGF 0x3F → HCI_CMD_OGF_3F (ROM 0x80030f1c)  ← 73 HCI VSC opcodes
            ├─ [optional] patch pre-hook @ PTR_DAT_80032058
            ├─ 0xFC20 → patch download (loads libre blob)
            ├─ 0xFC67 → LMP__25B + LMP__268(conn, ms)
            └─ 0xFC95 → AFH scan → may call patch FUN_8010f950

Air / LMP
  └─ LMP__268 (ROM 0x80009a6c)          ← vendor LMP opcode 0x268
       ├─ if bos+0xd8 → FUN_8010bba4 (PATCH gateway)
       │    ├─ alloc / busy check / conn lookup
       │    ├─ *0x8010bc64 → FUN_80047c50 (ROM)  ← 28-byte eSCO config
       │    └─ 5-byte LMP response via hci_event_sender
       └─ else FUN_80076110 (ROM timeout path)

  └─ LMP__25B (ROM 0x80009ac8)          ← vendor LMP opcode 0x25B
       ├─ [optional] patch fptr @ 0x80009b18
       └─ else LMP__25B_meat (ROM)

  └─ LMP__25C (ROM 0x80009a30)          ← vendor LMP opcode 0x25C
       ├─ [optional] patch fptr @ 0x80009a68
       └─ else FUN_80076090 (ROM)
```

---

## Minimum vs full libre surface

> **Full classification** (T0–T4 tiers, all 44 hooks, size estimates, implementation
> order): see `reverse_engineering_minimum_feature_set.md`.

### Minimum BT (scan, connect, basic ACL)

| Surface | Required in patch? |
|---------|-------------------|
| Patch download `0xFC20` | No — ROM handles before patch runs |
| `FUN_8010bba4` + `bos+0xd8` | **Yes** — without it, eSCO VSC LMP path uses ROM timeout fallback only |
| `FUN_80047c50` | No — ROM |
| HCI VSC router `0x80030f1c` | No — ROM |
| `FUN_8010ca20` (protocol type `0x67`) | **Optional** — BLE coexistence monitor; safe to stub if no BLE |
| AFH patch fns (`FUN_8010f950`, `FUN_8010ce0c`, …) | **Optional** — needed for AFH / Wi-Fi coexistence |

### Full feature parity (UB500 reference firmware)

All **PATCH** functions referenced from `FUN_8010a000` hook installs plus the `FUN_8010bba4` gateway. HCI/LMP VSC ROM handlers stay as ROM calls.

---

## Related documents

| File | Content |
|------|---------|
| `reverse_engineering_lmp_vsc_hook.md` | `FUN_8010bba4` gateway phases |
| `reverse_engineering_vsc_dispatcher.md` | `FUN_80047c50` PDU layout (leaf handler) |
| `reverse_engineering_protocol_dispatch_layer.md` | `FUN_8010ca20` / type `0x67`; `LMP__25C`/`LMP__268` pool refs |
| `reverse_engineering_sco_esco_layer.md` | `LMP__25B` from `FUN_8005d364`; `VSC_0xfc95` from `FUN_8010f950` |
| `kovah_function_list.md` | Kovah names for VSC/LMP ROM symbols |

---

## Scripts

- Expanded `DecompilePostDispatch.java` — LMP VSC stubs + HCI VSC handler samples + gateway pool dump
- `DecompileAddr.java 0x80030f1c` — full HCI OGF 0x3F router decompile
