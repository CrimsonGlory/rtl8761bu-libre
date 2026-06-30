// Rename FUN_8002bb50 -> role_index_remap_gate_invoke_connection_slot_reuse
// Pass 6 continuation (49), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002bb50 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002bb50");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002bb50");
            return;
        }
        String oldName = f.getName();
        f.setName("role_index_remap_gate_invoke_connection_slot_reuse",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}