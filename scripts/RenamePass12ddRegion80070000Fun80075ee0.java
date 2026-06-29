// RenamePass12ddRegion80070000Fun80075ee0.java — Pass 12dd typed resource slot registrar
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ddRegion80070000Fun80075ee0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075ee0L;
        String newName = "register_typed_resource_slot_if_index_free";
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
