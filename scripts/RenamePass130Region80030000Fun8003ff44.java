// Rename FUN_8003ff44 -> validate_lmp25c_role_record_pdu_and_program_dual_slot_credits_or_reject
// Pass 130, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass130Region80030000Fun8003ff44 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ff44");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ff44");
            return;
        }
        String oldName = f.getName();
        f.setName("validate_lmp25c_role_record_pdu_and_program_dual_slot_credits_or_reject",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}