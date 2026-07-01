// RenamePass97Region80010000Fun8001fd20.java — Pass 97 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass97Region80010000Fun8001fd20 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001fd20L;
        String newName = "drain_hci_evt_ring17_and_mode2_dispatch";
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