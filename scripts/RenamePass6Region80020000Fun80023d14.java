// Rename FUN_80023d14 -> continue_ssp_pairing_after_hci_debug_mode_write
// Pass 6 continuation (25), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023d14 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023d14");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023d14");
            return;
        }
        String oldName = f.getName();
        f.setName("continue_ssp_pairing_after_hci_debug_mode_write",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}