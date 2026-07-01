// List callers of FUN_8002f454
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo8002f454 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002f454L);
        Function fn = getFunctionAt(target);
        if (fn == null) {
            println("ERROR: no function at 0x8002f454");
            return;
        }
        println("Function: " + fn.getName() + " size=" + fn.getBody().getNumAddresses());
        ReferenceManager rm = currentProgram.getReferenceManager();
        int count = 0;
        for (Reference ref : rm.getReferencesTo(target)) {
            count++;
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("  0x%08x in %s", from.getOffset(), callerName));
        }
        println("xref_in=" + count);
    }
}