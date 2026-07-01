// RenamePass7nRegion80010000Fun800142f8.java — Pass 7n cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7nRegion80010000Fun800142f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800142f8L;
        String newName = "apply_fast_poll_mode_bb_reg_0x11c_irq_gated";
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