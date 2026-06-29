// RenamePass12edRegion80070000Fun80065068.java — Pass 12ed PSM/QoS eligibility bitmask builder
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12edRegion80070000Fun80065068 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80065068L;
        String newName = "build_psm_qos_channel_eligibility_bitmask_0x50";
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
