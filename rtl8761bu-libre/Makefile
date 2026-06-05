# Makefile — rtl8761bu-libre
#
# Builds a source-only replacement for rtl8761bu_fw.bin.
# No binary blobs used or required.
#
# Usage:
#   make              — build rtl8761bu_fw.bin + rtl8761bu_config.bin
#   make verify       — print EPatch header fields for sanity check
#   make size         — show PRAM usage
#   make clean        — remove build artefacts

# ── Toolchain ──────────────────────────────────────────────────────────
# Debian/Ubuntu: apt-get install gcc-mipsel-linux-gnu binutils-mipsel-linux-gnu
CC      := mipsel-linux-gnu-gcc
OBJCOPY := mipsel-linux-gnu-objcopy
SIZE    := mipsel-linux-gnu-size
PYTHON  := python3

# ── Source files ───────────────────────────────────────────────────────
# All C files in src/ are compiled. Add new hook implementations there.
SRCS := $(wildcard src/*.c) $(wildcard src/*.S)

# ── Compiler flags ─────────────────────────────────────────────────────
#   -mips32r2      : base ISA (MIPS16e is an extension of MIPS32r2)
#   (no -mips16)  : Binutils 2.40 BFD bug fires on MIPS16e + -nostdlib;
#                    compile as plain MIPS32r2 instead
#   -EL            : little-endian (explicit; mipsel- prefix implies it)
#   -msoft-float   : no FPU on this chip
#   -G 0           : do not use $gp for small-data; no GP is set up
#   -nostdlib      : do not link any standard library
#   -nostartfiles  : do not inject crt0/crti/crtn from the Linux target
#   -ffreestanding : bare-metal; do not assume a hosted environment
#   -fno-pic       : no position-independent code (PRAM is at fixed address)
#   -fno-builtin   : do not replace calls with compiler built-ins
#   -Os            : optimise for size (PRAM budget is 27808 bytes)
CFLAGS  := -mips32r2 -EL -msoft-float -G 0 \
           -nostdlib -nostartfiles -ffreestanding \
           -fno-pic -fno-builtin \
           -Os -Wall -Wextra

LDFLAGS := -T rtl8761bu.ld -nostdlib -nostartfiles

# ── EPatch constants ────────────────────────────────────────────────────
# chip_id  = rom_version + 1 = 2  (dmesg: rom_version=1 for RTL8761BU)
# fw_version = 0x09a98a6b          (from original EPatch header)
CHIP_ID    := 2
FW_VERSION := 0x09a98a6b

# Standard PRAM window for RTL8761BU.
# pad.py fills unused space with MIPS16e NOPs (00 65) and appends the
# version footer (0x09A95FD1) at the last 4 bytes.
PATCH_SIZE := 27808

# ── Targets ────────────────────────────────────────────────────────────
.PHONY: all clean verify size

all: rtl8761bu_fw.bin rtl8761bu_config.bin

# Step 1: compile all source files to a single ELF
build/patch.elf: $(SRCS) rtl8761bu.ld
	@mkdir -p build
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $(SRCS)

# Step 2: strip ELF to a raw binary
build/patch.bin: build/patch.elf
	$(OBJCOPY) -O binary $< $@

# Step 3: pad to PATCH_SIZE with MIPS16e NOPs + version footer
build/patch_padded.bin: build/patch.bin pad.py
	$(PYTHON) pad.py $< $(PATCH_SIZE) $@

# Step 4: wrap in EPatch v2 envelope
rtl8761bu_fw.bin: build/patch_padded.bin pack.py
	$(PYTHON) pack.py --chip-id $(CHIP_ID) $< $(FW_VERSION) $@
	@echo ""
	@echo "Built: $@"
	@echo "Install with:"
	@echo "  sudo cp $@ /lib/firmware/rtl_bt/rtl8761bu_fw.bin"

# Step 5: generate the 6-byte config from source (no blob needed)
rtl8761bu_config.bin: gen_config.py
	$(PYTHON) gen_config.py $@
	@echo "Built: $@"
	@echo "Install with:"
	@echo "  sudo cp $@ /lib/firmware/rtl_bt/rtl8761bu_config.bin"

# ── Utility targets ────────────────────────────────────────────────────
verify: rtl8761bu_fw.bin
	@$(PYTHON) -c "\
import struct; \
d=open('rtl8761bu_fw.bin','rb').read(); \
fv=struct.unpack_from('<I',d,8)[0]; \
n=struct.unpack_from('<H',d,12)[0]; \
cids=[struct.unpack_from('<H',d,14+i*2)[0] for i in range(n)]; \
lens=[struct.unpack_from('<H',d,14+n*2+i*2)[0] for i in range(n)]; \
offs=[struct.unpack_from('<I',d,14+n*4+i*4)[0] for i in range(n)]; \
ext=struct.unpack_from('<I',d,len(d)-4)[0]; \
print(f'magic      : {d[:8]}'); \
print(f'fw_version : 0x{fv:08x}'); \
[print(f'  patch[{i}] chip_id={cids[i]} length={lens[i]} offset=0x{offs[i]:x}{\" <- selected\" if cids[i]==$(CHIP_ID) else \"\"}') for i in range(n)]; \
print(f'ext magic  : 0x{ext:08x}  {\"OK\" if ext==0x77fd0451 else \"WRONG\"}'); \
"

size: build/patch.elf
	@echo "=== Firmware size ==="
	$(SIZE) $<
	@$(PYTHON) -c "\
import os; \
raw=os.path.getsize('build/patch.bin'); \
avail=$(PATCH_SIZE); \
print(f'Code+data : {raw} bytes'); \
print(f'Budget    : {avail} bytes'); \
print(f'Free      : {avail-raw-4} bytes  ({(avail-raw-4)/avail*100:.1f}% of PRAM)'); \
"

clean:
	rm -rf build
	rm -f rtl8761bu_fw.bin rtl8761bu_config.bin
