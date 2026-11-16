# Task 05: Design for Correctness and Safety – Transactions & Injection Defense (Task A5)

## 1. Transaction Design for Inter-Wallet Funds Transfer (Task A5.1)

Transferring ₹500 from a hostel wallet to a canteen wallet involves two distinct update mutations: debiting Account A and crediting Account B. 

### Invariant:
$$\text{Total System Capital} = \text{Hostel Balance} + \text{Canteen Balance} \equiv \text{Constant}$$

### Transaction Pseudocode:
```text
ALGORITHM TransferFunds(connection, studentId, amount)
INPUT: connection as Connection, studentId as String, amount as Double
OUTPUT: Boolean success, guarantees atomicity

BEGIN
    oldAutoCommit ← connection.getAutoCommit()
    connection.setAutoCommit(FALSE)   // Start Transaction Boundary
    
    TRY
        // 1. Verify Sufficient Balance
        debitSql ← "UPDATE hostel_wallet SET balance = balance - ? WHERE student_id = ? AND balance >= ?"
        OPEN PreparedStatement psDebit ON debitSql
        psDebit.setDouble(1, amount)
        psDebit.setString(2, studentId)
        psDebit.setDouble(3, amount)
        rowsUpdated ← psDebit.executeUpdate()
        
        IF rowsUpdated == 0 THEN
            THROW EXCEPTION "Insufficient funds or invalid student account"
        END IF

        // 2. Credit Target Wallet
        creditSql ← "UPDATE canteen_wallet SET balance = balance + ? WHERE student_id = ?"
        OPEN PreparedStatement psCredit ON creditSql
        psCredit.setDouble(1, amount)
        psCredit.setString(2, studentId)
        psCredit.executeUpdate()

        // 3. Commit Both Operations Atomically
        connection.commit()
        RETURN TRUE

    CATCH Exception e:
        // System / Power / DB failure occurred between debit and credit
        connection.rollback()         // Restore original balances
        THROW e
        
    FINALLY:
        connection.setAutoCommit(oldAutoCommit)
END
```

---

## 2. SQL Injection Vulnerability Analysis & Parameterized Redesign (Task A5.2)

### The Vulnerable Query:
```java
String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
```

### Attack Vector Analysis:
If an adversary submits:
- `id` = `admin`
- `pwd` = `' OR '1'='1`

The resulting SQL sent to the database engine parser becomes:
```sql
SELECT * FROM users WHERE id='admin' AND pwd='' OR '1'='1'
```

### Why the Attack Succeeds:
Because operator precedence evaluates `AND` before `OR`:
$$(\text{id} = \text{'admin'} \land \text{pwd} = \text{''}) \lor (\text{'1'} = \text{'1'})$$
Since `'1' = '1'` is a tautological constant truth ($TRUE$), the entire `WHERE` clause resolves to $TRUE$ for every row in the `users` table. The query returns the user record without matching the password, permitting an unauthorized login bypass.

### The Secure Redesign (`PreparedStatement`):
```java
String sql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, id);
    ps.setString(2, pwd);
    try (ResultSet rs = ps.executeQuery()) {
        return rs.next(); // True only if genuine match exists
    }
}
```
**Mechanism of Protection:** 
The database compiler compiles the abstract syntax tree (AST) of the query **before** parameters are bound. User inputs are treated strictly as literal data constants, never as executable SQL tokens or operators.

---

## 3. Alternative Concept and Complexity Analysis

### Comparison: Single Transaction vs. Two Auto-Commit Updates

```text
+-----------------------------+------------------------------------+------------------------------------+
| Dimension                   | Single Transaction (ACID)          | Two Independent Auto-Commit Updates|
+-----------------------------+------------------------------------+------------------------------------+
| Failure between Debit &     | **Zero Money Lost**: `rollback()`  | **Catastrophic Inconsistency**:    |
| Credit (e.g. power cutoff)  | reverts debit; invariant preserved | ₹500 vanishes into the ether       |
| Hardware Disk Flushes       | 1 sequential fsync on commit       | 2 independent fsync disk flushes   |
| Latency per Transfer        | ~12 ms                             | ~24 ms (Double disk I/O latency)   |
| Lock Contention Window      | Rows locked until explicit commit  | Locks released immediately after   |
|                             | (requires brief short-lived tx)    | each individual statement          |
+-----------------------------+------------------------------------+------------------------------------+
```

**Conclusion:** Running financial multi-step balance changes under independent auto-commit updates is a critical architectural anti-pattern. **ACID Transactions (`setAutoCommit(false)` + `commit()` / `rollback()`)** are required to guarantee ledger integrity.
