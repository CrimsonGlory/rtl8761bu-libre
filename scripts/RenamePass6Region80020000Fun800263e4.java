// Rename FUN_800263e4 -> dispatch_ssp_remote_oob_data_request_hci
// Pass 6 continuation (106), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800263e4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800263e4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800263e4");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_ssp_remote_oob_data_request_hci",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}