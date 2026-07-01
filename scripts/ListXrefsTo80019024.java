// ListXrefsTo80019024.java — xref_in for Pass 109 target
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80019024 extends GhidraScript {
    @Override
    public void run() throws Exception {
        ReferenceManager rm = currentProgram.getReferenceManager();
        Function target = getFunctionAt(toAddr(0x80019024L));
        if (target == null) {
            println("MISSING target");
            return;
        }
        int n = 0;
        for (Reference ref : rm.getReferencesTo(target.getEntryPoint())) {
            Function caller = getFunctionContaining(ref.getFromAddress());
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("xref %d: 0x%08x in %s", n++, ref.getFromAddress().getOffset(), callerName));
        }
        println("total=" + n);
    }
}