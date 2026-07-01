// Rename FUN_8003fa24 -> per_link_vsc_fc95_lmp268_gateway_with_param_scaled_timeout
// Pass 177, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass177Region80030000Fun8003fa24 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003fa24");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003fa24");
            return;
        }
        String oldName = f.getName();
        f.setName("per_link_vsc_fc95_lmp268_gateway_with_param_scaled_timeout",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}