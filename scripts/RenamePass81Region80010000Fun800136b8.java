// RenamePass81Region80010000Fun800136b8.java — Pass 81 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass81Region80010000Fun800136b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800136b8L;
        String newName = "emit_a5_framed_scheduler_slot_write_command";
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