// Rename FUN_80021a94 -> init_pool_slots_flush_packet_slots_and_initialize_global_struct
// Pass 6 continuation (280), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021a94 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021a94");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021a94");
            return;
        }
        String oldName = f.getName();
        f.setName("init_pool_slots_flush_packet_slots_and_initialize_global_struct",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}