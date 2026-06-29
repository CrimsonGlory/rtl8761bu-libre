// RenamePass12bzRegion80070000Fun80072304.java — Pass 12bz LMP max-slot-req sender
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bzRegion80070000Fun80072304 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80072304L;
        String newName = "send_lmp_max_slot_req_0x2e_pdu_on_feature_and_state";
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
