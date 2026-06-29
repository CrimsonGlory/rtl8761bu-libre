// RenamePass12faRegion80070000Fun80076c90.java — Pass 12fa LMP 0x274 emit + BB slot flag reset
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12faRegion80070000Fun80076c90 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076c90L;
        String newName = "emit_lmp_0x274_and_reset_bb_slot_timing_flags";
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
