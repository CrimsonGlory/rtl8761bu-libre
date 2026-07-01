// List xrefs to FUN_8002ed9c
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo8002ed9c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002ed9cL);
        ReferenceManager rm = currentProgram.getReferenceManager();
        ReferenceIterator it = rm.getReferencesTo(target);
        int n = 0;
        while (it.hasNext()) {
            Reference ref = it.next();
            println(String.format("xref_in from 0x%08x type=%s",
                ref.getFromAddress().getOffset(), ref.getReferenceType()));
            n++;
        }
        println("total_xrefs=" + n);
    }
}