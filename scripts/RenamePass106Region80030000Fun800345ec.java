// Rename FUN_800345ec -> commit_link_mode_snapshot_role_slots_and_materialize_lut
// Pass 106, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass106Region80030000Fun800345ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800345ec");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800345ec");
            return;
        }
        String oldName = f.getName();
        f.setName("commit_link_mode_snapshot_role_slots_and_materialize_lut",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}