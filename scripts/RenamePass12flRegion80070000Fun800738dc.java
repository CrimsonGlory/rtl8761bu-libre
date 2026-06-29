// RenamePass12flRegion80070000Fun800738dc.java — Pass 12fl resource-pool chain alloc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12flRegion80070000Fun800738dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800738dcL;
        String newName = "allocate_resource_pool_chain_slots_by_type";
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
