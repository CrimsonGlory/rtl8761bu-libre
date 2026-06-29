// RenamePass12chCrossRegionFun800363a0.java — Pass 12ch setup-chain step 3 packet-type mask
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12chCrossRegionFun800363a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800363a0L;
        String newName = "mask_packet_type_bitmask_by_edr_feature_flags_and_slot_mode";
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
