// Rename FUN_8003ce98 -> toggle_0x4000_status_bit_via_hook_on_trunc_page_counter_threshold
// Pass 102, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass102Region80030000Fun8003ce98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ce98");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ce98");
            return;
        }
        String oldName = f.getName();
        f.setName("toggle_0x4000_status_bit_via_hook_on_trunc_page_counter_threshold",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}