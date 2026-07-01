// Rename FUN_80024560 -> send_lmp_encryption_key_size_req_0x10_and_set_state_0x4a
// Pass 6 continuation (221), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024560 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024560");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024560");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_encryption_key_size_req_0x10_and_set_state_0x4a",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}