// Rename FUN_8003d558 -> irq_masked_program_slot_bit_in_reg2_and_clear_reg11c_by_conn_index
// Pass 98, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass98Region80030000Fun8003d558 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d558");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d558");
            return;
        }
        String oldName = f.getName();
        f.setName("irq_masked_program_slot_bit_in_reg2_and_clear_reg11c_by_conn_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}