// Rename FUN_80039258 -> read_modify_write_hw_reg_0x16_set_bits1_3_from_3bit_param
// Pass 202, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass202Region80030000Fun80039258 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039258");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039258");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x16_set_bits1_3_from_3bit_param",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}