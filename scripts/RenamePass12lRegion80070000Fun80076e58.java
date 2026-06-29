// RenamePass12lRegion80070000Fun80076e58.java — Pass 12l BB slot instant resolver
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12lRegion80070000Fun80076e58 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076e58L;
        String newName = "resolve_bb_slot_instant_by_status_timing_mode";
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
