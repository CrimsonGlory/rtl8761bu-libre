// RenamePass12daRegion80070000Fun80071370.java — Pass 12da EIR sentinel inquiry dispatch
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12daRegion80070000Fun80071370 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071370L;
        String newName = "dispatch_eir_sentinel_emit_inquiry_result_and_notify";
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
