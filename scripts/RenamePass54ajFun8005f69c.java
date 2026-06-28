// RenamePass54ajFun8005f69c.java — Pass 54aj cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54ajFun8005f69c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005f69cL;
        String newName = "apply_esco_timing_codec_params_to_slot";
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
