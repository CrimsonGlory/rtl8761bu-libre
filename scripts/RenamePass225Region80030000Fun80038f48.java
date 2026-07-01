// Rename FUN_80038f48 -> read_modify_write_hw_reg_0x18_bits14_15_if_config_byte1_bit4_set
// Pass 225, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass225Region80030000Fun80038f48 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038f48");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038f48");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x18_bits14_15_if_config_byte1_bit4_set",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}