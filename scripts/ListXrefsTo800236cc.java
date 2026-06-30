// List xrefs to FUN_800236cc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo800236cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = toAddr(0x800236ccL);
        Function fn = getFunctionAt(addr);
        if (fn == null) {
            println("ERROR: no function at 0x800236cc");
            return;
        }
        println("Xrefs to " + fn.getName() + " @ 0x800236cc:");
        ReferenceManager rm = currentProgram.getReferenceManager();
        for (Reference ref : rm.getReferencesTo(addr)) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "(no fn)";
            println(String.format("  from 0x%08x in %s", from.getOffset(), callerName));
        }
    }
}