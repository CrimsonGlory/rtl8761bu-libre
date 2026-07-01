// RenamePass7uRegion80010000Fun80013cec.java — Pass 7u cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7uRegion80010000Fun80013cec extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80013cecL;
        String newName = "clear_indexed_channel_slot_bit4_and_invoke_release_hook";
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