// List xrefs to FUN_8002b1f8
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo8002b1f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002b1f8L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        println("xrefs_to=0x8002b1f8");
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("  from=0x%08x caller=%s", from.getOffset(), callerName));
        }
    }
}