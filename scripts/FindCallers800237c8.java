// Find callers of FUN_800237c8
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class FindCallers800237c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = currentProgram.getAddressFactory().getAddress("0x800237c8");
        ReferenceManager rm = currentProgram.getReferenceManager();
        println("Callers of FUN_800237c8 @ 0x800237c8:");
        int count = 0;
        for (Reference ref : rm.getReferencesTo(target)) {
            if (!ref.getReferenceType().isCall()) continue;
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "(no fn)";
            println(String.format("  0x%08x in %s", from.getOffset(), callerName));
            count++;
        }
        println("total_callers=" + count);
    }
}