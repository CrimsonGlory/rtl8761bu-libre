// Rename FUN_800215e0 -> log_hci_evt_0x1f8_qos_conn_reason_if_no_patch3
// Pass 6 continuation (224), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800215e0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800215e0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800215e0");
            return;
        }
        String oldName = f.getName();
        f.setName("log_hci_evt_0x1f8_qos_conn_reason_if_no_patch3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}