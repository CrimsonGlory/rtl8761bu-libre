// RenamePass12ekRegion80070000Fun80065d94.java — Pass 12ek PSM/QoS state-0x16 retry-1 sub-state
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ekRegion80070000Fun80065d94 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80065d94L;
        String newName = "expand_psm_qos_state_0x16_retry1_random_bitpair_eligibility";
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
