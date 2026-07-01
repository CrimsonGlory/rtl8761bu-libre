// List xrefs to FUN_80033744
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80033744 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80033744L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        println("Xrefs to 0x80033744:");
        for (Reference ref : rm.getReferencesTo(target)) {
            println("  from " + ref.getFromAddress() + " type=" + ref.getReferenceType());
        }
    }
}