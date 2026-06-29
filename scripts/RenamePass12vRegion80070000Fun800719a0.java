// RenamePass12vRegion80070000Fun800719a0.java — Pass 12v AFH LAP channel-map clear
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12vRegion80070000Fun800719a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800719a0L;
        String newName = "clear_afh_lap_channel_map_for_matching_group";
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
