// RenamePass127Region80010000Fun800132f4.java — Pass 127 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass127Region80010000Fun800132f4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800132f4L;
        String newName = "reinit_sco_esco_credit_slot_globals_disable_all_and_apply_slot0_timing";
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