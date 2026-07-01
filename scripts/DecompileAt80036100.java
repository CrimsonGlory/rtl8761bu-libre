// Decompile FUN_80036100 and print disassembly
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;

public class DecompileAt80036100 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = toAddr(0x80036100L);
        Function fn = getFunctionAt(addr);
        if (fn == null) {
            println("ERROR: no function at 0x80036100");
            return;
        }
        println("Function: " + fn.getName() + " size=" + fn.getBody().getNumAddresses());
        println("--- DISASM ---");
        Instruction ins = getInstructionAt(addr);
        int count = 0;
        while (ins != null && ins.getAddress().compareTo(fn.getBody().getMaxAddress()) <= 0) {
            println(ins.getAddress() + ": " + ins.toString());
            ins = ins.getNext();
            if (++count > 20) break;
        }
        println("--- DECOMPILE ---");
        DecompInterface decomp = new DecompInterface();
        decomp.openProgram(currentProgram);
        DecompileResults res = decomp.decompileFunction(fn, 30, monitor);
        if (res.decompileCompleted()) {
            println(res.getDecompiledFunction().getC());
        } else {
            println("decompile failed: " + res.getErrorMessage());
        }
    }
}