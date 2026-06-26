# Rename Persistence Test Protocol

**Purpose:** Determine whether the rename-persistence bug (TODO item 1 in
`wairz_requested_changes.txt`) is fixed. This is the reproducibility bar defined
in that entry: a *fresh* rename, in a *new* session, showing up in `list_functions`
itself — not just one cherry-picked `decompile_function` call.

**Test target:**
- GZF: `/root/DarkFirmware_real_i/00_Ghidra-analyzed_files/2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`
- Address: `0x80000820`
- Baseline name: `FUN_80000820` (364 bytes — confirmed present in GZF)
- Probe name: `TEST_rename_persistence_probe_20260626`

Execute the following steps in order. At the end, print a single-line verdict.

---

## Step 1 — Confirm baseline (no prior contamination)

Call `mcp__wairz__list_functions` with:
- `binary_path`: the GZF path above
- `limit`: 500

Scan the result for both `FUN_80000820` and `TEST_rename_persistence_probe_20260626`.

**Expected:** `FUN_80000820` appears, probe name does NOT appear.
**If probe name already appears:** the test environment is contaminated — report
`CONTAMINATED: probe name already present before rename was applied` and stop.
**If FUN_80000820 is missing entirely:** report `BASELINE_MISSING: FUN_80000820
not found in list_functions — test cannot proceed` and stop.

---

## Step 2 — Write the rename script to disk

Use the `Write` tool (not `save_ghidra_script`) to create the file
`/root/wairz/ghidra/scripts/TestRenameProbe20260626.java` with exactly this content:

```java
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.SourceType;

public class TestRenameProbe20260626 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = toAddr(0x80000820L);
        Function func = getFunctionAt(addr);
        if (func == null) {
            println("PROBE_ERROR: No function found at 0x80000820");
            return;
        }
        String oldName = func.getName();
        String newName = "TEST_rename_persistence_probe_20260626";
        func.setName(newName, SourceType.USER_DEFINED);
        println("PROBE_RENAMED 0x80000820: " + oldName + " -> " + newName);
        println("PROBE_RENAME_COMPLETE");
    }
}
```

---

## Step 3 — Apply the rename via GZF process mode

Call `mcp__wairz__run_ghidra_headless` with:
- `binary_path`: the GZF path above
- `use_saved_project`: `true`
- `script_name`: `"TestRenameProbe20260626.java"`

**Check the output for all three of:**
1. `PROBE_RENAMED 0x80000820`
2. `PROBE_RENAME_COMPLETE`
3. `Save succeeded` (or equivalent Ghidra save-confirmation text)

If any of these three are missing, report:
`SCRIPT_FAILED: rename script did not complete successfully — output: <paste relevant lines>`
and stop. The bug cannot be evaluated if the rename script itself didn't run.

---

## Step 4 — Check list_functions (primary pass/fail criterion)

Call `mcp__wairz__list_functions` again with the same parameters as Step 1
(`binary_path`, `limit=500`).

Scan the result for `TEST_rename_persistence_probe_20260626` at address `0x80000820`.

- **PASS:** the probe name appears in the list.
- **FAIL:** address `0x80000820` still shows `FUN_80000820` (or any name other than the probe name).

---

## Step 5 — Check decompile_function

Call `mcp__wairz__decompile_function` with:
- `binary_path`: the GZF path above
- `function_name`: `"TEST_rename_persistence_probe_20260626"`

- **PASS:** returns valid C decompilation output (non-empty, not the generic
  "Decompilation produced no output. The function may be too small or a thunk." error).
- **FAIL:** returns the generic not-found/empty response.

---

## Step 6 — Report verdict

Print one of the following lines as the final output, followed by a one-paragraph
summary of what each step returned:

```
RENAME PERSISTENCE: PASS  (list_functions ✓, decompile_function ✓)
RENAME PERSISTENCE: PARTIAL  (list_functions ✗, decompile_function ✓ — same cherry-pick artifact as before)
RENAME PERSISTENCE: FAIL  (list_functions ✗, decompile_function ✗)
RENAME PERSISTENCE: SCRIPT_FAILED  (rename did not apply — see Step 3)
RENAME PERSISTENCE: CONTAMINATED  (probe name already present — see Step 1)
RENAME PERSISTENCE: BASELINE_MISSING  (FUN_80000820 not in GZF — see Step 1)
```

Then update `wairz_requested_changes.txt`:
- If **PASS**: change the TODO item 1 header from `[TODO]` to `[DONE]` and append a
  `CONFIRMED FIXED <date>: <one sentence>` note after the last re-verification entry.
- If **PARTIAL**: append a new re-verification note explaining the partial result;
  leave as `[TODO]`.
- If **FAIL**: append a new re-verification note with the date and exact step outputs;
  leave as `[TODO]`.
- If **SCRIPT_FAILED/CONTAMINATED/BASELINE_MISSING**: append a note with the error;
  leave as `[TODO]`.
