import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;

public class FindCallers80075ca8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address target = toAddr(0x80075ca8L);
        println("--- xrefs to 0x80075ca8 ---");
        var refs = getReferencesTo(target);
        int n = 0;
        for (var ref : refs) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            println("from=0x" + from + " caller="
                + (caller == null ? "NONE" : caller.getEntryPoint() + " " + caller.getName()));
            n++;
        }
        println("total_xrefs=" + n);
    }
}