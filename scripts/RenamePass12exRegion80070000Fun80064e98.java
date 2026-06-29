// RenamePass12exRegion80070000Fun80064e98.java — Pass 12ex ACL timing-stats aggregator
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12exRegion80070000Fun80064e98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80064e98L;
        String newName = "aggregate_acl_channel_timing_averages_into_10byte_buffer";
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
