// Rename FUN_80030560 -> release_resource_pool_chain_slot_type02_with_cleanup
// Pass 160, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass160Region80030000Fun80030560 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80030560");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80030560");
            return;
        }
        String oldName = f.getName();
        f.setName("release_resource_pool_chain_slot_type02_with_cleanup",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}