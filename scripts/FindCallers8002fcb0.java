// Find callers of FUN_8002fcb0
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.*;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;

public class FindCallers8002fcb0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002fcb0L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        ReferenceIterator it = rm.getReferencesTo(target);
        int n = 0;
        while (it.hasNext()) {
            Reference ref = it.next();
            if (ref.getReferenceType().isCall()) {
                Function caller = getFunctionContaining(ref.getFromAddress());
                println("CALL from 0x" + ref.getFromAddress() +
                        " in " + (caller != null ? caller.getName() : "?"));
                n++;
            }
        }
        println("total_callers=" + n);
    }
}