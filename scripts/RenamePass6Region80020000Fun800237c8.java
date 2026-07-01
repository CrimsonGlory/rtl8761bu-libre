// Rename FUN_800237c8 -> fHCI_User_Passkey_Request_Reply_0x34
// Pass 6 continuation (99), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800237c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800237c8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800237c8");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_User_Passkey_Request_Reply_0x34",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}