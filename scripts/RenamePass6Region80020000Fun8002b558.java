// Rename FUN_8002b558 -> program_active_tx_descriptor_slots_to_hw_registers
// Pass 6 continuation (33), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b558 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b558");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b558");
            return;
        }
        String oldName = f.getName();
        f.setName("program_active_tx_descriptor_slots_to_hw_registers",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}