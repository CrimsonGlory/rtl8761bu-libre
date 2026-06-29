// RenamePass12fbRegion80070000Fun80076270.java — Pass 12fb LMP 0x25B pending slot head pop+init
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fbRegion80070000Fun80076270 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076270L;
        String newName = "pop_head_and_init_lmp_25b_pending_slot_with_params";
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
