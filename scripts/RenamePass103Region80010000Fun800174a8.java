// RenamePass103Region80010000Fun800174a8.java — Pass 103 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass103Region80010000Fun800174a8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800174a8L;
        String newName = "binary_search_sorted_table_by_ushort_key";
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