// Rename FUN_800396bc -> read_timing_global_dat_byte_at_offset_8
// Pass 285, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass285Region80030000Fun800396bc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800396bc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800396bc");
            return;
        }
        String oldName = f.getName();
        f.setName("read_timing_global_dat_byte_at_offset_8",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}