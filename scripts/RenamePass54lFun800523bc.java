// RenamePass54lFun800523bc.java — Pass 54l cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54lFun800523bc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800523bcL;
        String newName = "alloc_and_wire_0x14_slot_index_table_in_link_globals";
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
