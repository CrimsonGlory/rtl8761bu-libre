// Rename FUN_800260f4 -> dispatch_ssp_user_passkey_request_or_notification
// Pass 6 continuation (69), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800260f4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800260f4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800260f4");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_ssp_user_passkey_request_or_notification",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}