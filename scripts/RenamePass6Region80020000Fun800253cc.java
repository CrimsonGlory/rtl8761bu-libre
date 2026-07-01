// Rename FUN_800253cc -> accept_in_rand_safer_plus_pin_encrypt_and_send_lmp_0x08
// Pass 6 continuation (198), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800253cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800253cc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800253cc");
            return;
        }
        String oldName = f.getName();
        f.setName("accept_in_rand_safer_plus_pin_encrypt_and_send_lmp_0x08",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}