// Rename FUN_800254b0 -> xor_inbound_lmp_key_and_update_crypto_by_type
// Pass 6 continuation (114), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800254b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800254b0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800254b0");
            return;
        }
        String oldName = f.getName();
        f.setName("xor_inbound_lmp_key_and_update_crypto_by_type",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}