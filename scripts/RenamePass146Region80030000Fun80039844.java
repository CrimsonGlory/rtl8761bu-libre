// Rename FUN_80039844 -> init_tx_power_runtime_from_config_blob_and_halve_delta_baselines
// Pass 146, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass146Region80030000Fun80039844 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039844");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039844");
            return;
        }
        String oldName = f.getName();
        f.setName("init_tx_power_runtime_from_config_blob_and_halve_delta_baselines",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}