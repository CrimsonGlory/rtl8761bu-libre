// Check xrefs to 0x8004e9a8 and compare with 0x8004e9a4
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;

public class CheckXrefs8004e9a8 extends GhidraScript {
    public void run() throws Exception {
        AddressFactory af = currentProgram.getAddressFactory();
        ReferenceManager rm = currentProgram.getReferenceManager();
        FunctionManager fm = currentProgram.getFunctionManager();
        for (long addr : new long[]{0x8004e9a4L, 0x8004e9a8L}) {
            Address a = af.getAddress(String.format("0x%08x", addr));
            Function f = fm.getFunctionAt(a);
            println("=== 0x" + Long.toHexString(addr) + " name=" + (f != null ? f.getName() : "null")
                + " size=" + (f != null ? f.getBody().getNumAddresses() : 0));
            var iter = rm.getReferencesTo(a);
            while (iter.hasNext()) {
                Reference ref = iter.next();
                Address from = ref.getFromAddress();
                Function caller = fm.getFunctionContaining(from);
                println("  xref from 0x" + Long.toHexString(from.getOffset())
                    + (caller != null ? " in " + caller.getName() : ""));
            }
        }
    }
}