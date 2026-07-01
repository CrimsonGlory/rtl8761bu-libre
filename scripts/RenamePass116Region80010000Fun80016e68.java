// RenamePass116Region80010000Fun80016e68.java — Pass 116 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass116Region80010000Fun80016e68 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80016e68L;
        String newName = "apply_SCO_sync_setup_timing_or_credit_scheduler_role_dispatch";
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