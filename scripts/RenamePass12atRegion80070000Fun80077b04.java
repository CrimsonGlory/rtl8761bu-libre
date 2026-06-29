// RenamePass12atRegion80070000Fun80077b04.java — Pass 12at quantizer bitmask packer
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12atRegion80070000Fun80077b04 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80077b04L;
        String newName = "pack_index_select_flags_into_bitmask_0x50";
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
