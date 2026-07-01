// Rename FUN_80021240 -> test_hci_evt_opcode_bypass_mask_bit_0x40_0x80
// Pass 6 continuation (174), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021240 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021240");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021240");
            return;
        }
        String oldName = f.getName();
        f.setName("test_hci_evt_opcode_bypass_mask_bit_0x40_0x80",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}