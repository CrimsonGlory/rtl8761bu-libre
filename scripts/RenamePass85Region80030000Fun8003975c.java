// Rename FUN_8003975c -> test_conn_slot_qualifies_by_mode2_or_sco_packet_type_table_lookup
// Pass 85, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass85Region80030000Fun8003975c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003975c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003975c");
            return;
        }
        String oldName = f.getName();
        f.setName("test_conn_slot_qualifies_by_mode2_or_sco_packet_type_table_lookup",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}