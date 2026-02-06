function main() {
    const n = 10;
  
    let chunk1 = Buffer.alloc(n);
    let chunk2 = Buffer.alloc(n);
  
    // Fill chunk1
    chunk1.write("HELLO", "utf8");
  
    // Copy chunk1 -> chunk2
    chunk1.copy(chunk2, 0, 0, n);
  
    // Print chunk2
    console.log("chunk2 before nulling chunk1:", chunk2.toString("utf8").replace(/\0/g, "\\0"));
  
    // "Free" chunk1 by dropping reference (GC-managed)
    chunk1 = null;
  
    // Print chunk2 again
    console.log("chunk2 after nulling chunk1: ", chunk2.toString("utf8").replace(/\0/g, "\\0"));
  
    // "Free" chunk2
    chunk2 = null;
  }
  
  main();
  