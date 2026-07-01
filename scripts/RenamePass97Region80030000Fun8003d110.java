// Rename FUN_8003d110 -> apply_bdaddr_scramble_slots_from_param_mask_0x6000
// Pass 97, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass97Region80030000Fun8003d110 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d110");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d110");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_bdaddr_scramble_slots_from_param_mask_0x6000",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}