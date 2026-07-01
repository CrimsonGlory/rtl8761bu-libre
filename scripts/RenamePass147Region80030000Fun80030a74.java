// Rename FUN_80030a74 -> validate_and_burst_write_indexed_bb_registers_by_role
// Pass 147, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass147Region80030000Fun80030a74 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80030a74");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80030a74");
            return;
        }
        String oldName = f.getName();
        f.setName("validate_and_burst_write_indexed_bb_registers_by_role",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}