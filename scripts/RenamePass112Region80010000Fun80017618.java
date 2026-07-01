// RenamePass112Region80010000Fun80017618.java — Pass 112 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass112Region80010000Fun80017618 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80017618L;
        String newName = "lookup_pending_callback_table_entry_by_conn_handle";
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