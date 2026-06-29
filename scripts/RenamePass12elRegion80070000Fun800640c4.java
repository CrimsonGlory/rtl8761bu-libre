// RenamePass12elRegion80070000Fun800640c4.java — Pass 12el PSM/QoS state-0x16 retry-0 sub-state
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12elRegion80070000Fun800640c4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800640c4L;
        String newName = "expand_psm_qos_state_0x16_retry0_sequential_channel_bitpair_eligibility";
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
