// Rename FUN_8003e694 -> gate_connection_slot_reuse_by_link_type_role_match
// Pass 142, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass142Region80030000Fun8003e694 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003e694");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003e694");
            return;
        }
        String oldName = f.getName();
        f.setName("gate_connection_slot_reuse_by_link_type_role_match",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}