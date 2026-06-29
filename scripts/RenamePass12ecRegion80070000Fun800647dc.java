// RenamePass12ecRegion80070000Fun800647dc.java — Pass 12ec PSM/QoS dual quantizer dispatcher
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ecRegion80070000Fun800647dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800647dcL;
        String newName = "run_psm_qos_dual_quantizer_search_and_emit_lmp_0x25c";
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
