# Phase 7 — Basic HCI Bring-Up Verification

**Goal**: Confirm the libre firmware exposes a working Bluetooth controller to
userspace after FC20 load — not merely that the patch downloads without timeout.

**Scope (this step)**: `hciconfig`, `hcitool scan` / inquiry. ACL connect is already
exercised on the production profile; BT 5.1 feature advertisement is the next WIP item.

---

## 1. Active test profile

| Field | Value |
|-------|-------|
| **Profile** | `full-inject-t3-pe5-vendor-tail` |
| **Make target** | `make full-inject-t3-pe5-vendor-tail` |
| **SHA256** | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` |
| **Staged** | `fw_to_test/rtl8761bu_fw.bin` (NeoPC scp source) |
| **Config SHA256** | `6c28a3f07c6a30ed208c4b64862a23f02b7d93543ea980edd24df16bab45095f` |

**Composition**: libre `patch_entry` prefix `[0, 0x764)` (PE-1..PE-5) + libre
`sub_installer_2` @ file `0x764` + vendor tail `[0xE4C, …)` + vendor T3 hook bodies.
Interim production profile until `[0x820, 0xE4C)` is fully libre.

---

## 2. Pass / fail criteria

### Tier A — Kernel load (prerequisite)

| Check | Pass | Fail |
|-------|------|------|
| FC20 download | No `command 0xfc20 tx timeout` / `download fw command failed (-110)` | FC20 timeout |
| Firmware accepted | `RTL: fw version 0x09a98a6b` in `dmesg` | Missing version line or load abort |
| HCI device registered | `Bluetooth: hci0: RTL: …` without immediate `0x2036` hang | `HCI_OP_*` tx timeout (`-110`) after load |

**Known failure mode**: libre NOP tail without vendor bytes → HCI cmd `0x2036` hang
(see `c14e18f5…` in `results.md`). PE-5 + vendor tail avoids this.

### Tier B — Controller up (`hciconfig`)

| Check | Pass | Fail |
|-------|------|------|
| `hciconfig hci0` | Shows `UP RUNNING` (or becomes so after `up`) | `DOWN`, `flags=…` errors, or no `hci0` |
| BD_ADDR | Non-zero, stable across replug | `00:00:00:00:00:00` or changes every boot |
| `hcitool dev` | Lists `hci0` with same BD_ADDR | Empty or error |

### Tier C — Inquiry / scan (`hcitool`)

| Check | Pass | Fail |
|-------|------|------|
| `hcitool scan` | Completes within ~30 s; lists ≥1 nearby device (phone, laptop, headphones) | Hang, `Inquiry complete` with zero devices when peers are visible to vendor fw, or I/O error |
| Optional `bluetoothctl scan on` | Discovers devices via D-Bus | Same as above |

**Code path**: inquiry uses ROM HCI + patch `FUN_8010daa4` (HCI event `0x1` inquiry
complete) and related T1 hooks. Connect OK on the same profile is **indirect**
evidence that HCI commands work, but inquiry is a distinct path and must be logged
explicitly.

### Not in scope for this step

- BT 5.1 extended-features LE advertisement → next WIP item
- SCO/eSCO audio → separate WIP item
- `linux-libre` blob policy audit → separate WIP item

---

## 3. Partial evidence (2026-06-09)

From `.cursor/skills/ub500-hardware-test/results.md` on profile `7f051e64…`:

| Test | Result |
|------|--------|
| FC20 | **PASS** |
| `fw version` | `0x09a98a6b` |
| `bluetoothctl connect` | **PASS** → `88:C9:E8:6B:F9:1E` |
| `hciconfig` / `hcitool scan` | **Not recorded** |

**Status**: HCI bring-up is **not signed off** until Tier B + C are run on NeoPC and
logged in `results.md`.

---

## 4. NeoPC procedure (user)

Device: TP-Link UB500 (`2357:0604`). Firmware is already staged on daas-dev.

```bash
# On NeoPC — flash staged blobs
./try_new_firmware.sh

# Tier A — kernel
journalctl -k -b --no-pager | grep -E 'RTL|fc20|hci0|Bluetooth'

# Tier B — controller up
sudo hciconfig hci0 up
hciconfig -a hci0
hcitool dev

# Tier C — inquiry
# Ensure at least one discoverable BT device is nearby (phone/laptop).
sudo hcitool scan
# Optional cross-check:
bluetoothctl --timeout 30 scan on
```

**Paste back**: full `journalctl` grep block + `hciconfig -a` + `hcitool scan` output.

---

## 5. Agent follow-up on user paste

1. Append row to `.cursor/skills/ub500-hardware-test/results.md` (full SHA256).
2. If Tier B+C pass → mark WIP `[DONE] Verify basic HCI bring-up`; promote BT 5.1 to `[NEXT]`.
3. If scan fails but connect worked before → triage `FUN_8010daa4` / inquiry hooks;
   compare against vendor `test-nf` on same host.
4. If HCI hang (`0x2036`) → wrong profile or tail regression; do not re-flash same SHA.

---

## 6. Reference — architecture matrix

From `reverse_engineering_libre_patch_architecture.md` §6.2:

| Test | Profile | Pass criterion |
|------|---------|----------------|
| FC20 load | P0 | `fw version 0x09a98a6b`; no timeout |
| HCI up | P1 | `hciconfig hci0 up` |
| Inquiry / scan | P1 | `hcitool scan` finds peers |

Current interim build is **beyond P0** (connect path verified) but **P1 scan sign-off
pending**.
