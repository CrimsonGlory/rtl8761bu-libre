// Rename FUN_8003b428 -> newton_floor_sqrt_lower16_shifted_left_6
// Pass 180, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass180Region80030000Fun8003b428 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b428");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b428");
            return;
        }
        String oldName = f.getName();
        f.setName("newton_floor_sqrt_lower16_shifted_left_6",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}