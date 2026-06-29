// RenamePass54atFun8005e174.java — Pass 54at cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54atFun8005e174 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005e174L;
        String newName = "commit_12byte_esco_crypto_band_regs_or_reject";
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
