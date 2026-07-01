// Rename FUN_800242b0 -> send_lmp_ext_pkt_0x7f_subopcode_0x22
// Pass 6 continuation (237), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800242b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800242b0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800242b0");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_ext_pkt_0x7f_subopcode_0x22",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}