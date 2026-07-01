// Rename FUN_800335f0 -> mask_merge_dword_globals_or3_and_clear_bos_bit3_at_0x164
// Pass 90, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass90Region80030000Fun800335f0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800335f0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800335f0");
            return;
        }
        String oldName = f.getName();
        f.setName("mask_merge_dword_globals_or3_and_clear_bos_bit3_at_0x164",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}