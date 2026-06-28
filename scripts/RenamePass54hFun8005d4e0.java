// RenamePass54hFun8005d4e0.java — Pass 54h cold-triage rank-2 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54hFun8005d4e0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005d4e0L;
        String newName = "alloc_tag0x1b_record_and_log_link_table_timing_by_index";
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
