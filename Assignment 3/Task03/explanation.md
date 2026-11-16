# Assignment 3 – Task 03 (Q3): Resource Safety, Debugging, and Robust Code

## 1. Problem Context: Multi-Store Report Consolidation

In a retail supply chain, a central billing service consolidates daily revenue by reading log files from regional store outlets (`store1.txt`, `store2.txt`, etc.) and writing an audited summary report. Operating system file descriptors and database connections are scarce operating system resources. Failing to close them causes **file descriptor leaks**, which crash servers under load.

---

## 2. Flaws in Manual `try-finally` Resource Management

In legacy Java code (pre-Java 7), developers wrote manual `try-finally` blocks:
```java
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("data.txt"));
    reader.readLine(); // Throws IOException A (Data Corrupt)
} finally {
    if (reader != null) {
        reader.close(); // Throws IOException B (Socket Timeout)
    }
}
```

### The Exception Masking Hazard:
- If `readLine()` throws `IOException A`, control transfers to `finally`.
- If `reader.close()` also throws `IOException B`, `IOException B` propagates up the stack.
- **`IOException A` is completely lost and swallowed!**
- The developer only sees `Socket Timeout`, while the actual root problem (`Data Corrupt`) remains undetected.

---

## 3. The `try-with-resources` Solution & `AutoCloseable` Contract

Java 7 introduced the `try-with-resources` statement (ARM):
```java
try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
    reader.readLine();
}
```

### Key Architectural Invariants:
1. **Reverse Order of Deallocation**: Resources declared in the header are closed in strict **LIFO (Last-In, First-Out)** order. If `Resource_A` is opened before `Resource_B`, `Resource_B.close()` executes before `Resource_A.close()`, preserving clean stack dependencies.
2. **Preservation via Suppressed Exceptions**:
   - If an exception occurs in the `try` block, it is retained as the **primary exception**.
   - If any `close()` method throws an exception during unwinding, the runtime invokes `primaryException.addSuppressed(closeException)`.
   - The caller catches the primary exception and inspects secondary errors via `e.getSuppressed()`.

---

## 4. Experimental Evidence

### Execution Log Trace (`TryWithResourcesReportGenerator.java`):
```text
--- 1. DEMONSTRATING SUPPRESSED EXCEPTIONS IN TRY-WITH-RESOURCES ---
  [TWR OPENED] Resource_A
  [TWR OPENED] Resource_B
  [TWR AUTO-CLOSING] Resource_B
  [TWR AUTO-CLOSING] Resource_A

[PRIMARY EXCEPTION CAUGHT]
  Message: PRIMARY FAILURE: Data checksum corruption in Resource_A

[SUPPRESSED EXCEPTIONS PRESERVED: 2 FOUND]
  Suppressed [1]: SECONDARY ERROR: Stream flush failed during close() of Resource_B
  Suppressed [2]: SECONDARY ERROR: Stream flush failed during close() of Resource_A

--- 2. CONSOLIDATING MULTI-STORE SALES REPORT SAFELY ---
  Successfully processed store log: store1.txt
  Successfully processed store log: store2.txt
  [WARNING] Corrupted or missing store file: store_missing.txt (No such file or directory)
Summary report written to 'summary_report.txt' | Total Revenue: Rs. 3,555.00
```

### Generated Consolidated Report (`summary_report.txt`):
```text
==================================================
   CONSOLIDATED MULTI-STORE DAILY SALES REPORT    
==================================================
Item: Paracetamol     | Qty: 10 | Total: Rs.   255.00
Item: Amoxicillin     | Qty:  5 | Total: Rs.   600.00
Item: CoughSyrup      | Qty:  8 | Total: Rs.   680.00
Item: Bandages        | Qty: 20 | Total: Rs.   300.00
Item: Antiseptic      | Qty: 12 | Total: Rs.   720.00
Item: Thermometer     | Qty:  4 | Total: Rs. 1,000.00
--------------------------------------------------
TOTAL TRANSACTIONS: 6
TOTAL GROSS SALES : Rs. 3,555.00
==================================================
```
