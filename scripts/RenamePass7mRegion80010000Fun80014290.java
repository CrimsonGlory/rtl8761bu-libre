// RenamePass7mRegion80010000Fun80014290.java — Pass 7m cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7mRegion80010000Fun80014290 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014290L;
        String newName = "apply_fast_poll_mode_bb_reg_0x298_irq_gated";
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