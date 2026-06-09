---
name: ub500-hardware-test
description: >-
  Build RTL8761BU libre firmware on daas-dev, stage one active profile in
  fw_to_test/, queue alternates as renamed siblings under rtl8761bu-libre/,
  document every run by full SHA256 in results.md. Use when the user runs
  try_new_firmware.sh, pastes NeoPC journalctl/dmesg, asks for hardware test
  handoff, FC20 bisect, or UB500 dongle flash workflow.
---

# UB500 hardware test (NeoPC)

User flashes **only** by running `try_new_firmware.sh` on NeoPC:

```
scp daas-dev:/root/rtl8761bu-libre/fw_to_test/rtl8761bu_fw.bin → /lib/firmware/rtl_bt/
```

**Agent builds everything; user never compiles.**

## Document every test (required)

After **each** user paste of `try_new_firmware.sh` / dmesg output:

1. Append a row to [results.md](results.md) **and** the SHA256 lookup table (full 64-char hash, never truncate).
2. Record: profile name, SHA256, FC20 pass/fail, `RTL: fw version …` line if present, functional notes (connect/scan), date.
3. Update `rtl8761bu-libre/test-queue.txt` (mark `done` / `ACTIVE`).
4. Promote the next profile into `fw_to_test/` when continuing a queue.

**Do not re-flash a SHA256 that already failed** unless the underlying code changed (new hash).

## Paths

| Role | Path |
|------|------|
| **Flash on NeoPC** (scp source) | `/root/rtl8761bu-libre/fw_to_test/rtl8761bu_fw.bin` |
| | `/root/rtl8761bu-libre/fw_to_test/rtl8761bu_config.bin` |
| **Build tree** | `/root/rtl8761bu-libre/rtl8761bu-libre/` (`make` output + profile siblings) |
| **Queued siblings** | `rtl8761bu-libre/rtl8761bu_fw.<profile>.bin` |

Only **one** profile occupies `fw_to_test/` — the build the user flashes next.
Vendor `test-nf` is promoted here too; never mix non-free blobs into `rtl8761bu-libre/rtl8761bu_fw.bin` as the scp default.

### Promote next test

```bash
.cursor/skills/ub500-hardware-test/scripts/promote-test.sh <profile>
# copies rtl8761bu_fw.<profile>.bin → ../fw_to_test/
sha256sum /root/rtl8761bu-libre/fw_to_test/rtl8761bu_fw.bin
```

Or after `stage-test.sh`, the first profile is copied to `fw_to_test/` automatically.

## Build (agent only)

```bash
cd /root/rtl8761bu-libre/rtl8761bu-libre
docker build -t rtl8761bu-libre .
docker run --rm -v "$(pwd)":/work \
  -v /root/rtl8761bu-libre/rtl8761bu-non-free:/nf_ref:ro \
  -e NF_REF=/nf_ref/rtl8761bu_fw.bin \
  rtl8761bu-libre make <target>
cp rtl8761bu_fw.bin rtl8761bu_fw.<profile>.bin   # if make didn't keep sibling
../.cursor/skills/ub500-hardware-test/scripts/promote-test.sh <profile>
```

| Target | Purpose |
|--------|---------|
| `test-nf` | Vendor byte-identical baseline |
| `minimal-null` | `jr $ra` only |
| `minimal` | `*(0x80120538)=4` + return |
| `docker` / `all` | Full libre installer |
| `hybrid SPLIT=0xN` | `vendor[:N]` + `libre[N:]` (vendor-head) |
| `libre-hybrid SPLIT=0xN` | `libre[:N]` + `vendor[N:]` (libre-head; early N unreliable — no early return) |
| `phase-bss` / `phase-si2` / `phase-si3` | Installer stops after milestone + `PATCH_FINISH` |

Batch:

```bash
.cursor/skills/ub500-hardware-test/scripts/stage-test.sh minimal-null minimal 'hybrid SPLIT=0x400'
```

## One handoff per message

1. **Active profile** + **full SHA256** (of `fw_to_test/rtl8761bu_fw.bin`)
2. **Queue** remaining profiles + hashes
3. User runs `try_new_firmware.sh`; paste kernel lines (+ connect/scan if tested)

## Pass / fail

| Tier | Criteria |
|------|----------|
| **FC20 pass** | No `command 0xfc20 tx timeout` / `download fw command failed (-110)` |
| **Vendor match** | `RTL: fw version 0x09a98a6b` (`test-nf` only so far) |
| **Stub pass** | FC20 pass + `fw version 0x000b8761` + MGMT up (`minimal` observed) |
| **Connect pass** | `bluetoothctl connect <addr>` → `Connected: yes` (vendor baseline confirmed) |

**Fail:** FC20 timeout; or connect `br-connection-page-timeout` on libre while vendor connects.

## Bisect order (default)

`test-nf` → `minimal-null` → `minimal` → `full` → `hybrid-*` (increase vendor prefix: `0x400`, `0x800`, `0xc00`, …)

## Critical constants

- FC20 → `0x8010A000`; ROM entry `0x8010A001` (body+0).
- Patch-active: `*(0x80120538) = 4`.
- Config SHA256: `6c28a3f07c6a30ed208c4b64862a23f02b7d93543ea980edd24df16bab45095f`
- `pack.py` requires `NF_REF` for patch0.

## Result log

[results.md](results.md) — profile table + SHA256 index + conclusions.
