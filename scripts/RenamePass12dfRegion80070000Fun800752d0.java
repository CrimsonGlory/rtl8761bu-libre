// RenamePass12dfRegion80070000Fun800752d0.java — Pass 12df resource pool bump heap allocator
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dfRegion80070000Fun800752d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800752d0L;
        String newName = "bump_alloc_aligned_from_resource_pool_heap";
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
