// RenamePass12wRegion80070000Fun80077638.java — Pass 12w VSC 0xFCA1 status bitfield decode
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12wRegion80070000Fun80077638 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80077638L;
        String newName = "decode_vsc_fca1_bitfield_and_log_bb_status_flags";
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
