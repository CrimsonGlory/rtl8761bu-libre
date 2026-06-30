// Rename FUN_8002143c -> validate_hci_role_switch_feasibility_for_bdaddr_and_role
// Pass 6 continuation (59), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002143c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002143c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002143c");
            return;
        }
        String oldName = f.getName();
        f.setName("validate_hci_role_switch_feasibility_for_bdaddr_and_role",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}