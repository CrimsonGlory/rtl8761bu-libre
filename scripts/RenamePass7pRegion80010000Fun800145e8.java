// RenamePass7pRegion80010000Fun800145e8.java — Pass 7p cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7pRegion80010000Fun800145e8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800145e8L;
        String newName = "program_single_tx_descriptor_slot_for_lmp_pdu_body";
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