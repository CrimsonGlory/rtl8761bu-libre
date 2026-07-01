// Rename FUN_8003ac7c -> read_fd49_extended_diag_build_dual_slot_bitmasks_and_shift_width
// Pass 123, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass123Region80030000Fun8003ac7c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ac7c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ac7c");
            return;
        }
        String oldName = f.getName();
        f.setName("read_fd49_extended_diag_build_dual_slot_bitmasks_and_shift_width",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}