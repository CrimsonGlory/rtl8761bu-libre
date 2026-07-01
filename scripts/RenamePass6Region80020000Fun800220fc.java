// Rename FUN_800220fc -> arm_encryption_before_deferred_role_switch
// Pass 6 continuation (107), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800220fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800220fc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800220fc");
            return;
        }
        String oldName = f.getName();
        f.setName("arm_encryption_before_deferred_role_switch",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}