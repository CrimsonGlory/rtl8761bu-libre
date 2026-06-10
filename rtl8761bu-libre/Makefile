# Makefile — rtl8761bu-libre
#
# Builds a source-only replacement for rtl8761bu_fw.bin.
#
# Usage:
#   make docker             — clean + full build + verify (use inside Docker)
#   make minimal            — P0 smoke test (entry @ patch body offset 0)
#   make verify             — sanity-check header + entry fingerprint
#
# FC20 downloads the patch body to 0x8010A000; ROM calls 0x8010A001 (body+0).

CC      := mipsel-linux-gnu-gcc
OBJCOPY := mipsel-linux-gnu-objcopy
READELF := mipsel-linux-gnu-readelf
SIZE    := mipsel-linux-gnu-size
PYTHON  := python3

# MINIMAL=1 → P0 smoke test; MINIMAL=null → jr $ra only; see src/init_*.S
ifeq ($(MINIMAL),null)
SRCS := src/init_null.S
else ifdef MINIMAL
SRCS := src/init_minimal.S
else ifdef PHASE
SRCS := src/init_phase_$(PHASE).S $(filter-out src/stub_macros.S src/init.S src/init_minimal.S src/init_null.S $(wildcard src/init_phase_*.S),$(wildcard src/*.S))
else
# Installer tail [0x820,0xE4C) is transcribed in patch_entry_tail.S; drop duplicate bodies.
INSTALLER_TAIL_EXCLUDE := src/bdaddr.S src/callees.S src/hook_stubs.S src/lmp_vsc.S
SRCS := $(wildcard src/*.c) $(filter-out src/stub_macros.S src/init.S src/init_minimal.S src/init_null.S \
	src/init_phase_%.S src/installer_vendor_early.S src/sub_installer_2_body.S \
	src/patch_entry_code.S src/patch_entry_pool.S \
	src/patch_entry_helpers.S src/patch_entry_si2.S src/patch_entry_tail.S \
	$(INSTALLER_TAIL_EXCLUDE),$(wildcard src/*.S))
PATCH_PREFIX_SRCS := src/patch_entry_code.S src/patch_entry_pool.S \
	src/patch_entry_helpers.S src/patch_entry_si2.S src/patch_entry_tail.S
endif

# LAYOUT=gap — legacy experiment (entry @ +0x6880); wrong for FC20 → 0x8010A000
ifeq ($(LAYOUT),gap)
LDSCRIPT      := rtl8761bu-gap.ld
ENTRY_OFFSET  := 0x6880
BUILD_PROFILE := $(if $(filter $(MINIMAL),null),null-gap,$(if $(MINIMAL),minimal-gap,full-gap))
else
LDSCRIPT      := rtl8761bu.ld
ENTRY_OFFSET  := 0
BUILD_PROFILE := $(if $(PHASE),phase-$(PHASE),$(if $(filter $(MINIMAL),null),null,$(if $(MINIMAL),minimal,full)))
endif

CFLAGS  := -mips32r2 -EL -msoft-float -G 0 -mno-abicalls \
           -nostdlib -nostartfiles -ffreestanding \
           -fno-pic -fno-builtin \
           -Os -Wall -Wextra \
           -Isrc
ifndef MINIMAL
ifndef PHASE
CFLAGS  += -DINSTALLER_TAIL_TRANSCRIBED
endif
endif

LDFLAGS := -T $(LDSCRIPT) -nostdlib -nostartfiles

CHIP_ID    := 2
FW_VERSION := 0x09a98a6b
PATCH_SIZE := 27808

# Non-free reference — test-nf, diff-prefix, inject-*, hybrid-*, bisect only (not make all).
NF_REF ?= ../rtl8761bu-non-free/rtl8761bu_fw.bin

# Vendor patch1 prefix length for .incbin early installer (see installer_vendor_early.S).
# UB500 connect needs vendor bytes [0,0x764); omit init.S and early bootstrap when set.
VENDOR_EARLY_PREFIX ?=
ifdef VENDOR_EARLY_PREFIX
VENDOR_EARLY_SRCS := src/installer_vendor_early.S src/bootstrap.S src/callees.S \
	src/hook_stubs.S src/lmp_vsc.S src/shims.S src/version_check.S src/bdaddr.S
SRCS := $(VENDOR_EARLY_SRCS)
CFLAGS += -DVENDOR_EARLY_PREFIX=$(VENDOR_EARLY_PREFIX)
BUILD_PROFILE := full-vendor-early-$(VENDOR_EARLY_PREFIX)
endif

.PHONY: all docker release minimal minimal-null test-nf hybrid libre-hybrid full-inject-t1 inject-t1 full-inject-t2 inject-t2 full-inject-t3 inject-t3 full-inject-t3-pe5-vendor-tail inject-t3-pe5-vendor-tail full-inject-t3-vendor-inst inject-t3-vendor-inst full-inject-t3-vendor-tail inject-t3-vendor-tail full-inject-t3-vendor-tail-split inject-t3-vendor-tail-split full-vendor-patch1 inject-vendor-patch1 phase-% clean verify size diff-prefix compliance compliance-ci

# Docker builds need NF_REF for inject/bisect profiles and diff-prefix (not default pack).
DOCKER_NF_VOL := -v "$(abspath $(dir $(NF_REF)))":/nf_ref:ro
DOCKER_NF_ENV := -e NF_REF=/nf_ref/$(notdir $(NF_REF))

all: rtl8761bu_fw.bin rtl8761bu_config.bin

# Default Docker entry: discard stale host build/ then build + verify.
docker: clean all verify

# Linux-libre release path — no NF_REF mount required in Docker.
release: docker

# CI gate: clean build + verify + strict compliance (no NF_REF, full profile).
compliance-ci: docker
	$(PYTHON) scripts/compliance_check.py --no-nf-ref --release --strict

minimal:
	$(MAKE) clean
	$(MAKE) MINIMAL=1 all

minimal-null:
	$(MAKE) clean
	$(MAKE) MINIMAL=null all

# Splice vendor patch1 prefix + libre tail (bisect).  Example: make hybrid SPLIT=0x400
SPLIT ?= 0x400
hybrid: build/patch_padded.bin hybrid.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	$(PYTHON) hybrid.py build/patch_padded.bin $(NF_REF) $(SPLIT) build/patch_hybrid.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_hybrid.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=hybrid-$(SPLIT)" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# Installer phase bisect (stops early + PATCH_FINISH).  Example: make phase-bss
phase-%:
	$(MAKE) clean
	$(MAKE) PHASE=$* all

# libre patch1 prefix + vendor tail.  Example: make libre-hybrid SPLIT=0x200
libre-hybrid: build/patch_padded.bin hybrid.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	$(PYTHON) hybrid.py --libre-head build/patch_padded.bin $(NF_REF) $(SPLIT) build/patch_hybrid.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_hybrid.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=libre-hybrid-$(SPLIT)" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# Libre installer + vendor T1 hook bodies at native patch1 offsets (connect test)
full-inject-t1:
	$(MAKE) clean
	$(MAKE) all
	$(MAKE) inject-t1

inject-t1: build/patch_padded.bin inject_vendor.py vendor_t1_manifest.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	$(PYTHON) inject_vendor.py build/patch_padded.bin $(NF_REF) build/patch_injected.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_injected.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=full-inject-t1" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# Libre installer + vendor T1+T2 hook bodies (all INSTALL_HOOK stubs except LMP hooks)
full-inject-t2:
	$(MAKE) clean
	$(MAKE) all
	$(MAKE) inject-t2

inject-t2: build/patch_padded.bin inject_vendor.py vendor_t1_manifest.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	$(PYTHON) inject_vendor.py build/patch_padded.bin $(NF_REF) build/patch_injected.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_injected.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=full-inject-t2" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# T1+T2+T3: all hook bodies vendor-injected (libre installer + callees only)
full-inject-t3:
	$(MAKE) clean
	$(MAKE) all
	$(MAKE) inject-t3

inject-t3: build/patch_padded.bin inject_vendor.py vendor_t1_manifest.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	$(PYTHON) inject_vendor.py build/patch_padded.bin $(NF_REF) build/patch_injected.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_injected.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=full-inject-t3" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# PE-5b: libre patch_entry prefix [0,0x764) + T3 inject + vendor tail [0xE4C,footer)
full-inject-t3-pe5-vendor-tail:
	$(MAKE) clean
	$(MAKE) all
	$(MAKE) inject-t3-pe5-vendor-tail

inject-t3-pe5-vendor-tail: build/patch_padded.bin inject_vendor.py vendor_t1_manifest.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	VENDOR_TAIL_FILL=1 $(PYTHON) inject_vendor.py build/patch_padded.bin $(NF_REF) build/patch_injected.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_injected.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=full-inject-t3-pe5-vendor-tail" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# inject-t3 + vendor patch1 prefix (master installer + early bodies @ [0,0xE4C))
full-inject-t3-vendor-inst:
	$(MAKE) clean
	$(MAKE) all
	$(MAKE) inject-t3-vendor-inst

inject-t3-vendor-inst: build/patch_padded.bin inject_vendor.py vendor_t1_manifest.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	VENDOR_INSTALLER_PREFIX=1 $(PYTHON) inject_vendor.py build/patch_padded.bin $(NF_REF) build/patch_injected.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_injected.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=full-inject-t3-vendor-inst" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# inject-t3 + vendor tail [0xE4C,footer) — vendor early prefix [0,0x764) via .incbin + libre si2+
full-inject-t3-vendor-tail:
	$(MAKE) clean
	$(MAKE) all VENDOR_EARLY_PREFIX=0x764
	$(MAKE) inject-t3-vendor-tail

inject-t3-vendor-tail: build/patch_padded.bin inject_vendor.py vendor_t1_manifest.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	VENDOR_TAIL_FILL=1 $(PYTHON) inject_vendor.py build/patch_padded.bin $(NF_REF) build/patch_injected.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_injected.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=full-inject-t3-vendor-tail" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# inject-t3 + vendor tail + partial vendor installer prefix [0,SPLIT) (bisect)
SPLIT ?= 0x800
full-inject-t3-vendor-tail-split:
	$(MAKE) clean
	$(MAKE) all
	$(MAKE) inject-t3-vendor-tail-split

inject-t3-vendor-tail-split: build/patch_padded.bin inject_vendor.py vendor_t1_manifest.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	VENDOR_TAIL_FILL=1 VENDOR_PREFIX_SPLIT=$(SPLIT) $(PYTHON) inject_vendor.py build/patch_padded.bin $(NF_REF) build/patch_injected.bin
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_injected.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=full-inject-t3-vendor-tail-split-$(SPLIT)" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@echo "split=$(SPLIT)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# Vendor patch1 body (prefix + tail); libre pack header only — connect sanity / tail bisect
full-vendor-patch1:
	$(MAKE) clean
	$(MAKE) all
	$(MAKE) inject-vendor-patch1

inject-vendor-patch1: build/patch_padded.bin inject_vendor.py vendor_t1_manifest.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	VENDOR_INSTALLER_PREFIX=1 VENDOR_TAIL_FILL=1 $(PYTHON) inject_vendor.py build/patch_padded.bin $(NF_REF) build/patch_injected.bin
	@$(PYTHON) -c "import sys; nf=open('$(NF_REF)','rb').read(); inj=open('build/patch_injected.bin','rb').read(); ok=nf[0x3780:0x3780+27808]==inj; print('inject-vendor-patch1: patch1 body matches vendor OK' if ok else 'inject-vendor-patch1: WARN patch1 body differs from vendor'); sys.exit(0)"
	$(PYTHON) pack.py --dual --chip-id $(CHIP_ID) build/patch_injected.bin $(FW_VERSION) rtl8761bu_fw.bin
	@echo "profile=full-vendor-patch1" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	@sha256sum rtl8761bu_fw.bin

# Vendor reference image (bisect baseline — must match non-free byte-for-byte)
test-nf: $(NF_REF) gen_config.py
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	@mkdir -p build
	@echo "profile=test-nf" > build/.profile
	@echo "entry_offset=0" >> build/.profile
	cp $(NF_REF) rtl8761bu_fw.bin
	$(PYTHON) gen_config.py rtl8761bu_config.bin
	@sha256sum rtl8761bu_fw.bin
	@cmp -s rtl8761bu_fw.bin $(NF_REF) && echo "test-nf: vendor-identical OK"

# Rebuild when profile (minimal/full × layout) changes
build/.profile.stamp:
	@mkdir -p build
	@stamp="$(BUILD_PROFILE):$(ENTRY_OFFSET)"; \
	if ! test -f build/.profile.stamp || ! grep -qxF "$$stamp" build/.profile.stamp; then \
		echo "$$stamp" > build/.profile.stamp; \
		echo "profile=$(BUILD_PROFILE)" > build/.profile; \
		echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile; \
	fi

ifdef VENDOR_EARLY_PREFIX
build/vendor_prefix.bin: $(NF_REF) scripts/extract_vendor_prefix.py
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	@mkdir -p build
	$(PYTHON) scripts/extract_vendor_prefix.py $(NF_REF) $(VENDOR_EARLY_PREFIX) $@

VENDOR_PREFIX_DEP := build/vendor_prefix.bin
VENDOR_HELPERS_DEP :=
else
VENDOR_PREFIX_DEP :=
# PE-1: FUN_8010a000 code [0,0x242) — libre src/patch_entry_code.S (no incbin)
# PE-2: literal pool [0x242,0x5d8) — libre src/patch_entry_pool.S (no incbin)
# PE-3: helper cluster [0x5d8,0x724) — libre src/patch_entry_helpers.S (no incbin)
VENDOR_HELPERS_DEP :=
SRCS += $(PATCH_PREFIX_SRCS)
endif

build/patch.elf: $(SRCS) $(LDSCRIPT) build/.profile.stamp $(VENDOR_PREFIX_DEP) $(VENDOR_HELPERS_DEP)
	@mkdir -p build
	@if test -f $@ && ! $(READELF) -h $@ >/dev/null 2>&1; then \
		echo "patch.elf invalid (stale host artifact?), rebuilding"; \
		rm -f $@; \
	fi
	@echo "profile=$(BUILD_PROFILE)" > build/.profile
	@echo "entry_offset=$(ENTRY_OFFSET)" >> build/.profile
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $(SRCS)

build/patch.bin: build/patch.elf
	@$(READELF) -h $< >/dev/null 2>&1 || \
		(echo "error: $< is not a valid ELF — run 'make clean' or 'make docker'" >&2; exit 1)
	$(OBJCOPY) -O binary --only-section=.text --only-section=.text.entry \
		--gap-fill=0x00 $< $@

build/patch_padded.bin: build/patch.bin pad.py
	$(PYTHON) pad.py $< $(PATCH_SIZE) $@ $(ENTRY_OFFSET)

rtl8761bu_fw.bin: build/patch_padded.bin pack.py
	$(PYTHON) pack.py --chip-id $(CHIP_ID) $< $(FW_VERSION) $@
	@echo ""
	@echo "Built: $@  profile=$(BUILD_PROFILE)  entry_off=$(ENTRY_OFFSET)"
	@echo "  make verify   — check entry fingerprint before installing"
	@echo "  sudo cp $@ /lib/firmware/rtl_bt/rtl8761bu_fw.bin"

rtl8761bu_config.bin: gen_config.py
	$(PYTHON) gen_config.py $@

# Byte-diff libre patch.bin vs vendor patch1 prefix (patch_entry RE progress)
diff-prefix: build/patch.bin scripts/diff_vendor_prefix.py $(NF_REF)
	@test -f $(NF_REF) || (echo "missing $(NF_REF)" && exit 1)
	$(PYTHON) scripts/diff_vendor_prefix.py build/patch.bin --vendor-fw $(NF_REF)

# Audit non-free bytes, NF_REF deps (see analysis/reverse_engineering_linux_libre_compliance.md)
compliance: scripts/compliance_check.py
	$(PYTHON) scripts/compliance_check.py \
	  $(if $(wildcard $(NF_REF)),--nf-ref $(NF_REF),--no-nf-ref) \
	  $(if $(PATCH),--patch $(PATCH),) \
	  $(if $(RELEASE),--release,) $(if $(STRICT),--strict,)

verify: rtl8761bu_fw.bin
	@test -f build/.profile || (echo "run make first" && exit 1)
	@$(PYTHON) -c "\
import struct, re; \
prof=dict(re.findall(r'^(\w+)=(.+)$$', open('build/.profile').read(), re.M)); \
profile=prof.get('profile','?'); \
eoff=int(prof.get('entry_offset','0'), 0); \
d=open('rtl8761bu_fw.bin','rb').read(); \
assert d[:8]==b'Realtech'; \
fv=struct.unpack_from('<I',d,8)[0]; \
n=struct.unpack_from('<H',d,12)[0]; \
off=14; \
cids=[struct.unpack_from('<H',d,off+i*2)[0] for i in range(n)]; \
off+=n*2; \
lens=[struct.unpack_from('<H',d,off+i*2)[0] for i in range(n)]; \
off+=n*2; \
offs=[struct.unpack_from('<I',d,off+i*4)[0] for i in range(n)]; \
ext=struct.unpack_from('<I',d,len(d)-4)[0]; \
poff=offs[cids.index($(CHIP_ID))]; \
entry=d[poff+eoff:poff+eoff+4]; \
print(f'profile    : {profile}'); \
print(f'magic      : {d[:8]}'); \
print(f'fw_version : 0x{fv:08x}'); \
print(f'file size  : {len(d)}'); \
[print(f'  patch[{i}] chip_id={cids[i]} len={lens[i]} off=0x{offs[i]:x}{\" *\" if cids[i]==$(CHIP_ID) else \"\"}') for i in range(n)]; \
print(f'entry@+{eoff:#x}: {entry.hex()}'); \
print(f'  (check build/.profile for minimal vs full)'); \
print(f'ext magic  : 0x{ext:08x}  {\"OK\" if ext==0x77fd0451 else \"WRONG\"}'); \
"

size: build/patch.elf
	@echo "=== Firmware size (profile=$(BUILD_PROFILE)) ==="
	$(SIZE) $<
	@$(PYTHON) -c "\
import os; \
raw=os.path.getsize('build/patch.bin'); \
avail=$(PATCH_SIZE); \
print(f'Code+data : {raw} bytes'); \
print(f'Budget    : {avail} bytes'); \
print(f'Free      : {avail-raw-4} bytes'); \
"

clean:
	rm -rf build
	rm -f rtl8761bu_fw.bin rtl8761bu_config.bin
