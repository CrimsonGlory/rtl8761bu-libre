// Rename FUN_80039b80 -> dispatch_tx_power_config_byte_0x2c_or_0x2d_via_dual_fptr_hooks
// Pass 83, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass83Region80030000Fun80039b80 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039b80");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039b80");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_tx_power_config_byte_0x2c_or_0x2d_via_dual_fptr_hooks",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}