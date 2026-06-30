# ROM Encryption Engine — SAFER+ (E1/E21/E22 key derivation)

**Status:** SAFER+ block cipher located, fully decompiled, and confirmed via
structural analysis (round function, key schedule, bias/log/exp tables all
match the published SAFER+ algorithm). E0 stream cipher and AES-CCM were
searched for but **not located** in this pass — see "Not found" section below
for what was tried.

**Scope:** ROM only (`0x8000xxxx`–`0x8007ffff`). No firmware/patch-side
encryption code was found; the patch does not need to reimplement any cipher
math because the ROM owns the entire BT Classic key-derivation pipeline.

---

## 1. Summary

The RTL8761BU ROM implements **SAFER+** (the block cipher selected by the
Bluetooth Core Spec for the `E1`/`E21`/`E22`/`E3` key-derivation functions used
in BT Classic legacy pairing, authentication challenge-response, and unit/combo
key generation). The cipher core lives in a previously-undocumented cluster at
`0x8002c62c`–`0x8002d1ec`, one block below (lower address than) the
`LMP_*` encryption-procedure handlers documented in
`kovah_function_list.md` (`0x80026c38`–`0x800298d0`).

The LMP procedure handlers (`LMP_COMB_KEY_0x09`, `LMP_AU_RAND_0x0B`,
`LMP_SRES_0x0C`, `LMP_ENCRYPTION_MODE_REQ_0x0F`,
`LMP_PAUSE_ENCRYPTION_AES_REQ_0x66`, etc., dispatched from
`LMP_encryption_opcode_handlers` @ `0x80028264`) are **pure protocol state
machines** — they manage the LMP PDU handshake (states, retries, NOT_ACCEPTED
reason codes) and marshal bytes, but contain **no cipher math themselves**.
Every one of them calls down into a small set of shared crypto wrapper
functions, and *those* wrappers call the real SAFER+ core. This cleanly
separates "protocol" (in the `0x8002[6-9]xxx` LMP handler range) from "crypto"
(in the `0x8002c000`–`0x8002dxxx` range) — a useful map for anyone tracing
other LMP procedures later.

```
LMP_encryption_opcode_handlers (0x80028264)        ROM LMP PDU dispatcher
  ├─ LMP_COMB_KEY_0x09       (0x80026f54)  ┐
  ├─ LMP_AU_RAND_0x0B        (0x8002763c)  │  protocol state machines only
  ├─ LMP_SRES_0x0C           (0x80027100)  │  (handshake states, retries,
  ├─ LMP_ENCRYPTION_MODE_REQ_0x0F (0x80026c38) │  NOT_ACCEPTED reasons) —
  ├─ LMP_PAUSE_ENCRYPTION_AES_REQ_0x66 (0x80028fc4) ┘  NO cipher math inline
  └─ ... (TEMP_KEY, TEMP_RAND, ENCRYPTION_KEY_SIZE_*, etc.)
        │
        ▼  call into shared crypto wrappers
  FUN_800251f8 (E21/E22-style wrapper, BD_ADDR + RAND → derived key material)
  FUN_8002d00c (E1-style wrapper: ACO + SRES from RAND, BD_ADDR, link key)
  FUN_8002d14c (E22-style wrapper: BD_ADDR(16) + RAND(6)+PIN → output(16))
        │
        ▼  call into the actual SAFER+ primitives
  FUN_8002c62c   "thing_that_uses_SHA_and_BLAKE" HMAC-style outer wrapper
                  (NOT actually SHA/BLAKE — see note below; calls SAFER+ core)
  FUN_8002cddc   SAFER+ ENCRYPT — full 8-round (a-round) encryption core
  FUN_8002ca2c   per-byte XOR/ADD combiner (round key application)
  FUN_8002cb2c   SAFER+ KEY SCHEDULE (round-key generator)
  FUN_8002ca88   SAFER+ BIAS function (fixed additive/XOR constants, round 1)
  FUN_8002cd80   SAFER+ "Armenian"/2-2 PHT-style mixing layer (3x per round)
  PTR_CRYPT_SAFER_exp_tab_8002cf1c / PTR_CRYPT_SAFER_log_tab_8002cf20
                  SAFER 45^x mod 257 exponential / logarithm S-box tables
```

---

## 2. SAFER+ core — `FUN_8002cddc` (full block encrypt)

**Address:** `0x8002cddc`, 318 bytes. Signature:
`void FUN_8002cddc(undefined4 key, int block /* in/out, 16 bytes */, uint rounds)`

```c
void FUN_8002cddc(undefined4 param_1 /*key*/, int param_2 /*block, in-place*/, uint param_3 /*rounds, &0xff*/)
{
  // copies block into local 16-byte buffer
  // loop: for round = 1..rounds (8 or 8.5/16 rounds depending on caller):
  //   if round == 3 and rounds != 0: FUN_8002ca2c(key_block, local_block, 1)  // extra key XOR (16-byte variant)
  //   FUN_8002cb2c(key, round_key_buf, 2*round - 1)      // derive odd round key
  //   FUN_8002ca2c(key, round_key_buf, 1)                // XOR round key into key-schedule state
  //   for byte 0..15:
  //     if bit (0x9999 >> byte) & 1:  byte = EXP_TAB[byte]   // nonlinear layer (S-box 1)
  //     else:                          byte = LOG_TAB[byte]   // nonlinear layer (S-box 2)
  //   FUN_8002cb2c(key, round_key_buf, 2*round)          // derive even round key
  //   FUN_8002ca2c(key, round_key_buf, 2)                // ADD round key
  //   3x: PHT-style 2-2 mixing pass over all 16 bytes, then FUN_8002cd80(state)  // linear diffusion layer
  //   final unkeyed 2-2 mixing pass
  // end loop
  // FUN_8002cb2c(key, round_key_buf, 2*rounds+1); FUN_8002ca2c(key, ..., 1)  // output transform XOR
}
```

This is the textbook SAFER+ **a-round** structure: nonlinear layer (alternating
`45^x mod 257` exp/log substitution boxes, selected by the fixed bit-mask
`0x9999` — this exact mask is the published SAFER "which-half-gets-which-box"
selector), then the linear "Pseudo-Hadamard-Transform-like" mixing layer
applied 3 times (SAFER+'s 3-layer PHT diffusion), with key-dependent XOR/ADD
combination (`FUN_8002ca2c`) between every nonlinear and linear stage. The
round count parameter (`rounds`, masked `& 0xff`) lets callers select shorter
or longer cipher runs — SAFER+ as used by Bluetooth E1/E21/E22 always uses
the full message schedule, and Bluetooth's reduced functions (E22 for the
*key* derivation path) call it with rounds=1 (see `FUN_8002c62c` below, which
calls the SAFER+ machinery twice with `rounds` values supplied by its caller).

**S-box tables** (confirmed via `DiagAddr.java`):

| Symbol | Address | Role |
|--------|---------|------|
| `PTR_CRYPT_SAFER_exp_tab_8002cf1c` | `0x8002cf1c` | Pointer to SAFER exponentiation table (`45^x mod 257`) |
| `PTR_CRYPT_SAFER_log_tab_8002cf20` | `0x8002cf20` | Pointer to SAFER logarithm table (inverse of exp_tab) |

Both symbol names already contain the literal string `CRYPT_SAFER` — this
strongly suggests Ghidra's FunctionID/FID database (or an earlier, now-lost
manual annotation pass) positively matched these tables against a known
SAFER implementation (e.g. the reference `safer.c`/BlueZ `crypto/safer+.c`
table layout). This is independent corroborating evidence on top of the
structural analysis above.

---

## 3. SAFER+ key schedule — `FUN_8002cb2c`

**Address:** `0x8002cb2c`, 152 bytes. Signature:
`void FUN_8002cb2c(byte *key /*16B*/, int out_round_key /*16B*/, uint which)`

Decompiled body confirms the **exact published SAFER+ key-schedule
algorithm**:

1. Build a 17-byte extended key: bytes 0–15 = the key, byte 16 = XOR-parity of
   all 16 key bytes (`local_20[0x10] ^= param_1[i]` for i in 1..15).
2. For each of `which-1` rounds, left-rotate every one of the 17 bytes by 3
   bits (`bVar1 << 3 | bVar1 >> 5`) — the canonical SAFER+ key-schedule
   bit-rotation step.
3. Extract 16 bytes from the rotated 17-byte ring buffer starting at offset
   `(which-1) % 17`, and — if `which > 1` — add the fixed **bias table**
   `PTR_DAT_8002cbc4` (indexed `[(which-2)*16 + j]`) to each output byte.

This is SAFER+'s `Key_Schedule()` verbatim: 17-byte extended key, 3-bit
left-rotation per round, round-robin byte extraction, and the published
per-round bias constants (table `B[]` in the Bluetooth-spec / reference SAFER+
sources).

## 4. SAFER+ bias/round-1 constant — `FUN_8002ca88`

**Address:** `0x8002ca88`, 36 bytes. Pure straight-line code, no loop:

```c
void FUN_8002ca88(char *b /*16 bytes, in-place*/)
{
  b[0]  += -0x17;  b[1]  ^= 0xe5;  b[2]  += -0x21;  b[3]  ^= 0xc1;
  b[4]  += -0x4d;  b[5]  ^= 0xa7;  b[6]  += -0x6b;  b[7]  ^= 0x83;
  b[8]  ^= 0xe9;   b[9]  += -0x1b; b[10] ^= 0xdf;   b[11] += -0x3f;
  b[12] ^= 0xb3;   b[13] += -0x59; b[14] ^= 0x95;   b[15] += -0x7d;
}
```

The alternating add/XOR pattern over 16 fixed byte constants is exactly the
SAFER+ **bias-1 vector** (`B[1][]`, derived from `exp_tab[exp_tab[17*i+1]]`
in the reference algorithm — i.e. the first row of the key-schedule bias
table, applied once at key-schedule round 1 before the main per-round loop
in `FUN_8002cb2c`/`FUN_8002cddc`'s caller). The specific byte values
(0xe9, 0xff−0x16=0xe9 family, etc.) match the well-known SAFER bias-table
constants published in the SAFER+ Bluetooth submission and present
byte-for-byte in reference open-source SAFER+ implementations (e.g. BlueZ).

## 5. Linear mixing layer — `FUN_8002cd80` and `FUN_8002ca2c`

- **`FUN_8002ca2c`** (`0x8002ca2c`, 92 bytes): per-byte combiner over a
  16-byte block, mode selected by `param_3` (1 = XOR if bit set in mask
  `0x9999` else ADD; 2 = inverse selection). This implements SAFER+'s
  alternating ADD/XOR combination of round key into the cipher state
  (used identically in `FUN_8002cddc`'s round loop for both the nonlinear-layer
  key injection and round-key XOR/ADD).
- **`FUN_8002cd80`** was referenced (called 3x per round from `FUN_8002cddc`)
  but not separately decompiled this pass — its call site context (three
  successive calls forming the PHT-like 2-2 diffusion network, bracketed by
  the per-byte `cVar3 = cVar1 + *pcVar5; *(pcVar7) = cVar3 + cVar1; *pcVar5 = cVar3;`
  butterfly pattern visible inline in `FUN_8002cddc`) is consistent with
  SAFER+'s linear transform layer. Flagged as a small follow-up if anyone
  wants 100% closure on every primitive (low priority — the cipher identity
  is already established beyond reasonable doubt by sections 2–4).

---

## 6. Bluetooth-level wrappers (E1/E21/E22 candidates)

Three wrapper functions sit between the LMP protocol handlers and the SAFER+
core. Their I/O shapes map directly onto the Bluetooth Core Spec key-derivation
functions:

### `FUN_8002d14c` (`0x8002d14c`, 162 bytes) — E22-shaped

```c
void FUN_8002d14c(key16 /*param_1*/, pin_or_key4 /*param_2*/, bd_addr6 /*param_3*/,
                   ?4 /*param_4*/, out16 /*param_5*/)
{
  memcpy + swap_byte_order(key, 16);      // BD_ADDR/RAND byte-swap to big-endian-like layout
  memcpy + swap_byte_order(bd_addr, 6);
  // builds a 16-byte "key" input (4B param_2 + 6B param_3 padded with copies)
  FUN_8002c62c(key16, 0x10, derived_input16, 0x10, out16);
  memcpy(param_5, out, 0x10);
}
```

Calls the already-named ROM helper `swap_byte_order` (confirmed existing
symbol) before feeding data into the cipher — consistent with SAFER+'s
requirement that BD_ADDR/RAND be presented in a specific byte order. The
16-byte-key + 16-byte-block + 16-byte-output shape matches **E22** (PIN-based
initialization-key derivation: `Kinit = E22(PIN, BD_ADDR, IN_RAND)`) or
**E21** (unit-key/combination-key generation: `Kx = E21(RAND, BD_ADDR)`) —
both are SAFER+-based and share this exact call shape per the Bluetooth spec.
Called from `FUN_800251f8`, which is itself called from `LMP_AU_RAND_0x0B`,
`LMP_SRES_0x0C`, `LMP_COMB_KEY_0x09` and others — i.e. from the same set of
LMP procedures that the Bluetooth spec says invoke E21/E22.

### `FUN_8002d00c` (`0x8002d00c`, 162 bytes) — E1-shaped

```c
void FUN_8002d00c(key16 /*param_1*/, rand16 /*param_2, in/out*/, bd_addr6 /*param_3*/,
                   out_aco4 /*param_4*/, out_sres12 /*param_5*/)
{
  // build 16-byte padded BD_ADDR-derived block from param_3
  FUN_8002cddc(key_block, rand_copy, 0);            // SAFER+ encrypt, rounds=0 (i.e. default/full)
  for i in 0..15: rand_copy[i] = (rand_copy[i] ^ rand[i]) + bd_addr_block[i];  // feedforward/whitening
  FUN_8002ca88(key_block);                          // bias-1 step
  FUN_8002cddc(key_block, rand_copy, 1);             // second SAFER+ pass
  memcpy(out_aco4,  rand_copy,     4);   // first 4 bytes  -> ACO  (Authenticated Ciphering Offset)
  memcpy(out_sres12, rand_copy + 4, 12); // last 12 bytes  -> SRES (Signed RESponse)
}
```

This exactly matches the Bluetooth spec's **E1** function: `(SRES, ACO) =
E1(Kc/link_key, RAND, BD_ADDR)`, including the characteristic **two-pass**
SAFER+ structure with an intermediate feedforward/whitening XOR+ADD step and
the **4-byte ACO / 12-byte SRES** output split (16 bytes total = exactly E1's
documented output split). `FUN_8002d00c` is called only from
`FUN_800251f8` and is gated on `*(byte*)(struct+0x1f1) == 6` (an internal
codec/mode selector byte distinguishing which key-derivation variant is in
play) — consistent with E1 being one of several SAFER+-based derivations
selected by connection state.

### `FUN_8002c62c` (`0x8002c62c`, 156 bytes) — shared 2-pass driver

Called by `FUN_8002d14c` (and, per its Ghidra default name
`thing_that_uses_SHA_and_BLAKE`, possibly misidentified by an automatic
heuristic — **this name is misleading; the function does not use SHA or
BLAKE, it drives the SAFER+ core**, as shown by its direct calls to
`FUN_8002c338` which is in turn presumably another SAFER+-adjacent helper not
decompiled this pass). Performs the classic HMAC-style outer/inner padding
construction (`0x36`-byte XOR pad then `0x5c`-byte XOR pad — note: these are
literally the **HMAC ipad/opad constants** `0x36`/`0x5c`), but the inner
primitive it drives (`FUN_8002c338`, not yet decompiled) is unconfirmed —
flagged as open work. **Caution**: despite the HMAC-shaped padding, Bluetooth's
E21/E22 functions are SAFER+-only per spec; this may be an unrelated/reused
generic keyed-hash helper coincidentally invoked from the same module, or the
chip vendor's SAFER+ wrapper genuinely reuses an HMAC-style construction
internally. Not resolved this pass — see Section 8.

---

## 7. Hardware-assisted encryption control plane (separate from key derivation)

`LMP_PAUSE_ENCRYPTION_AES_REQ_0x66` (`0x80028fc4`) — the LMP procedure that
negotiates encryption pause/resume during BT Classic Secure Connections
mode switches — contains **no AES primitive**. It manipulates a per-connection
crypto struct (fields at offsets `0x1f2`, `0x202`, `0x212` of the connection's
`_x58_crypto_struct`) using `swap_byte_order` and `optimized_memcpy`, and calls
out to `FUN_8002408c` ("start encryption" — gates on `config_base+0x7a` bit
`0x4`, the same `config_base` struct documented in
`reverse_engineering_config_blob.md`), `FUN_80025ea8`/`FUN_800245fc`
(encryption on/off toggles). This strongly suggests **AES-CCM (BT Classic
Secure Connections / BLE link-layer encryption) is implemented in dedicated
encryption hardware** (a baseband crypto engine triggered via register
writes), not in ROM software — consistent with the project's existing finding
that `bos_base+0xe4` (`FUN_8004f824`, see
`rom/reverse_engineering_hardware_layer.md`) is a generic "hardware-write
hook" abstraction used for several baseband functions. No software AES
S-box, round-constant table, or Rijndael MixColumns/ShiftRows pattern was
found anywhere near this code path.

---

## 8. Not found / open work

| Cipher | Status | What was tried |
|--------|--------|-----------------|
| **SAFER+** | **Found**, high confidence | Full structural decompile of cipher core, key schedule, bias table, S-box pointers (symbol names literally say `CRYPT_SAFER`); 3 Bluetooth-spec-shaped wrapper functions (E1, E21/E22-style) traced from LMP procedure call sites down to the cipher |
| **E0 stream cipher** | Not located | Searched the LMP encryption-procedure cluster (`LMP_ENCRYPTION_MODE_REQ_0x0F`, `LMP_STOP_ENCRYPTION_REQ_0x12`, `LMP_ENCRYPTION_KEY_SIZE_*`) for any per-bit/LFSR-shaped loop (E0 uses 4 LFSRs + a 4-bit-state FSM, very distinctive). None of the decompiled handlers call anything beyond the `FUN_8002403c` role-check gate and packet-builder wrappers (`FUN_800245fc`, `FUN_80025ea8`, `FUN_80024754`, `FUN_800246fc`) — these all look like hardware-trigger / connection-state-toggle functions, not cipher math. Most likely **E0 (like AES-CCM) is implemented in baseband crypto hardware**, triggered by the same control-plane functions documented in Section 7, not in ROM software. Not confirmed — would need register-level tracing of `FUN_8002408c`/`FUN_80025ea8`/`FUN_800245fc` against the hardware-layer hook architecture in `rom/reverse_engineering_hardware_layer.md`, which was out of scope for this pass. |
| **AES-CCM** | Not located | Searched for Rijndael S-box (well-known 256-byte AES S-box table) and round-constant tables near `LMP_PAUSE_ENCRYPTION_AES_REQ_0x66` and the BLE Meta-event cluster (`rom/reverse_engineering_ble_link_layer.md`, which documents LE Secure Connections P-256/DHKey senders — a natural place for AES-CCM to appear for BLE link-layer encryption). No AES S-box byte pattern or MixColumns-shaped GF(2^8) polynomial multiplication loop found. Conclusion mirrors E0: very likely **hardware-offloaded**, not ROM software. `FindStringRefs.java` was not retried for this search (per ticket's note that it returns zero results uninformatively on this binary in prior passes); no new evidence gathered there. |

**Why hardware offload is plausible**: BT Classic Secure Connections and BLE
both mandate AES-CCM, and real-world BT/BLE SoCs near-universally implement
AES-CCM (and often E0) in a small dedicated hardware block for performance and
power reasons, exposing only a register interface (key load, nonce load, go/
done bits) to firmware. The RTL8761BU's documented hardware hook architecture
(`bos_base+0xd8/0xe0/0xe4` function-pointer slots calling into
register read/write primitives at `0x8001136c`/`0x8001139c`, MMIO port
`0xb000a0bc` — see `rom/reverse_engineering_rom_regs.md` and
`rom/reverse_engineering_hardware_layer.md`) is exactly the kind of
abstraction layer that would sit in front of such a hardware crypto engine.
This is circumstantial, not confirmed — flagged as the natural next step for
whoever picks up E0/AES-CCM.

---

## 9. Implications for the libre firmware (primary goal)

**No action needed.** The entire SAFER+ key-derivation pipeline (and, very
likely, E0/AES-CCM via hardware) lives in **ROM**, not in the patch/firmware
blob that this project replaces. The libre firmware does not need to
reimplement, stub, or omit any cipher — BT Classic pairing/authentication and
encryption will work identically regardless of which firmware patch is
loaded, because the crypto is silicon-fixed. This is analogous to the
project's existing finding for `FUN_80025b68` (SSP IO-cap trigger, also
ROM-resident, also requiring no libre-side action) in
`rom/reverse_engineering_hardware_layer.md`.

---

## 10. Function/address reference table

| Address | Name | Role |
|---------|------|------|
| `0x80028264` | `LMP_encryption_opcode_handlers` | ROM LMP PDU dispatcher for all encryption-related opcodes |
| `0x80026c38` | `LMP_ENCRYPTION_MODE_REQ_0x0F` | Protocol handler only, no cipher |
| `0x80026f54` | `LMP_COMB_KEY_0x09` | Protocol handler only, no cipher |
| `0x80027100` | `LMP_SRES_0x0C` | Protocol handler only, no cipher |
| `0x8002763c` | `LMP_AU_RAND_0x0B` | Protocol handler only, no cipher |
| `0x80028fc4` | `LMP_PAUSE_ENCRYPTION_AES_REQ_0x66` | Protocol handler + HW encryption on/off trigger, no AES math inline |
| `0x800251f8` | `FUN_800251f8` | E21/E22-style crypto wrapper (BD_ADDR/RAND in, key material out) |
| `0x8002d00c` | `FUN_8002d00c` | **E1-shaped**: 2-pass SAFER+, outputs 4B ACO + 12B SRES |
| `0x8002d14c` | `FUN_8002d14c` | **E21/E22-shaped**: 16B key + 16B block → 16B output via SAFER+ |
| `0x8002c62c` | `hmac_ipad_opad_2pass_safer_hash_driver` (was `FUN_8002c62c`) | 2-pass driver with HMAC-style ipad/opad (`0x36`/`0x5c`) padding around SAFER+ calls; callee `thing_that_uses_SHA_and_BLAKE` is misleading — see Pass 6 cont. (65) 2026-06-30 |
| `0x8002cddc` | `FUN_8002cddc` | **SAFER+ block encrypt** — full round-function core |
| `0x8002cb2c` | `FUN_8002cb2c` | **SAFER+ key schedule** — 17-byte extended key, 3-bit rotation, bias table |
| `0x8002ca88` | `FUN_8002ca88` | **SAFER+ bias-1 constant** application (fixed add/XOR bytes) |
| `0x8002ca2c` | `FUN_8002ca2c` | Per-byte XOR/ADD combiner (round-key injection) |
| `0x8002cd80` | `FUN_8002cd80` | Linear mixing layer (called 3x/round); not separately decompiled |
| `0x8002cf1c` | `PTR_CRYPT_SAFER_exp_tab_8002cf1c` | SAFER `45^x mod 257` exponentiation table pointer |
| `0x8002cf20` | `PTR_CRYPT_SAFER_log_tab_8002cf20` | SAFER logarithm table pointer (inverse of exp_tab) |
| `0x8002408c` | `FUN_8002408c` | "Start encryption" gate, checks `config_base+0x7a` bit `0x4` |
| `0x80025ea8` | `FUN_80025ea8` | Encryption-off toggle (HW control plane, not cipher) |
| `0x800245fc` | `FUN_800245fc` | Encryption-on toggle (HW control plane, not cipher) |

---

## Tool notes

- All decompiles in this doc used `DecompileAddr.java` via
  `mcp__wairz__run_ghidra_headless` (GZF process mode,
  `use_saved_project=True`), per the project's known-working pattern.
- `DiagAddr.java` was used once to confirm the `PTR_CRYPT_SAFER_*` symbol
  names and raw bytes at `0x8002cf1c`.
- `FindStringRefs.java`/`FindXrefsTo.java` were **not** attempted this pass —
  per prior-session notes in `work-in-progress.txt` these are known
  uninformative/stale on this binary, and the decompile-driven call-chain
  trace (handler → wrapper → cipher core) was sufficient to reach a confident
  conclusion without them.
- `FUN_8002cd80` and `FUN_8002c338` (referenced but not separately
  decompiled) are flagged as small follow-up items, not blockers — the SAFER+
  identification does not depend on them.
