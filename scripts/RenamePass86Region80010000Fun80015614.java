// RenamePass86Region80010000Fun80015614.java — Pass 86 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass86Region80010000Fun80015614 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80015614L;
        String newName = "store_four_field_sync_timing_offsets_with_mode_adjust_and_hook";
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