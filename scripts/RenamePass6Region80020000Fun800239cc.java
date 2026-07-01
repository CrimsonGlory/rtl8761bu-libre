// Rename FUN_800239cc -> hci_resolve_conn_tail_call_dispatch_ssp_lmp_ext_0x1b_or_dhkey_0x41
// Pass 6 continuation (247), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800239cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800239cc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800239cc");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_resolve_conn_tail_call_dispatch_ssp_lmp_ext_0x1b_or_dhkey_0x41",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}