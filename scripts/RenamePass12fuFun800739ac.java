// Rename FUN_800739ac -> release_resource_pool_chain_slot_by_type
// Pass 12fu, region 0x80070000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass12fuFun800739ac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800739ac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800739ac");
            return;
        }
        String oldName = f.getName();
        f.setName("release_resource_pool_chain_slot_by_type",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}