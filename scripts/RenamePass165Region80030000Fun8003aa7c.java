// Rename FUN_8003aa7c -> sweep_fd49_extended_diag_30_channels_with_bb_reg_0xe_bit2_enable
// Pass 165, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass165Region80030000Fun8003aa7c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003aa7c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003aa7c");
            return;
        }
        String oldName = f.getName();
        f.setName("sweep_fd49_extended_diag_30_channels_with_bb_reg_0xe_bit2_enable",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}