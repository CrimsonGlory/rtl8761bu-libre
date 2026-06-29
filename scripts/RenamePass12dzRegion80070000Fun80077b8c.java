// RenamePass12dzRegion80070000Fun80077b8c.java — Pass 12dz quantizer bitmask unpack (40 mapped indices)
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dzRegion80070000Fun80077b8c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80077b8cL;
        String newName = "unpack_bitmask_40_mapped_bits_to_inverted_short_flags";
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
