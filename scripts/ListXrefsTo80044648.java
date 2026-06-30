// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;

public class ListXrefsTo80044648 extends GhidraScript {
    public void run() throws Exception {
        Address target = currentProgram.getAddressFactory().getAddress("0x80044648");
        ReferenceManager rm = currentProgram.getReferenceManager();
        FunctionManager fm = currentProgram.getFunctionManager();
        println("Xrefs TO 0x80044648:");
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            Function caller = fm.getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "unknown";
            println(String.format("  from 0x%08x in %s", from.getOffset(), callerName));
        }
        println("Xrefs FROM 0x80044648:");
        for (Reference ref : rm.getReferencesFrom(target)) {
            Address to = ref.getToAddress();
            println(String.format("  to 0x%08x", to.getOffset()));
        }
    }
}