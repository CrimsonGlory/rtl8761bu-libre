// Rename FUN_800293f0 -> handle_lmp_ext_io_capability_req_subopcode_0x19
// Pass 6 continuation (17), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800293f0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800293f0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800293f0");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_ext_io_capability_req_subopcode_0x19",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}