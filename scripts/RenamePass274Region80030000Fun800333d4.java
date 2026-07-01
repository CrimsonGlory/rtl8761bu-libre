// Rename FUN_800333d4 -> truncate_ushort_to_byte_if_conn_field0x206_eq_1
// Pass 274, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass274Region80030000Fun800333d4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800333d4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800333d4");
            return;
        }
        String oldName = f.getName();
        f.setName("truncate_ushort_to_byte_if_conn_field0x206_eq_1",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}