// Rename FUN_80025410 -> derive_pin_safer_plus_au_rand_and_send_lmp_0x0b
// Pass 6 continuation (143), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025410 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025410");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025410");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_pin_safer_plus_au_rand_and_send_lmp_0x0b",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}