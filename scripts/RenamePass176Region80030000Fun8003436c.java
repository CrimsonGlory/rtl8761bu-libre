// Rename FUN_8003436c -> walk_10_bos_slots_snapshot_last_valid_role_merge_crypto_to_slot0
// Pass 176, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass176Region80030000Fun8003436c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003436c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003436c");
            return;
        }
        String oldName = f.getName();
        f.setName("walk_10_bos_slots_snapshot_last_valid_role_merge_crypto_to_slot0",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}