// List xrefs to FUN_800257b0
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo800257b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x800257b0L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        ReferenceIterator it = rm.getReferencesTo(target);
        int n = 0;
        while (it.hasNext()) {
            Reference ref = it.next();
            Function caller = getFunctionContaining(ref.getFromAddress());
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("xref from 0x%08x in %s type=%s",
                ref.getFromAddress().getOffset(), callerName, ref.getReferenceType()));
            n++;
        }
        println("total_xrefs=" + n);
    }
}