// RenamePass54sFun80059ab0.java — Pass 54s cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54sFun80059ab0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80059ab0L;
        String newName = "bubble_sort_uint32_array_ascending";
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
