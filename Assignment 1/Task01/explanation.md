# Task 01: Understand the Problem and Identify Candidate Objects

## 1. Problem Analysis

The **Campus Smart-Parking System** manages vehicles entering, parking within, and exiting a multi-level university parking facility. The facility supports diverse users (students, staff, visitors) and multiple vehicle types (two-wheelers, cars, and electric vehicles). 

### Operational Flow:
1. **Entry**: When a vehicle arrives at the barrier:
   - The driver identifies their category (Student, Staff, Visitor) and vehicle type (Two-Wheeler, Car, EV).
   - The system checks available slots across floors matching the vehicle type.
   - Upon finding a suitable free slot, it allocates the slot, increments occupancy, and issues a timestamped parking ticket with a unique ticket identifier.
2. **Parking & Charging**:
   - Electric Vehicles (EVs) may optionally be assigned to charging-equipped slots and consume electrical energy (kWh).
3. **Exit & Payment**:
   - The driver presents the ticket at the exit barrier.
   - The system records departure time, calculates elapsed duration, applies vehicle-specific hourly rates (charging in full-hour increments), adds EV charging costs if applicable, and checks for fee waivers (students and staff with active monthly passes park free).
   - The parking slot is freed and marked available for upcoming vehicles.

---

## 2. System Inputs and Outputs

### Inputs:
- **Vehicle Registration Number**: Alphanumeric identifier (e.g., `TN01AB1111`).
- **Vehicle Type**: Two-Wheeler, Car, Electric Vehicle.
- **User Role / Category**: Student, Staff, Visitor.
- **Pass Status**: Presence and validity of a monthly parking pass.
- **Facility Capacity Specification**: Number of floors and capacity per floor (e.g., `int[] slotsPerFloor = {20, 20, 20}`).
- **Timestamps**: Entry and exit times (e.g., `LocalTime` or simulated epoch minutes).
- **Energy Consumption (EVs)**: kWh consumed while parked at a charging slot.

### Outputs:
- **Parking Ticket**: Ticket ID, entry timestamp, vehicle details, allocated floor and slot number.
- **Allocation Status**: Success status with floor/slot coordinates, or "Facility Full" notice.
- **Exit Invoice / Receipt**: Duration parked, billable hours, hourly base rate, charging subtotal, discount/waiver applied, and net parking fee.
- **Facility Dashboard**: Real-time availability by floor and vehicle category.

---

## 3. Candidate Classes and Method Identification

By applying object-oriented noun-verb analysis to the problem specification:

| Noun (Candidate Class) | Responsibilities | Verbs (Candidate Methods) | Justification |
| :--- | :--- | :--- | :--- |
| `ParkingFacility` | Manages multi-level floors and slots; coordinates global occupancy. | `allocate(Vehicle v)`, `release(int floor, int slot)`, `isFull()`, `getAvailableCount()` | Central controller required to maintain facility state across floors and slots. |
| `ParkingSlot` | Represents a single physical parking space. | `occupy(Vehicle v)`, `vacate()`, `isAvailable()`, `getSlotNumber()`, `getFloor()` | Encapsulates slot state, slot type (standard vs. EV-charging), and current occupant. |
| `Vehicle` (Abstract) | Encapsulates vehicle identity and base rate contract. | `getRegNo()`, `ratePerHour()`, `calculateFee(int minutes)` | Abstract root for vehicle polymorphism; isolates common state and enforces pricing behavior. |
| `TwoWheeler`, `Car`, `ElectricCar` | Concrete vehicle types with customized fee policies. | `ratePerHour()`, `chargingFee(int kWh)` (EV) | Subclasses specialize pricing; `ElectricCar` implements charging capabilities. |
| `ParkingTicket` | Immutable record of an active or past parking transaction. | `getTicketId()`, `getEntryTime()`, `getVehicle()`, `markExit(LocalTime exitTime)` | Critical audit record ensuring transactional integrity from gate entry to exit. |
| `User` / `PassHolder` | Models users with pass privileges. | `hasValidPass()`, `getUserCategory()` | Decouples vehicle hardware identity from human ownership/pass rights. |

---

## 4. Key Business Rules and Invariants

1. **Mutual Exclusion (Single Occupancy)**: A parking slot can never be assigned to more than one vehicle simultaneously.
2. **Ticket Uniqueness**: Every issued ticket receives a strictly monotonically increasing ID that is never reused, even after vehicle departure.
3. **Full-Hour Billing Ceiling**: Duration is calculated using ceiling arithmetic: any fraction of an hour is charged as a complete hour (e.g., 65 minutes = 2 billable hours).
4. **Pass-Holder Exemption**: Valid monthly pass holders (Staff and Students) incur Rs. 0 parking fee, but EV energy consumption charges still apply if utilized.
5. **Slot Type Compatibility**: Two-wheelers cannot occupy car slots unless designated; EVs requiring charging must be routed to EV-compatible charging slots.

---

## 5. Architectural Assumptions

1. The facility layout is pre-configured at startup via configuration arrays representing floor capacities.
2. Time can be represented linearly in minutes or via `java.time.LocalTime` for precision.
3. Slot lookup operates in a First-Available / Lowest-Floor order to minimize travel distance for patrons.

---

## 6. Alternative Concept & Complexity Analysis

### Comparison: Parallel Arrays vs. Object-Oriented Domain Model

#### Alternative Approach (Parallel Arrays):
In procedural programming, one might maintain parallel primitive arrays:
```java
String[] regNos = new String[MAX_CAPACITY];
String[] types = new String[MAX_CAPACITY];
int[] entryTimes = new int[MAX_CAPACITY];
int[] assignedFloors = new int[MAX_CAPACITY];
int[] assignedSlots = new int[MAX_CAPACITY];
```

#### Detailed Comparison Table:

| Criterion | Parallel Arrays Representation | Object-Oriented Model (`Vehicle`, `ParkingTicket`) |
| :--- | :--- | :--- |
| **Lookup by `regNo`** | **Time: $O(N)$** linear scan across `regNos[]`. | **Time: $O(N)$** linear scan on list, or **$O(1)$** with `Map<String, ParkingTicket>`. |
| **Adding New Attribute** (e.g., `isElectric`) | Requires declaring a new array `boolean[] isElectric = new boolean[MAX_CAPACITY]`, modifying every function signature that touches vehicle records. | Add a single attribute to class `Vehicle` or an interface `Chargeable`. Zero changes to existing caller APIs. |
| **Memory & Cache Layout** | High memory locality for single-field scans, but high indexing error risk and zero encapsulation. | Cache overhead of object headers (12–16 bytes per object), but strong type-safety and modularity. |
| **Space Complexity** | Space: $O(N)$ contiguous primitive references. Total bytes: $\approx N \times 24$ bytes. | Space: $O(N)$ object allocations. Total bytes: $\approx N \times (24 \text{ bytes fields} + 16 \text{ bytes header})$. |
| **Data Integrity** | Fragile. Deleting or swapping index `i` in one array requires synchronous updates across all parallel arrays; failure causes desynchronization. | Robust. Encapsulation guarantees atomic updates; an object reference always maintains consistent internal state. |

### Conclusion
While parallel arrays might seem lightweight for small fixed scripts, they violate encapsulation, lead to catastrophic desynchronization bugs upon element removal, and lack polymorphic dispatch. The object-oriented model provides maintainability, type safety, and extensible contracts.
