// RenamePass12bdRegion80070000Fun800791d0.java — Pass 12bd codec page bitfield parser
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bdRegion80070000Fun800791d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800791d0L;
        String newName = "parse_codec_page_bitfields_into_0x2c_descriptor";
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
