// Find callers of FUN_800218c0
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class FindCallers800218c0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x800218c0L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "(no fn)";
            println(String.format("ref from 0x%08x in %s type=%s",
                from.getOffset(), callerName, ref.getReferenceType()));
        }
    }
}