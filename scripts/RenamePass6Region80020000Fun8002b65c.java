// Rename FUN_8002b65c -> finalize_hw_crypto_slot_table_entry_and_set_exception_handler
// Pass 6 continuation (96), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b65c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b65c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b65c");
            return;
        }
        String oldName = f.getName();
        f.setName("finalize_hw_crypto_slot_table_entry_and_set_exception_handler",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}