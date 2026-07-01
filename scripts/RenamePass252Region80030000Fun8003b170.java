// Rename FUN_8003b170 -> or_bb_reg_0x111_bit2_delay_invoke_regscript_from_context_0x68_0x6c
// Pass 252, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass252Region80030000Fun8003b170 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b170");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b170");
            return;
        }
        String oldName = f.getName();
        f.setName("or_bb_reg_0x111_bit2_delay_invoke_regscript_from_context_0x68_0x6c",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}