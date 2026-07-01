// Rename FUN_80033e10 -> materialize_indexed_lut_5_ushort_buf0_18_ushort_buf1
// Pass 189, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass189Region80030000Fun80033e10 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033e10");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033e10");
            return;
        }
        String oldName = f.getName();
        f.setName("materialize_indexed_lut_5_ushort_buf0_18_ushort_buf1",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}