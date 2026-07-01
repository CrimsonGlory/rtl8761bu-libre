// List xrefs to FUN_80034ccc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80034ccc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80034cccL);
        ReferenceManager rm = currentProgram.getReferenceManager();
        int n = 0;
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = (caller != null) ? caller.getName() : "?";
            println(String.format("xref_in from 0x%08x in %s", from.getOffset(), callerName));
            n++;
        }
        println("total_xref_in=" + n);
    }
}