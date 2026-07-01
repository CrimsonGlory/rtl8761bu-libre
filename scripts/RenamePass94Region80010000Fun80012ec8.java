// RenamePass94Region80010000Fun80012ec8.java — Pass 94 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass94Region80010000Fun80012ec8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80012ec8L;
        String newName = "write_bb_reg_0x1f8_then_merge_0x144_shifted_byte_with_delay";
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