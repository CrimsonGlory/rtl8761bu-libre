// Rename FUN_80028634 -> handle_lmp_simple_pairing_confirm_not_accepted
// Pass 6 continuation (126), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80028634 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80028634");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80028634");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_simple_pairing_confirm_not_accepted",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}