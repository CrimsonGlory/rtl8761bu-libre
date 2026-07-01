// RenamePass93Region80010000Fun80013c64.java — Pass 93 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass93Region80010000Fun80013c64 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80013c64L;
        String newName = "write_baseband_reg_by_subopcode_byte_via_hook";
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