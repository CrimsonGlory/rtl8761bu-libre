// List xrefs to/from FUN_800445f4
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;

public class ListXrefs800445f4 extends GhidraScript {
    public void run() throws Exception {
        Address a = currentProgram.getAddressFactory().getAddress("0x800445f4");
        Function f = currentProgram.getFunctionManager().getFunctionAt(a);
        if (f == null) { println("MISSING"); return; }
        println("Function: " + f.getName() + " size=" + f.getBody().getNumAddresses());
        ReferenceManager rm = currentProgram.getReferenceManager();
        println("=== References TO ===");
        for (Reference ref : rm.getReferencesTo(a)) {
            Function caller = currentProgram.getFunctionManager().getFunctionContaining(ref.getFromAddress());
            String callerName = caller != null ? caller.getName() + " @ " + caller.getEntryPoint() : "?";
            println("  from " + ref.getFromAddress() + " (" + ref.getReferenceType() + ") in " + callerName);
        }
        println("=== References FROM (callees) ===");
        for (Reference ref : rm.getReferencesFrom(a)) {
            if (!ref.getReferenceType().isCall()) continue;
            Function callee = currentProgram.getFunctionManager().getFunctionAt(ref.getToAddress());
            String calleeName = callee != null ? callee.getName() : "?";
            println("  call to " + ref.getToAddress() + " -> " + calleeName);
        }
        // also show what's at Ram80044644
        Address ptr = currentProgram.getAddressFactory().getAddress("0x80044644");
        println("=== Data at 0x80044644 ===");
        var mem = currentProgram.getMemory().getInt(ptr, false);
        println("  dword = 0x" + Integer.toHexString(mem));
    }
}