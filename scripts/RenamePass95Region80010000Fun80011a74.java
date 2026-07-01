// RenamePass95Region80010000Fun80011a74.java — Pass 95 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass95Region80010000Fun80011a74 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80011a74L;
        String newName = "afh_flag_gated_set_or_clear_bb_reg_0xfc_bit_0x1000";
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