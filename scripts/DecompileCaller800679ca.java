// Decompile function containing call site 0x800679ca
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.*;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class DecompileCaller800679ca extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address site = toAddr(0x800679caL);
        Function fn = getFunctionContaining(site);
        if (fn == null) {
            println("ERROR: no function at 0x800679ca");
            return;
        }
        println("caller_fn=" + fn.getName() + " entry=" + fn.getEntryPoint());
        DecompInterface decomp = new DecompInterface();
        decomp.openProgram(currentProgram);
        DecompileResults res = decomp.decompileFunction(fn, 60, monitor);
        if (res.decompileCompleted()) {
            String text = res.getDecompiledFunction().getC();
            println(text.length() > 4000 ? text.substring(0, 4000) + "\n...truncated" : text);
        } else {
            println("decompile failed");
        }
    }
}