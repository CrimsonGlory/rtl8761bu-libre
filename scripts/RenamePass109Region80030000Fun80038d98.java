// Rename FUN_80038d98 -> program_bb_regs_0x1e_5bit_field_and_clear_0x1c_bit3
// Pass 109, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass109Region80030000Fun80038d98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038d98");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038d98");
            return;
        }
        String oldName = f.getName();
        f.setName("program_bb_regs_0x1e_5bit_field_and_clear_0x1c_bit3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}