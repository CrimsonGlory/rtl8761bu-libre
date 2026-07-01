// RenamePass107Region80010000Fun80011a3c.java — Pass 107 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass107Region80010000Fun80011a3c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80011a3cL;
        String newName = "set_or_clear_bb_reg_0xfc_bit_0x1000_by_param";
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