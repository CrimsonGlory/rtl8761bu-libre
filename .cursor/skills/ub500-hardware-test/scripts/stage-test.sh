#!/usr/bin/env bash
# stage-test.sh — build profiles and stage the first as rtl8761bu_fw.bin
#
# Usage:
#   stage-test.sh <profile> [profile ...]
#
# Profiles: test-nf | minimal-null | minimal | full | hybrid | libre-hybrid
# hybrid SPLIT=0x400       → vendor[:N] + libre[N:]
# libre-hybrid SPLIT=0x200 → libre[:N] + vendor[N:]

set -euo pipefail

REPO="$(cd "$(dirname "$0")/../../../.." && pwd)"
ROOT="$REPO/rtl8761bu-libre"
STAGE="$REPO/fw_to_test"
NF_DIR="$REPO/rtl8761bu-non-free"
IMAGE=rtl8761bu-libre
NF_REF=/nf_ref/rtl8761bu_fw.bin

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <profile> [profile ...]" >&2
  echo "  profiles: test-nf minimal-null minimal full hybrid" >&2
  echo "  hybrid:   hybrid SPLIT=0x400" >&2
  exit 1
fi

docker build -t "$IMAGE" "$ROOT" >/dev/null

profile_file_name() {
  local spec="$1"
  case "$spec" in hybrid*|libre-hybrid*) echo "${spec// /-}" ;; phase-*) echo "$spec" ;; *) echo "$spec" ;; esac
}

build_profile() {
  local spec="$1"
  local make_cmd profile_name
  profile_name="$(profile_file_name "$spec")"

  case "$spec" in
    test-nf)      make_cmd="make clean && make test-nf" ;;
    minimal-null) make_cmd="make clean && make minimal-null" ;;
    minimal)      make_cmd="make clean && make minimal" ;;
    full)         make_cmd="make docker" ;;
    hybrid*)
      local split="${spec#hybrid }"
      make_cmd="make clean && make docker && make ${split} hybrid"
      ;;
    libre-hybrid*)
      local split="${spec#libre-hybrid }"
      make_cmd="make clean && make docker && make ${split} libre-hybrid"
      ;;
    phase-*)
      local ph="${spec#phase-}"
      make_cmd="make phase-${ph}"
      ;;
    *)
      echo "unknown profile: $spec" >&2
      exit 1
      ;;
  esac

  echo "== build: $spec ==" >&2
  docker run --rm -v "$ROOT":/work -v "$NF_DIR":/nf_ref:ro -e NF_REF="$NF_REF" \
    "$IMAGE" sh -c "$make_cmd && sha256sum rtl8761bu_fw.bin rtl8761bu_config.bin" >&2

  cp "$ROOT/rtl8761bu_fw.bin" "$ROOT/rtl8761bu_fw.${profile_name}.bin"
  cp "$ROOT/rtl8761bu_config.bin" "$ROOT/rtl8761bu_config.${profile_name}.bin"
  sha256sum "$ROOT/rtl8761bu_fw.${profile_name}.bin" | awk '{print $1}'
}

PROFILES=("$@")
SHA_LINES=()
for p in "${PROFILES[@]}"; do
  SHA_LINES+=("$(build_profile "$p")")
done

FIRST_NAME="$(profile_file_name "${PROFILES[0]}")"
mkdir -p "$STAGE"
cp "$ROOT/rtl8761bu_fw.${FIRST_NAME}.bin" "$STAGE/rtl8761bu_fw.bin"
cp "$ROOT/rtl8761bu_config.${FIRST_NAME}.bin" "$STAGE/rtl8761bu_config.bin"

QUEUE="$ROOT/test-queue.txt"
{
  echo "# UB500 test queue — ACTIVE = fw_to_test/rtl8761bu_fw.bin"
  echo "# NeoPC: try_new_firmware.sh scp's from daas-dev:.../fw_to_test/"
  echo ""
  for i in "${!PROFILES[@]}"; do
    p="${PROFILES[$i]}"
    pn="$(profile_file_name "$p")"
    mark="queued"
    [[ $i -eq 0 ]] && mark="ACTIVE"
    printf '%s  %s  %s  rtl8761bu_fw.%s.bin\n' "$mark" "$p" "${SHA_LINES[$i]}" "$pn"
  done
} > "$QUEUE"

echo ""
echo "Staged active: ${PROFILES[0]} → $STAGE/rtl8761bu_fw.bin"
sha256sum "$STAGE/rtl8761bu_fw.bin"
echo "Queue: $QUEUE"
cat "$QUEUE"
