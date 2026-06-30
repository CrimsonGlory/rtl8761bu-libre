// List xrefs to FUN_8002aa8c
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.*;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;

public class ListXrefsTo8002aa8c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002aa8cL);
        ReferenceManager rm = currentProgram.getReferenceManager();
        ReferenceIterator it = rm.getReferencesTo(target);
        int n = 0;
        while (it.hasNext()) {
            Reference ref = it.next();
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("xref from 0x%08x in %s", from.getOffset(), callerName));
            n++;
        }
        println("total_xrefs=" + n);
    }
}