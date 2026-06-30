// Rename FUN_8002b020 -> transmit_acl_single_packet_direct_via_hw_tx_descriptor
// Pass 6 continuation (38), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b020 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b020");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b020");
            return;
        }
        String oldName = f.getName();
        f.setName("transmit_acl_single_packet_direct_via_hw_tx_descriptor",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}