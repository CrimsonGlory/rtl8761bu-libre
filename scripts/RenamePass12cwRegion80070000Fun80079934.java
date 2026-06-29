// RenamePass12cwRegion80070000Fun80079934.java — Pass 12cw HCI reset config apply
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12cwRegion80070000Fun80079934 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80079934L;
        String newName = "hci_reset_apply_bdaddr_scramble_and_patch_hooks";
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
