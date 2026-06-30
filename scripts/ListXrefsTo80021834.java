// List xrefs to globals used by FUN_80021754
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceManager;

public class ListXrefsTo80021834 extends GhidraScript {
    void listRefs(long addr) throws Exception {
        Address target = toAddr(addr);
        ReferenceManager rm = currentProgram.getReferenceManager();
        println("=== xrefs to 0x" + Long.toHexString(addr) + " ===");
        for (Reference ref : rm.getReferencesTo(target)) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            String callerName = caller != null ? caller.getName() : "(no fn)";
            println(String.format("  from 0x%08x in %s", from.getOffset(), callerName));
        }
    }
    @Override
    public void run() throws Exception {
        listRefs(0x8002182cL);
        listRefs(0x80021830L);
        listRefs(0x80021834L);
    }
}