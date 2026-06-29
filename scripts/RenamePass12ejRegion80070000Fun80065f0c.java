// RenamePass12ejRegion80070000Fun80065f0c.java — Pass 12ej PSM/QoS eligibility finalize helper
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ejRegion80070000Fun80065f0c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80065f0cL;
        String newName = "finalize_psm_qos_eligibility_bitpair_expand_or_fail_channel";
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
