// RenamePass12efRegion80070000Fun80066914.java — Pass 12ef PSM/QoS top-level bitmask dispatch
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12efRegion80070000Fun80066914 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80066914L;
        String newName = "dispatch_psm_qos_10byte_bitmask_by_channel_state";
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
