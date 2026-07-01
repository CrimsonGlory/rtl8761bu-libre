// Rename FUN_80024470 -> wrap_send_lmp_pkt_with_conn_cc_hook_and_validate
// Pass 6 continuation (95), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024470 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024470");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024470");
            return;
        }
        String oldName = f.getName();
        f.setName("wrap_send_lmp_pkt_with_conn_cc_hook_and_validate",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}