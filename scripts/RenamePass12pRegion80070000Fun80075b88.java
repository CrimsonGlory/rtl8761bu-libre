// RenamePass12pRegion80070000Fun80075b88.java — Pass 12p pool descriptor stack pop
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12pRegion80070000Fun80075b88 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075b88L;
        String newName = "pop_indexed_entry_from_pool_descriptor_stack";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSED at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
