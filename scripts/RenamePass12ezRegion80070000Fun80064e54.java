// RenamePass12ezRegion80070000Fun80064e54.java — Pass 12ez PSM/QoS tail channel-range wrapper
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ezRegion80070000Fun80064e54 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80064e54L;
        String newName = "fill_psm_qos_10byte_channel_timing_entries_10_to_79_for_slot";
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
