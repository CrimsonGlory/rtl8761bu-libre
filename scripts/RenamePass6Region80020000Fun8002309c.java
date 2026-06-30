// Rename FUN_8002309c -> fHCI_PIN_Code_Request_Reply_0xd
// Pass 6 continuation (56), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002309c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002309c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002309c");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_PIN_Code_Request_Reply_0xd",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}