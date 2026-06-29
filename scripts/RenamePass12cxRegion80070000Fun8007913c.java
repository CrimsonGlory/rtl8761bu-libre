// RenamePass12cxRegion80070000Fun8007913c.java — Pass 12cx HCI reset OGC3 OCF1 invoke
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12cxRegion80070000Fun8007913c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8007913cL;
        String newName = "hci_reset_invoke_ogc3_ocf1_zero_params_and_clear_global_bit2";
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
