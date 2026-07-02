// RenamePass130Region80010000Fun80011bf4.java — Pass 130 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass130Region80010000Fun80011bf4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80011bf4L;
        String newName = "pulse_bb_regs_then_merge_mask_chain_into_bb_reg";
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