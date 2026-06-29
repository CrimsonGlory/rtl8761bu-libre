// RenamePass12deRegion80070000Fun80075a64.java — Pass 12de resource pool buffer allocator
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12deRegion80070000Fun80075a64 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075a64L;
        String newName = "allocate_resource_pool_slot_with_scaled_buffer";
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
