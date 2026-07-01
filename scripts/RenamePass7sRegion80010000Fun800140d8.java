// RenamePass7sRegion80010000Fun800140d8.java — Pass 7s cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7sRegion80010000Fun800140d8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800140d8L;
        String newName = "write_hw_register_by_conn_cc_index_byte_half";
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