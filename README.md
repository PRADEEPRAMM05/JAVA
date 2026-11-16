# Object-Oriented Programming in Java – Assignments Repository

**Student / GitHub User:** [PRADEEPRAMM05](https://github.com/PRADEEPRAMM05)  
**Repository:** [JAVA](https://github.com/PRADEEPRAMM05/JAVA)  
**Environment:** Java (OpenJDK 21 / 26)  
**Build & Execution:** Standalone `javac` and `java` (no external runtime dependencies required)

---

## Repository Directory Structure

```text
JAVA/
├── README.md
├── .gitignore
├── Assignment 1/
│   ├── Task01/ (Problem Analysis & Objects)
│   ├── Task02/ (Data Representation)
│   ├── Task03/ (Control Logic & ParkingFacility.java)
│   ├── Task04/ (Classes & ParkingTicket.java)
│   ├── Task05/ (Static Members & StaticTicketDemo.java)
│   ├── Task06/ (Inheritance & InheritanceDemo.java)
│   ├── Task07/ (Method Overriding & OverrideDemo.java)
│   ├── Task08/ (Polymorphism & FeeDemo.java)
│   ├── Task09/ (Abstraction & AbstractionDemo.java)
│   ├── Task10/ (Packages & ParkingDemo.java)
│   ├── Task11/ (Full System & LookupBenchmark.java)
│   └── Task12/ (Day-4 Challenge: Jagged 5-Floor Redesign)
├── Assignment 2/
│   ├── Task01/ (Q1: String vs StringBuilder vs StringBuffer under load)
│   ├── Task02/ (Q2: ArrayList vs LinkedList vs ArrayDeque for Playlist)
│   ├── Task03/ (Q3: hashCode-equals Contract & Ghost Key Investigation)
│   ├── Task04/ (Q4: HashMap vs TreeMap vs LinkedHashMap for Pharmacy)
│   ├── Task05/ (Q5: Generic Ranking Component & PECS Leaderboard)
│   ├── Task06/ (Q6: Priority, Order and Fairness in a Triage Queue)
│   └── Task07/ (Q7: Day-4 Challenge: O(1) Fair Bucketed Triage Queue)
├── Assignment 3/ (Pending Instructions)
├── Assignment 4/ (Pending Instructions)
└── Assignment 5/ (Pending Instructions)
```

---

## Assignment 1 Summary: Campus Smart-Parking System (CO1)

| Task Folder | Focus Area | Source Files | Result / Key Takeaway |
| :--- | :--- | :--- | :--- |
| **`Task01/`** | Problem Analysis & Objects | `explanation.md` | Identified candidate classes, invariants, and proved $O(N)$ vs $O(1)$ lookup complexity. |
| **`Task02/`** | Data Representation | `explanation.md` | Justified jagged array representation; verified $90.5\%$ memory saving with `BitSet`. |
| **`Task03/`** | Control Logic & Edge Cases | `ParkingFacility.java` | Implemented slot allocation, defensive boundary checks, and double-release guards. |
| **`Task04/`** | Classes & Constructors | `ParkingTicket.java` | Applied constructor chaining via `this(...)`, copy constructors, and immutability. |
| **`Task05/`** | `static` Members & Lifecycle | `StaticTicketDemo.java` | Monotonic ticket counter, static block, and object dereferencing garbage collection lifecycle. |
| **`Task06/`** | Inheritance Hierarchy | `InheritanceDemo.java` | Single, hierarchical, and multilevel inheritance (`Vehicle` $\rightarrow$ `Car` $\rightarrow$ `ElectricCar`) with `super()`. |
| **`Task07/`** | Override & Specialize Behaviour | `OverrideDemo.java` | Overrode `ratePerHour()`, integer ceiling arithmetic, and enforced strict `equals()` / `hashCode()`. |
| **`Task08/`** | Polymorphism & Dynamic Dispatch | `FeeDemo.java` | Heterogeneous vehicle array processing without type checks; pattern matching `instanceof`. |
| **`Task09/`** | Abstraction (Classes vs Interfaces) | `AbstractionDemo.java` | Separated vehicle identity from orthogonal capabilities (`Chargeable`, `PassHolder`). |
| **`Task10/`** | Packages & Encapsulation | `ParkingDemo.java`, `parking/*/*.java` | 3-tier package layering (`model`, `service`, `app`); analyzed Integer Cache Pool ($[-128, 127]$). |
| **`Task11/`** | Implementation & Benchmark | `ParkingSystemDemo.java`, `LookupBenchmark.java` | End-to-end integration test suite; empirical benchmark showed HashMap lookup is $312\times$ faster. |
| **`Task12/`** | Day-4 Design Challenge | `ParkingSystemDemo.java` | Adapted to 5 floors (jagged layout), `EBike` (flat fee), `DeliveryVan` (surge pricing), and `OneDayPass`. |

---

## Assignment 2 Summary: Collections, Strings & Generics (CO2)

| Task Folder | Focus Area | Source Files | Execution Command | Result / Key Takeaway |
| :--- | :--- | :--- | :--- | :--- |
| **`Task01/`** | Building Text Under Load | `TicketBenchmark.java`, `explanation.md` | `javac TicketBenchmark.java && java TicketBenchmark` | Proved `String (+)` in loops is $O(N^2)$ due to char copying; `StringBuilder` delivered a **$5.6\times$ speedup** and eliminated 4,000,000 throwaway heap objects. |
| **`Task02/`** | Appropriate Engineering of `ArrayList` | `PlaylistBenchmark.java`, `explanation.md` | `javac PlaylistBenchmark.java && java PlaylistBenchmark` | Refuted the "LinkedList is faster" fallacy: `ArrayList` is **$360\times$ faster** at `get(k)` and **$27\times$ faster** at middle insertion due to CPU cache locality and vectorized `System.arraycopy`. |
| **`Task03/`** | Engineer the `hashCode–equals` Contract | `Attendance.java`, `ContractInvestigation.java`, `explanation.md` | `javac Attendance.java && java Attendance` | Diagnosed RFID attendance duplicate bug caused by missing `hashCode()`. Proved post-insertion mutation causes irreversible "ghost object" loss in hash tables. |
| **`Task04/`** | Map Selection for Pharmacy Inventory | `PharmacyInventoryBenchmark.java`, `explanation.md` | `javac PharmacyInventoryBenchmark.java && java PharmacyInventoryBenchmark` | Verified `HashMap` is **$13.5\times$ faster** for point-of-sale SKU dispensing ($O(1)$), while `TreeMap` enables range queries ($O(\log N)$) and `LinkedHashMap` guarantees FIFO restocking order. |
| **`Task05/`** | Generic Ranking Component | `LeaderboardDemo.java`, `RankingDesignTest.java`, `explanation.md` | `javac LeaderboardDemo.java && java LeaderboardDemo` | Built generic `Leaderboard<T>` adhering to PECS (`Comparator<? super T>`); chained criteria (points desc, goalDiff desc, name asc) and verified immutability. |
| **`Task06/`** | Priority, Order & Fairness in Queues | `Triage.java`, `TriageInvestigation.java`, `explanation.md` | `javac Triage.java && java Triage` | Built hospital ER triage with `PriorityQueue<Patient>`. Diagnosed binary heap instability without sequence counters and proved starvation of non-urgent patients. |
| **`Task07/`** | Day-4 Design Challenge: Redesign & Validate | `TriageRedesignValidation.java`, `explanation.md` | `javac TriageRedesignValidation.java && java TriageRedesignValidation` | Diagnosed heap overhead and starvation; redesigned with a 5-bucket `ArrayDeque` multi-level queue with aging escalation, achieving **$3\times$ speedup** and eliminating starvation. |

---

## Progressive Commit History

Progressive commits have been recorded sequentially:
1. `Assignment 1 Task 01: Problem analysis and candidate objects identification`
2. `Assignment 1 Task 02: Data representation and memory complexity analysis`
3. `Assignment 1 Task 03: Completed ParkingFacility control logic and verification`
4. `Assignment 1 Task 04: Class and constructor design with defensive validation`
5. `Assignment 1 Task 05: Applied static members, constants, and object lifecycle analysis`
6. `Assignment 1 Task 06: Implemented inheritance hierarchy and constructor chaining`
7. `Assignment 1 Task 07: Specialized behavior via method overriding and equals/hashCode contract`
8. `Assignment 1 Task 08: Completed FeeDemo with runtime polymorphism and dynamic dispatch`
9. `Assignment 1 Task 09: Abstract classes, interfaces, and capability decoupling`
10. `Assignment 1 Task 10: Encapsulation, package architecture, and access modifier defense`
11. `Assignment 1 Task 11: End-to-end implementation, test cases, and lookup benchmark`
12. `Assignment 1 Task 12: Day-4 Design Challenge - Jagged 5-floor architecture, strategy pricing, and new vehicle categories`
13. `Add comprehensive root README documenting Assignment 1 tasks and execution guides`
14. `Assignment 2 Task 01 (Q1): Empirical benchmark of String, StringBuilder, and StringBuffer under load`
15. `Assignment 2 Task 02 (Q2): Empirical evaluation of ArrayList vs LinkedList vs ArrayDeque for playlist management`
16. `Assignment 2 Task 03 (Q3): Engineered the hashCode-equals contract and investigated mutable key hazards`
17. `Assignment 2 Task 04 (Q4): Evaluated Map implementations for pharmacy inventory with O(1) vs O(log n) trade-offs`
18. `Assignment 2 Task 05 (Q5): Derived generic Leaderboard component with comparator chaining and immutability`
19. `Assignment 2 Task 06 (Q6): Emergency room triage with stable PriorityQueue and starvation analysis`
20. `Assignment 2 Task 07 (Q7): Day-4 Design Challenge - Diagnosed PriorityQueue starvation and validated O(1) multi-level fair bucketed triage`
