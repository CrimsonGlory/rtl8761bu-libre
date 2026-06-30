// List xrefs to FUN_800239cc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;

public class ListXrefsTo800239cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x800239ccL);
        Function fn = getFunctionAt(target);
        if (fn == null) {
            println("ERROR: no function at 0x800239cc");
            return;
        }
        println("Function: " + fn.getName() + " size=" + fn.getBody().getNumAddresses());
        int n = 0;
        ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(target);
        while (it.hasNext()) {
            Reference ref = it.next();
            Function caller = getFunctionContaining(ref.getFromAddress());
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("  0x%08x in %s", ref.getFromAddress().getOffset(), callerName));
            n++;
        }
        println("xref_in=" + n);
    }
}