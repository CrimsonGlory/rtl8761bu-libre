// Pass 52cl (region 0x80040000): >150B rank-5 conn-table gate clear + codec apply sweep
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;

public class RenamePass52clRegion80040000Fun800431a0 extends GhidraScript {

    static final Object[][] RENAMES = {
        { 0x800431a0L, "sweep_conn_table_clear_esco_gate_bytes_and_apply_codec_config" },
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