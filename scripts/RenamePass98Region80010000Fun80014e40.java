// RenamePass98Region80010000Fun80014e40.java — Pass 98 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass98Region80010000Fun80014e40 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014e40L;
        String newName = "alloc_and_commit_credit_scheduler_tx_descriptor_batch";
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