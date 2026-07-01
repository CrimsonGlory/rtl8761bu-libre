// Rename FUN_80036100 -> increment_esco_slot_counter_and_apply_codec_if_gate_armed
// Pass 118, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass118Region80030000Fun80036100 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80036100");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80036100");
            return;
        }
        String oldName = f.getName();
        f.setName("increment_esco_slot_counter_and_apply_codec_if_gate_armed",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}