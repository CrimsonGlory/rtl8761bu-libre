// List callers of FUN_80025524
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80025524 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80025524L);
        Function fn = getFunctionAt(target);
        if (fn == null) {
            println("ERROR: no function at 0x80025524");
            return;
        }
        println("Function: " + fn.getName() + " size=" + fn.getBody().getNumAddresses());
        ReferenceManager rm = currentProgram.getReferenceManager();
        ReferenceIterator it = rm.getReferencesTo(target);
        int count = 0;
        while (it.hasNext()) {
            Reference ref = it.next();
            count++;
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("  0x%08x in %s", from.getOffset(), callerName));
        }
        println("xref_in=" + count);
    }
}