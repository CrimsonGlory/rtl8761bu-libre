// RenamePass115Region80010000Fun80014048.java — Pass 115 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass115Region80010000Fun80014048 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014048L;
        String newName = "read_global_packet_type_hw_mode_byte";
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