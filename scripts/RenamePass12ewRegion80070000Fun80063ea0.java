// RenamePass12ewRegion80070000Fun80063ea0.java — Pass 12ew PSM/QoS per-entry channel-slot eligibility test
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ewRegion80070000Fun80063ea0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80063ea0L;
        String newName = "test_psm_qos_channel_slot_eligibility_entry";
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
