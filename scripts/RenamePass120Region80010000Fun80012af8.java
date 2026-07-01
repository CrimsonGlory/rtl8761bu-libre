// RenamePass120Region80010000Fun80012af8.java — Pass 120 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass120Region80010000Fun80012af8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80012af8L;
        String newName = "commit_hw_reg_bitmask_with_link_mode_and_lmp_power_clk_gate";
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