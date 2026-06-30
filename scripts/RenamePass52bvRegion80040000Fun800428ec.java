// Pass 52bv (region 0x80040000): rank-76 substantive from refreshed 1-150B cold-triage
// Access-code sync-word compute + fixed 5-byte XOR mask wrapper.
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;

public class RenamePass52bvRegion80040000Fun800428ec extends GhidraScript {

    static final Object[][] RENAMES = {
        { 0x800428ecL, "compute_access_code_sync_word_xor_fixed_mask_from_bdaddr" },
    };

    public void run() throws Exception {
        FunctionManager fm = currentProgram.getFunctionManager();
        int renamed = 0, alreadyOk = 0, missing = 0, failed = 0;
        for (Object[] row : RENAMES) {
            long addr = (Long) row[0];
            String newName = (String) row[1];
            Address a = currentProgram.getAddressFactory().getAddress(String.format("0x%08x", addr));
            Function f = fm.getFunctionAt(a);
            if (f == null) { missing++; println("MISSING: " + a); continue; }
            if (f.getName().equals(newName)) { alreadyOk++; continue; }
            try {
                f.setName(newName, ghidra.program.model.symbol.SourceType.USER_DEFINED);
                renamed++;
                println("RENAMED: " + a + " -> " + newName);
            } catch (Exception e) {
                failed++;
                println("FAILED: " + a + " : " + e.getMessage());
            }
        }
        println(String.format("renamed=%d alreadyOk=%d missing=%d failed=%d", renamed, alreadyOk, missing, failed));
    }
}