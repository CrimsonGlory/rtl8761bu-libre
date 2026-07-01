// List xrefs to FUN_80026920
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80026920 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80026920L);
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