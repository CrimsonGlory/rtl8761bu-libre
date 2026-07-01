// Rename FUN_80039a14 -> compute_scaled_tx_power_level_from_config_hook
// Pass 246, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass246Region80030000Fun80039a14 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039a14");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039a14");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_scaled_tx_power_level_from_config_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}