import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;

public class GetFunctionAt80065008 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Function fn = getFunctionContaining(toAddr(0x80065008L));
        if (fn == null) {
            println("NO_FUNCTION");
            return;
        }
        println("entry=0x" + fn.getEntryPoint() + " name=" + fn.getName()
            + " size=" + fn.getBody().getNumAddresses());
    }
}
