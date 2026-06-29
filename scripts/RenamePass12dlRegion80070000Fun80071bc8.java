// RenamePass12dlRegion80070000Fun80071bc8.java — Pass 12dl LMP 0x268 multi-timer dispatch
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dlRegion80070000Fun80071bc8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071bc8L;
        String newName = "dispatch_lmp_268_timers_with_hook_and_config_gates";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSED at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
