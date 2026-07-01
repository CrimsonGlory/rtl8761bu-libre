// RenamePass109Region80010000Fun80019024.java — Pass 109 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass109Region80010000Fun80019024 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80019024L;
        String newName = "validate_sync_conn_feature_page_and_reject_conn_status_0x03";
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