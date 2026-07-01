// Rename FUN_800391d0 -> read_modify_write_hw_reg_0x22_set_bits7_9_from_3bit_param
// Pass 199, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass199Region80030000Fun800391d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800391d0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800391d0");
            return;
        }
        String oldName = f.getName();
        f.setName("read_modify_write_hw_reg_0x22_set_bits7_9_from_3bit_param",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}