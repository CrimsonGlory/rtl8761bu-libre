# Hardware-validated firmware archive

Immutable copies of **NeoPC-confirmed** builds. Survives `git reset`, profile renames, and `fw_to_test/` overwrites.

## Layout

```
artifacts/
  README.md           — this file
  manifest.tsv        — index (committed to git)
  hw-validated/       — binaries (gitignored)
    rtl8761bu_fw.<full-sha256>.bin
    rtl8761bu_config.<full-sha256>.bin
    patch_padded.<full-sha256>.bin   — when build tree still has it
```

## When to archive (required on PASS)

After NeoPC confirms a **gate**, before starting the next experiment:

```bash
.cursor/skills/ub500-hardware-test/scripts/archive-hw-pass.sh \
  <profile> <gate> "short notes"
```

| Gate | Meaning |
|------|---------|
| `fc20` | FC20 load OK |
| `hci` | FC20 + HCI OK (no `0x2036`) |
| `connect` | FC20 + HCI + connect OK |
| `vendor-ref` | Full vendor `test-nf` sanity |
| `bisect` | Closed bisect interval (note bracket in notes) |

**Do not archive** on FAIL-only runs (log in `results.md` only).

## Restore a lost baseline

```bash
# List archived builds
.cursor/skills/ub500-hardware-test/scripts/archive-hw-pass.sh --list

# Restore by full or prefix SHA → fw_to_test/
.cursor/skills/ub500-hardware-test/scripts/promote-test.sh --restore 62198d8c
```

Then flash from `fw_to_test/` as usual.

## Agent workflow

1. Build → `promote-test.sh <profile>` → handoff SHA to user
2. User pastes NeoPC log
3. Append `results.md` (every run)
4. **On PASS** → `archive-hw-pass.sh` + commit if milestone ([git-commit-criteria](../.cursor/rules/git-commit-criteria.mdc))
5. Update `fw_to_test/bisect-state.yaml`

## NeoPC backup (user)

After a good flash:

```bash
SHA=$(sha256sum /lib/firmware/rtl_bt/rtl8761bu_fw.bin | awk '{print $1}')
cp /lib/firmware/rtl_bt/rtl8761bu_fw.bin ~/fw-backup-$(date +%F)-${SHA:0:8}.bin
```

Scp back to daas-dev if daas-dev lost the artifact.

## New conversation at key moments

Start a **fresh chat** when context is getting long or the task phase changes. Paste a short carry-over block (see [conversation-handoff rule](../.cursor/rules/conversation-handoff.mdc)).

**Start new chat after:**

- A hardware **gate passes** (archive + commit done; next phase is new work)
- A **bisect interval closes** (document bracket + next split in `bisect-state.yaml`)
- Before **risky experiments** (vendor embeds, `git checkout` on hooks, wide reverts)
- When the thread is mostly **journalctl paste / SHA bookkeeping** (>3 hardware rounds)

**Stay in the same chat for:**

- One build → one flash → one log cycle
- Tight debug on the same failing SHA

Carry-over template:

```
Repo: /root/rtl8761bu-libre
Active bisect: fw_to_test/bisect-state.yaml
Last PASS archived: artifacts/manifest.tsv (SHA …)
Next: <one sentence>
Do not repeat: <failed SHAs / rejected approaches>
```
