// RenamePass7yRegion80010000Fun80014450.java — Pass 7y cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7yRegion80010000Fun80014450 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014450L;
        String newName = "apply_packet_type_via_hw_register_programmer";
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