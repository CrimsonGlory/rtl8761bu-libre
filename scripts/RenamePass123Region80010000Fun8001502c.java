// RenamePass123Region80010000Fun8001502c.java — Pass 123 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass123Region80010000Fun8001502c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001502cL;
        String newName = "program_credit_scheduler_slot_and_arm_bb_regs_for_index";
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