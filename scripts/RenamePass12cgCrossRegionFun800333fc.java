// RenamePass12cgCrossRegionFun800333fc.java — Pass 12cg packet-type mask by LMP 0x47E hook thresholds
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12cgCrossRegionFun800333fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800333fcL;
        String newName = "narrow_packet_type_bitmask_by_lmp_47e_hook_thresholds";
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
