// List xrefs to FUN_8002f478
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo8002f478 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002f478L);
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