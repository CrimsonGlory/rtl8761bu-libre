// RenamePass12kRegion80070000Fun80076dc8.java — Pass 12k BB slot timing drift counter
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12kRegion80070000Fun80076dc8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076dc8L;
        String newName = "accumulate_bb_slot_timing_drift_counters_or_set_mode";
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
