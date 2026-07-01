// RenamePass82Region80010000Fun80011468.java — Pass 82 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass82Region80010000Fun80011468 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80011468L;
        String newName = "poll_indexed_bb_register_until_stable_read";
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