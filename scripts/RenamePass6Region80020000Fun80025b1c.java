// Rename FUN_80025b1c -> start_encryption_ssp_or_legacy_lmp_arm_substate_0x49_0x4b
// Pass 6 continuation (175), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025b1c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025b1c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025b1c");
            return;
        }
        String oldName = f.getName();
        f.setName("start_encryption_ssp_or_legacy_lmp_arm_substate_0x49_0x4b",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}