// Pass 52am (region 0x80040000): rank-31 substantive from refreshed 1-150B cold-triage
// (ranks 1-30 skipped as artifacts or already done).
// IRQ-off snapshot of list-A head at conn+0x140, reset to 0xa0a sentinel,
// collect overflow via FUN_8004b29c, splice via FUN_8004b3c0, schedule via FUN_8004b468.
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;

public class RenamePass52amRegion80040000Fun8004b6ec extends GhidraScript {

    static final Object[][] RENAMES = {
        { 0x8004b6ecL, "atomically_take_conn_list_a_collect_overflow_and_schedule_tx" },
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