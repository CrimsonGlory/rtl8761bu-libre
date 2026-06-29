// RenamePass12crRegion80070000Fun80078b94.java — Pass 12cr HCI reset LMP 0x268 gateway
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12crRegion80070000Fun80078b94 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80078b94L;
        String newName = "hci_reset_invoke_lmp_268_with_param_block_dword";
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
