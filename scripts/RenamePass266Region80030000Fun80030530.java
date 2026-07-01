// Rename FUN_80030530 -> gate_resource_pool_chain_type03_cmd_by_connection_handle_lookup
// Pass 266, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass266Region80030000Fun80030530 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80030530");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80030530");
            return;
        }
        String oldName = f.getName();
        f.setName("gate_resource_pool_chain_type03_cmd_by_connection_handle_lookup",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}