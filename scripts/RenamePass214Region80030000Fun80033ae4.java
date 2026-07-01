// Rename FUN_80033ae4 -> invoke_link_mode_change_optional_prehook_status_byte
// Pass 214, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass214Region80030000Fun80033ae4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033ae4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033ae4");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_link_mode_change_optional_prehook_status_byte",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}