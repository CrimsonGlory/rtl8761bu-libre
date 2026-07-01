// Rename FUN_80032e40 -> irq_safe_append_bytes_to_2048b_ring_buffer
// Pass 161, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass161Region80030000Fun80032e40 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80032e40");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80032e40");
            return;
        }
        String oldName = f.getName();
        f.setName("irq_safe_append_bytes_to_2048b_ring_buffer",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}