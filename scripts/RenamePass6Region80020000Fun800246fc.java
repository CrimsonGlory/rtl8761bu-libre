// Rename FUN_800246fc -> accept_lmp_encryption_mode_enable_and_branch_by_bdaddr_random
// Pass 6 continuation (160), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800246fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800246fc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800246fc");
            return;
        }
        String oldName = f.getName();
        f.setName("accept_lmp_encryption_mode_enable_and_branch_by_bdaddr_random",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}