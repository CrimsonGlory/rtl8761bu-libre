// RenamePass12feRegion80070000Fun80075ffc.java — Pass 12fe mod64 ring-slot scan logger
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12feRegion80070000Fun80075ffc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075ffcL;
        String newName = "advance_mod64_ring_cursor_and_log_active_slot";
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
