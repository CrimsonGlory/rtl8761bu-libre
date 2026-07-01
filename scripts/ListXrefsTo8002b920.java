// ListXrefsTo8002b920.java — find references to FUN_8002b920
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;

public class ListXrefsTo8002b920 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002b920L);
        Function fn = getFunctionAt(target);
        if (fn == null) {
            println("no function at target");
            return;
        }
        int n = 0;
        ReferenceIterator it = currentProgram.getReferenceManager().getReferencesTo(target);
        while (it.hasNext()) {
            Reference ref = it.next();
            Function caller = getFunctionContaining(ref.getFromAddress());
            String callerName = caller != null ? caller.getName() : "?";
            println(String.format("xref from 0x%08x type=%s caller=%s",
                ref.getFromAddress().getOffset(), ref.getReferenceType().getName(), callerName));
            n++;
        }
        println("total_xrefs=" + n);
    }
}