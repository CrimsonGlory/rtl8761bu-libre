// Rename FUN_800392f8 -> read_modify_write_hw_reg_0x17_set_bits0_2_from_3bit_param
// Pass 204, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass204Region80030000Fun800392f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800392f8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800392f8");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x17_set_bits0_2_from_3bit_param",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}