// RenamePass12hbRegion80070000Fun80076ee4.java — Pass 12hb BB slot instant sync stub
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12hbRegion80070000Fun80076ee4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076ee4L;
        String newName = "sync_bb_slot_current_instant_to_reference_clear_timing_flags";
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