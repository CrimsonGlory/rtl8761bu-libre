// RenamePass12fcRegion80070000Fun80073a5c.java — Pass 12fc LMP power-req readiness gate
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fcRegion80070000Fun80073a5c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80073a5cL;
        String newName = "gate_lmp_power_req_when_all_bos_inactive_and_links_idle";
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
