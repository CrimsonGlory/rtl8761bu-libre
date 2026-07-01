// Rename FUN_8003b64c -> read_modify_write_hw_reg_0x44_set_bits12_14_from_3bit_param
// Pass 194, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass194Region80030000Fun8003b64c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b64c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b64c");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x44_set_bits12_14_from_3bit_param",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}