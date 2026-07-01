// RenamePass89Region80010000Fun800143b0.java — Pass 89 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass89Region80010000Fun800143b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800143b0L;
        String newName = "irq_masked_commit_hw_channel_table_merge_by_conn_cc_index";
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