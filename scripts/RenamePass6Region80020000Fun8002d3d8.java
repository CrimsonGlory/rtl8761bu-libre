// Rename FUN_8002d3d8 -> derive_encryption_key_material_safer_plus_mode6
// Pass 6 continuation (83), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002d3d8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002d3d8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002d3d8");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_encryption_key_material_safer_plus_mode6",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}