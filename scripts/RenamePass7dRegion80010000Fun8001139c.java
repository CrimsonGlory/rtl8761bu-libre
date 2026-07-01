// RenamePass7dRegion80010000Fun8001139c.java — Pass 7d cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7dRegion80010000Fun8001139c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001139cL;
        String newName = "write_baseband_register_mmio_indexed";
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