// RenamePass12blRegion80070000Fun80074940.java — Pass 12bl LMP feature-page response dispatcher
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12blRegion80070000Fun80074940 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80074940L;
        String newName = "dispatch_lmp_feature_page_response_by_bitmask";
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
