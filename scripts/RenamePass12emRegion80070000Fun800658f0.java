// RenamePass12emRegion80070000Fun800658f0.java — Pass 12em PSM/QoS state-0x16 even-retry sub-state
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12emRegion80070000Fun800658f0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800658f0L;
        String newName = "expand_psm_qos_state_0x16_even_retry_bitpair_adjust_eligibility";
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
