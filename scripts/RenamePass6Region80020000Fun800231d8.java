// Rename FUN_800231d8 -> fHCI_Set_Connection_Encryption_0x13
// Pass 6 continuation (47), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800231d8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800231d8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800231d8");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_Set_Connection_Encryption_0x13",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}