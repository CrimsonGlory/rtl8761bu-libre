// RenamePass7Region80010000Fun80011608.java — Pass 7 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7Region80010000Fun80011608 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80011608L;
        String newName = "write_baseband_register_masked_busywait";
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