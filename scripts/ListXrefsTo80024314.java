// List xrefs to FUN_80024314
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80024314 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80024314L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        println("xrefs_to=0x80024314");
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "(no fn)";
            println(String.format("  from=0x%08x caller=%s", from.getOffset(), callerName));
        }
    }
}