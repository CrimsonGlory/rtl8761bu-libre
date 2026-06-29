// RenamePass12aoRegion80070000Fun80077a50.java — Pass 12ao quicksort Lomuto partition
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12aoRegion80070000Fun80077a50 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80077a50L;
        String newName = "quicksort_lomuto_partition_int16_and_index_perm";
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
