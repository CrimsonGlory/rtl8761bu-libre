// Rename FUN_800255fc -> send_lmp_comb_or_unit_key_and_set_changed_link_key_type
// Pass 6 continuation (214), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800255fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800255fc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800255fc");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_comb_or_unit_key_and_set_changed_link_key_type",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}