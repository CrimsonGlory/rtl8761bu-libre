// Rename FUN_800222b0 -> kickoff_post_role_switch_encryption_or_auth_by_link_type
// Pass 6 continuation (117), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800222b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800222b0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800222b0");
            return;
        }
        String oldName = f.getName();
        f.setName("kickoff_post_role_switch_encryption_or_auth_by_link_type",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}