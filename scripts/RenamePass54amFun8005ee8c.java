// RenamePass54amFun8005ee8c.java — Pass 54am cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54amFun8005ee8c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005ee8cL;
        String newName = "commit_pairing_key_material_and_dispatch_lmp26f";
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
