// List functions in 0x8002f900-0x8002fb00
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;

public class ListFns8002f900_8002fb00 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address start = toAddr(0x8002f900L);
        Address end = toAddr(0x8002fb00L);
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(start, true);
        while (it.hasNext()) {
            Function fn = it.next();
            if (fn.getEntryPoint().compareTo(end) > 0) break;
            int size = (int) fn.getBody().getNumAddresses();
            println(String.format("0x%08x %3dB %s",
                fn.getEntryPoint().getOffset(), size, fn.getName()));
        }
    }
}