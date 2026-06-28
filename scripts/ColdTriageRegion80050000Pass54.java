// ColdTriageRegion80050000Pass54.java
// Cold-triage re-rank for ROM region 0x80050000 — Pass 54
// Same 9-artifact exclusion list as Pass 48/49/50/51/52/53.
// Ranks all unnamed FUN_* in [0x80050000, 0x80060000) by xref count descending,
// then prints the top 20 with size + xref count.

import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;
import ghidra.program.model.symbol.*;
import java.util.*;

public class ColdTriageRegion80050000Pass54 extends GhidraScript {

    // 9 confirmed mis-disassembly artifacts — excluded from unnamed tally
    private static final long[] ARTIFACT_ADDRS = {
        0x8005d548L, 0x8005dd04L, 0x8005e3a8L, 0x8005e71cL, 0x8005f880L,
        0x80052f18L, 0x80052f1aL, 0x80059cdeL, 0x80059ce0L
    };

    @Override
    public void run() throws Exception {
        AddressFactory af = currentProgram.getAddressFactory();
        AddressSpace space = af.getDefaultAddressSpace();
        FunctionManager fm = currentProgram.getFunctionManager();
        ReferenceManager rm = currentProgram.getReferenceManager();

        Address regionStart = space.getAddress(0x80050000L);
        Address regionEnd   = space.getAddress(0x8005ffffL);
        AddressSetView regionSet = af.getAddressSet(regionStart, regionEnd);

        // Build artifact set
        Set<Long> artifactSet = new HashSet<>();
        for (long a : ARTIFACT_ADDRS) artifactSet.add(a);

        int total = 0, named = 0, unnamed = 0;
        List<long[]> candidates = new ArrayList<>(); // [addr, size, xrefCount]

        FunctionIterator it = fm.getFunctions(regionSet, true);
        while (it.hasNext()) {
            Function f = it.next();
            long addr = f.getEntryPoint().getOffset();
            if (artifactSet.contains(addr)) continue;
            total++;
            String name = f.getName();
            boolean isFun = name.startsWith("FUN_");
            if (!isFun) { named++; continue; }
            unnamed++;
            long size = f.getBody().getNumAddresses();
            int xrefs = rm.getReferencesTo(f.getEntryPoint()).length;
            candidates.add(new long[]{addr, size, xrefs});
        }

        // Sort by xref count desc, then size desc
        candidates.sort((a, b) -> {
            int c = Long.compare(b[2], a[2]);
            if (c != 0) return c;
            return Long.compare(b[1], a[1]);
        });

        println("=== ColdTriage Pass54 Region 0x80050000 ===");
        println("Total (excl artifacts): " + total);
        println("Named: " + named);
        println("Unnamed: " + unnamed);
        println("Top 20 candidates (rank, addr, size, xrefs):");
        int rank = 1;
        for (long[] c : candidates) {
            if (rank > 20) break;
            println(String.format("  %2d. FUN_%08x  %4dB  xrefs=%d",
                rank, c[0], c[1], c[2]));
            rank++;
        }
    }
}
