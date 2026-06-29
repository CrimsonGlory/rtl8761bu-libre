// RenamePass12ffRegion80070000Fun80073888.java — Pass 12ff 20-slot resource pool init
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ffRegion80070000Fun80073888 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80073888L;
        String newName = "init_resource_pool_20_slot_sequential_ids_clear_five_ptrs";
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
