# Assignment 2 – Task 03 (Q3): Engineer the `hashCode–equals` Contract

## 1. Problem Context: The Campus RFID Attendance Bug

A university tracks student attendance at entry turnstiles. Because students occasionally tap their RFID cards more than once, card scans are collected in a `HashSet<Student>`. However, campus security observed duplicate records for the same student whenever they scanned their card at Gate 1 and subsequently at Gate 3.

---

## 2. The Internal Mechanics of `HashSet` & `HashMap`

In Java, `HashSet<E>` is backed by an internal `HashMap<E, Object>`. When `set.add(student)` is invoked:
1. **Step 1: Compute Hash & Target Bucket**:
   $$\text{bucketIndex} = (\text{capacity} - 1) \ \& \ \text{hash}(\text{student.hashCode()})$$
2. **Step 2: Traverse Bucket Chain / Tree**:
   The JVM scans nodes in the bucket. For each node `e`, it checks:
   ```java
   if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
       // Duplicate detected! Overwrite value and reject duplicate in Set
   }
   ```
3. **The Root Cause**:
   If a class overrides `equals()` but **fails to override `hashCode()`**, it inherits `Object.hashCode()`, which computes a hash based on the object's physical **memory address**:
   - `scanGate1` (Object 1 on heap) $\rightarrow \text{hashCode} = 0x3F2A \rightarrow \text{Bucket } 3$.
   - `scanGate3` (Object 2 on heap, same `regNo`) $\rightarrow \text{hashCode} = 0x8B1C \rightarrow \text{Bucket } 9$.
   - Because they land in **different buckets**, `equals()` is **never even invoked**. The set duplicates the student!

---

## 3. "Complete the Code" Experimental Evidence (`Attendance.java`)

### Code Solution:
```java
class Student {
    private final String regNo;
    private final String name;

    Student(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student other)) return false;
        return Objects.equals(this.regNo, other.regNo);
    }

    // TODO 1: Override hashCode() consistent with equals()
    @Override
    public int hashCode() {
        return Objects.hash(regNo);
    }
}
```

### Comparative Results:
- **Before Completing TODO 1 (Missing `hashCode()`)**:
  ```text
  Unique students: 3   <-- (Defect: duplicate "192311001" admitted)
  ```
- **After Completing TODO 1 (Consistent `hashCode()`)**:
  ```text
  Unique students: 2   <-- (Correct: duplicate rejected)
  ```

---

## 4. Contract Investigation & The 3 Critical Test Cases (`ContractInvestigation.java`)

```text
--- TEST 1: WHY DEFECTIVE CODE MAY APPEAR TO PASS NAIVE TESTS ---
Added: s1, Divya, s1 (same instance reference)
Predicted Set Size: 2 | Observed Set Size: 2
Reason: HashSet checks (e.hash == hash && ((k = e.key) == key || key.equals(k))).
Since s1 == s1 is true, reference equality masked the missing hashCode()!

--- TEST 2: DISTINCT OBJECTS SHARING THE SAME LOGICAL IDENTITY ---
Added: scanGate1 ("192311001"), scanGate2 ("192311002"), scanGate3 ("192311001")
Expected Unique Count: 2 | Observed Defective Count: 3
Explanation: scanGate1 and scanGate3 generated DIFFERENT System.identityHashCode() values.
They were mapped into DIFFERENT hash buckets, so equals() was NEVER even called!

--- TEST 3: THE POST-INSERTION MUTATION DISASTER ---
Inserted student with regNo '192311001'. Initial hash bucket determined.
contains(student) BEFORE mutation: true

[MUTATION] Modified student.regNo to '192311999' while inside the HashSet!
contains(student) AFTER mutation : false (GHOST OBJECT TRAP!)
Set size after adding new student with '192311999': 2
Explanation: Mutation altered the hashCode()! When querying contains(), the HashSet
scans the bucket for hash('192311999'), while the object sits stranded in the bucket
for hash('192311001'). The object cannot be found, updated, or removed!
```

---

## 5. Architectural Rule: Immutability of Key Fields

Test 3 highlights a severe engineering hazard: **mutable fields participating in `hashCode()`**.
- When an object's state changes after being placed in a hash table, its hash code changes.
- The object becomes a **"ghost"**: it resides in the table, occupies memory, cannot be found by `.contains()`, cannot be removed by `.remove()`, and causes memory leaks.
- **Rule**: All fields participating in `equals()` and `hashCode()` **MUST be declared `final`** (e.g., `private final String regNo;`).
