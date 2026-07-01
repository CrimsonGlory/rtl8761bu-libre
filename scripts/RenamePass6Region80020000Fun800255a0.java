// Rename FUN_800255a0 -> pairing_continue_comb_or_unit_key_lmp_and_crypto_update
// Pass 6 continuation (155), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800255a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800255a0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800255a0");
            return;
        }
        String oldName = f.getName();
        f.setName("pairing_continue_comb_or_unit_key_lmp_and_crypto_update",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}