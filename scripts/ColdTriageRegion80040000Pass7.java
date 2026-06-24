// GHIDRA HEADLESS SCRIPT
// ColdTriageRegion80040000Pass7.java
// Cold-triage remaining 151-600B tier (xrefs:1 candidates)
// Builds on Pass 6's exhaustion of xrefs:2 tier.
// Outputs: (size) address, xref count, existing name, relative strength

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.*;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.data.*;
import java.util.*;

public class ColdTriageRegion80040000Pass7 extends GhidraScript {
  public void run() throws Exception {
    
    // Target region: 0x80040000 - 0x8004ffff
    Address regionStart = currentProgram.getAddressFactory().getAddress("0x80040000");
    Address regionEnd = currentProgram.getAddressFactory().getAddress("0x8004ffff");
    
    // Already-renamed (PASS 2-6 HIGH confidence)
    Set<String> highConfidenceAddrs = new HashSet<String>(Arrays.asList(
      "0x80041c18", "0x80042188", "0x80042420", "0x80042a14", "0x80042a28", 
      "0x80042a3c", "0x80042a58", "0x80042b38", "0x80043810", "0x80044430",
      "0x8004a71c", "0x8004c0f4", // PASS 2
      "0x8004d8b8", "0x80049d20", // PASS 3 / 3-cont HIGH
      "0x8004966c", // PASS 3-cont HIGH
      "0x8004ca7c", // PASS 4 HIGH
      "0x80043884", "0x80043984" // PASS 6 HIGH
    ));
    
    // Already-decompiled (MEDIUM/MEDIUM-HIGH, not re-triaging)
    Set<String> alreadyDecompiledAddrs = new HashSet<String>(Arrays.asList(
      "0x80043e04", "0x80040a24", "0x8004147c", "0x80041dac", "0x8004d294",
      "0x8004ce70", "0x80040594", "0x80041230", "0x80042640", "0x800401c4",
      "0x80040e60", "0x80041900", "0x80043c7c", "0x80043a60", "0x80041a94",
      "0x800435a8", "0x80041028", "0x8004c4a8", "0x800483c0", "0x80047628",
      "0x80047304", "0x8004cb48", "0x80047c50", "0x80046900", "0x800480b0",
      "0x8004b468", "0x80045964", // PASS 3 / 3-cont
      "0x8004090c", "0x8004f580", // PASS 4
      "0x8004bde8", "0x8004ef08", "0x80040060", "0x8004326c", "0x8004b3c0",
      "0x8004fd6c", "0x8004b898", // PASS 5
      "0x8004eb18", "0x8004f374", "0x8004f730", "0x800442bc", "0x8004ea2c",
      "0x8004c940", "0x8004f25c", "0x80043884", "0x80043984" // PASS 6
    ));
    
    FunctionManager fm = currentProgram.getFunctionManager();
    ReferenceManager refMgr = currentProgram.getReferenceManager();
    
    Map<String, Object[]> candidates = new TreeMap<String, Object[]>();
    
    // Enumerate all functions in region
    for (Function fn : fm.getFunctions(regionStart, true)) {
      Address fnAddr = fn.getEntryPoint();
      if (!fnAddr.getAddressSpace().getName().equals(regionStart.getAddressSpace().getName())) continue;
      long offset = fnAddr.getOffset();
      if (offset < 0x80040000 || offset > 0x8004ffff) continue;
      
      String addrStr = "0x" + Long.toHexString(offset).toUpperCase();
      
      // Skip already-HIGH or already-decompiled
      if (highConfidenceAddrs.contains(addrStr) || alreadyDecompiledAddrs.contains(addrStr))
        continue;
      
      // Size filter: 151-600B tier
      long size = fn.getBody().getNumAddresses();
      if (size < 151 || size > 600) continue;
      
      // XRef count (in-script query, not via MCP wrapper)
      int xrefCount = refMgr.getReferencesTo(fnAddr).length;
      
      // Symbol name
      String name = fn.getName();
      
      candidates.put(addrStr, new Object[]{size, xrefCount, name, fn});
    }
    
    // Sort by xref count (descending), then size (descending)
    List<Map.Entry<String, Object[]>> sorted = new ArrayList<>(candidates.entrySet());
    sorted.sort((a, b) -> {
      int xcA = (int) a.getValue()[1];
      int xcB = (int) b.getValue()[1];
      if (xcB != xcA) return Integer.compare(xcB, xcA);
      return Long.compare((long) b.getValue()[0], (long) a.getValue()[0]);
    });
    
    println("=== PASS 7: XRefs:1 Tier Candidates (151-600B, xrefs:1 only) ===");
    println("Count: " + sorted.size());
    println("\nTop candidates (xrefs:1, size desc):");
    
    int shown = 0;
    for (Map.Entry<String, Object[]> e : sorted) {
      int xref = (int) e.getValue()[1];
      if (xref != 1) continue;
      if (shown >= 20) break;
      println(String.format("%s (size %dB, xrefs:%d) %s",
        e.getKey(), e.getValue()[0], xref, e.getValue()[2]));
      shown++;
    }
    
    println("\n\nRemaining xrefs:2+ in this tier:");
    int xref2Plus = 0;
    for (Map.Entry<String, Object[]> e : sorted) {
      int xref = (int) e.getValue()[1];
      if (xref >= 2) {
        if (xref2Plus == 0) println("  (sorted by xref desc, size desc)");
        if (xref2Plus < 15) println(String.format("  %s (size %dB, xrefs:%d) %s",
          e.getKey(), e.getValue()[0], xref, e.getValue()[2]));
        xref2Plus++;
      }
    }
    println("  Total: " + xref2Plus);
    
    println("\n\nSummary: " + sorted.size() + " 151-600B tier candidates remain untouched");
  }
}
