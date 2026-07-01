// Rename FUN_80033794 -> gate_lmp_power_clk_adj_eligibility_by_conn_state
// Pass 62, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass62Region80030000Fun80033794 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033794");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033794");
            return;
        }
        String oldName = f.getName();
        f.setName("gate_lmp_power_clk_adj_eligibility_by_conn_state",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}