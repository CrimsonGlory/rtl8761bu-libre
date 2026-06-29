// RenamePass12eoRegion80070000Fun800646e8.java — Pass 12eo PSM/QoS fast-path 10-byte template merge+copy
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12eoRegion80070000Fun800646e8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800646e8L;
        String newName = "apply_psm_qos_10byte_fastpath_template_merge_and_copy";
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
