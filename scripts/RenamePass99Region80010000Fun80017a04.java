// RenamePass99Region80010000Fun80017a04.java — Pass 99 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass99Region80010000Fun80017a04 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80017a04L;
        String newName = "process_role_matched_acl_tx_completions_and_emit_hci_completed_packets";
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