// Rename FUN_80022328 -> on_random_bdaddr_send_lmp_0x3a_if_feature_page_bit_armed
// Pass 6 continuation (228), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022328 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022328");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022328");
            return;
        }
        String oldName = f.getName();
        f.setName("on_random_bdaddr_send_lmp_0x3a_if_feature_page_bit_armed",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}