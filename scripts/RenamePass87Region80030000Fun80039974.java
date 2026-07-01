// Rename FUN_80039974 -> dispatch_be_u16_pairs_reverse_step2_from_buf_via_fptr_hook
// Pass 87, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass87Region80030000Fun80039974 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039974");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039974");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_be_u16_pairs_reverse_step2_from_buf_via_fptr_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}