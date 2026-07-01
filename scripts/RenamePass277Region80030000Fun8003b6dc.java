// Rename FUN_8003b6dc -> read_hw_reg_0x44_bit0_via_indirect_read
// Pass 277, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass277Region80030000Fun8003b6dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b6dc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b6dc");
            return;
        }
        String oldName = f.getName();
        f.setName("read_hw_reg_0x44_bit0_via_indirect_read",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}