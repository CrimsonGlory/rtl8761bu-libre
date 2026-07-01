// Rename FUN_8003e648 -> clear_active_stride88_connection_buffers_and_drain_hci_cmds
// Pass 70, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass70Region80030000Fun8003e648 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003e648");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003e648");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_active_stride88_connection_buffers_and_drain_hci_cmds",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}