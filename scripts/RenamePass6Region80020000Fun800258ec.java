// Rename FUN_800258ec -> send_lmp_ext_pkt_0x7f_subopcode_0x18_legacy_encryption_mode_req
// Pass 6 continuation (262), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800258ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800258ec");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800258ec");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_ext_pkt_0x7f_subopcode_0x18_legacy_encryption_mode_req",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}