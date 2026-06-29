// RenamePass12eiRegion80070000Fun800666a0.java — Pass 12ei PSM/QoS state-0x16 alternate sub-dispatch
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12eiRegion80070000Fun800666a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800666a0L;
        String newName = "run_psm_qos_state_0x16_alternate_eligibility_subdispatch";
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
