// Rename FUN_800362b4 -> arm_page_inquiry_scan_timer_if_idle_else_flush_codec_slots
// Pass 55, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass55Region80030000Fun800362b4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800362b4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800362b4");
            return;
        }
        String oldName = f.getName();
        f.setName("arm_page_inquiry_scan_timer_if_idle_else_flush_codec_slots",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}