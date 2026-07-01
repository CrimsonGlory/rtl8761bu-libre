// Rename FUN_80038640 -> apply_clamped_tx_power_to_link_class_table_with_hook_fallback
// Pass 247, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass247Region80030000Fun80038640 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038640");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038640");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_clamped_tx_power_to_link_class_table_with_hook_fallback",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}