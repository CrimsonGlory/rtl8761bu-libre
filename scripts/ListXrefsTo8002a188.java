// List xrefs to FUN_8002a188
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo8002a188 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x8002a188L);
        ReferenceManager rm = currentProgram.getReferenceManager();
        int n = 0;
        for (Reference ref : rm.getReferencesTo(target)) {
            println(String.format("xref from 0x%08x type=%s",
                ref.getFromAddress().getOffset(), ref.getReferenceType()));
            n++;
        }
        println("total_xrefs=" + n);
    }
}