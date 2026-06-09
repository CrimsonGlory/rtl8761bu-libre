#!/usr/bin/env bash
# split-bisect.sh — next SPLIT and libre/vendor diff for byte-split bisect
set -euo pipefail

PATCH1_BASE=0x8010A000
PATCH1_OFF=0x3780
PATCH1_LEN=27808

usage() {
  echo "Usage: $0 next <fail_lo_hex> <pass_hi_hex>" >&2
  echo "       $0 diff <lo_hex> <hi_hex> <libre_padded.bin> <vendor_fw.bin>" >&2
  exit 1
}

[[ $# -ge 1 ]] || usage

cmd=$1
shift

case "$cmd" in
  next)
    [[ $# -eq 2 ]] || usage
    fail_lo=$(printf '%d' "$1")
    pass_hi=$(printf '%d' "$2")
    if (( fail_lo >= pass_hi )); then
      echo "error: fail_lo ($1) >= pass_hi ($2)" >&2
      exit 1
    fi
    mid=$(( (fail_lo + pass_hi) / 2 ))
    # align down to 16 for MIPS16e-ish boundaries (optional; keeps splits stable)
    mid=$(( mid & ~0xf ))
    if (( mid <= fail_lo )); then
      mid=$(( fail_lo + 0x10 ))
    fi
    if (( mid >= pass_hi )); then
      echo "INTERVAL_DONE width=$(( pass_hi - fail_lo ))" >&2
      printf '0x%x\n' "$pass_hi"
      exit 0
    fi
    printf '0x%x\n' "$mid"
    ;;

  diff)
    [[ $# -eq 4 ]] || usage
    lo=$(printf '%d' "$1")
    hi=$(printf '%d' "$2")
    libre=$3
    vendor_fw=$4
    python3 - "$lo" "$hi" "$PATCH1_BASE" "$PATCH1_OFF" "$libre" "$vendor_fw" <<'PY'
import sys
lo, hi = int(sys.argv[1]), int(sys.argv[2])
base = int(sys.argv[3], 0)
off_p = int(sys.argv[4], 0)
libre_path, nf_path = sys.argv[5], sys.argv[6]
with open(nf_path, "rb") as f:
    f.seek(off_p)
    v = f.read(hi + 0x1000)
with open(libre_path, "rb") as f:
    l = f.read()
diff = [i for i in range(lo, hi) if v[i] != l[i]]
print(f"interval [0x{lo:x}, 0x{hi:x}) runtime [{base+lo:#x}, {base+hi:#x})")
print(f"width {hi-lo} B, libre!=vendor on {len(diff)} B")
s = diff[0] if diff else None
p = s
for i in diff[1:] + [None]:
    if i is None or i != p + 1:
        if s is not None:
            print(f"  {base+s:#x} len={p-s+1}")
        s = i
    if i is not None:
        p = i
if lo < len(v) and lo < len(l):
    chunk = min(64, hi - lo)
    print(f"vendor: {v[lo:lo+chunk].hex()}")
    print(f"libre:  {l[lo:lo+chunk].hex()}")
PY
    ;;

  *)
    usage
    ;;
esac
