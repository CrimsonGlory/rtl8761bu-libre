// Rename FUN_8003a630 -> dual_pending_vsc_fc95_and_lmp268_gateway_with_config_timeout
// Pass 175, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass175Region80030000Fun8003a630 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003a630");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003a630");
            return;
        }
        String oldName = f.getName();
        f.setName("dual_pending_vsc_fc95_and_lmp268_gateway_with_config_timeout",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}