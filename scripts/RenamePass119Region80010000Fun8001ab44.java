// RenamePass119Region80010000Fun8001ab44.java — Pass 119 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass119Region80010000Fun8001ab44 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001ab44L;
        String newName = "compute_slot_offset_and_send_lmp_slot_offset_0x13_pdu";
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