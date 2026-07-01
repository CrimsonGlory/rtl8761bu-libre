// Rename FUN_80024754 -> accept_lmp_encryption_mode_disable_and_branch_by_bdaddr_random
// Pass 6 continuation (153), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024754 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024754");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024754");
            return;
        }
        String oldName = f.getName();
        f.setName("accept_lmp_encryption_mode_disable_and_branch_by_bdaddr_random",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}