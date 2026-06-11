#!/usr/bin/env bash
# promote-test.sh — copy a built profile into fw_to_test/ for NeoPC scp
#
# Usage:
#   promote-test.sh <profile>
#   promote-test.sh --restore <sha-prefix> [profile-label]
#
# After NeoPC PASS, archive the validated build:
#   archive-hw-pass.sh <profile> <gate> "notes"
#
# Restore a previously archived PASS, then stage:
#   promote-test.sh --restore 62198d8c p4-libre

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
BUILD="$REPO/rtl8761bu-libre"
STAGE="$REPO/fw_to_test"
ARCHIVE_SH="$SCRIPT_DIR/archive-hw-pass.sh"

if [[ "${1:-}" == "--restore" ]]; then
  SHA="${2:?usage: promote-test.sh --restore <sha-prefix> [profile-label]}"
  PROFILE="${3:-restored}"
  "$ARCHIVE_SH" --restore "$SHA"
  echo "Staged restored $SHA → $STAGE/ (profile label: $PROFILE)"
  exit 0
fi

PROFILE="${1:?usage: promote-test.sh <profile> | promote-test.sh --restore <sha> [label]}"

FW_SRC="$BUILD/rtl8761bu_fw.${PROFILE}.bin"
CFG_SRC="$BUILD/rtl8761bu_config.${PROFILE}.bin"

if [[ ! -f "$FW_SRC" ]]; then
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
echo ""
echo "After NeoPC PASS, archive:"
echo "  $ARCHIVE_SH $PROFILE <gate> \"notes\""
echo "  gates: fc20 | hci | connect | vendor-ref | bisect"
