// Pass 52hy: pivot to 1-19B unnamed tier (size>=20B and >150B exhausted)
// @category Analysis
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;
import java.util.*;

public class ColdTriageRegion80040000Pass52hy2 extends GhidraScript {
    public void run() throws Exception {
        AddressFactory af = currentProgram.getAddressFactory();
        Address start = af.getAddress("0x80040000");
        Address end = af.getAddress("0x8004ffff");
        FunctionManager fm = currentProgram.getFunctionManager();
        ReferenceManager rm = currentProgram.getReferenceManager();
        List<long[]> tiny = new ArrayList<>();
        FunctionIterator fiter = fm.getFunctions(start, true);
        int total = 0, unnamed = 0;
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
            if (size >= 1 && size < 20) tiny.add(new long[]{addr, xrefs, size});
        }
        tiny.sort((a, b) -> {
            if (b[1] != a[1]) return Long.compare(b[1], a[1]);
            return Long.compare(b[2], a[2]);
        });
        println("Total: " + total + " Unnamed: " + unnamed + " 1-19B: " + tiny.size());
        for (int i = 0; i < Math.min(8, tiny.size()); i++) {
            long[] c = tiny.get(i);
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