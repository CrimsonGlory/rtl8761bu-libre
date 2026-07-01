// Rename FUN_80029944 -> on_random_bdaddr_lmp_detach_if_reason_set
// Pass 6 continuation (225), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029944 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029944");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029944");
            return;
        }
        String oldName = f.getName();
        f.setName("on_random_bdaddr_lmp_detach_if_reason_set",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}