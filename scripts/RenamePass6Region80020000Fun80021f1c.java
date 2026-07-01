// Rename FUN_80021f1c -> store_dword_pair_to_crypto_global_offsets_b0_b4
// Pass 6 continuation (303), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021f1c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021f1c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021f1c");
            return;
        }
        String oldName = f.getName();
        f.setName("store_dword_pair_to_crypto_global_offsets_b0_b4",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}