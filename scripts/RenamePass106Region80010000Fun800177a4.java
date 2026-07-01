// RenamePass106Region80010000Fun800177a4.java — Pass 106 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass106Region80010000Fun800177a4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800177a4L;
        String newName = "sweep_linked_conn_slots_clear_pending_timing_by_index_and_type";
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