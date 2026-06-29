// RenamePass12egRegion80070000Fun800661b8.java — Pass 12eg PSM/QoS state-0x16 sub-dispatch
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12egRegion80070000Fun800661b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800661b8L;
        String newName = "run_psm_qos_state_0x16_eligibility_subdispatch";
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
