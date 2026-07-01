// Rename FUN_800221f0 -> program_key_block_and_arm_mode3_encryption
// Pass 6 continuation (276), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800221f0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800221f0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800221f0");
            return;
        }
        String oldName = f.getName();
        f.setName("program_key_block_and_arm_mode3_encryption",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}