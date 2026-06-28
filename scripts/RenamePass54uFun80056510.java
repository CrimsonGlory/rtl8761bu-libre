// RenamePass54uFun80056510.java — Pass 54u cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54uFun80056510 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80056510L;
        String newName = "irq_safe_enqueue_slot0_to_indexed_ring_and_notify_if_idle";
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
