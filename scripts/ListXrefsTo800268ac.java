// List xrefs to FUN_800268ac
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo800268ac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x800268acL);
        ReferenceManager rm = currentProgram.getReferenceManager();
        int n = 0;
        for (Reference ref : rm.getReferencesTo(target)) {
            println(String.format("xref_in from 0x%08x type=%s",
                ref.getFromAddress().getOffset(), ref.getReferenceType()));
            n++;
        }
        println("total_xrefs=" + n);
    }
}