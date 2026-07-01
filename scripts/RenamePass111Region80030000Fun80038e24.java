// Rename FUN_80038e24 -> program_bb_reg_0x6f_7bit_field_at_bits7_13_via_hook
// Pass 111, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass111Region80030000Fun80038e24 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038e24");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038e24");
            return;
        }
        String oldName = f.getName();
        f.setName("program_bb_reg_0x6f_7bit_field_at_bits7_13_via_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}