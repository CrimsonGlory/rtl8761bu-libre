// RenamePass85Region80010000Fun8001840c.java — Pass 85 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass85Region80010000Fun8001840c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001840cL;
        String newName = "process_acl_tx_completion_credits_and_emit_hci_completed_packets";
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