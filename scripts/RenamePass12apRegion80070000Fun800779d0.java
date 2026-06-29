// RenamePass12apRegion80070000Fun800779d0.java — Pass 12ap 4-tap FIR smoothing filter
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12apRegion80070000Fun800779d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800779d0L;
        String newName = "moving_average_fir_smooth_int16_4tap";
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
