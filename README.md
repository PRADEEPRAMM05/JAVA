# Object-Oriented Programming in Java – Assignments Repository

**Student / GitHub User:** [PRADEEPRAMM05](https://github.com/PRADEEPRAMM05)  
**Repository:** [JAVA](https://github.com/PRADEEPRAMM05/JAVA)  
**Environment:** Java (OpenJDK 21 / 26)  
**Build & Execution:** Standalone `javac` and `java` (no third-party runtime dependencies required)

---

## Repository Directory Structure

```text
JAVA/
├── README.md
├── .gitignore
├── Assignment 1/
│   ├── Task01/
│   │   └── explanation.md
│   ├── Task02/
│   │   └── explanation.md
│   ├── Task03/
│   │   ├── ParkingFacility.java
│   │   └── explanation.md
│   ├── Task04/
│   │   ├── ParkingTicket.java
│   │   └── explanation.md
│   ├── Task05/
│   │   ├── StaticTicketDemo.java
│   │   └── explanation.md
│   ├── Task06/
│   │   ├── InheritanceDemo.java
│   │   └── explanation.md
│   ├── Task07/
│   │   ├── OverrideDemo.java
│   │   └── explanation.md
│   ├── Task08/
│   │   ├── FeeDemo.java
│   │   └── explanation.md
│   ├── Task09/
│   │   ├── AbstractionDemo.java
│   │   └── explanation.md
│   ├── Task10/
│   │   ├── ParkingDemo.java
│   │   ├── parking/
│   │   │   ├── model/ (Vehicle.java, ParkingSlot.java)
│   │   │   ├── service/ (ParkingService.java)
│   │   │   └── app/ (ParkingDemo.java)
│   │   └── explanation.md
│   ├── Task11/
│   │   ├── ParkingSystemDemo.java
│   │   ├── LookupBenchmark.java
│   │   └── explanation.md
│   └── Task12/
│       ├── ParkingSystemDemo.java
│       └── explanation.md
├── Assignment 2/ (Pending Instructions)
├── Assignment 3/ (Pending Instructions)
├── Assignment 4/ (Pending Instructions)
└── Assignment 5/ (Pending Instructions)
```

---

## Assignment 1 Summary: Campus Smart-Parking System (CO1)

### Tasks & Summary Table:

| Task Folder | Focus Area | Source Files | Execution Command | Result / Key Takeaway |
| :--- | :--- | :--- | :--- | :--- |
| **`Task01/`** | Problem Analysis & Objects | `explanation.md` | *Analytical Documentation* | Comprehensive analysis of inputs, outputs, candidate classes, business invariants, and parallel arrays vs. OOP complexity. |
| **`Task02/`** | Data Representation | `explanation.md` | *Analytical Documentation* | Evaluated 1D, 2D, and jagged arrays vs. `BitSet`. Demonstrates jagged array memory optimization for multi-level facilities. |
| **`Task03/`** | Control Logic & Edge Cases | `ParkingFacility.java`, `explanation.md` | `javac ParkingFacility.java && java ParkingFacility` | Implemented slot allocation and defensive release checks. Validated against capacity overflow and double-release. |
| **`Task04/`** | Classes, Objects & Constructors | `ParkingTicket.java`, `explanation.md` | `javac ParkingTicket.java && java ParkingTicket` | Demonstrated constructor chaining using `this(...)`, parameterized constructors, copy constructors, and invariant validation. |
| **`Task05/`** | `static` Members & Object Life Cycle | `StaticTicketDemo.java`, `explanation.md` | `javac StaticTicketDemo.java && java StaticTicketDemo` | Implemented static ticket sequence generator, static blocks, and demonstrated garbage collection eligibility upon dereferencing. |
| **`Task06/`** | Inheritance Hierarchy | `InheritanceDemo.java`, `explanation.md` | `javac InheritanceDemo.java && java InheritanceDemo` | Implemented single, hierarchical, and multilevel inheritance with `super()` chaining across `Vehicle`, `Car`, and `ElectricCar`. |
| **`Task07/`** | Override & Specialize Behaviour | `OverrideDemo.java`, `explanation.md` | `javac OverrideDemo.java && java OverrideDemo` | Overrode `ratePerHour()`, `toString()`, and enforced the strict `equals()` and `hashCode()` contract on license plates. |
| **`Task08/`** | Polymorphism & Dynamic Dispatch | `FeeDemo.java`, `explanation.md` | `javac FeeDemo.java && java FeeDemo` | Processed heterogeneous vehicle collections in a single loop using dynamic dispatch and pattern matching for `instanceof`. |
| **`Task09/`** | Abstraction (Abstract Classes vs. Interfaces) | `AbstractionDemo.java`, `explanation.md` | `javac AbstractionDemo.java && java AbstractionDemo` | Decoupled core identity (`Vehicle`) from cross-cutting capabilities (`Chargeable`, `PassHolder`) to support future vehicle types cleanly. |
| **`Task10/`** | Packages & Encapsulation | `ParkingDemo.java`, `parking/*/*.java`, `explanation.md` | `javac ParkingDemo.java && java ParkingDemo` | Layered architecture into `model`, `service`, and `app` packages; analyzed the Integer Cache Pool (`-128` to `127`) equality traps. |
| **`Task11/`** | Implementation, Testing & Benchmark | `ParkingSystemDemo.java`, `LookupBenchmark.java`, `explanation.md` | `javac *.java && java ParkingSystemDemo && java LookupBenchmark` | Comprehensive integration suite covering normal, boundary, and invalid scenarios; empirical benchmark proved HashMap lookup is $312\times$ faster than linear scan. |
| **`Task12/`** | Day-4 Design Challenge | `ParkingSystemDemo.java`, `explanation.md` | `javac ParkingSystemDemo.java && java ParkingSystemDemo` | Adapted system for 5 floors (jagged layout), `EBike` (flat fee), `DeliveryVan` (surge pricing during 9 AM–5 PM), and `OneDayPass` via Strategy pattern. |

---

## Progressive Commit History

Progressive commits for Assignment 1 have been recorded sequentially:
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
