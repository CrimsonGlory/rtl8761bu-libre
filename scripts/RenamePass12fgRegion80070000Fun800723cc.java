// RenamePass12fgRegion80070000Fun800723cc.java — Pass 12fg BOS-slot LMP 0x2e sweep
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fgRegion80070000Fun800723cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800723ccL;
        String newName = "sweep_bos_slots_and_send_lmp_max_slot_req_0x2e";
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
