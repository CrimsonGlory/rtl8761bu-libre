# Dockerfile — rtl8761bu-libre build environment
#
# Provides a reproducible build environment for the MIPS16e libre
# firmware.  Uses Debian bookworm's mipsel cross-toolchain.
#
# Build the image:
#   docker build -t rtl8761bu-libre .
#
# Build the firmware (bind-mount the source tree):
#   docker run --rm -v "$(pwd)":/work rtl8761bu-libre make docker
#
# CI / linux-libre compliance gate (no NF_REF required):
#   docker run --rm -v "$(pwd)":/work rtl8761bu-libre make compliance-ci
#
# The output files will appear in the host's working directory:
#   rtl8761bu_fw.bin
#   rtl8761bu_config.bin

FROM debian:bookworm-slim

# Install MIPS little-endian cross-toolchain + build utilities.
# gcc-mipsel-linux-gnu 12.2.0 ships with -mips16 support.
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
        gcc-mipsel-linux-gnu \
        binutils-mipsel-linux-gnu \
        make \
        python3 \
 && rm -rf /var/lib/apt/lists/*

# Smoke-test that MIPS16e compilation works before any user hits a
# confusing error at build time.
RUN echo 'void f(void){}' \
  | mipsel-linux-gnu-gcc \
      -mips32r2 -EL -msoft-float -G 0 \
      -nostdlib -nostartfiles -ffreestanding -fno-pic \
      -x c -c -o /dev/null - \
 && echo "MIPS16e toolchain: OK"

WORKDIR /work

# Default: clean build + verify (avoids stale host build/ artifacts).
CMD ["make", "docker"]
