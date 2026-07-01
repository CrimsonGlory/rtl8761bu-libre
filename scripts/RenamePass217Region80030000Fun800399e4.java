// Rename FUN_800399e4 -> invoke_calibration_hook_for_byte_offsets_0_to_0x20
// Pass 217, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass217Region80030000Fun800399e4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800399e4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800399e4");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_calibration_hook_for_byte_offsets_0_to_0x20",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}