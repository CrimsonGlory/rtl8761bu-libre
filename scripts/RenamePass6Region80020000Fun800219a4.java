// Rename FUN_800219a4 -> flush_check_seven_packet_slots_via_call_fptr_if_set_wraps_packet_slot_flush_check
// Pass 6 continuation (194), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800219a4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800219a4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800219a4");
            return;
        }
        String oldName = f.getName();
        f.setName("flush_check_seven_packet_slots_via_call_fptr_if_set_wraps_packet_slot_flush_check",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}