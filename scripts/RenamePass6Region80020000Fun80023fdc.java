// Rename FUN_80023fdc -> get_feature_page6_bit3_when_page8_bit0_enabled_local_and_conn
// Pass 6 continuation (259), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023fdc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023fdc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023fdc");
            return;
        }
        String oldName = f.getName();
        f.setName("get_feature_page6_bit3_when_page8_bit0_enabled_local_and_conn",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}