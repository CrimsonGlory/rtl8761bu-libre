// RenamePass12fiRegion80070000Fun80076d90.java — Pass 12fi BB slot instant delta threshold gate
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fiRegion80070000Fun80076d90 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076d90L;
        String newName = "evaluate_bb_slot_instant_delta_exceeded_flag";
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
