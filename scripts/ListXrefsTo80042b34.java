// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;

public class ListXrefsTo80042b34 extends GhidraScript {
    public void run() throws Exception {
        Address target = currentProgram.getAddressFactory().getAddress("0x80042b34");
        ReferenceManager rm = currentProgram.getReferenceManager();
        FunctionManager fm = currentProgram.getFunctionManager();
        println("Xrefs TO 0x80042b34:");
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            Function caller = fm.getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "unknown";
            println(String.format("  from 0x%08x in %s", from.getOffset(), callerName));
        }
    }
}