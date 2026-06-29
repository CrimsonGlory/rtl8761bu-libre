// RenamePass12bjRegion80070000Fun800747b0.java — Pass 12bj TLV feature-page parser
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bjRegion80070000Fun800747b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800747b0L;
        String newName = "parse_tlv_tag3_ushort_tag5_uint32_tag11_block_from_stream";
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
