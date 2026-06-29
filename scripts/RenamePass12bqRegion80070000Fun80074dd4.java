// RenamePass12bqRegion80070000Fun80074dd4.java — Pass 12bq feature-page hook fallback
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bqRegion80070000Fun80074dd4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80074dd4L;
        String newName = "invoke_feature_page_hook_fallback_with_log_0x385";
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
