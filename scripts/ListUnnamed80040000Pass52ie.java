// Pass 52ie: list all remaining unnamed in region 0x80040000
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;
import java.util.*;

public class ListUnnamed80040000Pass52ie extends GhidraScript {
    public void run() throws Exception {
        AddressFactory af = currentProgram.getAddressFactory();
        Address start = af.getAddress("0x80040000");
        Address end = af.getAddress("0x8004ffff");
        FunctionManager fm = currentProgram.getFunctionManager();
        ReferenceManager rm = currentProgram.getReferenceManager();
        List<long[]> all = new ArrayList<>();
        FunctionIterator fiter = fm.getFunctions(start, true);
        while (fiter.hasNext()) {
            Function f = fiter.next();
            if (f.getEntryPoint().compareTo(end) > 0) break;
            Symbol sym = f.getSymbol();
            if (sym.getSource() != SourceType.DEFAULT) continue;
            long addr = f.getEntryPoint().getOffset();
            int xrefs = countRefs(rm, f.getEntryPoint());
            long size = f.getBody().getNumAddresses();
            all.add(new long[]{addr, xrefs, size});
        }
        all.sort((a, b) -> Long.compare(a[0], b[0]));
        println("Remaining unnamed count: " + all.size());
        for (long[] c : all) {
            println(String.format("0x%08x  xrefs=%d  size=%dB", c[0], c[1], c[2]));
        }
    }
    private int countRefs(ReferenceManager rm, Address addr) {
        int count = 0;
        var iter = rm.getReferencesTo(addr);
        while (iter.hasNext()) { iter.next(); count++; }
        return count;
    }
}