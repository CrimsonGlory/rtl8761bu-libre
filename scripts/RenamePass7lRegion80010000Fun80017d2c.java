// RenamePass7lRegion80010000Fun80017d2c.java — Pass 7l cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7lRegion80010000Fun80017d2c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80017d2cL;
        String newName = "clear_sync_gate_byte_bits_by_remapped_role_index";
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