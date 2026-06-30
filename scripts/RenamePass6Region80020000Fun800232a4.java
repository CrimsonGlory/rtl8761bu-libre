// Rename FUN_800232a4 -> fHCI_Change_Connection_Link_Key_0x15
// Pass 6 continuation (71), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800232a4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800232a4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800232a4");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_Change_Connection_Link_Key_0x15",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}