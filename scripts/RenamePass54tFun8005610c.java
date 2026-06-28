// RenamePass54tFun8005610c.java — Pass 54t cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54tFun8005610c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005610cL;
        String newName = "write_mmio_reg_pair_low_bytes_if_slot_lt_11_and_dispatch_3";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSING at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
