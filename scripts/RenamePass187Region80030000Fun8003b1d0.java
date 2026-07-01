// Rename FUN_8003b1d0 -> get_tx_power_byte_from_config_field453_plus_channel_or_hook
// Pass 187, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass187Region80030000Fun8003b1d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b1d0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b1d0");
            return;
        }
        String oldName = f.getName();
        f.setName("get_tx_power_byte_from_config_field453_plus_channel_or_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}