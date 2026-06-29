// RenamePass12eqRegion80070000Fun80064444.java — Pass 12eq PSM/QoS 5-byte eligibility conn-slot sync
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12eqRegion80070000Fun80064444 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80064444L;
        String newName = "sync_psm_qos_5byte_eligibility_to_conn_slots_by_channel_bitmask";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSED at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
