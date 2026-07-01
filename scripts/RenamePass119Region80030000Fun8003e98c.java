// Rename FUN_8003e98c -> connection_setup_arm_stride88_slot_and_apply_packet_types
// Pass 119, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass119Region80030000Fun8003e98c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003e98c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003e98c");
            return;
        }
        String oldName = f.getName();
        f.setName("connection_setup_arm_stride88_slot_and_apply_packet_types",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}