// RenamePass54aeFun800518dc.java — Pass 54ae cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54aeFun800518dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800518dcL;
        String newName = "vsc_0xfc95_init_if_uninitialized";
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
