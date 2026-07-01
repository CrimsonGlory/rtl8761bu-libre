// Rename FUN_80025910 -> send_lmp_ext_io_capability_resp_subopcode_0x1a_from_crypto
// Pass 6 continuation (208), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025910 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025910");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025910");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_ext_io_capability_resp_subopcode_0x1a_from_crypto",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}