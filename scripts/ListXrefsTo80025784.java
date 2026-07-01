// List xrefs to FUN_80025784
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80025784 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80025784L);
        Function fn = getFunctionAt(target);
        if (fn == null) {
            println("ERROR: no function at 0x80025784");
            return;
        }
        println("Target: " + fn.getName() + " size=" + fn.getBody().getNumAddresses());
        ReferenceManager rm = currentProgram.getReferenceManager();
        int n = 0;
        for (Reference ref : rm.getReferencesTo(target)) {
            n++;
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "(no fn)";
            println(String.format("xref %d: from 0x%08x in %s", n,
                from.getOffset(), callerName));
        }
        println("total_xrefs=" + n);
    }
}