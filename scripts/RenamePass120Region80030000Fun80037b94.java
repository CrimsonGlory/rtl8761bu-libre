// Rename FUN_80037b94 -> role_switch_apply_packet_types_on_stride84_slot
// Pass 120, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass120Region80030000Fun80037b94 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80037b94");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80037b94");
            return;
        }
        String oldName = f.getName();
        f.setName("role_switch_apply_packet_types_on_stride84_slot",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}