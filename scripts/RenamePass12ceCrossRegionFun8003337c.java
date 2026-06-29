// RenamePass12ceCrossRegionFun8003337c.java — Pass 12ce packet-type mask by max-slot fields
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ceCrossRegionFun8003337c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8003337cL;
        String newName = "mask_packet_type_bitmask_by_max_slot_fields_0x24a_0x24b";
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
