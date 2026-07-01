// Rename FUN_800396ec -> compute_timing_short_from_callback_scaled_by_byte_delta_div100
// Pass 261, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass261Region80030000Fun800396ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800396ec");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800396ec");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_timing_short_from_callback_scaled_by_byte_delta_div100",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}