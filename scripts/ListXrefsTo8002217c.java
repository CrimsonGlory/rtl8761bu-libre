// List xrefs to FUN_8002217c
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.listing.Function;

public class ListXrefsTo8002217c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002217cL);
        ReferenceIterator refs = currentProgram.getReferenceManager().getReferencesTo(target);
        int n = 0;
        while (refs.hasNext()) {
            Reference ref = refs.next();
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = (caller != null) ? caller.getName() : "?";
            println(String.format("xref from 0x%08x in %s", from.getOffset(), callerName));
            n++;
        }
        println("total_xref_in=" + n);
    }
}