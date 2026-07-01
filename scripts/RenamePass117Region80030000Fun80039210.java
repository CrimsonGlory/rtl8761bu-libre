// Rename FUN_80039210 -> apply_hw_reg_0x2b_slot_nibble_if_config_bit3
// Pass 117, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass117Region80030000Fun80039210 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039210");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039210");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_hw_reg_0x2b_slot_nibble_if_config_bit3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}