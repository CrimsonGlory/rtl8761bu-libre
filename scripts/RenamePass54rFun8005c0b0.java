// RenamePass54rFun8005c0b0.java — Pass 54r cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54rFun8005c0b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005c0b0L;
        String newName = "compare_afh_channel_key_to_slot10_and_pack_or_restage";
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
