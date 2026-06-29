// RenamePass12btRegion80070000Fun80074dfc.java — Pass 12bt feature-page predicate-or-fallback
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12btRegion80070000Fun80074dfc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80074dfcL;
        String newName = "invoke_feature_page_predicate_or_hook_fallback_0x385";
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
