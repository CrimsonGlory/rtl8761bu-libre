// RenamePass12doRegion80070000Fun800786dc.java — Pass 12do AFH quantizer history IIR blend
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12doRegion80070000Fun800786dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800786dcL;
        String newName = "iir_blend_afh_quantizer_int16_history_80tap";
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
