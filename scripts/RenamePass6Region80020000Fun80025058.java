// Rename FUN_80025058 -> program_encryption_key_and_send_lmp_start_encryption_req
// Pass 6 continuation (34), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025058 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025058");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025058");
            return;
        }
        String oldName = f.getName();
        f.setName("program_encryption_key_and_send_lmp_start_encryption_req",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}