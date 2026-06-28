// RenamePass54cFun8002b6f4.java — single HIGH rename for Pass 54c pipeline finalizer
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54cFun8002b6f4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8002b6f4L;
        String newName = "apply_per_slot_quota_delta_and_validate_link_register";
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
