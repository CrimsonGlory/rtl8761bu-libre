// RenamePass54iFun80059e64.java — Pass 54i cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54iFun80059e64 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80059e64L;
        String newName = "clear_dual_status_flag_bits_and_log_pending_mask";
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
