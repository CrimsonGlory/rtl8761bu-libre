// Rename FUN_80021614 -> compute_and_store_connection_qos_poll_interval
// Pass 6 continuation (22), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021614 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021614");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021614");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_and_store_connection_qos_poll_interval",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}