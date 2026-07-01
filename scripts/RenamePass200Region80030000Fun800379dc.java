// Rename FUN_800379dc -> handle_trunc_page_complete_status_bit8_via_optional_hook_and_log_0x6e
// Pass 200, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass200Region80030000Fun800379dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800379dc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800379dc");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_trunc_page_complete_status_bit8_via_optional_hook_and_log_0x6e",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}