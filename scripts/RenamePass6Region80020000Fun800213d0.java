// Rename FUN_800213d0 -> validate_hci_periodic_inquiry_mode_params
// Pass 6 continuation (176), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800213d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800213d0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800213d0");
            return;
        }
        String oldName = f.getName();
        f.setName("validate_hci_periodic_inquiry_mode_params",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}