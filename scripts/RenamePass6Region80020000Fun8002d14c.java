// Rename FUN_8002d14c -> derive_e21_or_e22_16byte_block_via_hmac_driver
// Pass 6 continuation (62), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002d14c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002d14c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002d14c");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_e21_or_e22_16byte_block_via_hmac_driver",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}