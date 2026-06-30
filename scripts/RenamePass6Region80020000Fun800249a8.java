// Rename FUN_800249a8 -> finalize_encryption_procedure_and_notify_hci
// Pass 6 continuation (11), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800249a8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800249a8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800249a8");
            return;
        }
        String oldName = f.getName();
        f.setName("finalize_encryption_procedure_and_notify_hci",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}