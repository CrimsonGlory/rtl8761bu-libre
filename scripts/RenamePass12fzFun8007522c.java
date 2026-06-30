// Rename FUN_8007522c -> emit_patch_absent_a5_diag_packet_via_logger1
// Pass 12fz, region 0x80070000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass12fzFun8007522c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8007522c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8007522c");
            return;
        }
        String oldName = f.getName();
        f.setName("emit_patch_absent_a5_diag_packet_via_logger1",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}