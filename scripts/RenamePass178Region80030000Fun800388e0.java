// Rename FUN_800388e0 -> arm_afh_cleanup_on_lmp_pdu_nibble_e0_f0_tid_gated
// Pass 178, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass178Region80030000Fun800388e0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800388e0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800388e0");
            return;
        }
        String oldName = f.getName();
        f.setName("arm_afh_cleanup_on_lmp_pdu_nibble_e0_f0_tid_gated",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}