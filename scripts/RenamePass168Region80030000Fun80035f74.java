// Rename FUN_80035f74 -> apply_lmp_3ee_case2_and_link_mode_byte_on_signed_status
// Pass 168, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass168Region80030000Fun80035f74 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035f74");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035f74");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_lmp_3ee_case2_and_link_mode_byte_on_signed_status",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}