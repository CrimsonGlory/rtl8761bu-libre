import ghidra.app.script.GhidraScript;

public class DumpD618Pool extends GhidraScript {
  public void run() throws Exception {
    long start = 0x8010d7c0L;
    int n = 48;
    println("POOL_START");
    for (int i = 0; i < n; i += 4) {
      long v = 0;
      for (int b = 0; b < 4; b++) {
        v |= (long)(getByte(toAddr(start + i + b)) & 0xff) << (8 * b);
      }
      println(String.format("0x%08x: 0x%08x", start + i, v));
    }
    println("POOL_END");
  }
}
