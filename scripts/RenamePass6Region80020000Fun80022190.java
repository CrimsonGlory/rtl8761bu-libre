// Rename FUN_80022190 -> disable_link_encryption_per_slot_and_public_crypto_table
// Pass 6 continuation (156), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022190 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022190");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022190");
            return;
        }
        String oldName = f.getName();
        f.setName("disable_link_encryption_per_slot_and_public_crypto_table",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}