// Rename FUN_80021f6c -> lookup_role_switch_block_code_from_crypto_substate
// Pass 6 continuation (234), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021f6c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021f6c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021f6c");
            return;
        }
        String oldName = f.getName();
        f.setName("lookup_role_switch_block_code_from_crypto_substate",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}