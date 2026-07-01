// RenamePass7jRegion80010000Fun80014524.java — Pass 7j cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7jRegion80010000Fun80014524 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014524L;
        String newName = "release_active_tx_descriptor_slots_via_hw_programmer";
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