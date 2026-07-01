// Rename FUN_8003b9c0 -> log_eight_rotating_ring_buffer_dword_pairs_when_diag_gates_active
// Pass 131, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass131Region80030000Fun8003b9c0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b9c0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b9c0");
            return;
        }
        String oldName = f.getName();
        f.setName("log_eight_rotating_ring_buffer_dword_pairs_when_diag_gates_active",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}