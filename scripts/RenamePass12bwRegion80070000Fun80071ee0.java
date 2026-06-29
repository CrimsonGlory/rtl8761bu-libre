// RenamePass12bwRegion80070000Fun80071ee0.java — Pass 12bw LMP preferred-rate gate
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bwRegion80070000Fun80071ee0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071ee0L;
        String newName = "gate_lmp_preferred_rate_send_by_version_and_edr_config";
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
