// Rename FUN_80021d7c -> init_inquiry_page_scan_slot_table_from_counter
// Pass 6 continuation (299), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021d7c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021d7c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021d7c");
            return;
        }
        String oldName = f.getName();
        f.setName("init_inquiry_page_scan_slot_table_from_counter",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}