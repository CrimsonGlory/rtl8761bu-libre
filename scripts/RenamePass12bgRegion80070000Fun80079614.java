// RenamePass12bgRegion80070000Fun80079614.java — Pass 12bg codec partial-byte flush helper
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bgRegion80070000Fun80079614 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80079614L;
        String newName = "flush_codec_partial_byte_remainder_via_patch_hook";
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
