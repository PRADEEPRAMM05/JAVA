# Task 07: Engineer an Adversarial Input – Security & Robustness Forensics (Task B2)

## 1. Adversarial SQL Injection Testing

### Attack Payloads Tested:
1. `' OR '1'='1`
2. `officer_admin' OR '1'='1`
3. `' OR 1=1 --`

### Observed Behavior:
- **Unsafe Dynamic Query Engine:** All 3 payloads successfully bypassed authentication, granting root administrative access without knowledge of the password.
- **Parameterized `PreparedStatement`:** All 3 payloads were safely treated as literal user password strings, completely rejecting the breach attempts.

---

## 2. Adversarial Corrupted File Testing

A maliciously degraded bus-pass file was ingested into the parser to test fault isolation:

| Line Tested | Adversarial Input | Parser Defense Reaction | Diagnostic Log Output |
| :--- | :--- | :--- | :--- |
| **Line 2** | `\n` (Empty newline) | Caught & ignored | `Line 2: Empty line rejected.` |
| **Line 3** | `P1002,DAMAGED_RECORD_WITH_NO_AMOUNT` | Caught by `tokens.length != 5` | `Line 3: Expected 5 fields, found 2` |
| **Line 4** | `P1003,192311003,Divya,NOT_A_ROUTE,3800.0` | Caught by `Integer.parseInt` | `Line 4: Number format parse error on route` |
| **Line 5** | `P1004,192311004,Karthik,8,FREE_PASS_NaN` | Caught by `Double.parseDouble` | `Line 5: Number format parse error on amount` |
| **Line 6** | `P1005,192311005,Malicious,Name,Injection,5,2500.0` | Caught by `tokens.length == 7` | `Line 6: Expected 5 fields, found 7 (Delimiter Injection)` |

**Outcome:** Exactly 2 valid records (`P1001` and `P1006`) were cleanly accepted; all 5 malicious/corrupt lines were safely quarantined without halting the pipeline.

---

## 3. What These Attacks Reveal About the Original Naive Designs

1. **Failure of Assumptions:** Naive file parsers assume that all input lines conform to strict schemas, causing uncaught `ArrayIndexOutOfBoundsException` or `NumberFormatException` that crashes the entire application.
2. **Failure to Distinguish Code from Data:** Unsafe dynamic SQL concatenation conflates executable language commands with data values, allowing attackers to hijack control flow.

---

## 4. Alternative Concept and Complexity Analysis

### Comparison: Manual Sanitization / Regex Escaping vs. `PreparedStatement`

```text
+-----------------------------+------------------------------------+------------------------------------+
| Dimension                   | Manual Regex / Quote Escaping      | Parameterized `PreparedStatement`  |
+-----------------------------+------------------------------------+------------------------------------+
| Security Robustness         | **Fragile & Vulnerable**:          | **Mathematically Immune**:         |
|                             | Bypassed by `NO_BACKSLASH_ESCAPES`,| Input never enters SQL syntax      |
|                             | multibyte character shifts (GBK),  | parser; treated as raw data        |
|                             | or second-order database storage   | constants at the protocol level    |
| CPU Runtime Latency         | High: regex compile & string scan  | Low: query compiled once, bind     |
|                             | over every input char (~25 µs)     | variables sent in binary (~2 µs)   |
| Maintenance Overhead        | High (endless blacklist patching)  | Zero (standard JDBC idiom)         |
+-----------------------------+------------------------------------+------------------------------------+
```

**Conclusion:** Blacklist validation and manual string escaping are recognized security anti-patterns. **Parameterized `PreparedStatement`** provides 100% mathematical immunity to SQL injection at superior execution speeds.
