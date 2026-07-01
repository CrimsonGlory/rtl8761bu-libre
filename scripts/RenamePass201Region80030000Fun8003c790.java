// Rename FUN_8003c790 -> irq_masked_program_bb_regs_0x188_0x18a_mode0_and_clear_config_bit0x10
// Pass 201, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass201Region80030000Fun8003c790 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003c790");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003c790");
            return;
        }
        String oldName = f.getName();
        f.setName("irq_masked_program_bb_regs_0x188_0x18a_mode0_and_clear_config_bit0x10",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}