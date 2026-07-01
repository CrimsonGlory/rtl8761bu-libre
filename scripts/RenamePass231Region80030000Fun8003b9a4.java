// Rename FUN_8003b9a4 -> invoke_register_script_from_literal_pool_with_num_halfwords_0x40
// Pass 231, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass231Region80030000Fun8003b9a4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b9a4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b9a4");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_register_script_from_literal_pool_with_num_halfwords_0x40",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}