// Rename FUN_800259c8 -> derive_simple_pairing_confirm_and_send_lmp_0x3f
// Pass 6 continuation (79), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800259c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800259c8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800259c8");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_simple_pairing_confirm_and_send_lmp_0x3f",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}