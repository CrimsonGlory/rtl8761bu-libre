// Rename FUN_80038f6c -> build_16bit_inclusive_bit_range_mask
// Pass 116, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass116Region80030000Fun80038f6c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038f6c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038f6c");
            return;
        }
        String oldName = f.getName();
        f.setName("build_16bit_inclusive_bit_range_mask",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}