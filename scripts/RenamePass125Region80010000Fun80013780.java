// RenamePass125Region80010000Fun80013780.java — Pass 125 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass125Region80010000Fun80013780 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80013780L;
        String newName = "capture_cp0_exception_context_emit_scheduler_slot_dump_then_halt";
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