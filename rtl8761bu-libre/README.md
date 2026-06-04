# rtl8761bu-libre

Free (GPL-2.0-or-later) replacement firmware for the Realtek RTL8761BU
Bluetooth 5.0 chip, as found in the TP-Link UB500 USB dongle and similar
devices.  Intended for use with linux-libre.

## What this is

The Linux kernel's `btrtl` driver requires a firmware blob at
`/lib/firmware/rtl_bt/rtl8761bu_fw.bin` to operate the RTL8761BU.
This project replaces that blob with source-built MIPS16e code.

The chip's internal silicon ROM already contains a complete Bluetooth
stack.  The proprietary firmware blob overrides 21 functions in that
stack via DRAM function-pointer slots.  This replacement firmware runs
the required hardware initialisation (calling 25 ROM functions) and
then returns without installing any overrides, letting the ROM defaults
handle everything.

## Hardware

```
CPU  : MIPS16e (MIPS32r2 base, little-endian)
PRAM : 0x8010A000 – 0x80110C9F  (27808 bytes, overlays ROM)
DRAM : 0x80120000 – 0x80133FFF
ROM  : 0x80000000 – 0x80DFFFFF  (silicon, not a loadable blob)
```

## Building

### With Docker (recommended, fully reproducible)

```bash
docker build -t rtl8761bu-libre .
docker run --rm -v "$(pwd)":/work rtl8761bu-libre
```

Output: `rtl8761bu_fw.bin` and `rtl8761bu_config.bin` in the current
directory.

### Without Docker (requires mipsel cross-toolchain)

```bash
# Debian/Ubuntu:
sudo apt-get install gcc-mipsel-linux-gnu binutils-mipsel-linux-gnu make python3
make
```

### Verify the build

```bash
make verify   # print EPatch header fields
make size     # show how much PRAM the code uses
```

## Installing

```bash
# Back up the original blob first
sudo cp /lib/firmware/rtl_bt/rtl8761bu_fw.bin \
        /lib/firmware/rtl_bt/rtl8761bu_fw.bin.orig

# Install the libre firmware
sudo cp rtl8761bu_fw.bin    /lib/firmware/rtl_bt/rtl8761bu_fw.bin
sudo cp rtl8761bu_config.bin /lib/firmware/rtl_bt/rtl8761bu_config.bin

# Unplug and re-plug the dongle (or reload the driver)
sudo rmmod btusb && sudo modprobe btusb
```

## Testing

```bash
dmesg | grep -E "RTL|hci0"
hciconfig
hcitool dev
```

**Phase 1 success** looks like:
```
Bluetooth: hci0: RTL: loading rtl_bt/rtl8761bu_fw.bin
Bluetooth: hci0: RTL: fw version 0x09a98a6b
hci0:   Type: Primary  Bus: USB
        BD Address: XX:XX:XX:XX:XX:XX  ACL MTU: ...
```

**Phase 1 failure** (ROM defaults insufficient) looks like one of:
- `command 0xfc20 tx timeout` → hardware init ROM calls are wrong; verify addresses
- Device comes up but Bluetooth scan/pairing fails → specific hooks needed

## Phase 2: adding hook implementations

If Phase 1 is insufficient, identify which of the 21 DRAM hook slots
the failing feature routes through, reimplement that function in a new
`.c` file, and install it via `write_hook()` in `patch_entry()`.

### Hook table (DRAM slot → original patch function offset)

| Hook | DRAM slot     | Patch fn at    | Notes                         |
|------|---------------|----------------|-------------------------------|
|  23  | 0x80121414    | PRAM+0x04F4    | First slot called by BT stack |
|  22  | 0x80120CF8    | PRAM+0x0504    |                               |
|  12  | 0x8012136C    | PRAM+0x0668    |                               |
|   7  | 0x801286C0    | PRAM+0x1254    |                               |
|  10  | 0x8012088C    | PRAM+0x12DC    |                               |
|  39  | 0x80120960    | PRAM+0x1A00    |                               |
|  31  | 0x80121458    | PRAM+0x1040    |                               |
|  24  | 0x801213DC    | PRAM+0x1610    |                               |
|  11  | 0x80121368    | PRAM+0x26F8    |                               |
|  21  | 0x80120F3C    | PRAM+0x25F0    |                               |
|  26  | 0x80120590    | PRAM+0x299C    |                               |
|  25  | 0x80121348    | PRAM+0x29FC    |                               |
|  14  | 0x80121344    | PRAM+0x2BF0    |                               |
|  13  | 0x80121360    | PRAM+0x2CF4    |                               |
|   3  | 0x801206AC    | PRAM+0x1350    |                               |
|  27  | 0x8012067C    | PRAM+0x3534    |                               |
|  15  | 0x80125550    | PRAM+0x1D20    |                               |
|  32  | 0x80121410    | PRAM+0x4F30    |                               |
|  29  | 0x801213E8    | PRAM+0x4FFC    |                               |
|  28  | 0x80120F4C    | PRAM+0x50E0    |                               |
|  30  | 0x801213C8    | PRAM+0x51B4    |                               |

### Example stub (src/hook_23.c)

```c
/* Hook 23 — DRAM slot 0x80121414
 * Disassemble original at PRAM+0x04F4 in Ghidra (select MIPS16e,
 * load rtl8761bu_fw.bin at base 0x8010A000) to understand what this
 * function must do, then reimplement it here.
 */
#include <stdint.h>

__attribute__((mips16))
void hook_23(void)
{
    /* TODO: reimplement */
}
```

Then in `patch_entry()` in `src/init.c`:
```c
    write_hook(0x80121414, hook_23);
```

And add `src/hook_23.c` to the Makefile's compile step.

### Function pointer convention

All functions stored in DRAM slots must be MIPS16e (`__attribute__((mips16))`).
`write_hook()` sets the MIPS16e interwork bit (LSB=1) automatically.

## Verifying ROM call addresses

The 25 hardware-init ROM function addresses in `src/init.c` were
extracted by analysing the original firmware's init function.  Before
fully trusting them, verify each address in Ghidra:

1. Load `rtl8761bu_fw.bin` as a raw binary.
2. Set base address: `0x8010A000`.
3. Select language: **MIPS → MIPS16e → LE → default**.
4. Disassemble from `0x8010A000`.
5. Confirm JAL/JALX targets match the addresses in `src/init.c`.

ROM addresses marked `/* UNVERIFIED */` in `src/init.c` are the most
important to check before declaring this a complete deblob.

## Licence

SPDX-License-Identifier: GPL-2.0-or-later

This project contains no binary blobs.  The ROM function addresses
referred to in `src/init.c` are addresses of functions in the chip's
silicon ROM (hardware), not loadable firmware.
