// RenamePass7gRegion80010000Fun80013e78.java — Pass 7g cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7gRegion80010000Fun80013e78 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80013e78L;
        String newName = "program_bb_regs_0xee_0x60_0xa_0_via_hook";
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