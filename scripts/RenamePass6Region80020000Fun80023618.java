// Rename FUN_80023618 -> fHCI_User_Confirmation_Request_Reply_0x33
// Pass 6 continuation (97), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023618 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023618");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023618");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_User_Confirmation_Request_Reply_0x33",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}