// RenamePass90Region80010000Fun80011b6c.java — Pass 90 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass90Region80010000Fun80011b6c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80011b6cL;
        String newName = "pulse_bb_regs_0x00_0x10_with_cc_mode_bits_and_spin_delays";
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