// RenamePass12fjRegion80070000Fun80074244.java — Pass 12fj resource-pool BDADDR/page matcher
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fjRegion80070000Fun80074244 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80074244L;
        String newName = "match_active_resource_pool_slot_by_bdaddr_page_update_tlv";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSED at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
