// Rename FUN_80024020 -> lookup_crypto_encryption_state_0x14_0x1f_flag
// Pass 6 continuation (285), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024020 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024020");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024020");
            return;
        }
        String oldName = f.getName();
        f.setName("lookup_crypto_encryption_state_0x14_0x1f_flag",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}