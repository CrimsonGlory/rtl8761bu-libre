// Rename FUN_80033048 -> copy_config_sco_timing_triplets_to_globals_and_toggle_0x2000_bit
// Pass 132, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass132Region80030000Fun80033048 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033048");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033048");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_config_sco_timing_triplets_to_globals_and_toggle_0x2000_bit",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}