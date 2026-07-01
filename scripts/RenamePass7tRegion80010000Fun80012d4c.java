// RenamePass7tRegion80010000Fun80012d4c.java — Pass 7t cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7tRegion80010000Fun80012d4c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80012d4cL;
        String newName = "write_bb_reg_0x144_shifted_param_and_poll_status_irq_gated";
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