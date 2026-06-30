// RenamePass12hjRegion80070000Fun80072bac.java — Pass 12hj AFH/LAP extended group slot registrar
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12hjRegion80070000Fun80072bac extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80072bacL;
        String newName = "register_afh_lap_group_slot_with_peer_channel_merge";
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