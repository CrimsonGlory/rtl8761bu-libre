// Rename FUN_80039234 -> read_modify_write_hw_reg_0x22_bits7_9_if_config_byte1_bit2_set
// Pass 224, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass224Region80030000Fun80039234 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039234");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039234");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x22_bits7_9_if_config_byte1_bit2_set",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}