// RenamePass12cjRegion80070000Fun80071a84.java — Pass 12cj AFH LAP free-group index finder
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12cjRegion80070000Fun80071a84 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071a84L;
        String newName = "find_free_afh_lap_group_index_after_map_clear";
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
