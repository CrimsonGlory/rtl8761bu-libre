// Rename FUN_80029364 -> handle_lmp_ext_io_capability_resp_subopcode_0x1a
// Pass 6 continuation (93), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029364 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029364");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029364");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_ext_io_capability_resp_subopcode_0x1a",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}