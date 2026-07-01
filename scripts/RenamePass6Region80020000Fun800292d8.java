// Rename FUN_800292d8 -> handle_lmp_ext_subopcode_0x1e_keypress_notification_by_ssp_state
// Pass 6 continuation (98), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800292d8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800292d8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800292d8");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_ext_subopcode_0x1e_keypress_notification_by_ssp_state",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}