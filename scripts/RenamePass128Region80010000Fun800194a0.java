// RenamePass128Region80010000Fun800194a0.java — Pass 128 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass128Region80010000Fun800194a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800194a0L;
        String newName = "validate_sync_conn_feature_page_and_reject_unless_wildcard_sync_intervals";
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