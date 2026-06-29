// RenamePass12ehRegion80070000Fun80066330.java — Pass 12eh PSM/QoS state-0x0a sub-dispatch
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ehRegion80070000Fun80066330 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80066330L;
        String newName = "run_psm_qos_state_0x0a_tiered_eligibility_subdispatch";
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
