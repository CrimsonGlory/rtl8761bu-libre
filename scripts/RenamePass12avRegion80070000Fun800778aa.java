// RenamePass12avRegion80070000Fun800778aa.java — Pass 12av VSC FCA1 log thunk
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12avRegion80070000Fun800778aa extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800778aaL;
        String newName = "log_vsc_fca1_decoded_bb_status_bit";
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
