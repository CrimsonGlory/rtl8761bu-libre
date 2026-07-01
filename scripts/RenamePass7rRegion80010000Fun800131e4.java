// RenamePass7rRegion80010000Fun800131e4.java — Pass 7r cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7rRegion80010000Fun800131e4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800131e4L;
        String newName = "apply_sco_esco_credit_slot_timing_and_active_bitmask";
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