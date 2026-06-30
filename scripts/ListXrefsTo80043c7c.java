// List xrefs to FUN_80043c7c
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;
import ghidra.program.model.symbol.*;

public class ListXrefsTo80043c7c extends GhidraScript {
    public void run() throws Exception {
        Address target = currentProgram.getAddressFactory().getAddress("0x80043c7c");
        ReferenceManager rm = currentProgram.getReferenceManager();
        FunctionManager fm = currentProgram.getFunctionManager();
        println("Xrefs TO " + target + ":");
        var iter = rm.getReferencesTo(target);
        int n = 0;
        while (iter.hasNext()) {
            Reference ref = iter.next();
            n++;
            Function caller = fm.getFunctionContaining(ref.getFromAddress());
            String callerName = caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "unknown";
            println(String.format("  from 0x%08x in %s (type=%s)", ref.getFromAddress().getOffset(), callerName, ref.getReferenceType()));
        }
        println("Total: " + n);
    }
}