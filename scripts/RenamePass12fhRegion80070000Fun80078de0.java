// RenamePass12fhRegion80070000Fun80078de0.java — Pass 12fh param TLV codec-page apply gate
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fhRegion80070000Fun80078de0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80078de0L;
        String newName = "apply_codec_page_on_param_tlv_0xff_status_0x5d_nibble_2";
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
