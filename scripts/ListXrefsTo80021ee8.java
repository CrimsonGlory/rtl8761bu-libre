// List xrefs to FUN_80021ee8
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80021ee8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80021ee8L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        ReferenceIterator it = rm.getReferencesTo(target);
        int n = 0;
        while (it.hasNext()) {
            Reference ref = it.next();
            println("xref from " + ref.getFromAddress() + " type=" + ref.getReferenceType());
            n++;
        }
        println("total_xrefs=" + n);
    }
}