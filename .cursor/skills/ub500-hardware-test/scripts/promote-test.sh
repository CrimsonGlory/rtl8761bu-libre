#!/usr/bin/env bash
# promote-test.sh — copy a built profile into fw_to_test/ for NeoPC scp
#
# Usage: promote-test.sh <profile>
#   profile: test-nf | minimal | full | phase-bss | … (see rtl8761bu_fw.<profile>.bin)

set -euo pipefail

REPO="$(cd "$(dirname "$0")/../../../.." && pwd)"
BUILD="$REPO/rtl8761bu-libre"
STAGE="$REPO/fw_to_test"
PROFILE="${1:?usage: promote-test.sh <profile>}"

FW_SRC="$BUILD/rtl8761bu_fw.${PROFILE}.bin"
CFG_SRC="$BUILD/rtl8761bu_config.${PROFILE}.bin"

if [[ ! -f "$FW_SRC" ]]; then
  # test-nf / fresh build may only exist as rtl8761bu_fw.bin in BUILD
  if [[ "$PROFILE" == "test-nf" && -f "$BUILD/rtl8761bu_fw.bin" ]]; then
    cp "$BUILD/rtl8761bu_fw.bin" "$FW_SRC"
    cp "$BUILD/rtl8761bu_config.bin" "$CFG_SRC"
  else
    echo "missing $FW_SRC (run make / stage-test.sh first)" >&2
    exit 1
  fi
fi

mkdir -p "$STAGE"
cp "$FW_SRC" "$STAGE/rtl8761bu_fw.bin"
cp "$CFG_SRC" "$STAGE/rtl8761bu_config.bin"

echo "Staged $PROFILE → $STAGE/"
sha256sum "$STAGE/rtl8761bu_fw.bin" "$STAGE/rtl8761bu_config.bin"
