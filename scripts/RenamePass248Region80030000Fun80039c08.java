// Rename FUN_80039c08 -> dispatch_scaled_tx_power_adjustment_by_mode_via_fptr_hook
// Pass 248, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass248Region80030000Fun80039c08 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039c08");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039c08");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_scaled_tx_power_adjustment_by_mode_via_fptr_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}