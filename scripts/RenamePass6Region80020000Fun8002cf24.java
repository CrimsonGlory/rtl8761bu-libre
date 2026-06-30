// Rename FUN_8002cf24 -> pad_concat_safer_plus_encrypt_16byte_key_block
// Pass 6 continuation (89), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002cf24 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002cf24");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002cf24");
            return;
        }
        String oldName = f.getName();
        f.setName("pad_concat_safer_plus_encrypt_16byte_key_block",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}