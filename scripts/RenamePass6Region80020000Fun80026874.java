// Rename FUN_80026874 -> build_occupied_link_key_bdaddr_and_key_ptr_arrays
// Pass 6 continuation (217), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80026874 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80026874");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80026874");
            return;
        }
        String oldName = f.getName();
        f.setName("build_occupied_link_key_bdaddr_and_key_ptr_arrays",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}