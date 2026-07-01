// List xrefs to FUN_80021910
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80021910 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80021910L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        int n = 0;
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            var fn = getFunctionContaining(from);
            String fnName = fn != null ? fn.getName() : "(no fn)";
            println(String.format("xref from 0x%08x in %s", from.getOffset(), fnName));
            n++;
        }
        println("total_xrefs=" + n);
    }
}