// Rename FUN_800305f4 -> allocate_resource_pool_chain_slots_hci_feature_page_bind_0x1ac_struct
// Pass 241, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass241Region80030000Fun800305f4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800305f4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800305f4");
            return;
        }
        String oldName = f.getName();
        f.setName("allocate_resource_pool_chain_slots_hci_feature_page_bind_0x1ac_struct",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}