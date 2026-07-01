// Rename FUN_8002cd80 -> safer_plus_armenian_shuffle_block
// Pass 6 continuation (149), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002cd80 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002cd80");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002cd80");
            return;
        }
        String oldName = f.getName();
        f.setName("safer_plus_armenian_shuffle_block",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}