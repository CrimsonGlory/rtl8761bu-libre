#!/usr/bin/env bash
# Persistent bisect state at fw_to_test/bisect-state.yaml
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../../../" && pwd)"
STATE_FILE="${BISECT_STATE:-$ROOT/fw_to_test/bisect-state.yaml}"
WIP_FILE="${WIP_FILE:-$ROOT/work-in-progress.txt}"

usage() {
  cat <<'EOF'
Usage: split-bisect-state.sh init [fail_lo pass_hi]
       split-bisect-state.sh show
       split-bisect-state.sh get <key>
       split-bisect-state.sh set <key> <value>
       split-bisect-state.sh apply-result <fail|ok|connect_ok> <split_hex>
       split-bisect-state.sh stage-pending <split_hex> <sha256>
       split-bisect-state.sh next-split
       split-bisect-state.sh is-done
       split-bisect-state.sh post-worker

Keys: status profile fail_lo pass_hi min_interval metric pending_split pending_sha256
      last_split last_result found_lo found_hi notes
EOF
}

py() {
  python3 - "$STATE_FILE" "$@" <<'PY'
import sys, os, re
from datetime import datetime, timezone

path = sys.argv[1]
cmd = sys.argv[2]

DEFAULTS = {
    "version": "1",
    "status": "active",
    "profile": "full-inject-t3-vendor-tail-split",
    "make_target": "full-inject-t3-vendor-tail-split",
    "fail_lo": "0x0",
    "pass_hi": "0xe4c",
    "min_interval": "0x10",
    "metric": "connect",
    "installer_end": "0xe4c",
    "patch1_base": "0x8010a000",
    "pending_split": "",
    "pending_sha256": "",
    "last_split": "",
    "last_result": "",
    "found_lo": "",
    "found_hi": "",
    "notes": "",
    "updated_at": "",
}

def parse_int(s):
    if not s:
        return 0
    return int(s, 0)

def load():
    d = dict(DEFAULTS)
    if os.path.isfile(path):
        with open(path) as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                if ":" not in line:
                    continue
                k, v = line.split(":", 1)
                k, v = k.strip(), v.strip().strip('"')
                if v.lower() in ("null", "~", ""):
                    v = ""
                d[k] = v
    return d

def save(d):
    os.makedirs(os.path.dirname(path) or ".", exist_ok=True)
    d["updated_at"] = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    order = [
        "version", "status", "profile", "make_target",
        "fail_lo", "pass_hi", "min_interval", "metric",
        "installer_end", "patch1_base",
        "pending_split", "pending_sha256",
        "last_split", "last_result",
        "found_lo", "found_hi", "notes", "updated_at",
    ]
    with open(path, "w") as f:
        f.write("# Byte-split bisect state — workers read/write; supervisor resumes from here.\n")
        for k in order:
            v = d.get(k, "")
            if v == "":
                f.write(f"{k}:\n")
            else:
                f.write(f'{k}: "{v}"\n')

def width(d):
    return parse_int(d["pass_hi"]) - parse_int(d["fail_lo"])

def interval_done(d):
    return width(d) <= parse_int(d["min_interval"])

if cmd == "init":
    d = dict(DEFAULTS)
    if len(sys.argv) > 3:
        d["fail_lo"] = sys.argv[3]
    if len(sys.argv) > 4:
        d["pass_hi"] = sys.argv[4]
    if interval_done(d):
        d["status"] = "interval_found"
        d["found_lo"] = d["fail_lo"]
        d["found_hi"] = d["pass_hi"]
    save(d)
    print(f"INIT_OK path={path}")

elif cmd == "show":
    if not os.path.isfile(path):
        print(f"missing: {path}", file=sys.stderr)
        sys.exit(2)
    print(open(path).read(), end="")

elif cmd == "get":
    d = load()
    key = sys.argv[3]
    print(d.get(key, ""))

elif cmd == "set":
    d = load()
    d[sys.argv[3]] = sys.argv[4]
    save(d)
    print(f"SET_OK {sys.argv[3]}={sys.argv[4]}")

elif cmd == "apply-result":
    result = sys.argv[3]
    split = parse_int(sys.argv[4])
    d = load()
    d["last_split"] = f"0x{split:x}"
    d["last_result"] = result
    d["pending_split"] = ""
    d["pending_sha256"] = ""
    fail_lo = parse_int(d["fail_lo"])
    pass_hi = parse_int(d["pass_hi"])
    if result in ("fail", "fc20_fail", "hci_timeout"):
        fail_lo = max(fail_lo, split)
    elif result in ("ok", "fc20_ok", "connect_ok"):
        pass_hi = min(pass_hi, split)
    else:
        print(f"unknown result: {result}", file=sys.stderr)
        sys.exit(2)
    d["fail_lo"] = f"0x{fail_lo:x}"
    d["pass_hi"] = f"0x{pass_hi:x}"
    if interval_done(d):
        d["status"] = "interval_found"
        d["found_lo"] = d["fail_lo"]
        d["found_hi"] = d["pass_hi"]
    else:
        d["status"] = "active"
    save(d)
    print(f"APPLY_OK fail_lo={d['fail_lo']} pass_hi={d['pass_hi']} status={d['status']}")

elif cmd == "stage-pending":
    d = load()
    d["pending_split"] = sys.argv[3]
    d["pending_sha256"] = sys.argv[4]
    d["status"] = "waiting_hardware"
    save(d)
    print(f"STAGED split={d['pending_split']} sha={d['pending_sha256'][:16]}…")

elif cmd == "next-split":
    d = load()
    if interval_done(d):
        print("INTERVAL_DONE")
        print(f"fail_lo={d['fail_lo']} pass_hi={d['pass_hi']}")
        sys.exit(0)
    repo = os.path.dirname(os.path.dirname(path))  # fw_to_test/.. = repo root
    script = os.path.join(repo, ".cursor/skills/byte-split-bisect/scripts/split-bisect.sh")
    import subprocess
    out = subprocess.check_output(
        [script, "next", d["fail_lo"], d["pass_hi"]], text=True, stderr=subprocess.STDOUT
    ).strip()
    print(out.splitlines()[-1])

elif cmd == "is-done":
    d = load()
    done = d["status"] == "interval_found" or interval_done(d)
    print(f"DONE={'yes' if done else 'no'}")
    print(f"status={d['status']}")
    print(f"width=0x{width(d):x}")
    sys.exit(0 if done else 1)

elif cmd == "post-worker":
    d = load()
    print(f"status={d['status']}")
    print(f"fail_lo={d['fail_lo']} pass_hi={d['pass_hi']}")
    if d["pending_split"]:
        print(f"pending_split={d['pending_split']}")
    if d["pending_sha256"]:
        print(f"pending_sha256={d['pending_sha256']}")
    if d["status"] == "waiting_hardware":
        print("SHOULD_COMMIT=yes")
        print("SHOULD_CONTINUE=no")
        print("STOP_REASON=bisect_handoff")
        sys.exit(0)
    if d["status"] == "interval_found":
        print("SHOULD_COMMIT=yes")
        print("SHOULD_CONTINUE=no")
        print("STOP_REASON=bisect_complete")
        sys.exit(0)
    print("SHOULD_COMMIT=no")
    print("SHOULD_CONTINUE=no")
    print("STOP_REASON=bisect_no_handoff")
    sys.exit(1)

else:
    print(f"unknown cmd: {cmd}", file=sys.stderr)
    sys.exit(2)
PY
}

[[ $# -ge 1 ]] || { usage; exit 2; }
cmd=$1
shift
py "$cmd" "$@"
