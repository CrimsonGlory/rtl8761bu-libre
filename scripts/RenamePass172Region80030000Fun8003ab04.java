// Rename FUN_8003ab04 -> read_fd49_diag_and_bb_reg_7e_pair_via_mode_41_select
// Pass 172, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass172Region80030000Fun8003ab04 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ab04");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ab04");
            return;
        }
        String oldName = f.getName();
        f.setName("read_fd49_diag_and_bb_reg_7e_pair_via_mode_41_select",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}