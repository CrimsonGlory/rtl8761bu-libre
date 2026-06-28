// RenamePass54afFun80059af8.java — Pass 54af cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54afFun80059af8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80059af8L;
        String newName = "compute_circular_mod_distance";
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
