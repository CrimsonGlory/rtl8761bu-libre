// RenamePass12hRegion80070000Fun80076f58.java — Pass 12h BB slot modulo timing flags
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12hRegion80070000Fun80076f58 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076f58L;
        String newName = "classify_bb_slot_modulo_timing_flags_and_offset";
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
