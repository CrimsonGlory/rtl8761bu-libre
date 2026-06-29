// RenamePass12bcRegion80070000Fun80078fdc.java — Pass 12bc feature-page bit-field merge
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bcRegion80070000Fun80078fdc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80078fdcL;
        String newName = "merge_feature_page_bytes_into_conn_record_bitfields_0x44_0x49";
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
