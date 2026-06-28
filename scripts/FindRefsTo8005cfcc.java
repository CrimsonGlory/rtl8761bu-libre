// FindRefsTo8005cfcc.java — one-shot xref finder
import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.*;

public class FindRefsTo8005cfcc extends GhidraScript {
    @Override
    public void run() throws Exception {
        var addr = toAddr(0x8005cfccL);
        var rm = currentProgram.getReferenceManager();
        println("refs to FUN_8005cfcc:");
        int n = 0;
        for (Reference ref : rm.getReferencesTo(addr)) {
            var from = ref.getFromAddress();
            var fn = getFunctionContaining(from);
            String fnName = fn != null ? fn.getName() : "?";
            println(String.format("  from 0x%08x in %s type=%s",
                from.getOffset(), fnName, ref.getReferenceType()));
            n++;
        }
        println("total=" + n);
    }
}
