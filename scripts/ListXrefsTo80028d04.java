// List xrefs to FUN_80028d04
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.listing.Function;

public class ListXrefsTo80028d04 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80028d04L);
        ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(target);
        int n = 0;
        while (it.hasNext()) {
            Reference ref = it.next();
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "(no fn)";
            println(String.format("xref from 0x%08x in %s refType=%s",
                from.getOffset(), callerName, ref.getReferenceType()));
            n++;
        }
        println("total_xrefs=" + n);
    }
}