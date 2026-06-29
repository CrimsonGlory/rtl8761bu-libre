// RenamePass12fdRegion80070000Fun80072020.java — Pass 12fd AFH LAP channel-map debug logger
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fdRegion80070000Fun80072020 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80072020L;
        String newName = "log_afh_lap_channel_map_when_offset_byte_active";
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
