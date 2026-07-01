// Rename FUN_8003f9fc -> program_dual_slot_lmp25c_credits_if_role_record_byte1_set
// Pass 218, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass218Region80030000Fun8003f9fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003f9fc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003f9fc");
            return;
        }
        String oldName = f.getName();
        f.setName("program_dual_slot_lmp25c_credits_if_role_record_byte1_set",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}