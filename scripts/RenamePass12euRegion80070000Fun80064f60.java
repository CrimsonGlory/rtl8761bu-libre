// RenamePass12euRegion80070000Fun80064f60.java — Pass 12eu PSM/QoS channel-slot validate+emit
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12euRegion80070000Fun80064f60 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80064f60L;
        String newName = "validate_psm_qos_channel_slot_and_emit_lmp_0x268";
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
