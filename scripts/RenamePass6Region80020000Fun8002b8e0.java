// Rename FUN_8002b8e0 -> irq_safe_set_active_slot_bit_at_0x138
// Pass 6 continuation (201), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b8e0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b8e0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b8e0");
            return;
        }
        String oldName = f.getName();
        f.setName("irq_safe_set_active_slot_bit_at_0x138",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}