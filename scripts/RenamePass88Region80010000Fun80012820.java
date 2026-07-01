// RenamePass88Region80010000Fun80012820.java — Pass 88 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass88Region80010000Fun80012820 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80012820L;
        String newName = "apply_link_mode_change_timeout_to_bb_regs_0x6e_0x6c";
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