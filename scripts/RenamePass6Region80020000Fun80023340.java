// Rename FUN_80023340 -> fHCI_Authentication_Requested_0x11
// Pass 6 continuation (64), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023340 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023340");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023340");
            return;
        }
        String oldName = f.getName();
        f.setName("fHCI_Authentication_Requested_0x11",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}