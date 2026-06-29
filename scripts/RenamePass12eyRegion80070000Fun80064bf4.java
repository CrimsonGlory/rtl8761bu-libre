// RenamePass12eyRegion80070000Fun80064bf4.java — Pass 12ey PSM/QoS 10-byte timing entry filler
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12eyRegion80070000Fun80064bf4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80064bf4L;
        String newName = "fill_psm_qos_10byte_channel_timing_entries_from_acl_with_qos_adjust";
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
