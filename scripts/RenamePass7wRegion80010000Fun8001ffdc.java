// RenamePass7wRegion80010000Fun8001ffdc.java — Pass 7w cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7wRegion80010000Fun8001ffdc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001ffdcL;
        String newName = "enqueue_hci_evt_to_ring17_and_dispatch_or_drain";
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