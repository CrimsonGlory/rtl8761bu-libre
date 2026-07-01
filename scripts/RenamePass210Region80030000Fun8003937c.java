// Rename FUN_8003937c -> write_hw_reg_0x10_four_config_bytes_via_hw_hook
// Pass 210, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass210Region80030000Fun8003937c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003937c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003937c");
            return;
        }
        String oldName = f.getName();
        f.setName("write_hw_reg_0x10_four_config_bytes_via_hw_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}