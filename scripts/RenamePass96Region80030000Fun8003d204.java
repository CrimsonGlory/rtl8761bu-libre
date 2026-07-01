// Rename FUN_8003d204 -> apply_bdaddr_scramble_slots_from_config_fc_fd_mask
// Pass 96, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass96Region80030000Fun8003d204 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d204");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d204");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_bdaddr_scramble_slots_from_config_fc_fd_mask",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}