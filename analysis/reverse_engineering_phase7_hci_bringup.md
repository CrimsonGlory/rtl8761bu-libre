# Phase 7 — Basic HCI Bring-Up Verification

**Goal**: Confirm the libre firmware exposes a working Bluetooth controller to
userspace after FC20 load — not merely that the patch downloads without timeout.

**Scope**: `hciconfig`, `hcitool scan`, BT 5.1 feature verification, A2DP audio.
SCO/eSCO voice and linux-libre compliance are noted separately.

---

## 1. Active test profile

### Pure-libre P2 (Phase 8.3 — current)

| Field | Value |
|-------|-------|
| **Profile** | `p2-libre` |
| **Make target** | `make p2-libre` (default `make docker` / `make all`) |
| **SHA256** | `8a5893981c90e9f7f939d2807e575da30ab6f707121ac861d932a719e0c635c0` |
| **Staged** | `fw_to_test/rtl8761bu_fw.bin` (NeoPC scp source) |
| **Config SHA256** | `6c28a3f07c6a30ed208c4b64862a23f02b7d93543ea980edd24df16bab45095f` |

**Composition**: 100% libre single-patch (27,928 B); T1+T2 hook bodies transcribed;
T3 AFH hooks + sub-installer #2 targets still `STUB_RET`. No `inject_vendor`, no
`NF_REF`. `make compliance-ci` PASS. Code 23,168 B + 4,636 B NOP pad to 27,808 B.

**Pass criteria**: FC20 OK, `fw version 0x09a98a6b`, `hciconfig` UP, ACL connect,
BT 5.1 features, A2DP and/or SCO/eSCO (hardware pending).

### Interim inject profile (Phase 7 reference)

| Field | Value |
|-------|-------|
| **Profile** | `full-inject-t3-pe5-vendor-tail` |
| **Make target** | `make full-inject-t3-pe5-vendor-tail` |
| **SHA256** | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` |

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
- `linux-libre` blob policy audit → `reverse_engineering_linux_libre_compliance.md` (FAIL; remediation roadmap)

---

## 3. Verified results (2026-06-09)

Profile `7f051e64…` on NeoPC — **signed off** (Tier A–C):

| Test | Result |
|------|--------|
| FC20 | **PASS** |
| `fw version` | `0x09a98a6b` |
| `hciconfig hci0` | **PASS** — `UP RUNNING`, BD_ADDR `A8:29:48:6A:97:9D` |
| HCI / LMP | **5.1** (0xa); LMP subversion `0x8a6b` |
| `hcitool scan` | **PASS** — `08:28:02:B2:5D:5E ADMIRAL TV` |
| `bluetoothctl connect` | **PASS** after `hciconfig up` + retry; 1st attempt often `page-timeout` |

**Quirk**: immediate connect after USB replug frequently fails with
`br-connection-page-timeout`; `sudo hciconfig hci0 up` then retry succeeds. Same
behaviour may affect `try_new_firmware.sh` auto-connect — not a firmware load failure.

**Status**: HCI bring-up **complete**. BT 5.1 feature verification **complete** (§7).

---

## 7. BT 5.1 feature verification (2026-06-09)

Profile `7f051e64…`, same NeoPC session as §3.

### Pass criteria

| Check | Source | Pass |
|-------|--------|------|
| HCI version ≥ 5.1 | `hciconfig -a` | **5.1** (0xa), revision 0x9a9 |
| LMP version ≥ 5.1 | `hciconfig -a` | **5.1** (0xa), subversion 0x8a6b |
| Dual-role capable | `bluetoothctl show` | central + peripheral |
| LE extended advertising | `bluetoothctl show` | SupportedInstances: **4** |
| LE PHY beyond 1M | `bluetoothctl show` | Secondary channels: **2M**, **Coded** |
| Adv data elements | `bluetoothctl show` | tx-power, appearance, local-name |

### Observed `bluetoothctl show` (excerpt)

```
Controller A8:29:48:6A:97:9D (public)
        Powered: yes
        Roles: central
        Roles: peripheral
Advertising Features:
        SupportedInstances: 0x04 (4)
        SupportedIncludes: tx-power
        SupportedIncludes: appearance
        SupportedIncludes: local-name
        SupportedSecondaryChannels: 1M
        SupportedSecondaryChannels: 2M
        SupportedSecondaryChannels: Coded
```

RTL8761BU silicon is BT **5.1** (not 5.3/5.4 as some dongle packaging claims).
Libre patch does not regress version reporting or LE capability advertisement vs
vendor baseline (`fw version 0x09a98a6b`).

**Status**: BT 5.1 feature advertisement **signed off**.

---

## 8. Audio playback (2026-06-09)

Profile `7f051e64…`, same NeoPC session.

| Test | Result |
|------|--------|
| Headset pairing | Pre-existing (paired under vendor blob) |
| A2DP music playback | **PASS** — user confirms audio works on libre fw |
| SCO/eSCO voice (HFP/HSP) | **Not tested** — music path only |

Libre patch includes eSCO negotiator `FUN_80109c08` @ `0x80109c08`; A2DP success
indicates ACL + audio profile stack is functional. Voice-call / eSCO slot budget
remains unverified on hardware.

---

## 4. NeoPC procedure (user)

Device: TP-Link UB500 (`2357:0604`). Firmware is already staged on daas-dev.

```bash
# On NeoPC — flash staged blobs (Tier A is in script output; connect at end is optional)
./try_new_firmware.sh
# If auto-connect fails with page-timeout, continue — ensure target BT peer is on/range
# only if you want to re-test connect. Tier B+C below are the sign-off tests.

# Tier A — kernel (if not already captured from script)
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
