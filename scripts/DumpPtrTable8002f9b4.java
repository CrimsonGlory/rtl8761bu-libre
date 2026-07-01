// Dump function pointer table at PTR_PTR_8002f9b4 (7 entries for VSC 0xFCF0)
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.mem.Memory;

public class DumpPtrTable8002f9b4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address tablePtr = toAddr(0x8002f9b4L);
        Memory mem = currentProgram.getMemory();
        int ptrVal = mem.getInt(tablePtr);
        Address table = toAddr(ptrVal & 0xffffffffL);
        println("PTR_PTR_8002f9b4 -> table at 0x" + Long.toHexString(table.getOffset()));
        for (int i = 0; i < 7; i++) {
            int fnPtr = mem.getInt(table.add(i * 4));
            Address fnAddr = toAddr(fnPtr & 0xffffffffL);
            Function fn = getFunctionAt(fnAddr);
            String name = fn != null ? fn.getName() : "?";
            println(String.format("  [%d] 0x%08x %s", i, fnAddr.getOffset(), name));
        }
    }
}