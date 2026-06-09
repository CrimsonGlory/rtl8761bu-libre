# RTL8761BU Config Blob & BD_ADDR Format

Documents the `rtl8761bu_config.bin` file format as defined by the Linux `btrtl`
driver, how it is delivered to the chip, and how ROM + patch firmware consume it.
Resolves Phase 5 `[TODO]` for BD_ADDR / config blob documentation.

**Sources:** Linux `drivers/bluetooth/btrtl.c` + `btrtl.h` (kernel 6.x),
Ghidra decompile of ROM `copies_config_bdaddr` (`0x8000fd38`) and patch
`FUN_8010a7b8` (TLV applier), Kovah UB500 dmesg traces.

---

## 1. Two-file firmware model

| File | Kernel path | Role |
|------|-------------|------|
| `rtl8761bu_fw.bin` | `rtl_bt/rtl8761bu_fw.bin` | EPatch v2 envelope + MIPS16e patch body |
| `rtl8761bu_config.bin` | `rtl_bt/rtl8761bu_config.bin` | Vendor config TLV (optional overrides) |

The config file is **not** embedded in the `.fw.bin`. `btrtl` concatenates them at
download time:

```
total_download = selected_patch_bytes + cfg_len
```

UB500 typical dmesg:

```
RTL: loading rtl_bt/rtl8761bu_fw.bin
RTL: loading rtl_bt/rtl8761bu_config.bin
RTL: cfg_sz 6, total sz 27814
RTL: fw version 0x09a98a6b
```

`27814 = 27808` (patch 1 body) `+ 6` (default config header).

For RTL8761BU, `config_needed = false` in `ic_id_table` — the driver loads the
config file when present but does **not** fail if it is missing. Shipping the
6-byte default header is still recommended so `FUN_8010a7b8` sees a valid magic.

---

## 2. Kernel structure (`btrtl.h`)

```c
struct rtl_vendor_config_entry {
    __le16 offset;   /* byte offset into chip config_base */
    __u8   len;      /* payload length */
    __u8   data[];   /* len bytes */
} __packed;

struct rtl_vendor_config {
    __le32 signature;    /* RTL_CONFIG_MAGIC = 0x8723ab55 */
    __le16 total_len;    /* sum of all entry wire sizes (see §3) */
    struct rtl_vendor_config_entry entry[];
} __packed;
```

Driver constant:

```c
#define RTL_CONFIG_MAGIC 0x8723ab55
```

On the wire the magic appears as literal bytes `55 AB 23 87` (little-endian u32).
The `0x8723` prefix is **not** an LMP subversion field or config offset — it is
part of the signature (historically shared with `RTL_ROM_LMP_8723B = 0x8723`).

`btrtl_get_uart_settings()` validates `signature` and walks `entry[]` for UART
chips. RTL8761BU is USB (`HCI_USB`); the driver never parses individual entries
in kernel space — it forwards the raw bytes to the chip.

---

## 3. Entry wire format and `total_len`

Each entry occupies **`3 + len`** bytes on the wire:

| Field | Size | Encoding |
|-------|------|----------|
| `offset` | 2 | LE `uint16` — destination = `config_base + offset` |
| `len` | 1 | `uint8` — number of data bytes following |
| `data` | `len` | opaque payload copied verbatim |

`total_len` is the **sum of `(3 + entry.len)` for every entry** — i.e. the total
size of all entry records after the 6-byte header. It is **not** the entry count.

Example: one 6-byte BD_ADDR entry → wire size `2 + 1 + 6 = 9` → `total_len = 9`
→ bytes `09 00`.

The driver checks `cfg_len - 6 >= total_len`. The patch TLV applier
(`FUN_8010a7b8`) uses the same byte count at runtime (`remaining` at
`0x80111600`): each loop iteration subtracts `entry[1] + 3`.

---

## 4. Default config (6 bytes)

```
Offset  Hex bytes    Field
──────  ─────────    ─────
0x00    55 AB 23 87  signature = 0x8723ab55
0x04    00 00        total_len = 0  (no entries)
```

**Effect on chip:** ROM leaves runtime TLV counter at `0x80111600` as zero.
`FUN_8010a7b8` (patch TLV applier) validates magic `0x8723ab55` at `0x801115fc`
then exits immediately — no `config_base` patches. BD_ADDR comes from ROM
factory storage in `config_base+0x30` (unchanged).

This matches `gen_config.py` output (`55ab23870000`) even though older comments
in that script mis-labelled bytes 2–5 as "offset 0x8723 / value 0x0000".

---

## 5. BD_ADDR override entry

Public BD_ADDR is **not** settable via standard HCI on Realtek USB dongles.
Kovah demonstrated that the sanctioned path is config entry **`offset = 0x0030`**
with **`len = 6`**.

### 5.1 Byte layout

Example: set address `AA:BB:CC:DD:EE:FF` (MSB-first notation):

```
55 AB 23 87   09 00   30 00 06   FF EE DD CC BB AA
│  signature  │ total │ offset│len│  BD_ADDR (LSB first on wire)
│  0x8723ab55 │ =9    │ 0x30  │ 6 │
```

Total file size: **15 bytes** (6-byte header + 9-byte entry).

| Byte offset | Value | Meaning |
|-------------|-------|---------|
| 0–3 | `55 AB 23 87` | Magic |
| 4–5 | `09 00` | `total_len = 9` (= 2 + 1 + 6) |
| 6–7 | `30 00` | `offset = 0x0030` → `config_base + 0x30` |
| 8 | `06` | `len = 6` |
| 9–14 | `FF EE DD CC BB AA` | Address octets **reversed** (LSB = `FF` first) |

### 5.2 Multiple entries

Entries are concatenated after the 6-byte header. `total_len` is the sum of all
`(3 + len)` values. Offsets are independent — any `offset < 0x411` is accepted
by `FUN_8010a7b8` (larger types are skipped).

---

## 6. Download path (Linux → ROM)

### 6.1 `btrtl_setup_rtl8723b()` (8761 family)

1. `rtlbt_parse_firmware()` — select patch where `chip_id == rom_version + 1`
   (UB500: `rom_version=1` → `chip_id=2`, patch @ file `0x3780`, 27,808 B).
2. Append `cfg_data` to the end of the patch buffer.
3. `rtl_download_firmware()` — fragment into **252-byte** payloads via HCI VSC
   **`0xFC20`** (`OCF 0x20`, OGF `0x3F`).

Per-chunk structure (`struct rtl_download_cmd`):

| Byte | Field |
|------|-------|
| 0 | Sequence index; **bit 7 set** on final chunk |
| 1–252 | Payload (patch bytes; config bytes ride in the **last** chunk(s)) |

The driver writes `fw_version` into the last **4 bytes** of the patch body before
download (`rtlbt_parse_firmware`); config bytes follow that patched body.

### 6.2 ROM `VSC_0xfc20__download_patch__FUN_8002fee0`

Decompiled behaviour (186 B, `0x8002fee0`):

- Tracks fragment index; on index `0` resets write pointer to **`0x8010A000`**
  (PRAM patch base).
- `optimized_memcpy(dest, payload, len)` for each chunk.
- On final chunk (index bit 7): sets download-complete flags, then enters ROM
  boot / patch-apply path (infinite loop in Ghidra snapshot — handoff to patch
  entry).

Config bytes appended after the patch image land contiguously **after** the patch
body in the download buffer. ROM firmware (between FC20 completion and patch
entry) copies/normalises them into the runtime TLV arena at **`0x801115F8`–`0x80113FFF`**.

---

## 7. On-chip consumption chain

Patch entry `FUN_8010a000` calls two ROM/patch functions in order:

```
FUN_8010a7b8()        /* TLV applier — patch, 114 B */
copies_config_bdaddr() /* ROM 0x8000fd38, 120 B */
```

### 7.1 `FUN_8010a7b8` — TLV applier

Runtime layout (DATA block snapshot):

```
0x801115F8   blob start guard
0x801115FC   LE32 magic 0x8723ab55   ← must match
0x80111600   LE16 remaining bytes  ← 0 in default config
0x80111602   first TLV record
...
0x80113FF0   blob end guard
```

Loop logic (per record):

```c
rec_len = entry[1] + 3;   /* uint16 type + uint8 len + data */
if (entry[0] < 0x411)
    optimized_memcpy(config_base + entry[0], &entry[3], entry[1]);
```

For BD_ADDR: `entry[0] == 0x0030`, `entry[1] == 6` → copies 6 bytes to
**`config_base + 0x30`** (`0x801200A0` in DATA snapshot; `config_base` pointer
is `0x80120070`).

Apply function: ROM `optimized_memcpy` at `0x8000E85C`.

### 7.2 `copies_config_bdaddr` — ROM `0x8000fd38`

Decompiled (120 B):

```c
void copies_config_bdaddr(void) {
    config_struct *cfg = PTR_config_base;          /* 0x80120070 */

    /* If !(cfg+0x7a & 4): clear bits 12–13 of cfg+0xd8 */
    if ((cfg->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ & 4) == 0)
        cfg->field208_0xd8 &= ~0x3000;

    /* Compare 6-byte factory BD (global) vs config_base BD */
    if (memcmp(PTR_some_other_g_bdaddr, PTR_config_bdaddr, 6) != 0)
        optimized_memcpy(PTR_DAT_8000fdbc, PTR_config_bdaddr, 6);
}
```

`PTR_config_bdaddr` points at **`config_base + 0x30`** (the field updated by
the TLV applier). `PTR_some_other_g_bdaddr` is the current global BD_ADDR ROM
was using. If the config blob changed `config_base+0x30`, this function
propagates the new address into the global BT stack BD_ADDR used for HCI.

**Libre firmware:** call ROM `copies_config_bdaddr` at `0x8000FD38` after
`FUN_8010a7b8` — no reimplementation required. Ensure TLV applier runs first.

---

## 8. `config_base` context

| Address | Role |
|---------|------|
| `0x80120070` | `config_base` — ROM-allocated HW/BT config struct |
| `0x801200A0` | `config_base + 0x30` — **BD_ADDR field** (6 bytes) |
| `0x801200B9` | `+0x49` — ASCII `"RRTK_BT_5.0"` identity string |

The config blob does **not** replace the entire `config_base` — it applies sparse
overrides via offset/type entries. Factory BD_ADDR remains unless offset `0x0030`
is supplied.

---

## 9. Validation rules (driver-side)

From `btrtl.c` / `btrtl.h`:

| Check | Failure mode |
|-------|--------------|
| `signature == 0x8723ab55` | `invalid config magic` (UART path) |
| `sizeof(header) + total_len <= cfg_len` | `config is too short` |
| Per-entry bounds | `invalid UART config entry` (UART only) |

RTL8761BU USB: only the concatenation length matters for download; magic
validation happens on-chip in `FUN_8010a7b8`.

---

## 10. Libre build implications

| Item | Action |
|------|--------|
| `rtl8761bu_config.bin` | Generate with `gen_config.py` (default 6 B) |
| Custom BD_ADDR | Use `gen_config.py --bdaddr AA:BB:CC:DD:EE:FF` or hand-build §5.1 |
| Patch binary | Unchanged — config is separate file |
| `FUN_8010a7b8` | Implement 114 B TLV loop; safe no-op when `remaining == 0` |
| BD_ADDR load | `jal` ROM `0x8000FD38` after TLV applier |
| Factory address | Omit BD entry in config → ROM default preserved |

Install both files:

```bash
sudo cp rtl8761bu_fw.bin    /lib/firmware/rtl_bt/
sudo cp rtl8761bu_config.bin /lib/firmware/rtl_bt/
```

---

## 11. Quick reference

### Hex dumps

```
# Default (6 B)
55 ab 23 87 00 00

# BD_ADDR AA:BB:CC:DD:EE:FF (15 B)
55 ab 23 87 09 00 30 00 06 ff ee dd cc bb aa
```

### Related addresses

| Symbol | Address |
|--------|---------|
| FC20 download handler | `0x8002FEE0` |
| TLV applier | `0x8010A7B8` |
| `copies_config_bdaddr` | `0x8000FD38` |
| `optimized_memcpy` | `0x8000E85C` |
| TLV magic (runtime) | `0x801115FC` |
| TLV counter | `0x80111600` |
| `config_base` | `0x80120070` |
| BD_ADDR field | `0x801200A0` (`+0x30`) |

### Related documents

| File | Content |
|------|---------|
| `reverse_engineering_patch_installer.md` | Appendix E — `FUN_8010a7b8` pool dump |
| `reverse_engineering_libre_patch_layout.md` | §7 config blob summary |
| `reverse_engineering_mandatory_hooks.md` | BD_ADDR copy marked mandatory ROM call |
| `rtl8761bu-libre/gen_config.py` | Config file generator |
| Linux `drivers/bluetooth/btrtl.c` | Download + struct definitions |

---

## 12. Corrections to prior notes

1. **6-byte default is header-only** (`total_len = 0`), not an entry at
   offset `0x8723`.
2. **`copies_config_bdaddr` does not parse the file** — it syncs
   `config_base+0x30` into the global BD_ADDR after `FUN_8010a7b8` applies TLV
   records.
3. **`0x8723` in bytes 2–3** is part of magic `0x8723ab55`, not a config offset.
4. **BD_ADDR byte order on wire** is LSB-first (reverse of canonical `aa:bb:…`
   string notation).
