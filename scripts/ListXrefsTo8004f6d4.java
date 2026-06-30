// List xrefs to FUN_8004f6d4
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;

public class ListXrefsTo8004f6d4 extends GhidraScript {
    public void run() throws Exception {
        Address target = currentProgram.getAddressFactory().getAddress("0x8004f6d4");
        ReferenceManager rm = currentProgram.getReferenceManager();
        FunctionManager fm = currentProgram.getFunctionManager();
        println("Xrefs TO " + target + ":");
        var iter = rm.getReferencesTo(target);
        int n = 0;
        while (iter.hasNext()) {
            Reference ref = iter.next();
            Address from = ref.getFromAddress();
            Function caller = fm.getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "(no fn)";
            println(String.format("  from 0x%08x refType=%s caller=%s", from.getOffset(), ref.getReferenceType(), callerName));
            n++;
        }
        println("Total: " + n);
    }
}