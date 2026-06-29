// RenamePass12diRegion80070000Fun80075c68.java — Pass 12di resource pool 11-slot 12-byte table init
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12diRegion80070000Fun80075c68 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075c68L;
        String newName = "init_resource_pool_11_slot_12byte_descriptors";
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
