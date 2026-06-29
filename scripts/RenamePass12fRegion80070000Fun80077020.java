// RenamePass12fRegion80070000Fun80077020.java — Pass 12f BB link-param register writer
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fRegion80070000Fun80077020 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80077020L;
        String newName = "program_bb_link_param_regs_0x26e_0x274";
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
