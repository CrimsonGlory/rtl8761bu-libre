// Rename FUN_8003a124 -> config_gated_read_hook_arg4_cache_6bit_at_data_offset8
// Pass 255, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass255Region80030000Fun8003a124 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003a124");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003a124");
            return;
        }
        String oldName = f.getName();
        f.setName("config_gated_read_hook_arg4_cache_6bit_at_data_offset8",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}