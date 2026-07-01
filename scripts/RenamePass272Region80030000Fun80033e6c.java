// Rename FUN_80033e6c -> gather_9_uint32_from_lut_indexed_base_to_buffer
// Pass 272, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass272Region80030000Fun80033e6c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033e6c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033e6c");
            return;
        }
        String oldName = f.getName();
        f.setName("gather_9_uint32_from_lut_indexed_base_to_buffer",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}