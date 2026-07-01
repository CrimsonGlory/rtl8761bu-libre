// Rename FUN_800212a0 -> clamp_connection_qos_poll_interval_from_stored_limits
// Pass 6 continuation (147), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800212a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800212a0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800212a0");
            return;
        }
        String oldName = f.getName();
        f.setName("clamp_connection_qos_poll_interval_from_stored_limits",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}