// List xrefs to FUN_800379cc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo800379cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x800379ccL);
        ReferenceManager rm = currentProgram.getReferenceManager();
        println("xrefs_to FUN_800379cc:");
        for (Reference ref : rm.getReferencesTo(target)) {
            println("  from " + ref.getFromAddress() + " type=" + ref.getReferenceType());
        }
        println("DAT_800379d8 refs:");
        Address dat = toAddr(0x800379d8L);
        for (Reference ref : rm.getReferencesTo(dat)) {
            println("  from " + ref.getFromAddress() + " type=" + ref.getReferenceType());
        }
    }
}