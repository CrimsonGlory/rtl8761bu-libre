// Rename FUN_8002b394 -> store_packed_dual_nibbles_with_bit8_to_global
// Pass 6 continuation (295), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b394 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b394");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b394");
            return;
        }
        String oldName = f.getName();
        f.setName("store_packed_dual_nibbles_with_bit8_to_global",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}