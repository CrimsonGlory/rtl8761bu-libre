// Rename FUN_8003f9bc -> find_public_bdaddr_bos_slot_index_by_byte_0xCC
// Pass 263, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass263Region80030000Fun8003f9bc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003f9bc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003f9bc");
            return;
        }
        String oldName = f.getName();
        f.setName("find_public_bdaddr_bos_slot_index_by_byte_0xCC",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}