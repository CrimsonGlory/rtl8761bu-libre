// Rename FUN_80025980 -> send_lmp_simple_pairing_number_from_crypto_0xe8
// Pass 6 continuation (180), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025980 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025980");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025980");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_simple_pairing_number_from_crypto_0xe8",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}