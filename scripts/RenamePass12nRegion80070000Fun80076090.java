// RenamePass12nRegion80070000Fun80076090.java — Pass 12n LMP 0x25B pending-slot unlink
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12nRegion80070000Fun80076090 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076090L;
        String newName = "unlink_lmp_25b_pending_slot_from_index_queue";
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
