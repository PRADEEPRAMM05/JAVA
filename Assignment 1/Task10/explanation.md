# Task 10: Enforce Encapsulation through Packages and Access Modifiers

## 1. Package Architecture and Separation of Concerns

Organizing source code into modular packages establishes structural boundaries and enforces encapsulation:

```text
Task10/
├── ParkingDemo.java
├── parking/
│   ├── model/
│   │   ├── Vehicle.java       (Domain Entity)
│   │   └── ParkingSlot.java   (Physical Domain State)
│   ├── service/
│   │   └── ParkingService.java (Business Operations)
│   └── app/
│       └── ParkingDemo.java    (Application Entry Point)
```

- **`parking.model`**: Contains core domain entities (`Vehicle`, `ParkingSlot`). Fields are `private` with validation checks in mutating methods.
- **`parking.service`**: Contains business logic (`ParkingService`) coordinating slot reservation, occupancy verification, and releasing. Callers do not touch internal fields of `ParkingSlot` directly.
- **`parking.app`**: Client presentation layer executing simulation workflows.

---

## 2. Access Modifier Matrix

| Access Level | Same Class | Same Package | Subclass (Outside Pkg) | Universe (Everywhere) | Used In System For |
| :--- | :---: | :---: | :---: | :---: | :--- |
| `private` | Yes | No | No | No | State variables (`regNo`, `currentVehicle`, `slotId`). Prevents external tampering. |
| *(default)* | Yes | Yes | No | No | Package-internal helpers (`assignVehicle`). |
| `protected` | Yes | Yes | Yes | No | Base class hooks (`ratePerHour()`) accessible only to specialized child classes. |
| `public` | Yes | Yes | Yes | Yes | Service APIs (`parkVehicle`, `releaseVehicle`) and controlled getters. |

---

## 3. Why Fields Should Never Be Public

Direct public exposure of mutable fields (e.g., `public int parkingDurationMinutes`) creates severe architectural liabilities:
1. **Loss of Invariant Validation**: A rogue caller could assign a negative duration (`vehicle.parkingDurationMinutes = -50`), corrupting the entire billing engine.
2. **Tight Coupling**: If the internal representation changes (e.g., from `int minutes` to `java.time.Duration`), every external class accessing the field breaks.
3. **Encapsulation Defense**: Setters reject illegal arguments (`if (minutes < 0) throw new IllegalArgumentException(...)`), keeping object state valid at all times.

---

## 4. Wrapper Classes, Autoboxing, and Integer Cache Analysis

Java provides wrapper classes (`Integer`, `Double`, etc.) to bridge primitive types into object graphs.

### The Integer Cache Pool Trap:
- The JVM maintains an internal flyweight cache for `Integer` objects in the range $[-128, 127]$.
- When autoboxing or using `Integer.valueOf(120)`, the JVM returns a shared cached object reference.
- Hence: `(Integer.valueOf(127) == Integer.valueOf(127))` evaluates to `true`.
- However, for values outside this range: `Integer.valueOf(1000) == Integer.valueOf(1000)` evaluates to **`false`** because two distinct object references are instantiated on the heap.
- **Rule**: Always compare wrapper class instances using `.equals()`, never `==`.

---

## 5. Execution Output

```text
=== TASK 10: PACKAGES AND ENCAPSULATION DEMO ===

--- 1. Encapsulated Vehicle Details ---
Vehicle Registration : TN01AB1234
Recorded Duration    : 120 minutes

--- 2. Service Layer State Management ---
[Success] Vehicle TN01AB1234 parked in Slot 5
Slot Occupancy State: true

--- 3. Autoboxing & Wrapper Class Equality ---
Boxed Integer   : 120
Unboxed int     : 120

Comparing cached integers (127 == 127): true (Cached pool reference)
Comparing uncached integers (1000 == 1000 with ==): false (False! Distinct heap objects)
Comparing uncached integers (1000.equals(1000))   : true (True! Value equality)

--- 4. Slot Release & Encapsulated Mutability ---
[Success] Slot 5 vacated. Vehicle TN01AB1234 exited.
Slot Occupied after exit: false
```
