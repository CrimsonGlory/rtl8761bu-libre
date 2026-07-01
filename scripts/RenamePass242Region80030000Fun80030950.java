// Rename FUN_80030950 -> configure_resource_pool_feature_page_slot_bind_0x1ac_struct
// Pass 242, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass242Region80030000Fun80030950 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80030950");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80030950");
            return;
        }
        String oldName = f.getName();
        f.setName("configure_resource_pool_feature_page_slot_bind_0x1ac_struct",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}