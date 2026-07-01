// Rename FUN_8003de48 -> lmp_status_apply_packet_types_on_stride88_slot
// Pass 121, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass121Region80030000Fun8003de48 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003de48");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003de48");
            return;
        }
        String oldName = f.getName();
        f.setName("lmp_status_apply_packet_types_on_stride88_slot",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}