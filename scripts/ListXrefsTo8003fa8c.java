// List xrefs to FUN_8003fa8c
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo8003fa8c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8003fa8cL);
        ReferenceManager rm = currentProgram.getReferenceManager();
        ghidra.program.model.symbol.ReferenceIterator it = rm.getReferencesTo(target);
        int count = 0;
        while (it.hasNext()) {
            Reference ref = it.next();
            count++;
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = (caller != null) ? caller.getName() : "?";
            println(String.format("  from 0x%08x in %s", from.getOffset(), callerName));
        }
        println("xrefs_to=0x8003fa8c count=" + count);
    }
}