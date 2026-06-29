// RenamePass12bsRegion80070000Fun80074eb4.java — Pass 12bs feature-page tag dispatcher
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bsRegion80070000Fun80074eb4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80074eb4L;
        String newName = "dispatch_feature_page_by_tag_900_or_0x385";
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
