// RenamePass104Region80010000Fun80012e80.java — Pass 104 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass104Region80010000Fun80012e80 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80012e80L;
        String newName = "write_baseband_reg_field_5bit_indexed";
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