// Rename FUN_800397d0 -> test_config_dc_bit8_and_hook_sample_minus_0x27d_lte_threshold
// Pass 110, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass110Region80030000Fun800397d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800397d0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800397d0");
            return;
        }
        String oldName = f.getName();
        f.setName("test_config_dc_bit8_and_hook_sample_minus_0x27d_lte_threshold",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}