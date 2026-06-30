// Rename FUN_80023878 -> fHCI_Remote_OOB_Data_Request_Negative_Reply_0x2e
// Pass 6 continuation (48), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023878 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023878");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023878");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_Remote_OOB_Data_Request_Negative_Reply_0x2e",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}