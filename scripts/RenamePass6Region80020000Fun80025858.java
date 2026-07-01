// Rename FUN_80025858 -> send_lmp_ext_pkt_0x7f_subopcode_0x1d_ssp_dhkey_check_rsp
// Pass 6 continuation (265), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025858 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025858");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025858");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_ext_pkt_0x7f_subopcode_0x1d_ssp_dhkey_check_rsp",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}