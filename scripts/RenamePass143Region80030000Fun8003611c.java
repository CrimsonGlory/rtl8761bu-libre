// Rename FUN_8003611c -> step_conn_esco_codec_counter_and_apply_if_gate_armed
// Pass 143, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass143Region80030000Fun8003611c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003611c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003611c");
            return;
        }
        String oldName = f.getName();
        f.setName("step_conn_esco_codec_counter_and_apply_if_gate_armed",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}