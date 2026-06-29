// RenamePass12dxRegion80070000Fun80078d8c.java — Pass 12dx param TLV lookup
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dxRegion80070000Fun80078d8c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80078d8cL;
        String newName = "lookup_param_tlv_entry_type_0xff_if_status_0x5d";
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
