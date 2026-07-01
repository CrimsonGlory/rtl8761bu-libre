// Rename FUN_800214f4 -> validate_hci_create_connection_params
// Pass 6 continuation (128), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800214f4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800214f4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800214f4");
            return;
        }
        String oldName = f.getName();
        f.setName("validate_hci_create_connection_params",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}