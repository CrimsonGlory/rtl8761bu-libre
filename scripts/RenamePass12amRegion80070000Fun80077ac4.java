// RenamePass12amRegion80070000Fun80077ac4.java — Pass 12am quicksort recursion helper
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12amRegion80070000Fun80077ac4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80077ac4L;
        String newName = "quicksort_int16_keys_with_index_perm_recursive";
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
