// List xrefs to FUN_800218ec
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo800218ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x800218ecL);
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