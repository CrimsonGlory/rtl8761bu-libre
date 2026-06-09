# UB500 hardware test results

Device: TP-Link UB500 (`2357:0604`), NeoPC. `try_new_firmware.sh` scp's `daas-dev:…/fw_to_test/` → `/lib/firmware/rtl_bt/`.

Config (all runs): `6c28a3f07c6a30ed208c4b64862a23f02b7d93543ea980edd24df16bab45095f`

## By profile

| Profile | SHA256 (`rtl8761bu_fw.bin`) | FC20 | `fw version` / notes | Date |
|---------|------------------------------|------|----------------------|------|
| test-nf | `148baa25c3db17148047e2ca970b62b0a7a75b960b4762ea20f045b7977cff76` | OK | `0x09a98a6b`; **connect OK** `88:C9:E8:6B:F9:1E` | 2026-06-09 |
| minimal-null | `d5bad9d1c0b60cb6f04acaa1c2408d69bdea4c7e90b83afac31a4a5f15efe0f5` | **timeout** | — | 2026-06-09 |
| minimal | `58ed77abbd46e051f2875ae3dd37fd2789d41390e54b76937ffc7c3444745f7d` | OK | `0x000b8761`; MGMT 1.22 | 2026-06-09 |
| minimal (old `PATCH_ACTIVE` @ `0x8010e159`) | `a1ec43f14674b0af1bd272b4c847985102ded67bf94576571657f1b0e7317860` | **timeout** | wrong sentinel addr | 2026-06-09 |
| full (macro pool bug) | `9cfd8d5e3992aeba8d3e3bca0e0e763923259f7ef2e9ba972010d59d00ce5171` | **timeout** | colliding `1f`/`2f` in macros | 2026-06-09 |
| full (macro fix, wrong sentinel) | `74fec1169d1879c5868a6ffb1e4b98f91f67c5601732c94ad658ea5e867e5c6f` | **timeout** | | 2026-06-09 |
| full (`PATCH_ACTIVE` @ `0x80120538`) | `8779e87e324d4ea367aad1a6692d4b55560fa083008ad982bc254a8202632f74` | **timeout** | bad BSS stub | 2026-06-09 |
| full (vendor BSS) | `34eab8a992068cd1a7132e42f548212a86a8615fc3c17f5a51bdbfb7c5db299c` | OK (FC20) | `0x818c9223`; **connect FAIL** `br-connection-page-timeout` (fw_to_test path; vendor connects) | 2026-06-09 |
| hybrid `SPLIT=0x400` | `66854b39ab47a71fd93b1745311353b4ccd65eefc6e58d3c07aada9bfe0382d4` | **timeout** | vendor[:0x400] + libre[0x400:] | 2026-06-09 |
| hybrid `SPLIT=0x800` | `101a07bce607a9f003b6f26f11bd0eb4f6cbc496c022fd0546e53d23ea9c26e5` | **timeout** | vendor[:0x800] + libre[0x800:] | 2026-06-09 |
| hybrid `SPLIT=0xc00` | `c1700f25ef1a4e884081a0d937dc10f0467e9e45c8da7206adc155d37ae24147` | **timeout** | vendor[:0xc00] + libre[0xc00:] | 2026-06-09 |

## By SHA256 (lookup)

| SHA256 | Profile | Result |
|--------|---------|--------|
| `148baa25c3db17148047e2ca970b62b0a7a75b960b4762ea20f045b7977cff76` | test-nf | **PASS** (connect OK) |
| `58ed77abbd46e051f2875ae3dd37fd2789d41390e54b76937ffc7c3444745f7d` | minimal | **PASS** (FC20) |
| `d5bad9d1c0b60cb6f04acaa1c2408d69bdea4c7e90b83afac31a4a5f15efe0f5` | minimal-null | FAIL |
| `a1ec43f14674b0af1bd272b4c847985102ded67bf94576571657f1b0e7317860` | minimal (old sentinel) | FAIL |
| `66854b39ab47a71fd93b1745311353b4ccd65eefc6e58d3c07aada9bfe0382d4` | hybrid 0x400 | FAIL |
| `101a07bce607a9f003b6f26f11bd0eb4f6cbc496c022fd0546e53d23ea9c26e5` | hybrid 0x800 | FAIL |
| `c1700f25ef1a4e884081a0d937dc10f0467e9e45c8da7206adc155d37ae24147` | hybrid 0xc00 | FAIL |
| `60c4ffc7c7946ceb5fe9fdf661e660ab7d36b3c5f8125baa4a041a08930f4b4d` | libre-hybrid 0x80 | FAIL |
| `164b816986a4c41fbb330ae00b00d8455d6566a2630fec3b1d31d36051dcd373` | libre-hybrid 0x40 | FAIL |
| `cdec7c0d3fab65ec399396dc54f531b9b9bb92294d124e61cc14974679065a9f` | libre-hybrid 0x20 | FAIL |
| `66d61882d2a09c0cc1c267baaa00eaa19180a1bdab606136eefbddbc77e7903b` | libre-hybrid 0x30 | FAIL |
| `cfd84e2fea78459fa1e3a1fe0829837502918346fa53437c4e9569dad9c68c20` | phase-bss | FAIL |
| `f8ca0ae348aa90f261e29d8eff52dca14d3c6526a9cdd693c73cf24a0a937856` | phase-zero | **PASS** |
| `4cc6fd5db6272d84b4f34be96aa88765441909ca9200447405c4bd2eef4a7e55` | phase-prebss | **PASS** |
| `6b3a7ffade1da5a33b0e9babb59a8f444beaeae05ad33a41f549d583c76e31c7` | phase-bssonly (stub) | FAIL |
| `3b38be4be70c6a0f250815df7244efde889515a2b5605db05bda0d003193159a` | phase-bssonly (vendor bss) | **PASS** |
| `c692c34729fa3c892058b2ee3c4d512d5cc9313f247d93fce5ea7a5e08d60bc7` | phase-bss (vendor bss) | **PASS** |
| `60baec0574c691e7a9d5f0710a037366122241b30bd3e6e59701e3fd7b829fd3` | phase-si2 | **PASS** |
| `a73b7effb75034fe695dffae24377b38b19b4440c64871aa4f3c4389e4ff8ab4` | phase-si3 | **PASS** |
| `8779e87e324d4ea367aad1a6692d4b55560fa083008ad982bc254a8202632f74` | full (stub BSS) | FAIL |
| `34eab8a992068cd1a7132e42f548212a86a8615fc3c17f5a51bdbfb7c5db299c` | full (vendor BSS) | FC20 **PASS**; connect **FAIL** |
| `74fec1169d1879c5868a6ffb1e4b98f91f67c5601732c94ad658ea5e867e5c6f` | full (macro fix) | FAIL |
| `9cfd8d5e3992aeba8d3e3bca0e0e763923259f7ef2e9ba972010d59d00ce5171` | full (macro bug) | FAIL |

## Conclusions (2026-06-09)

1. **Envelope OK** — `test-nf` passes; pack/patch0/config path is sound.
2. **Patch-active required** — `minimal-null` fails; `minimal` passes with `*(0x80120538)=4`.
3. **Sentinel address** — must be `0x80120538`, not `0x8010e159`.
4. **Full installer hangs** — after flag write path works; bug is in libre installer body.
5. **Vendor-head hybrid fails** — `0x400`, `0x800`, `0xc00` all timeout; vendor prefix still needs vendor bytes after split.
6. **Libre-head byte splice** — unreliable (no early `jr $ra` in full installer).
7. **Vendor baseline** (`test-nf` `148baa25…`) — FC20, `0x09a98a6b`, connect to headphones OK. Libre full FC20 OK but connect fails → hook stubs, not environment.
8. **Flash path** — NeoPC scp's `fw_to_test/` only (not `rtl8761bu-libre/rtl8761bu_fw.bin`).
9. **`dmesg` fw version** on libre builds ≠ header `0x09a98a6b` — config-word split side effect; not a load failure.

## Pending queue

| Profile | SHA256 | Status |
|---------|--------|--------|
| libre-hybrid `SPLIT=0x80` | `60c4ffc7c7946ceb5fe9fdf661e660ab7d36b3c5f8125baa4a041a08930f4b4d` | **timeout** | libre[:0x80] + vendor[0x80:] | 2026-06-09 |
| libre-hybrid `SPLIT=0x40` | `164b816986a4c41fbb330ae00b00d8455d6566a2630fec3b1d31d36051dcd373` | **timeout** | libre[:0x40] + vendor[0x40:] | 2026-06-09 |
| libre-hybrid `SPLIT=0x20` | `cdec7c0d3fab65ec399396dc54f531b9b9bb92294d124e61cc14974679065a9f` | **timeout** | libre[:0x20] + vendor[0x20:] — **not** same bytes as `minimal` | 2026-06-09 |
| libre-hybrid `SPLIT=0x30` | `66d61882d2a09c0cc1c267baaa00eaa19180a1bdab606136eefbddbc77e7903b` | **timeout** | libre[:0x30] + vendor[0x30:] | 2026-06-09 |
| libre-hybrid `SPLIT=0x30` | `66d61882d2a09c0cc1c267baaa00eaa19180a1bdab606136eefbddbc77e7903b` | queued | libre[:0x30] + vendor[0x30:] |

| phase-bss | `cfd84e2fea78459fa1e3a1fe0829837502918346fa53437c4e9569dad9c68c20` | **FAIL** (stub BSS) | after `fn_bss_init` | 2026-06-09 |
| phase-bss | `c692c34729fa3c892058b2ee3c4d512d5cc9313f247d93fce5ea7a5e08d60bc7` | **PASS** (FC20 OK; `fw version 0x890c96a3`) | prologue + vendor BSS | 2026-06-09 |
| phase-zero | `f8ca0ae348aa90f261e29d8eff52dca14d3c6526a9cdd693c73cf24a0a937856` | **PASS** (FC20 OK; `fw version 0x000b8761`) | clear `0x80120f0c`/`0x80120c80` | 2026-06-09 |
| phase-prebss | `4cc6fd5db6272d84b4f34be96aa88765441909ca9200447405c4bd2eef4a7e55` | **PASS** (FC20 OK; `fw version 0x898c92a3`) | + config-word split, no BSS | 2026-06-09 |
| phase-bssonly | `6b3a7ffade1da5a33b0e9babb59a8f444beaeae05ad33a41f549d583c76e31c7` | **FAIL** (FC20 timeout) | libre `LOAD_ADDR`/`li` memset stub | 2026-06-09 |
| phase-bssonly | `3b38be4be70c6a0f250815df7244efde889515a2b5605db05bda0d003193159a` | **PASS** (FC20 OK; `fw version 0x000b8761`) | vendor `FUN_8010a6c8` machine code (36 B) | 2026-06-09 |
| phase-si2 | `60baec0574c691e7a9d5f0710a037366122241b30bd3e6e59701e3fd7b829fd3` | **PASS** (FC20 OK; `fw version 0xa5889223`) | prologue + 4 hooks + sub-inst #1/#2 | 2026-06-09 |
| phase-si3 | `a73b7effb75034fe695dffae24377b38b19b4440c64871aa4f3c4389e4ff8ab4` | **PASS** (FC20 OK; `fw version 0xa00c962b`) | + 12 more hooks + sub-inst #3 | 2026-06-09 |
| full | `34eab8a992068cd1a7132e42f548212a86a8615fc3c17f5a51bdbfb7c5db299c` | FC20 **PASS**; connect **FAIL** | vendor BSS; `fw_to_test` handoff | 2026-06-09 |

See also `rtl8761bu-libre/test-queue.txt`.
