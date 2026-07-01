// Rename FUN_80023bdc -> fHCI_Refresh_Encryption_Key_0x14
// Pass 6 continuation (125), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023bdc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023bdc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023bdc");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_Refresh_Encryption_Key_0x14",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}