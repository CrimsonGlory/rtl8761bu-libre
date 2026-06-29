// RenamePass12eaRegion80070000Fun80077bcc.java — Pass 12ea PSM/QoS quantizer search (0x28 channels)
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12eaRegion80070000Fun80077bcc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80077bccL;
        String newName = "search_psm_qos_quantizer_and_pack_channel_bitmask_0x28";
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
