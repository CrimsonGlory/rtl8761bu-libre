// RenamePass12djRegion80070000Fun80071a1c.java — Pass 12dj AFH LAP channel-map clear by +0x4f key
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12djRegion80070000Fun80071a1c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071a1cL;
        String newName = "clear_afh_lap_channel_map_for_matching_offset_group";
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
