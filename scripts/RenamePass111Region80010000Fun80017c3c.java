// RenamePass111Region80010000Fun80017c3c.java — Pass 111 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass111Region80010000Fun80017c3c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80017c3cL;
        String newName = "set_sync_gate_byte_one_by_remapped_role_index";
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