// RenamePass54gFun800531d4.java — Pass 54g cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54gFun800531d4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800531d4L;
        String newName = "compute_table_offset_pair_plus_peer_slot_us_timing";
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
