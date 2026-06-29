// RenamePass12epRegion80070000Fun800645a0.java — Pass 12ep PSM/QoS 5-byte fast-path tail commit
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12epRegion80070000Fun800645a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800645a0L;
        String newName = "finalize_psm_qos_5byte_fastpath_staged_bitmask_commit";
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
