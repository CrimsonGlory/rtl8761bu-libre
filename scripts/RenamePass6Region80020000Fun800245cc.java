// Rename FUN_800245cc -> send_lmp_encryption_mode_req_0x0f_mode_on_wrapper
// Pass 6 continuation (284), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800245cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800245cc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800245cc");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_encryption_mode_req_0x0f_mode_on_wrapper",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}