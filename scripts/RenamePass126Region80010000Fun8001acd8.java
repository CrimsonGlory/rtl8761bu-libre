// RenamePass126Region80010000Fun8001acd8.java — Pass 126 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass126Region80010000Fun8001acd8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001acd8L;
        String newName = "lookup_role_switch_conn_slot_validate_and_invoke_kickoff";
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