// RenamePass7fRegion80010000Fun8001359c.java — Pass 7f cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7fRegion80010000Fun8001359c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001359cL;
        String newName = "busywait_status_bits_0x60_then_write_byte";
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