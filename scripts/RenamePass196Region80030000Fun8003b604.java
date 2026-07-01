// Rename FUN_8003b604 -> read_modify_write_hw_reg_0x44_set_bits7_10_from_4bit_param
// Pass 196, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass196Region80030000Fun8003b604 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b604");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b604");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x44_set_bits7_10_from_4bit_param",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}