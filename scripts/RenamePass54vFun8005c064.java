// RenamePass54vFun8005c064.java — Pass 54v cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54vFun8005c064 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005c064L;
        String newName = "commit_sco_esco_timing_via_clock_and_log_if_debug_flag";
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
