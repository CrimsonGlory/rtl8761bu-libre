// RenamePass12awRegion80070000Fun800778d4.java — Pass 12aw VSC FCA1 BB reg 0x18 writer
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12awRegion80070000Fun800778d4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800778d4L;
        String newName = "write_bb_reg_0x18_when_status_mask_matches";
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
