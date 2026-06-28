// RenamePass54wFun80059f54.java — Pass 54w cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54wFun80059f54 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80059f54L;
        String newName = "remap_negotiation_param2_2bit_subfield_to_abort_code";
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
