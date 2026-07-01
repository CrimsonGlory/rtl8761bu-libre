// RenamePass7bRegion80010000Fun80011510.java — Pass 7b cold-triage rank-2 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7bRegion80010000Fun80011510 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80011510L;
        String newName = "read_baseband_register_masked_busywait";
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