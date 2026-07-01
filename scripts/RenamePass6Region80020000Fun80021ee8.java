// Rename FUN_80021ee8 -> get_connection_crypto_encryption_key_size_byte
// Pass 6 continuation (279), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021ee8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021ee8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021ee8");
            return;
        }
        String oldName = f.getName();
        f.setName("get_connection_crypto_encryption_key_size_byte",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}