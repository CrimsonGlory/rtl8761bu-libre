// Rename FUN_80039b50 -> compute_tx_power_sum_from_doubled_index_and_global_baselines
// Pass 269, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass269Region80030000Fun80039b50 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039b50");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039b50");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_tx_power_sum_from_doubled_index_and_global_baselines",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}