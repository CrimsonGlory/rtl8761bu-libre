// Rename FUN_8002bd04 -> program_or_restore_sco_esco_link_register_slot_banks
// Pass 6 continuation (6), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002bd04 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002bd04");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002bd04");
            return;
        }
        String oldName = f.getName();
        f.setName("program_or_restore_sco_esco_link_register_slot_banks",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}