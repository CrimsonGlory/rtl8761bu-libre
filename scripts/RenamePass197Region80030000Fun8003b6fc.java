// Rename FUN_8003b6fc -> read_modify_write_hw_reg_0x44_set_bit1
// Pass 197, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass197Region80030000Fun8003b6fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b6fc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b6fc");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x44_set_bit1",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}