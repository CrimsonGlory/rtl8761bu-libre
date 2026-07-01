// Rename FUN_800260b8 -> derive_ssp_numeric_passkey_from_lcg_prng_and_mask
// Pass 6 continuation (206), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800260b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800260b8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800260b8");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_ssp_numeric_passkey_from_lcg_prng_and_mask",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}