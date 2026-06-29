// RenamePass12bvRegion80070000Fun80071620.java — Pass 12bv crypto fptr finalizer wrapper
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bvRegion80070000Fun80071620 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071620L;
        String newName = "invoke_crypto_state_machine_finalizer";
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
