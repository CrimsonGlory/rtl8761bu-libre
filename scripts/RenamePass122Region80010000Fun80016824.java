// RenamePass122Region80010000Fun80016824.java — Pass 122 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass122Region80010000Fun80016824 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80016824L;
        String newName = "sync_ogc3_config_bit1_from_tlv_gate_and_dispatch_0x230";
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