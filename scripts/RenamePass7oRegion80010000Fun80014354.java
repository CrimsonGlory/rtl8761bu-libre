// RenamePass7oRegion80010000Fun80014354.java — Pass 7o cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7oRegion80010000Fun80014354 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014354L;
        String newName = "read_halfword_via_bb_reg_0x1f0_esco_slot_select_by_conn_index";
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