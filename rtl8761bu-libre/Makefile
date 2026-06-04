# Makefile — rtl8761bu-libre
#
# Builds a source-only replacement for rtl8761bu_fw.bin suitable for
# linux-libre.  No binary blobs are used or required.
#
# Usage:
#   make              — build rtl8761bu_fw.bin + rtl8761bu_config.bin
#   make verify       — print EPatch header fields for sanity check
#   make size         — show how much PRAM the firmware uses
#   make clean        — remove all build artefacts

# ── Toolchain ──────────────────────────────────────────────────────────
# Debian/Ubuntu: apt-get install gcc-mipsel-linux-gnu binutils-mipsel-linux-gnu
CC      := mipsel-linux-gnu-gcc
OBJCOPY := mipsel-linux-gnu-objcopy
SIZE    := mipsel-linux-gnu-size
PYTHON  := python3

# ── Compiler flags ─────────────────────────────────────────────────────
#   -mips32r2    : base ISA (required for MIPS16e extension)
#   -mips16      : generate MIPS16e instructions
#   -EL          : little-endian (default for mipsel- prefix, explicit here)
#   -msoft-float : no FPU on this chip
#   -G 0         : do not use global pointer ($gp) for small-data access;
#                  we have no GP set up and no global data anyway
#   -nostdlib    : no libc, no crt0
#   -ffreestanding: bare-metal; do not assume hosted environment
#   -fno-pic     : no position-independent code
#   -fno-builtin : do not replace calls with compiler built-ins
#   -Os          : optimise for size (PRAM is limited)
CFLAGS  := -mips32r2 -mips16 -EL -msoft-float -G 0 \
           -nostdlib -ffreestanding -fno-pic -fno-builtin \
           -Os -Wall -Wextra

LDFLAGS := -T rtl8761bu.ld -nostdlib -static

# ── EPatch constants ────────────────────────────────────────────────────
# Must match the values the btrtl driver expects in the EPatch header.
# chip_id = rom_version + 1 = 2  (confirmed from dmesg: rom_version=1)
# fw_version = 0x09a98a6b        (from original EPatch header)
CHIP_ID    := 2
FW_VERSION := 0x09a98a6b

# Standard PRAM size for RTL8761BU.  The pad.py script fills unused
# space with MIPS16e NOPs (bytes 00 65) and appends the version footer.
PATCH_SIZE := 27808

# ── Build targets ──────────────────────────────────────────────────────
.PHONY: all clean verify size

all: rtl8761bu_fw.bin rtl8761bu_config.bin

# Step 1: compile to ELF
build/patch.elf: src/init.c rtl8761bu.ld
	@mkdir -p build
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $<

# Step 2: strip to raw binary
build/patch.bin: build/patch.elf
	$(OBJCOPY) -O binary $< $@

# Step 3: pad to PATCH_SIZE with MIPS16e NOPs + version footer
build/patch_padded.bin: build/patch.bin pad.py
	$(PYTHON) pad.py $< $(PATCH_SIZE) $@

# Step 4: wrap in EPatch v2 envelope
rtl8761bu_fw.bin: build/patch_padded.bin pack.py
	$(PYTHON) pack.py --chip-id $(CHIP_ID) $< $(FW_VERSION) $@
	@echo ""
	@echo "Built $@ — install with:"
	@echo "  sudo cp $@ /lib/firmware/rtl_bt/rtl8761bu_fw.bin"

# Step 5: generate the 6-byte config (no binary blob needed)
rtl8761bu_config.bin: gen_config.py
	$(PYTHON) gen_config.py $@
	@echo "Built $@ — install with:"
	@echo "  sudo cp $@ /lib/firmware/rtl_bt/rtl8761bu_config.bin"

# ── Utility targets ────────────────────────────────────────────────────
verify: rtl8761bu_fw.bin
	@$(PYTHON) -c "\
import struct; \
d = open('rtl8761bu_fw.bin','rb').read(); \
magic = d[:8]; \
fv = struct.unpack_from('<I',d,8)[0]; \
n  = struct.unpack_from('<H',d,12)[0]; \
cids = [struct.unpack_from('<H',d,14+i*2)[0] for i in range(n)]; \
lens = [struct.unpack_from('<H',d,14+n*2+i*2)[0] for i in range(n)]; \
offs = [struct.unpack_from('<I',d,14+n*4+i*4)[0] for i in range(n)]; \
ext  = struct.unpack_from('<I',d,len(d)-4)[0]; \
print(f'magic      : {magic}'); \
print(f'fw_version : 0x{fv:08x}'); \
[print(f'  patch[{i}]  chip_id={cids[i]}  length={lens[i]}  offset=0x{offs[i]:04x}{\" ← selected\" if cids[i]==$(CHIP_ID) else \"\"}') for i in range(n)]; \
print(f'ext magic  : 0x{ext:08x}  {\"OK\" if ext==0x77fd0451 else \"WRONG — driver will reject this\"}'); \
"

size: build/patch.elf
	@echo "=== Firmware size breakdown ==="
	$(SIZE) $<
	@$(PYTHON) -c "\
import os; \
raw = os.path.getsize('build/patch.bin'); \
avail = $(PATCH_SIZE); \
print(f'Raw code+data : {raw} bytes'); \
print(f'PRAM budget   : {avail} bytes'); \
print(f'Free (NOPs)   : {avail - raw - 4} bytes  ({(avail-raw-4)/avail*100:.1f}%)'); \
"

clean:
	rm -rf build
	rm -f rtl8761bu_fw.bin rtl8761bu_config.bin
