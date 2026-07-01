// Rename FUN_800308ec -> clear_resource_pool_slot_occupancy_bit_hci_type03
// Pass 251, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass251Region80030000Fun800308ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800308ec");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800308ec");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_resource_pool_slot_occupancy_bit_hci_type03",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}