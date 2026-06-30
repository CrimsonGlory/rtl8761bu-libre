// Rename FUN_8002b2b8 -> advance_or_restore_link_register_subslot_quota_step
// Pass 6 continuation (43), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b2b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b2b8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b2b8");
            return;
        }
        String oldName = f.getName();
        f.setName("advance_or_restore_link_register_subslot_quota_step",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}