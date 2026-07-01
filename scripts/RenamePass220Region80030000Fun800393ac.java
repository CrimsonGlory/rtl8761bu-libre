// Rename FUN_800393ac -> write_hw_reg_0x10_four_config_bytes_if_config_byte1_bit7_set
// Pass 220, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass220Region80030000Fun800393ac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800393ac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800393ac");
            return;
        }
        String oldName = f.getName();
        f.setName("write_hw_reg_0x10_four_config_bytes_if_config_byte1_bit7_set",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}