// Rename FUN_8003024c -> dispatch_lmp_0x2a_qos_req_via_tx_hook
// Pass 108, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass108Region80030000Fun8003024c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003024c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003024c");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_lmp_0x2a_qos_req_via_tx_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}