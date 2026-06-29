// RenamePass12dkRegion80070000Fun800786c4.java — Pass 12dk PSM/QoS quantizer ctx reset
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dkRegion80070000Fun800786c4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800786c4L;
        String newName = "reset_psm_or_qos_quantizer_context_length_and_history";
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
