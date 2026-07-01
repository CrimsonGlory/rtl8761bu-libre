// Rename FUN_8003bad4 -> poll_fd49_extended_diag_bb_registers_and_return_status_bytes
// Pass 133, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass133Region80030000Fun8003bad4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003bad4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003bad4");
            return;
        }
        String oldName = f.getName();
        f.setName("poll_fd49_extended_diag_bb_registers_and_return_status_bytes",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}