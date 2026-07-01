// Rename FUN_8003497c -> read_hw_clock_dword_and_optional_slot_offset_by_role_index
// Pass 80, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass80Region80030000Fun8003497c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003497c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003497c");
            return;
        }
        String oldName = f.getName();
        f.setName("read_hw_clock_dword_and_optional_slot_offset_by_role_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}