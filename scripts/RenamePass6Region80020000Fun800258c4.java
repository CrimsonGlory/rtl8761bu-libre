// Rename FUN_800258c4 -> send_lmp_ext_pkt_0x7f_subopcode_0x1e_keypress_notification
// Pass 6 continuation (257), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800258c4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800258c4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800258c4");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_ext_pkt_0x7f_subopcode_0x1e_keypress_notification",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}