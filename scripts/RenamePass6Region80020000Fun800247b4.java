// Rename FUN_800247b4 -> finalize_stop_encryption_procedure_and_notify_hci
// Pass 6 continuation (14), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800247b4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800247b4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800247b4");
            return;
        }
        String oldName = f.getName();
        f.setName("finalize_stop_encryption_procedure_and_notify_hci",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}