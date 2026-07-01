// List xrefs to FUN_80022694
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;
import ghidra.program.model.listing.Function;

public class ListXrefsTo80022694 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80022694L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        int n = 0;
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("xref from 0x%08x in %s", from.getOffset(), callerName));
            n++;
        }
        println("total_xrefs=" + n);
    }
}