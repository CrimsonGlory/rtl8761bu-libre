// Rename FUN_8003fa8c -> apply_random_bdaddr_tx_power_delta_via_vsc_fc95_lmp_268
// Pass 164, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass164Region80030000Fun8003fa8c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003fa8c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003fa8c");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_random_bdaddr_tx_power_delta_via_vsc_fc95_lmp_268",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}