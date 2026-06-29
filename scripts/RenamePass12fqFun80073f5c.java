// Rename FUN_80073f5c -> retry_ext_feature_page_req_and_flush_quality_average_sample
// Pass 12fq, region 0x80070000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass12fqFun80073f5c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80073f5c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80073f5c");
            return;
        }
        String oldName = f.getName();
        f.setName("retry_ext_feature_page_req_and_flush_quality_average_sample",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}
