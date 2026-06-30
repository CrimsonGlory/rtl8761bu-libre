// Rename FUN_80024c08 -> update_crypto_struct_key_material_xor_or_copy_by_type
// Pass 6 continuation (74), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024c08 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024c08");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024c08");
            return;
        }
        String oldName = f.getName();
        f.setName("update_crypto_struct_key_material_xor_or_copy_by_type",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}