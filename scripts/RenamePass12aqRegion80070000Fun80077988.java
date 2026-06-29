// RenamePass12aqRegion80070000Fun80077988.java — Pass 12aq int16 window mean scaler
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12aqRegion80070000Fun80077988 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80077988L;
        String newName = "mean_int16_window_divide_and_shift7";
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
