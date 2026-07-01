// Rename FUN_800269e8 -> handle_lmp_not_accepted_opcode_0x10_encryption_key_size_finalize
// Pass 6 continuation (131), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800269e8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800269e8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800269e8");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_not_accepted_opcode_0x10_encryption_key_size_finalize",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}