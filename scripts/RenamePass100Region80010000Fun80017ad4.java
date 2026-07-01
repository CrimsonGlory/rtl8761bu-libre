// RenamePass100Region80010000Fun80017ad4.java — Pass 100 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass100Region80010000Fun80017ad4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80017ad4L;
        String newName = "probe_acl_tx_buffer_walk_eligibility_by_role_and_sync_gates";
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