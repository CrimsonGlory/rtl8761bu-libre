// RenamePass7iRegion80010000Fun800115c8.java — Pass 7i cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7iRegion80010000Fun800115c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800115c8L;
        String newName = "read_baseband_register_byte_masked_busywait";
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