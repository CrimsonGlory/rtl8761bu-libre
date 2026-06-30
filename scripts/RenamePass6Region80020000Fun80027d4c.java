// Rename FUN_80027d4c -> dispatch_lmp_not_accepted_recovery_alt_by_rejected_opcode
// Pass 6 continuation (73), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80027d4c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80027d4c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80027d4c");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_lmp_not_accepted_recovery_alt_by_rejected_opcode",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}