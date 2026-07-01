// Rename FUN_80039334 -> read_modify_write_hw_reg_0x17_bits0_2_if_config_byte0_bit2_set
// Pass 223, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass223Region80030000Fun80039334 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039334");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039334");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x17_bits0_2_if_config_byte0_bit2_set",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}