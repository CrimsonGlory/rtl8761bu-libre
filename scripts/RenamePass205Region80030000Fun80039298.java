// Rename FUN_80039298 -> read_modify_write_hw_reg_0x23_set_bits5_7_from_3bit_param
// Pass 205, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass205Region80030000Fun80039298 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039298");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039298");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x23_set_bits5_7_from_3bit_param",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}