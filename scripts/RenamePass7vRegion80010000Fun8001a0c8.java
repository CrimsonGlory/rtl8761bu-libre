// RenamePass7vRegion80010000Fun8001a0c8.java — Pass 7v cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7vRegion80010000Fun8001a0c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8001a0c8L;
        String newName = "conditionally_apply_ogc3_ocf47_hw_bit0x100_when_idle";
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