// Rename FUN_800286a8 -> handle_lmp_simple_pairing_number_not_accepted
// Pass 6 continuation (31), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800286a8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800286a8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800286a8");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_simple_pairing_number_not_accepted",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}