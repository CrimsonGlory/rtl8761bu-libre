// Rename FUN_800236cc -> fHCI_Remote_OOB_Data_Request_Reply_0x30
// Pass 6 continuation (32), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800236cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800236cc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800236cc");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_Remote_OOB_Data_Request_Reply_0x30",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}