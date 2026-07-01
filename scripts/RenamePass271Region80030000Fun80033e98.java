// Rename FUN_80033e98 -> scatter_9_uint32_from_buffer_to_lut_indexed_base
// Pass 271, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass271Region80030000Fun80033e98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033e98");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033e98");
            return;
        }
        String oldName = f.getName();
        f.setName("scatter_9_uint32_from_buffer_to_lut_indexed_base",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}