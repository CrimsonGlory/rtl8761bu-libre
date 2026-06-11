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
| full-inject-t1 | `e0a0db248b76141d34dfa82e3978c24c2cd5e8718c3488f0aa73644ecdd1e190` | OK (FC20) | `0xa1889623`; **connect FAIL** `br-connection-page-timeout` (same as full) | 2026-06-09 |
| full-inject-t2 | `c812cf41776e479865aa0c386b75ea788edfc7b554b77479892009930f044670` | OK (FC20) | `0xa98c822b`; **connect FAIL** `br-connection-page-timeout` (same as full / T1) | 2026-06-09 |
| full-inject-t3 | `b686bc64649986169a8239fdcc11d4ae2f571d8a4b5095e2c87c91b8397d2a91` | OK (FC20) | `0x890c96a3`; **connect FAIL** `br-connection-page-timeout` (all hooks vendor-injected) | 2026-06-09 |
| full-inject-t3 (PE-5 libre prefix) | `387e9916d5f5939b6beef8ded8efbd8c5c2bf29f54ac3eff8d854db6028c828d` | **FC20 timeout** | libre `[0,0x764)` byte-match but `sub_installer_2` linked @ 0x8d0 not 0x764; `patch_entry_tail` wrongly @ 0x764 | 2026-06-09 |
| full-inject-t3 (PE-5 si2 @ 0x764 fix) | `c14e18f59573fd5777a9bec7554248578edb9d7c779f12821b0bd824dfff3286` | OK (FC20) | `0x09a98a6b`; **HCI hang** `0x2036`/`-110` (libre NOP tail; same as vendor-inst) | 2026-06-09 |
| full-inject-t3 (PE-5 libre prefix + vendor tail) | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` | OK (FC20) | `0x09a98a6b`; **connect OK** `88:C9:E8:6B:F9:1E`; libre `[0,0x764)` + vendor tail `[0xE4C,…)` | 2026-06-09 |
| full-inject-t3 (PE-5 libre prefix + vendor tail) **retry** | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` | OK (FC20) | `0x09a98a6b`; MGMT 1.22; **connect FAIL** `br-connection-page-timeout` (target `88:C9:E8:6B:F9:1E` likely off/range); Tier B/C not run | 2026-06-09 |
| full-inject-t3 (PE-5) **HCI bring-up sign-off** | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` | OK (FC20) | Tier B **PASS** (`UP RUNNING`, BD_ADDR `A8:29:48:6A:97:9D`, HCI/LMP 5.1); Tier C **PASS** (`hcitool scan` → `ADMIRAL TV`); connect OK after `hciconfig up` + retry (1st attempt page-timeout) | 2026-06-09 |
| full-inject-t3 (PE-5) **BT 5.1 features** | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` | OK | HCI/LMP **5.1**; `bluetoothctl show`: central+peripheral; LE adv 2M+Coded PHY, 4 instances, tx-power/appearance/local-name | 2026-06-09 |
| full-inject-t3 (PE-5) **A2DP audio** | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` | OK | Headset paired under vendor blob; **music playback OK** on libre fw (user confirm) | 2026-06-09 |
| full-inject-t3-vendor-inst | `91fe1b2da26fb3c726d6bbb3d9293679820cc54bd0ce1702d267cf76c3d4eb63` | OK (FC20) | `0x09a98a6b`; **HCI hang** `0x2036`/`-110` tx timeout; ~15 KB NOP tail still libre | 2026-06-09 |
| full-inject-t3-vendor-tail | `88dd6722353760489bfe3b1d1286404b15a5adeee8d657b0de61e17efdc37d0f` | OK (FC20) | `0x8d8c8223`; HCI OK; **connect FAIL** `page-timeout` (same as T3) | 2026-06-09 |
| full-inject-t3-vendor-tail (sub_installer_2 fix) | `7feb8b00be4c065c1ef90390d1d0aecd802d9b95b586e0aa34e82be899af93f7` | OK (FC20) | `0xa18c9623`; HCI OK; **connect FAIL** `page-timeout` | 2026-06-09 |
| full-inject-t3-vendor-tail (vendor early prefix) | `4099d4bd6e809cb860f12ebd741b19033a2f660af0bf106d5a08ed0b30beb25c` | OK (FC20) | `0x09a98a6b`; **connect OK** `88:C9:E8:6B:F9:1E` | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x800` | `58855f9557ed7d55e71860b75ee84a5765600f6647d30c2775cb14dbe246581f` | OK (FC20) | `0x09a98a6b`; **connect OK** `88:C9:E8:6B:F9:1E` | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x400` | `ab71eca65612b305082712ade125628f8a194c936bdd7cc6a80850f8d97cf982` | **FC20 timeout** | `0xfc20` tx timeout; no controller | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x600` | `99fd26eaa76a10eb7d0e0d68aedc11a3da321edcd97c777c1335e12751fbbae2` | **FC20 timeout** | same `0xfc20` failure | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x700` | `3f3b98fb510165a5c52e72dddc865427f5ffff9d3ec1a2507e0157efc5968746` | **FC20 timeout** | same `0xfc20` failure | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x780` | `7c2d577ea503988ab2b25db00dceed2b166b589c4971f4b380f2921cd7c28d78` | OK (FC20) | `0x09a98a6b`; **connect OK** | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x740` | `94883f070d306dbbd847bb8641a314c14a53f9e3788c07fecc546966bf2ba54d` | **FC20 timeout** | `0xfc20` failure | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x760` | `1e377bdcf316c213c623ea81d06319aee7b7562c128232c7c12dee34364ae87f` | **FC20 timeout** | `0xfc20` failure | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x770` | `b301298f333da74b5ed43bcdd834be935cb45ef79f9075f290c0d00a93977d40` | **FC20 timeout** | `0xfc20` failure | 2026-06-09 |
| full-inject-t3-vendor-tail-split `SPLIT=0x764` | `51340e31896ac68521afa47adedcb70cd4ff45ec52bc058c8e1fbf2e5e768dd4` | OK (FC20) | `0x09a98a6b`; **connect OK** `88:C9:E8:6B:F9:1E` | 2026-06-09 |
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
| `e0a0db248b76141d34dfa82e3978c24c2cd5e8718c3488f0aa73644ecdd1e190` | full-inject-t1 | FC20 **PASS**; connect **FAIL** |
| `c812cf41776e479865aa0c386b75ea788edfc7b554b77479892009930f044670` | full-inject-t2 | FC20 **PASS**; connect **FAIL** |
| `b686bc64649986169a8239fdcc11d4ae2f571d8a4b5095e2c87c91b8397d2a91` | full-inject-t3 | FC20 **PASS**; connect **FAIL** |
| `387e9916d5f5939b6beef8ded8efbd8c5c2bf29f54ac3eff8d854db6028c828d` | full-inject-t3 PE-5 | FC20 **FAIL** (si2 wrong offset) |
| `c14e18f59573fd5777a9bec7554248578edb9d7c779f12821b0bd824dfff3286` | full-inject-t3 PE-5 si2 fix | FC20 **PASS**; HCI **FAIL** `0x2036` |
| `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` | PE-5 libre prefix + vendor tail | FC20 **PASS**; HCI **PASS**; BT 5.1 **PASS**; A2DP audio **PASS** |
| `91fe1b2da26fb3c726d6bbb3d9293679820cc54bd0ce1702d267cf76c3d4eb63` | full-inject-t3-vendor-inst | FC20 **PASS**; HCI **FAIL** |
| `88dd6722353760489bfe3b1d1286404b15a5adeee8d657b0de61e17efdc37d0f` | full-inject-t3-vendor-tail (pre-fix) | FC20 **PASS**; connect **FAIL** |
| `7feb8b00be4c065c1ef90390d1d0aecd802d9b95b586e0aa34e82be899af93f7` | full-inject-t3-vendor-tail (si2 fix) | FC20 **PASS**; connect **FAIL** |
| `58855f9557ed7d55e71860b75ee84a5765600f6647d30c2775cb14dbe246581f` | tail-split `0x800` | FC20 **PASS**; connect **OK** |
| `ab71eca65612b305082712ade125628f8a194c936bdd7cc6a80850f8d97cf982` | tail-split `0x400` | FC20 **FAIL** |
| `99fd26eaa76a10eb7d0e0d68aedc11a3da321edcd97c777c1335e12751fbbae2` | tail-split `0x600` | FC20 **FAIL** |
| `3f3b98fb510165a5c52e72dddc865427f5ffff9d3ec1a2507e0157efc5968746` | tail-split `0x700` | FC20 **FAIL** |
| `7c2d577ea503988ab2b25db00dceed2b166b589c4971f4b380f2921cd7c28d78` | tail-split `0x780` | FC20 **PASS**; connect **OK** |
| `94883f070d306dbbd847bb8641a314c14a53f9e3788c07fecc546966bf2ba54d` | tail-split `0x740` | FC20 **FAIL** |
| `1e377bdcf316c213c623ea81d06319aee7b7562c128232c7c12dee34364ae87f` | tail-split `0x760` | FC20 **FAIL** |
| `b301298f333da74b5ed43bcdd834be935cb45ef79f9075f290c0d00a93977d40` | tail-split `0x770` | FC20 **FAIL** |
| `51340e31896ac68521afa47adedcb70cd4ff45ec52bc058c8e1fbf2e5e768dd4` | tail-split `0x764` | FC20 **PASS**; connect **OK** |
| `4099d4bd6e809cb860f12ebd741b19033a2f660af0bf106d5a08ed0b30beb25c` | full-inject-t3-vendor-tail (vendor early) | FC20 **PASS**; connect **OK** |
| `74fec1169d1879c5868a6ffb1e4b98f91f67c5601732c94ad658ea5e867e5c6f` | full (macro fix) | FAIL |
| `9cfd8d5e3992aeba8d3e3bca0e0e763923259f7ef2e9ba972010d59d00ce5171` | full (macro bug) | FAIL |
| `ea7a57423983716cf61cceb7c278e8587f1542c5329428acdd63a6ca86898a56` | p4-libre | **pending** (NeoPC); **SHIP: YES** (compliance-ci) |

## linux-libre compliance (SHIP gate)

| Profile | SHA256 (`rtl8761bu_fw.bin`) | `make compliance-ci` | SHIP | Date |
|---------|------------------------------|----------------------|------|------|
| p2-libre | `addd6593d34144a3334910f32ba7b6f7d96acecb22787976f7aa6c79047920bb` | PASS (single-patch 27,928 B) | YES | 2026-06-10 |
| p4-libre | `ea7a57423983716cf61cceb7c278e8587f1542c5329428acdd63a6ca86898a56` | PASS (single-patch 27,928 B; patch1 `61d465d62bc55370789835163b8aa8e9be42f43d7cb8bb24b2f27babce397f31`) | **YES** | 2026-06-10 |

P4 artifact: PRAM 27,680 B code + 124 B NOP pad; 43 IMPL + 1 SHIM hooks; sub2 19/19 IMPL; 0 STUB_RET among hook installs. No NF_REF, no incbin blockers, no inject overlay.

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
10. **T1 vendor inject** (`full-inject-t1`) — FC20 OK but connect still `page-timeout`; T1 bodies alone insufficient.
11. **T2 vendor inject** (`full-inject-t2`) — FC20 OK, connect still `page-timeout`; T2 stubs alone not the cause.
12. **T3 vendor LMP** (`full-inject-t3`) — all hook bodies vendor-injected; connect still `page-timeout` → not hook stubs.
13. **Vendor installer prefix** (`full-inject-t3-vendor-inst`) — installer `[0,0xE4C)` matches vendor; **HCI cmd timeout**. ~15 KB libre NOP tail breaks HCI.
14. **Vendor tail + libre installer** (`full-inject-t3-vendor-tail`) — HCI OK; connect **page-timeout**. Full vendor installer not required if prefix is right.
15. **Installer bisect `SPLIT=0x800`** (`58855f95…`) — vendor **`[0,0x800)`** + libre **`[0x800,0xE4C)`** + vendor tail + T3 hooks → **connect OK**. Libre from `0x800` suffices with vendor tail.
16. **Installer bisect `SPLIT=0x400`** — FC20 fails; prefix **> 0x400** required.
17. **Installer bisect `SPLIT=0x600` / `0x700`** — FC20 fails.
18. **Installer bisect `SPLIT=0x780`** — FC20 + connect OK with **1920 B** vendor prefix.
19. **Installer bisect through `SPLIT=0x770`** — FC20 fails until `0x780`. **Critical vendor bytes: `[0x770,0x780)`** — **16 B** at runtime **`0x8010A770`–`0x8010A780`** (libre installer bug localized).
20. **`sub_installer_2` vendor-loop fix** (`7feb8b00…`) — `[0x770,0x780)` now matches vendor; FC20 OK; connect still **page-timeout**. Fix necessary but **not sufficient** for full libre installer. **`SPLIT=0x780`** still needs vendor **`[0,0x780)`** — remaining connect bugs in **`[0,0x764)`** (patch_entry through early hooks / sub-inst #1).
21. **`SPLIT=0x764`** (`51340e31…`) — vendor **`[0,0x764)`** + libre **`[0x764,0xE4C)`** (fixed si2) + vendor tail + T3 → **connect OK**; `fw version 0x09a98a6b`. **Connect-critical libre bugs confined to `[0,0x764)`** (`patch_entry` … `sub_installer_1`). Libre from `sub_installer_2` onward (with si2 fix) suffices. FC20 cliff: `0x770` fail, `0x764` pass.
22. **`full-inject-t3-vendor-tail` build-time prefix** (`4099d4bd…`) — vendor **`[0,0x764)`** via `.incbin` at link + libre si2 @ file `0x764` + T3 hooks + vendor tail → **connect OK** without `VENDOR_PREFIX_SPLIT` inject. **`make full-inject-t3-vendor-tail` is the working production profile** (still depends on `NF_REF` for prefix/tail until `patch_entry` is libre).

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
| full-inject-t1 | `e0a0db248b76141d34dfa82e3978c24c2cd5e8718c3488f0aa73644ecdd1e190` | FC20 **PASS**; connect **FAIL** | vendor T1 bodies injected; ~20 T2 hooks still `STUB_RET` | 2026-06-09 |
| full-inject-t2 | `c812cf41776e479865aa0c386b75ea788edfc7b554b77479892009930f044670` | FC20 **PASS**; connect **FAIL** | T1+T2 inject; libre LMP hooks only | 2026-06-09 |
| full-inject-t3 | `b686bc64649986169a8239fdcc11d4ae2f571d8a4b5095e2c87c91b8397d2a91` | FC20 **PASS**; connect **FAIL** | all hooks vendor; libre installer remains | 2026-06-09 |
| full-inject-t3 (PE-5 libre prefix) | `387e9916d5f5939b6beef8ded8efbd8c5c2bf29f54ac3eff8d854db6028c828d` | **FC20 timeout** | si2 @ 0x8d0 not 0x764 (entry pool calls 0x8010A764) | 2026-06-09 |
| full-inject-t3 (PE-5 si2 @ 0x764 fix) | `c14e18f59573fd5777a9bec7554248578edb9d7c779f12821b0bd824dfff3286` | OK (FC20) | `0x09a98a6b`; HCI **FAIL** `0x2036` | 2026-06-09 |
| full-inject-t3 (PE-5 libre prefix + vendor tail) | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` | OK (FC20) | `0x09a98a6b`; **connect OK** `88:C9:E8:6B:F9:1E` | 2026-06-09 |
| full-inject-t3-vendor-inst | `91fe1b2da26fb3c726d6bbb3d9293679820cc54bd0ce1702d267cf76c3d4eb63` | FC20 **PASS**; HCI **FAIL** | vendor prefix only; libre NOP tail | 2026-06-09 |
| full-inject-t3-vendor-tail (pre-fix) | `88dd6722353760489bfe3b1d1286404b15a5adeee8d657b0de61e17efdc37d0f` | FC20 **PASS**; connect **FAIL** | libre installer + vendor tail | 2026-06-09 |
| full-inject-t3-vendor-tail (si2 fix) | `7feb8b00be4c065c1ef90390d1d0aecd802d9b95b586e0aa34e82be899af93f7` | FC20 **PASS**; connect **FAIL** | vendor 28 B si2 loop; `[0,0x764)` still libre | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x800` | `58855f9557ed7d55e71860b75ee84a5765600f6647d30c2775cb14dbe246581f` | FC20 **PASS**; connect **OK** | vendor prefix 2 KiB | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x400` | `ab71eca65612b305082712ade125628f8a194c936bdd7cc6a80850f8d97cf982` | FC20 **FAIL** | prefix too short | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x600` | `99fd26eaa76a10eb7d0e0d68aedc11a3da321edcd97c777c1335e12751fbbae2` | FC20 **FAIL** | prefix too short | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x700` | `3f3b98fb510165a5c52e72dddc865427f5ffff9d3ec1a2507e0157efc5968746` | FC20 **FAIL** | prefix too short | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x780` | `7c2d577ea503988ab2b25db00dceed2b166b589c4971f4b380f2921cd7c28d78` | FC20 **PASS**; connect **OK** | min prefix ≤ 1920 B | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x740` | `94883f070d306dbbd847bb8641a314c14a53f9e3788c07fecc546966bf2ba54d` | FC20 **FAIL** | critical in [0x740,0x780) | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x760` | `1e377bdcf316c213c623ea81d06319aee7b7562c128232c7c12dee34364ae87f` | FC20 **FAIL** | critical in [0x760,0x780) | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x770` | `b301298f333da74b5ed43bcdd834be935cb45ef79f9075f290c0d00a93977d40` | FC20 **FAIL** | critical in [0x770,0x780) 16 B | 2026-06-09 |
| full-inject-t3-vendor-tail-split `0x764` | `51340e31896ac68521afa47adedcb70cd4ff45ec52bc058c8e1fbf2e5e768dd4` | FC20 **PASS**; connect **OK** | vendor early prefix; libre from si2 | 2026-06-09 |

See also `rtl8761bu-libre/test-queue.txt`.

## Pending hardware (awaiting NeoPC paste)

| Profile | SHA256 (`rtl8761bu_fw.bin`) | Staged | Notes |
|---------|------------------------------|--------|-------|
| **p4-libre** | `ea7a57423983716cf61cceb7c278e8587f1542c5329428acdd63a6ca86898a56` | 2026-06-10 | **100% libre** P4 single-patch 27,928 B; full parity (T1+T2+T3+T4 + sub2 19/19 IMPL). PRAM 27,680 B code + 124 B NOP pad; patch1 `61d465d62bc55370789835163b8aa8e9be42f43d7cb8bb24b2f27babce397f31`. **`make compliance-ci` PASS → SHIP: YES** (linux-libre gate). Hardware **pending** NeoPC. Pass: FC20 OK, `fw version 0x09a98a6b`, HCI UP, connect, A2DP/SCO, AFH under Wi-Fi, BLE coexistence, reconnect stress. Fail bisect: FC20 → entry; HCI hang → installer; audio → T2; AFH/BLE → T3; sub2/T4 → callee gap. |
| ~~p3-libre~~ | `e8c15f3ac6662d43792bebdc28d8f60a1fac935061a5760b8be62149aa8643e2` | 2026-06-10 | superseded by p4-libre (P3-only; missing sub2 + T4) |
| ~~p2-libre~~ | `addd6593d34144a3334910f32ba7b6f7d96acecb22787976f7aa6c79047920bb` | 2026-06-10 | superseded by p4-libre (P2-only; T3 stub) |
| ~~p1-libre~~ | `67a1b3bafa6d989815baf62962630b5169d22e3f97d2967ab78305c5bf1f4e9a` | 2026-06-10 | superseded by p4-libre (P1-only; T2+ stubs) |

## 2026-06-11 session (connect tail / fn_b174)

| SHA256 | Profile | FC20 | HCI | Connect | Notes |
|--------|---------|------|-----|---------|-------|
| `cf37bc779e832ced7cd771d346cf34884326d93530a0453bb3fa600927c177d2` | p4 (`fn_b174` vendor embed only) | **FAIL** `0xfc20 tx timeout` | — | — | vendor `[0x1174,0x13D8)` alone breaks FC20; cliff confirmed |
| `e46865915acbc2409aa4018bc66dff508dfd040c9fbc374bf443ef58da66b270` | p4 (wrong baseline re-stage) | **PASS** `0x09a98a6b` | **FAIL** `Opcode 0x2036` | — | ≠ confirmed baseline `62198d8c…`; agent had dropped uncommitted HCI anchors/pool fix |
| `62198d8c1c530133ba76e848676a730f988e38a0ca4b0d5e10d0291f0494ee55` | p4 (HCI gaps + pool `0x3f4`) | **PASS** | **PASS** | **FAIL** page-timeout | hardware-confirmed 2026-06-10/11; **not reproducible** from current tree HEAD (`e4686591` instead) |

| `e46865915acbc2409aa4018bc66dff508dfd040c9fbc374bf443ef58da66b270` | p4 (HCI gaps + pool fix, retest) | **PASS** `0x09a98a6b` | **FAIL** `Opcode 0x2036` | — | **Retest 2026-06-11** after anchor/pool restore; image has vendor bytes @ pool+3 HCI gaps — **still not `62198d8c`**; tree drift or missing fix |

**Open:** recover `62198d8c` artifact or git-bisect to HCI-passing tree; `e4686591` is **not** a safe baseline despite gap bytes in staged image.

| `1ab2abc2082e591f49a1f8b413fdcb15462d322deebb3720a96b26a5ebc074ae` | p4 (HCI pre-gaps only, no pool `0x3f4`) | FC20 **PASS**; **HCI FAIL** `Opcode 0x2036` | — | pool fix **ruled out**; same failure as `e4686591` |
| `148baa25c3db17148047e2ca970b62b0a7a75b960b4762ea20f045b7977cff76` | test-nf (vendor sanity retest) | FC20 **PASS**; HCI **PASS**; **connect OK** | — | NeoPC stack OK; libre p4 HCI regression is **source-side** |
| `c184b3ea1e447b7d99bf4376ec5443a73833650992f90885088d9a49d72219d7` | libre-hybrid `SPLIT=0xE4C` (retest) | FC20 **PASS**; HCI **PASS**; connect **OK** 2nd try (1st page-timeout); **audio low** vs test-nf | 2026-06-11 |

## Pending hardware (HCI regression)

| Profile | SHA256 | Staged | Notes |
|---------|--------|--------|-------|
| ~~p4-hci-gaps-only~~ | `1ab2abc2082e591f49a1f8b413fdcb15462d322deebb3720a96b26a5ebc074ae` | 2026-06-11 | **HCI FAIL** `0x2036` — pre-gaps alone insufficient vs Jun-10 `328632bc`/`62198d8c` |
| ~~test-nf~~ (vendor sanity) | `148baa25c3db17148047e2ca970b62b0a7a75b960b4762ea20f045b7977cff76` | 2026-06-11 | FC20 **PASS**; HCI **PASS**; **connect OK** `Connected: yes` — NeoPC+dongle **OK** |
| ~~libre-hybrid~~ `SPLIT=0xE4C` | `c184b3ea1e447b7d99bf4376ec5443a73833650992f90885088d9a49d72219d7` | 2026-06-11 | FC20 OK; HCI OK; connect OK 2nd try; **audio low** (user) |
| `62198d8c1c530133ba76e848676a730f988e38a0ca4b0d5e10d0291f0494ee55` | p4-libre (fn_c09c fix retest) | FC20 **PASS** `0x09a98a6b`; HCI **PASS**; connect **FAIL** `page-timeout` ×4 | 2026-06-11 |
| `3f2ff4d6b1a1d66354d70b18f370e36befb7da6dea367742371f1625abd6a9b8` | p4 tail-split `SPLIT=0x3D74` | FC20 **PASS**; HCI **PASS**; connect **FAIL** `page-timeout` ×5 | 2026-06-11 |
| `0807cd1c63bec926f28be7394aeb1da048b359984fa5bf419a4d4b99448e4b3f` | p4 tail-split `SPLIT=0x25E0` | FC20 **PASS**; HCI **PASS**; connect **FAIL** `page-timeout` ×3 | 2026-06-11 |
| `3b08a03432acfe9d6055612c5b79be2f5291a3b3502bb0aa0db1ff2cff7f2610` | p4 tail-split `SPLIT=0x1A16` | FC20 **PASS**; HCI **PASS**; connect **FAIL** `page-timeout` ×4 | 2026-06-11 |
| `872dadd0fde1df96efdabd112b9986de03785811916d7eb9557e7071be8e54ea` | p4 tail-split `SPLIT=0x1431` | FC20 **PASS**; HCI **PASS**; connect **FAIL** `page-timeout` ×4 | 2026-06-11 |
| `c8e6a218fcebe48690553c9bfdc09fcba36768f79965998f13cd6484cb3ced2e` | p4 tail-split `SPLIT=0x113E` | **FC20 FAIL** `0xfc20 tx timeout`; byte-split mid-fn | 2026-06-11 |
| `536b8ac13fe6209f6f9dfec37bf3c18fa48654e84f4e13385e50d3e3c98dbb2f` | p4 tail-split `SPLIT=0x1174` (`fn_b174`) | **FC20 FAIL** `0xfc20 tx timeout` | 2026-06-11 |
| `4b33982178290bf3e59721adbccc34274e170bf73b84b86a46f329d7c79bf85f` | p4 tail-split `SPLIT=0x10A4` (`fn_b0a4`) | **FC20 FAIL** `0xfc20 tx timeout` | 2026-06-11 |
| `e91bce4ced5270f86b9e14d783e22bdaab930688b6d0e9eb2c1ef2eb8673ceed` | p4 tail-split `SPLIT=0xE4C` (vendor tail control) | FC20 **PASS**; HCI **PASS**; connect **OK** `Connected: yes` | 2026-06-11 |
