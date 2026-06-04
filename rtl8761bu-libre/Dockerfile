# Dockerfile — rtl8761bu-libre build environment
#
# Provides a reproducible build environment for the MIPS16e libre
# firmware.  Uses Debian bookworm's mipsel cross-toolchain.
#
# Build the image:
#   docker build -t rtl8761bu-libre .
#
# Build the firmware (bind-mount the source tree):
#   docker run --rm -v "$(pwd)":/work rtl8761bu-libre
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
      -mips32r2 -mips16 -EL -msoft-float -G 0 \
      -nostdlib -ffreestanding -fno-pic \
      -x c -c -o /dev/null - \
 && echo "MIPS16e toolchain: OK"

WORKDIR /work

# Default: build everything.
CMD ["make", "all"]
