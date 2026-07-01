// Rename FUN_8002f9b8 -> set_vsc_fcf0_active_byte_from_subcmd1_gated_on_status_bit2
// Pass 6 continuation (229), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002f9b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002f9b8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002f9b8");
            return;
        }
        String oldName = f.getName();
        f.setName("set_vsc_fcf0_active_byte_from_subcmd1_gated_on_status_bit2",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}