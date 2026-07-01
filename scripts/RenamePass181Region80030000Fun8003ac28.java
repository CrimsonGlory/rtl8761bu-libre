// Rename FUN_8003ac28 -> dispatch_fd49_diag_bit0_preserve_bb_or_program_bb_bundle
// Pass 181, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass181Region80030000Fun8003ac28 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ac28");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ac28");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_fd49_diag_bit0_preserve_bb_or_program_bb_bundle",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}