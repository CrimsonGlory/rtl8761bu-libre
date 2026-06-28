// RenamePass54oFun80055f68.java — Pass 54o cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54oFun80055f68 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80055f68L;
        String newName = "enqueue_deduped_slot_to_indexed_ring_and_set_pending_bit";
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
