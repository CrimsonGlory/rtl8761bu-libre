// Rename FUN_8003f980 -> dispatch_fptr_opcodes_0x8f_0x82_with_status_bytes_set_to_ff
// Pass 89, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass89Region80030000Fun8003f980 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003f980");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003f980");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_fptr_opcodes_0x8f_0x82_with_status_bytes_set_to_ff",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}