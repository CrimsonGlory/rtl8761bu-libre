// Rename FUN_8002cddc -> safer_plus_block_encrypt
// Pass 6 continuation (21), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002cddc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002cddc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002cddc");
            return;
        }
        String oldName = f.getName();
        f.setName("safer_plus_block_encrypt",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}