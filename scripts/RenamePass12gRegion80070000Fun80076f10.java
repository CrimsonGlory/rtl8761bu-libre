// RenamePass12gRegion80070000Fun80076f10.java — Pass 12g BB slot clock wrap guard
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12gRegion80070000Fun80076f10 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076f10L;
        String newName = "reset_bb_slot_instant_on_clock_wrap_guard";
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
