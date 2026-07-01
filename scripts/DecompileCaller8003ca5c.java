// Find and decompile function containing xref at 0x8003cac8
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.*;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class DecompileCaller8003ca5c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address callSite = toAddr(0x8003cac8L);
        Function caller = getFunctionContaining(callSite);
        if (caller == null) {
            println("no containing function at 0x8003cac8");
            return;
        }
        println("caller=" + caller.getName() + " @ 0x" +
            Long.toHexString(caller.getEntryPoint().getOffset()) +
            " size=" + caller.getBody().getNumAddresses());
        DecompInterface decomp = new DecompInterface();
        decomp.openProgram(currentProgram);
        DecompileResults results = decomp.decompileFunction(caller, 30, monitor);
        if (results.decompileCompleted()) {
            println(results.getDecompiledFunction().getC());
        } else {
            println("decompile failed");
        }
    }
}