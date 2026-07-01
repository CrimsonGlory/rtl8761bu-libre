// Read literal pool dword at 0x8002129c (adjacent to FUN_80021290)
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;

public class ReadDword8002129c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = toAddr(0x8002129cL);
        int val = getInt(addr);
        println("dword@0x8002129c = 0x" + Integer.toHexString(val) + " (" + val + ")");
    }
}