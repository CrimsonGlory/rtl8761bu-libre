#!/usr/bin/env bash
# archive-hw-pass.sh — store NeoPC-validated firmware under artifacts/hw-validated/
#
# Usage:
#   archive-hw-pass.sh <profile> <gate> ["notes"]
#   archive-hw-pass.sh --list [sha-prefix]
#   archive-hw-pass.sh --restore <sha-prefix|full-sha>
#
# Gates: fc20 | hci | connect | vendor-ref | bisect
#
# Called by the agent after NeoPC PASS (see artifacts/README.md).
# promote-test.sh --restore delegates here.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
BUILD="$REPO/rtl8761bu-libre"
STAGE="$REPO/fw_to_test"
ARCHIVE="$REPO/artifacts/hw-validated"
MANIFEST="$REPO/artifacts/manifest.tsv"

sha256_file() {
  sha256sum "$1" | awk '{print $1}'
}

ensure_manifest_header() {
  if [[ ! -f "$MANIFEST" ]]; then
    printf 'date\tprofile\tgate\tfw_sha256\tpatch_sha256\tconfig_sha256\tnotes\n' >"$MANIFEST"
  elif ! head -1 "$MANIFEST" | grep -q fw_sha256; then
    echo "manifest.tsv missing header" >&2
    exit 1
  fi
}

manifest_has_sha() {
  local sha="$1"
  grep -qF "$sha" "$MANIFEST" 2>/dev/null || return 1
}

resolve_fw_src() {
  local profile="$1"
  if [[ -f "$STAGE/rtl8761bu_fw.bin" ]]; then
    echo "$STAGE/rtl8761bu_fw.bin"
  elif [[ -n "$profile" && -f "$BUILD/rtl8761bu_fw.${profile}.bin" ]]; then
    echo "$BUILD/rtl8761bu_fw.${profile}.bin"
  elif [[ -f "$BUILD/rtl8761bu_fw.bin" ]]; then
    echo "$BUILD/rtl8761bu_fw.bin"
  else
    echo ""
  fi
}

resolve_cfg_src() {
  local profile="$1"
  if [[ -f "$STAGE/rtl8761bu_config.bin" ]]; then
    echo "$STAGE/rtl8761bu_config.bin"
  elif [[ -n "$profile" && -f "$BUILD/rtl8761bu_config.${profile}.bin" ]]; then
    echo "$BUILD/rtl8761bu_config.${profile}.bin"
  elif [[ -f "$BUILD/rtl8761bu_config.bin" ]]; then
    echo "$BUILD/rtl8761bu_config.bin"
  else
    echo ""
  fi
}

cmd_list() {
  local prefix="${1:-}"
  ensure_manifest_header
  if [[ -z "$prefix" ]]; then
    cat "$MANIFEST"
    return
  fi
  head -1 "$MANIFEST"
  tail -n +2 "$MANIFEST" | grep -i "^[^\t]*\t[^\t]*\t[^\t]*\t${prefix}" || true
}

cmd_restore() {
  local want="${1:?usage: --restore <sha-prefix>}"
  ensure_manifest_header
  local line
  line="$(tail -n +2 "$MANIFEST" | awk -F'\t' -v w="$want" '$4 ~ w {print; exit}')"
  if [[ -z "$line" ]]; then
    # try filename prefix in archive dir
    local fw
    fw="$(find "$ARCHIVE" -maxdepth 1 -name "rtl8761bu_fw.${want}*.bin" 2>/dev/null | head -1)"
    if [[ -z "$fw" ]]; then
      echo "no archive for SHA prefix: $want" >&2
      exit 1
    fi
    local sha
    sha="$(basename "$fw" | sed 's/^rtl8761bu_fw\.//;s/\.bin$//')"
  else
    sha="$(echo "$line" | awk -F'\t' '{print $4}')"
    fw="$ARCHIVE/rtl8761bu_fw.${sha}.bin"
  fi
  local cfg="$ARCHIVE/rtl8761bu_config.${sha}.bin"
  if [[ ! -f "$fw" ]]; then
    echo "missing $fw" >&2
    exit 1
  fi
  mkdir -p "$STAGE"
  cp "$fw" "$STAGE/rtl8761bu_fw.bin"
  if [[ -f "$cfg" ]]; then
    cp "$cfg" "$STAGE/rtl8761bu_config.bin"
  fi
  echo "Restored $sha → $STAGE/"
  sha256sum "$STAGE/rtl8761bu_fw.bin" "${STAGE}/rtl8761bu_config.bin" 2>/dev/null || sha256sum "$STAGE/rtl8761bu_fw.bin"
}

cmd_archive() {
  local profile="${1:?usage: archive-hw-pass.sh <profile> <gate> [notes]}"
  local gate="${2:?usage: archive-hw-pass.sh <profile> <gate> [notes]}"
  local notes="${3:-}"

  case "$gate" in
    fc20|hci|connect|vendor-ref|bisect) ;;
    *)
      echo "unknown gate: $gate (use fc20|hci|connect|vendor-ref|bisect)" >&2
      exit 1
      ;;
  esac

  local fw_src cfg_src
  fw_src="$(resolve_fw_src "$profile")"
  cfg_src="$(resolve_cfg_src "$profile")"
  if [[ -z "$fw_src" || ! -f "$fw_src" ]]; then
    echo "no firmware found for profile=$profile" >&2
    exit 1
  fi
  if [[ -z "$cfg_src" || ! -f "$cfg_src" ]]; then
    echo "no config found for profile=$profile" >&2
    exit 1
  fi

  local fw_sha cfg_sha patch_sha patch_src
  fw_sha="$(sha256_file "$fw_src")"
  cfg_sha="$(sha256_file "$cfg_src")"
  patch_sha=""
  patch_src="$BUILD/build/patch_padded.bin"
  if [[ -f "$patch_src" ]]; then
    patch_sha="$(sha256_file "$patch_src")"
  fi

  mkdir -p "$ARCHIVE"
  local fw_dst="$ARCHIVE/rtl8761bu_fw.${fw_sha}.bin"
  local cfg_dst="$ARCHIVE/rtl8761bu_config.${fw_sha}.bin"

  if [[ -f "$fw_dst" ]]; then
    echo "already archived: $fw_sha"
  else
    cp "$fw_src" "$fw_dst"
    cp "$cfg_src" "$cfg_dst"
    echo "archived fw+config → $ARCHIVE/"
  fi

  if [[ -n "$patch_sha" ]]; then
    local patch_dst="$ARCHIVE/patch_padded.${patch_sha}.bin"
    if [[ ! -f "$patch_dst" ]]; then
      cp "$patch_src" "$patch_dst"
      echo "archived patch_padded.${patch_sha}.bin"
    fi
  fi

  ensure_manifest_header
  if manifest_has_sha "$fw_sha"; then
    echo "manifest already lists $fw_sha"
  else
    printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
      "$(date -Iseconds)" "$profile" "$gate" "$fw_sha" "${patch_sha:-}" "$cfg_sha" "$notes" >>"$MANIFEST"
    echo "manifest.tsv +1 row"
  fi

  echo "fw_sha256=$fw_sha"
  [[ -n "$patch_sha" ]] && echo "patch_sha256=$patch_sha"
}

main() {
  case "${1:-}" in
    --list)
      cmd_list "${2:-}"
      ;;
    --restore)
      cmd_restore "${2:?}"
      ;;
    -h|--help)
      sed -n '2,12p' "$0"
      ;;
    *)
      cmd_archive "$@"
      ;;
  esac
}

main "$@"
