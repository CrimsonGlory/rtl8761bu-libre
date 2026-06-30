// Find callers of FUN_8002ee84
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class FindCallers8002ee84 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002ee84L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        println("Callers of 0x8002ee84:");
        for (Reference ref : rm.getReferencesTo(target)) {
            if (!ref.getReferenceType().isCall()) continue;
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "(no fn)";
            println(String.format("  0x%08x from %s", from.getOffset(), callerName));
        }
    }
}