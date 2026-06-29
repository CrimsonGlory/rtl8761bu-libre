// RenamePass12dwRegion80070000Fun8007190c.java — Pass 12dw config RSSI threshold reader
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dwRegion80070000Fun8007190c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8007190cL;
        String newName = "read_config_rssi_threshold_high_byte_or_low_word";
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
