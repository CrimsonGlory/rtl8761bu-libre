// Rename FUN_8003b698 -> read_modify_write_hw_reg_0x44_set_bit0
// Pass 71, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass71Region80030000Fun8003b698 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b698");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b698");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x44_set_bit0",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}