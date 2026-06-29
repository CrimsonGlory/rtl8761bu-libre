// RenamePass12erRegion80070000Fun80064b08.java — Pass 12er PSM/QoS opcode-mode dispatch
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12erRegion80070000Fun80064b08 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80064b08L;
        String newName = "dispatch_psm_qos_opcode_mode_merge_bitmask_and_sync";
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
