// Pass 52ie: refresh >150B ranks 1-5 after 1-19B tier COMPLETE
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;
import java.util.*;

public class ColdTriageRegion80040000Pass52ie extends GhidraScript {
    public void run() throws Exception {
        AddressFactory af = currentProgram.getAddressFactory();
        Address start = af.getAddress("0x80040000");
        Address end = af.getAddress("0x8004ffff");
        FunctionManager fm = currentProgram.getFunctionManager();
        ReferenceManager rm = currentProgram.getReferenceManager();
        List<long[]> big = new ArrayList<>();
        int total = 0, unnamed = 0;
        FunctionIterator fiter = fm.getFunctions(start, true);
        while (fiter.hasNext()) {
            Function f = fiter.next();
            if (f.getEntryPoint().compareTo(end) > 0) break;
            total++;
            Symbol sym = f.getSymbol();
            if (sym.getSource() != SourceType.DEFAULT) continue;
            unnamed++;
            long addr = f.getEntryPoint().getOffset();
            int xrefs = countRefs(rm, f.getEntryPoint());
            long size = f.getBody().getNumAddresses();
            if (size > 150) big.add(new long[]{addr, xrefs, size});
        }
        big.sort((a, b) -> {
            if (b[1] != a[1]) return Long.compare(b[1], a[1]);
            return Long.compare(b[2], a[2]);
        });
        println("Total: " + total + " Unnamed: " + unnamed + " >150B: " + big.size());
        for (int i = 0; i < Math.min(5, big.size()); i++) {
            long[] c = big.get(i);
            println(String.format("rank%d: 0x%08x  xrefs=%d  size=%dB", i + 1, c[0], c[1], c[2]));
        }
    }
    private int countRefs(ReferenceManager rm, Address addr) {
        int count = 0;
        var iter = rm.getReferencesTo(addr);
        while (iter.hasNext()) { iter.next(); count++; }
        return count;
    }
}