// Rename FUN_80039c98 -> apply_tx_power_runtime_mode_byte_and_reconfigure_tables_and_links
// Pass 129, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass129Region80030000Fun80039c98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039c98");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039c98");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_tx_power_runtime_mode_byte_and_reconfigure_tables_and_links",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}