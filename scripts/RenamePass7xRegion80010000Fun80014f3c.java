// RenamePass7xRegion80010000Fun80014f3c.java — Pass 7x cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7xRegion80010000Fun80014f3c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014f3cL;
        String newName = "program_single_tx_descriptor_slot_via_credit_scheduler";
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