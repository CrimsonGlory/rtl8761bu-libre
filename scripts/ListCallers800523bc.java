// ListCallers800523bc.java — find xrefs to FUN_800523bc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;
import ghidra.program.model.symbol.*;

public class ListCallers800523bc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x800523bcL);
        ReferenceManager rm = currentProgram.getReferenceManager();
        Reference[] refs = rm.getReferencesTo(target);
        println("refs_to_800523bc count=" + refs.length);
        for (Reference ref : refs) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("  from 0x%08x in %s refType=%s",
                from.getOffset(), callerName, ref.getReferenceType()));
        }
    }
}
