// Rename FUN_80037af0 -> query_bb_regs_76_77_78_snapshot_and_log_when_gated
// Pass 101, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass101Region80030000Fun80037af0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80037af0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80037af0");
            return;
        }
        String oldName = f.getName();
        f.setName("query_bb_regs_76_77_78_snapshot_and_log_when_gated",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}