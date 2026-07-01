// Rename FUN_8002cfac -> bdaddr_pad_safer_plus_encrypt_xor6_16byte_key_block
// Pass 6 continuation (142), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002cfac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002cfac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002cfac");
            return;
        }
        String oldName = f.getName();
        f.setName("bdaddr_pad_safer_plus_encrypt_xor6_16byte_key_block",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}