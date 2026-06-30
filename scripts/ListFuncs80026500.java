// List functions 0x80026500-0x80026900
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;

public class ListFuncs80026500 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address start = toAddr(0x80026500L);
        Address end = toAddr(0x80026900L);
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(start, true);
        while (it.hasNext()) {
            Function fn = it.next();
            if (fn.getEntryPoint().compareTo(end) > 0) break;
            int size = (int) fn.getBody().getNumAddresses();
            println(String.format("0x%08x %4dB %s", fn.getEntryPoint().getOffset(), size, fn.getName()));
        }
    }
}