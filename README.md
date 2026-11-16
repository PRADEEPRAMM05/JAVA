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
├── Assignment 1/ (CO1: Campus Smart-Parking System - Tasks 01 to 12)
├── Assignment 2/ (CO2: Collections, Strings & Generics - Tasks 01 to 07)
├── Assignment 3/ (CO3: Exception Handling & Multithreading - Tasks 01 to 07)
├── Assignment 4/ (CO4: Event-Driven GUI Design - Tasks 01 to 06)
└── Assignment 5/ (CO5: Data Persistence Engineering: Files & JDBC - Tasks 01 to 09)
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

## Assignment 3 Summary: Exception Handling & Multithreading (CO3)

| Task Folder | Focus Area | Source Files | Execution Command | Result / Key Takeaway |
| :--- | :--- | :--- | :--- | :--- |
| **`Task01/`** | ATM Hardware & Network Fault Simulation | `ATMSimulation.java`, `ATMAlternativeErrorCode.java`, `explanation.md` | `javac ATMSimulation.java && java ATMSimulation` | Structured checked/unchecked exception taxonomy (`ATMException`, `CardBlockedException`, `CashDispenserException`); proved `finally` block guarantees hardware card ejection. |
| **`Task02/`** | Online Examination Portal Resilience | `StorageDemo.java`, `ExamPortalPropagation.java`, `explanation.md` | `javac ExamPortalPropagation.java && java ExamPortalPropagation` | Evaluated exception propagation through 4 architectural tiers; applied exception chaining (`initCause`) to preserve low-level root cause without breaking API abstraction. |
| **`Task03/`** | Resource Management & Suppressed Exceptions | `ManualReportGenerator.java`, `TryWithResourcesReportGenerator.java`, `explanation.md` | `javac TryWithResourcesReportGenerator.java && java TryWithResourcesReportGenerator` | Demonstrated try-with-resources and `AutoCloseable` closing in reverse order; verified `getSuppressed()` preserves secondary close exceptions without masking primary I/O failures. |
| **`Task04/`** | Airport Scanner Thread Lifecycle & Interrupts | `ScannerStationSimulation.java`, `explanation.md` | `javac ScannerStationSimulation.java && java ScannerStationSimulation` | Simulated Thread states (`NEW`, `RUNNABLE`, `TIMED_WAITING`, `TERMINATED`); implemented cooperative thread interruption checking `Thread.currentThread().isInterrupted()`. |
| **`Task05/`** | Cinema Ticket Booking Race Conditions | `SeatBooking.java`, `SeatBookingBenchmark.java`, `explanation.md` | `javac SeatBookingBenchmark.java && java SeatBookingBenchmark` | Diagnosed check-then-act double booking race condition under 50 concurrent threads; eliminated overselling using synchronized critical section monitors. |
| **`Task06/`** | Producer-Consumer Counter & Deadlock Prevention | `TokenCounter.java`, `WalletDeadlockDemo.java`, `explanation.md` | `javac TokenCounter.java && java TokenCounter` | Built thread-safe bounded buffer using `wait()` / `notifyAll()`; diagnosed circular wait deadlock and eliminated deadlocks via global lock hierarchy ordering. |
| **`Task07/`** | Day-4 Design Challenge: Fault-Tolerant Semester Processor | `SemesterResultProcessor.java`, `explanation.md` | `javac SemesterResultProcessor.java && java SemesterResultProcessor` | Built concurrent semester batch processor with bounded worker thread pool, poison-pill fault isolation, and atomic counter updates. |

---

## Assignment 4 Summary: Event-Driven GUI Design (CO4)

| Task Folder | Focus Area | Source Files | Execution Command | Result / Key Takeaway |
| :--- | :--- | :--- | :--- | :--- |
| **`Task01/`** | GUI Design Review: Refactoring FeeWindow | `RepairedFeeWindow.java`, `explanation.md` | `javac RepairedFeeWindow.java && java RepairedFeeWindow` | Diagnosed 10 design anti-patterns in junior developer code; decoupled UI presentation from business logic using `FeeService`, parameterized SQL, and independent listeners. |
| **`Task02/`** | Hospital Outpatient Appointment Desk | `AppointmentDesk.java`, `explanation.md` | `javac AppointmentDesk.java && java AppointmentDesk` | Built interactive Swing appointment booking desk with cascading dropdowns (Department $\rightarrow$ Doctor), modal confirmation dialogs, and dirty-form window close interceptor. |
| **`Task03/`** | Campus Library Search Screen | `LibrarySearchFrame.java`, `explanation.md` | `javac LibrarySearchFrame.java && java LibrarySearchFrame` | Implemented nested container layout (`BorderLayout` + `FlowLayout`) ensuring that results table viewport dynamically expands while search rows and button bars retain fixed height. |
| **`Task04/`** | Event-Handling Forensics: DrawPad | `DrawPad.java`, `explanation.md` | `javac DrawPad.java && java DrawPad` | Diagnosed `mouseclicked` lowercase override bug and `getGraphics()` ephemeral drawing loss; refactored to retained-mode `List<Point>` model redrawing in `paint(Graphics g)`. |
| **`Task05/`** | Same Screen, Different Event-Handling Styles | `Converter.java`, `ConverterFourStyles.java`, `explanation.md` | `javac Converter.java && java Converter` | Implemented currency converter across 4 styles (Frame listener, inner class, anonymous class, lambda); evaluated requirement change friction and Swing `Action` reusability. |
| **`Task06/`** | Day-4 Design Challenge: Event Registration App | `EventRegistrationApp.java`, `schema.sql`, `explanation.md` | `javac EventRegistrationApp.java && java EventRegistrationApp` | Built complete 3-tier desktop application with `JFrame`, `JMenuBar`, `JTable`, input validation, duplicate prevention, and responsive background processing via `SwingWorker`. |

---

## Assignment 5 Summary: Data Persistence Engineering – Files and JDBC (CO5)

| Task Folder | Focus Area | Source Files | Execution Command | Result / Key Takeaway |
| :--- | :--- | :--- | :--- | :--- |
| **`Task01/`** | Task A1: Bus-Pass File Format Design | `BusPassFormatDemo.java`, `explanation.md` | `javac BusPassFormatDemo.java && java BusPassFormatDemo` | Designed RFC 4180 delimited text layout vs `DataOutputStream` binary format; implemented damage skipping and shadow-file atomic rename swap recovery. |
| **`Task02/`** | Task A2: Buffered I/O Cost Model & Redesign | `BufferedIOCostModel.java`, `explanation.md` | `javac BufferedIOCostModel.java && java BufferedIOCostModel` | Formulated mathematical cost model for 50 MB log reading; proved 8 KB buffering reduces OS kernel syscalls from $52,428,800$ to $6,400$ ($99.988\%$ reduction). |
| **`Task03/`** | Task A3: Generic Data-Access Pattern (`DAO`) | `GenericDaoDemo.java`, `explanation.md` | `javac GenericDaoDemo.java && java GenericDaoDemo` | Designed generic `Dao<T, K>` interface; mapped `Student` domain entity to relational `students` SQL table; demonstrated zero blast radius when refactoring schemas. |
| **`Task04/`** | Task A4: Object Serialization & Transient Fields | `HostelSerializationDemo.java`, `explanation.md` | `javac HostelSerializationDemo.java && java HostelSerializationDemo` | Excluded live network sockets (`Connection`) and passwords via `transient`; demonstrated class schema evolution and backwards compatibility via `serialVersionUID`. |
| **`Task05/`** | Task A5: Transactions & SQL Injection Defense | `WalletTransactionDemo.java`, `explanation.md` | `javac WalletTransactionDemo.java && java WalletTransactionDemo` | Implemented ACID-compliant inter-wallet transfer with `rollback()` on crash; proved `' OR '1'='1` breach against dynamic SQL and mathematical immunity of `PreparedStatement`. |
| **`Task06/`** | Task B1: Instrumented File Store (`PassStore`) | `PassStore.java`, `explanation.md` | `javac PassStore.java && java PassStore` | Completed `PassStore.java` with damage skipping; benchmarked $O(n)$ search latency growth from 1,000 to 50,000 records; verified 200× speedup with buffering. |
| **`Task07/`** | Task B2: Adversarial Input Engineering | `AdversarialSecurityTest.java`, `explanation.md` | `javac AdversarialSecurityTest.java && java AdversarialSecurityTest` | Stress-tested dynamic SQL and corrupt CSV formats (empty lines, missing tokens, non-numeric values, delimiter injection); proved robust error quarantine. |
| **`Task08/`** | Task B3: Statement vs PreparedStatement vs Batch | `PassLoader.java`, `explanation.md` | `javac PassLoader.java && java PassLoader` | Completed `loadAll()` batched transaction loader; benchmarked $1,000$ to $100,000$ rows proving batched `PreparedStatement` is **$20.5\times$ faster**; verified rollback. |
| **`Task09/`** | Task B4: 12-Campus Scale & Production Decision | `CampusMigrationApp.java`, `explanation.md` | `javac CampusMigrationApp.java && java CampusMigrationApp` | Built 12-campus migration pipeline with audit reporting; inspected environment via `DatabaseMetaData`; formulated production decision matrix for 800,000 passes. |

---

## Progressive Commit History

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
21. `Update root README with comprehensive Assignment 2 documentation and results`
22. `Assignment 3 Task 01 (Q1): ATM failure simulation with checked/unchecked taxonomy and finally cleanup`
23. `Assignment 3 Task 02 (Q2): User-defined exception hierarchy and exception chaining propagation in exam portal`
24. `Assignment 3 Task 03 (Q3): Resource safety, suppressed exception handling, and multi-file report consolidation`
25. `Assignment 3 Task 04 (Q4): Airport scanner station simulating Thread lifecycle states and cooperative interruption`
26. `Assignment 3 Task 05 (Q5): Elimination of check-then-act race conditions in cinema booking via synchronized monitors`
27. `Assignment 3 Task 06 (Q6): Producer-consumer token counter and global lock ordering deadlock prevention`
28. `Assignment 3 Task 07 (Q7): Day-4 Design Challenge - Concurrent fault-tolerant semester exam processor`
29. `Assignment 4 Task 01 (Q1): GUI Design Review - Diagnosed anti-patterns and implemented refactored RepairedFeeWindow`
30. `Assignment 4 Task 02 (Q2): Event-driven design and implementation of Hospital Outpatient Appointment Desk`
31. `Assignment 4 Task 03 (Q3): From sketch to layout - Campus Library Search Screen with nested responsive containers`
32. `Assignment 4 Task 04 (Q4): Event-handling forensics - Repaired DrawPad with retained point rendering and @Override enforcement`
33. `Assignment 4 Task 05 (Q5): Currency converter implementation across four event-handling styles and Swing Action analysis`
34. `Assignment 4 Task 06 (Q6): Day-4 Design Challenge - Layered Swing-JDBC Event Registration Application with SwingWorker and CSV export`
35. `Assignment 5 Task 01 (Task A1): Bus-pass file format design - Delimited text vs DataOutputStream binary and damage recovery`
36. `Assignment 5 Task 02 (Task A2): Buffered I/O design - Syscall prediction and micro-benchmark verification`
37. `Assignment 5 Task 03 (Task A3): Generic Data Access Object pattern and Object-Relational mapping`
38. `Assignment 5 Task 04 (Task A4): Object serialization design - Transient field exclusion and serialVersionUID evolution`
39. `Assignment 5 Task 05 (Task A5): Transaction design with rollback recovery and SQL injection defense`
40. `Assignment 5 Task 06 (Task B1): Instrumented PassStore implementation with damage skipping and growth benchmarks`
41. `Assignment 5 Task 07 (Task B2): Adversarial inputs - SQL injection tautology exploit and corrupted file stress testing`
42. `Assignment 5 Task 08 (Task B3): Statement vs PreparedStatement vs Batch processing benchmarks and transactional atomicity`
43. `Assignment 5 Task 09 (Task B4): Final engineering decision - 12-campus migration pipeline and DatabaseMetaData introspection`
