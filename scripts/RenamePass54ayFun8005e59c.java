// RenamePass54ayFun8005e59c.java — Pass 54ay cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54ayFun8005e59c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005e59cL;
        String newName = "commit_sco_8byte_params_hook5_alloc_tag9_no_ext_adv_clear";
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
