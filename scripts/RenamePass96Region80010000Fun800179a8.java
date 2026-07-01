// RenamePass96Region80010000Fun800179a8.java — Pass 96 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass96Region80010000Fun800179a8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800179a8L;
        String newName = "conn_slot_table_entry_ptr_from_index_stride_0x1c";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSING at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}