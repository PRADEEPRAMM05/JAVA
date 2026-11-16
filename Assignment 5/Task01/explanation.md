# Task 01: File Format Design for University Bus-Pass Records (Task A1)

## 1. Record Representation Design (Task A1.1)

The university transport office issues ~8,000 passes annually. Each record requires 7 distinct fields.

### (a) Delimited Text File Layout (RFC 4180 CSV Compliant)
- **Field Order:** `PassID,RegisterNumber,StudentName,RouteNumber,BoardingStop,ValidityDate,AmountPaid`
- **Field Delimiter:** Comma (`,`), Record Delimiter: Newline (`\n` or `\r\n`).
- **Delimiter Collision Handling:** If `StudentName` or `BoardingStop` contains commas, quotes, or newlines, the field is enclosed in double quotes (`"`). Internal quotes are escaped as `""` (e.g. `"Patel, Aarav"` or `"John ""The Boss"" Doe"`).
- **Sample Text Record:**
  ```text
  BP-101,192311001,"Patel, Aarav",12,Tambaram,2027-05-31,6500.00
  ```

### (b) Binary Record Layout (`DataOutputStream` / `DataInputStream`)
- Uses primitive typed binary encoding to eliminate delimiter collision and parsing overhead:
  - `writeUTF(passId)`: 2-byte length prefix + modified UTF-8 bytes.
  - `writeUTF(regNo)`: 2-byte length prefix + modified UTF-8 bytes.
  - `writeUTF(name)`: 2-byte length prefix + modified UTF-8 bytes.
  - `writeInt(routeNumber)`: 4-byte big-endian integer.
  - `writeUTF(boardingStop)`: 2-byte length prefix + modified UTF-8 bytes.
  - `writeUTF(validityDate)`: 2-byte length prefix + modified UTF-8 bytes.
  - `writeDouble(amountPaid)`: 8-byte IEEE 754 floating point.
- **Binary Footprint:** ~80–90 bytes per record (vs ~110 bytes in ASCII text).

---

## 2. Operations Pseudocode & Stream Selection (Task A1.2)

### Stream Selection Justification:
- **`BufferedWriter` / `BufferedReader`** are chosen over raw `FileWriter` / `FileReader` because:
  1. Internal 8 KB character buffer consolidates frequent small writes/reads into batch OS filesystem syscalls.
  2. Character decoders handle UTF-8 encodings transparently.
  3. `readLine()` allows natural line-by-line processing without buffering the entire 8,000-pass file in heap RAM.

### Pseudocode Operations:

```text
ALGORITHM AddPass(pass)
INPUT: pass record containing 7 fields
OUTPUT: Appended record in passes.txt

BEGIN
    escapedName ← IF pass.name CONTAINS "," THEN '"' + REPLACE(pass.name, '"', '""') + '"' ELSE pass.name
    line ← JOIN(",", pass.passId, pass.regNo, escapedName, pass.routeNumber, pass.boardingStop, pass.validityDate, pass.amountPaid)
    
    OPEN BufferedWriter ON "passes.txt" WITH OPTIONS (CREATE, APPEND)
    WRITE line
    WRITE NEWLINE
    CLOSE BufferedWriter
END
```

```text
ALGORITHM FindPassById(targetId)
INPUT: targetId as String
OUTPUT: BusPass object or NULL

BEGIN
    IF NOT FILE_EXISTS("passes.txt") THEN RETURN NULL

    OPEN BufferedReader ON "passes.txt"
    WHILE (line ← READ_LINE()) != NULL DO
        fields ← PARSE_CSV_LINE(line)
        IF fields.length == 7 AND fields[0] EQUALS_IGNORE_CASE targetId THEN
            CLOSE BufferedReader
            RETURN CONSTRUCT_BUS_PASS(fields)
        END IF
    END WHILE
    CLOSE BufferedReader
    RETURN NULL
END
```

```text
ALGORITHM ListPassesByRoute(targetRoute)
INPUT: targetRoute as Integer
OUTPUT: List of matching BusPass objects

BEGIN
    matchingList ← NEW LIST
    IF NOT FILE_EXISTS("passes.txt") THEN RETURN matchingList

    OPEN BufferedReader ON "passes.txt"
    WHILE (line ← READ_LINE()) != NULL DO
        fields ← PARSE_CSV_LINE(line)
        IF fields.length == 7 AND PARSE_INT(fields[3]) == targetRoute THEN
            matchingList.ADD(CONSTRUCT_BUS_PASS(fields))
        END IF
    END WHILE
    CLOSE BufferedReader
    RETURN matchingList
END
```

---

## 3. Resilience to Damaged Data, Power Outages & Missing Files (Task A1.3)

1. **Damaged / Malformed Lines:**
   - The line parser inspects token count (`fields.size() == 7`) and validates numerical types inside a `try-catch(NumberFormatException)` block. Corrupted lines are skipped with a warning log, allowing the remaining passes to load safely.
2. **Missing File:**
   - Evaluated proactively with `Files.exists(path)`. Returns empty list or `null` instead of throwing an unhandled `FileNotFoundException`.
3. **Incomplete Write / Sudden Power Failure:**
   - For batch updates or full compaction, write to a temporary shadow file: `passes.tmp`.
   - Flush and sync to disk using `FileChannel.force(true)` or `FileOutputStream.getFD().sync()`.
   - Perform an **atomic rename swap**: `Files.move(tmpPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)`. If a crash occurs midway, the original `passes.txt` remains uncorrupted.

---

## 4. Alternative Concept and Complexity Analysis

### Comparison: Delimited Text File vs. Fixed-Length `RandomAccessFile`

```text
+-----------------------------+------------------------------------+------------------------------------+
| Dimension                   | Delimited Text File                | RandomAccessFile (Fixed-Length)   |
+-----------------------------+------------------------------------+------------------------------------+
| Lookup by Index / ID        | O(n) sequential stream scan        | O(1) seek (`offset = id * size`)   |
| Search by Route             | O(n) scan                          | O(n) scan                          |
| Storage Efficiency          | High: variable-length fields       | Poor: wasted space due to maximum  |
|                             | compact strings without padding    | padding (e.g. name padded to 64 B) |
| Human Readability           | High: open in Excel / Text Editor  | Zero: raw binary byte records      |
| Schema Evolution            | Easy to add optional columns       | Hard: changing recordSize breaks   |
|                             | at end of line                     | offsets of all existing files      |
+-----------------------------+------------------------------------+------------------------------------+
```

**Verdict:** For 8,000 passes (~800 KB total size), a **buffered delimited text file** with shadow-file atomic write guarantees is ideal: human-auditable, lightweight, and searches complete in under 5 milliseconds.
