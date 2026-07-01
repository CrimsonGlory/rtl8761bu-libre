// Rename FUN_8003b3e4 -> compute_2d_squared_distance_clamp_0xffff_at_threshold
// Pass 259, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass259Region80030000Fun8003b3e4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b3e4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b3e4");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_2d_squared_distance_clamp_0xffff_at_threshold",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}