// RenamePass54aaFun80050164.java — Pass 54aa cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54aaFun80050164 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80050164L;
        String newName = "init_zero_0x208_dual_256b_chunk_with_boundary_markers";
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
