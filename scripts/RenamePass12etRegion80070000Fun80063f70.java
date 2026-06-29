// RenamePass12etRegion80070000Fun80063f70.java — Pass 12et PSM/QoS 10-byte triple AND-merge
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12etRegion80070000Fun80063f70 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80063f70L;
        String newName = "and_merge_psm_qos_10byte_three_template_buffers";
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
