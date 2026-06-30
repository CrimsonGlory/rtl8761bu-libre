// Rename FUN_80071cbc -> dispatch_lmp_25c_multi_slot_emit_with_config_gates
// Pass 12fv, region 0x80070000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass12fvFun80071cbc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80071cbc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80071cbc");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_lmp_25c_multi_slot_emit_with_config_gates",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}