// RenamePass12abRegion80070000Fun80079654.java — Pass 12ab codec bit→field triplet router
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12abRegion80070000Fun80079654 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80079654L;
        String newName = "select_codec_field_triplet_by_bit_and_feed";
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
