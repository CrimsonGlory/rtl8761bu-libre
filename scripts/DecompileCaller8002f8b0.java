// Decompile function containing 0x8002f8b0
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.*;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class DecompileCaller8002f8b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address callSite = toAddr(0x8002f8b0L);
        Function fn = getFunctionContaining(callSite);
        if (fn == null) {
            println("ERROR: no function at 0x8002f8b0");
            return;
        }
        println("caller=" + fn.getName() + " @ " + fn.getEntryPoint() +
                " size=" + fn.getBody().getNumAddresses());
        DecompInterface decomp = new DecompInterface();
        decomp.openProgram(currentProgram);
        DecompileResults res = decomp.decompileFunction(fn, 60, monitor);
        if (res.decompileCompleted()) {
            println(res.getDecompiledFunction().getC());
        } else {
            println("DECOMPILE FAILED");
        }
    }
}