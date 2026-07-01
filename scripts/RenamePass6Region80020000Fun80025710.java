// Rename FUN_80025710 -> clear_global_status_bit0_on_four_slots
// Pass 6 continuation (287), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025710 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025710");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025710");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_global_status_bit0_on_four_slots",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}