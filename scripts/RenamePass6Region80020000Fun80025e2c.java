// Rename FUN_80025e2c -> precompute_dual_ssp_confirm_hmac_blocks_for_eir_snapshot
// Pass 6 continuation (132), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025e2c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025e2c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025e2c");
            return;
        }
        String oldName = f.getName();
        f.setName("precompute_dual_ssp_confirm_hmac_blocks_for_eir_snapshot",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}