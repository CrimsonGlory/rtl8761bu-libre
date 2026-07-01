// Rename FUN_80025524 -> derive_comb_key_xor_and_send_lmp_0x09
// Pass 6 continuation (113), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025524 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025524");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025524");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_comb_key_xor_and_send_lmp_0x09",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}