// RenamePass129Region80010000Fun8001ac74.java — Pass 129 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass129Region80010000Fun8001ac74 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001ac74L;
        String newName = "kickoff_role_switch_or_defer_if_slot_busy";
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