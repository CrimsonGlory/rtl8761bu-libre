# Firmware staged for NeoPC hardware test

NeoPC `try_new_firmware.sh` scp's **only** from this directory:

```
root@daas-dev.codexgigassys.com:/root/rtl8761bu-libre/fw_to_test/rtl8761bu_fw.bin
root@daas-dev.codexgigassys.com:/root/rtl8761bu-libre/fw_to_test/rtl8761bu_config.bin
```

Built profiles (libre, vendor baseline, bisect phases) live under `rtl8761bu-libre/` as
`rtl8761bu_fw.<profile>.bin`. The agent promotes one profile here before each handoff.

```bash
.cursor/skills/ub500-hardware-test/scripts/promote-test.sh full
```

Do not place vendor reference blobs here except via `make test-nf` promotion — keeps
libre tree free of non-free bytes at the scp path.
