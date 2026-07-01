// Rename FUN_8002587c -> send_lmp_ext_pkt_0x7f_subopcode_0x1c_ssp_number_confirmation_rsp
// Pass 6 continuation (264), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002587c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002587c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002587c");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_ext_pkt_0x7f_subopcode_0x1c_ssp_number_confirmation_rsp",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}