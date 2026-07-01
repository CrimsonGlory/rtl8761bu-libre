// Rename FUN_80023fb8 -> advance_crypto_substate_via_transition_table
// Pass 6 continuation (291), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023fb8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023fb8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023fb8");
            return;
        }
        String oldName = f.getName();
        f.setName("advance_crypto_substate_via_transition_table",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}