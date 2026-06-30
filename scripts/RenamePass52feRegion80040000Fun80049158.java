// Pass 52fe (region 0x80040000): >150B rank-4 eSCO timing-pair HCI handler.
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;

public class RenamePass52feRegion80040000Fun80049158 extends GhidraScript {

    static final Object[][] RENAMES = {
        { 0x80049158L, "hci_validate_and_commit_esco_timing_pair_hook1_or_pending_masks" },
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