// RenamePass12dmRegion80070000Fun80071c68.java — Pass 12dm LMP 0x25C pending-flag cleanup
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dmRegion80070000Fun80071c68 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071c68L;
        String newName = "emit_lmp_25c_and_clear_conn_pending_flags_d9_204";
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
