// Rename FUN_8003a520 -> dispatch_complex_pair_arithmetic_by_opcode
// Pass 245, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass245Region80030000Fun8003a520 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003a520");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003a520");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_complex_pair_arithmetic_by_opcode",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}