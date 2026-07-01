// Rename FUN_80038ee0 -> read_modify_write_hw_reg_0x18_set_bits14_15_from_2bit_param
// Pass 206, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass206Region80030000Fun80038ee0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038ee0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038ee0");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x18_set_bits14_15_from_2bit_param",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}