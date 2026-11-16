# Task 02: Choose Data Representation & Memory Analysis

## 1. Data Type Selection & Field Rationalization

Each field in the Smart-Parking domain is mapped to an appropriate Java primitive or reference type to guarantee precision, range safety, and semantic clarity:

| Field | Chosen Java Type | Technical Justification |
| :--- | :--- | :--- |
| `floor` | `int` | Floor indices range from 0 to 10+. Primitive `int` provides standard array indexing with zero memory overhead. |
| `slotNumber` | `int` | Zero-indexed numerical position on a given floor. Avoids string parsing overhead. |
| `vehicleNo` / `regNo` | `String` | Alphanumeric license plates (e.g., `"TN01AB1111"`). Immutable and provides built-in `.equals()` and `.hashCode()`. |
| `entryTime` / `exitTime` | `java.time.LocalTime` or `int` | `int` (representing minutes elapsed since midnight, 0–1439) provides fast arithmetic. `LocalTime` provides human-readable ISO-8601 formatting for receipts. |
| `durationMinutes` | `int` | Elapsed parking duration in minutes. An `int` safely covers stays up to 4,000+ years without integer overflow. |
| `fee` / `hourlyRate` | `double` | Monetary currencies require decimal precision for partial rates and kilowatt-hour charging fees (e.g., Rs. 12.50). |
| `ticketId` | `int` or `String` | Formatted identifier (e.g., `"TKT-1001"`). Backed by an internal `static int` counter. |

---

## 2. Multi-Level Facility Structure: 1D vs. 2D vs. Jagged Array

A university parking building often has differing structural layouts per floor (e.g., Basement 1 accommodates 60 cars, Ground floor accommodates 30 VIP slots, Upper floor accommodates 45 slots).

### Structural Choices:
1. **1D Linear Array (`String[] slots`)**:
   - Requires manual offset arithmetic: $\text{index} = \text{floor} \times \text{slotsPerFloor} + \text{slot}$.
   - **Flaw**: Requires every floor to have an identical number of slots. Cannot model uneven or irregularly shaped floors without wasting space on dummy entries.
2. **Standard Rectangular 2D Array (`String[floors][slots]`)**:
   - Assumes uniform matrix dimensions $M \times N$.
   - **Flaw**: Suffers from internal memory fragmentation when lower floors have larger capacities than upper floors.
3. **Jagged (Ragged) 2D Array (`String[][] slots`) [Chosen Representation]**:
   - Allocated dynamically according to an array specifying capacity per floor:
     ```java
     int[] slotsPerFloor = {60, 40, 50}; // Floor 0 has 60, Floor 1 has 40, Floor 2 has 50
     slots = new String[slotsPerFloor.length][];
     for (int f = 0; f < slotsPerFloor.length; f++) {
         slots[f] = new String[slotsPerFloor[f]];
     }
     ```
   - **Advantage**: Zero wasted references. Each sub-array strictly matches the real physical capacity of that specific floor.

---

## 3. Tracking Active Tickets and Slot Availability

- **Slot Availability**:
  - In `String[][] slots`, an unoccupied slot is represented as `null`.
  - An occupied slot directly stores the vehicle's unique registration number string (`vehicleNo`), or an object reference to a `ParkingSlot` / `Vehicle`.
  - Allocating a slot is an atomic write: `slots[f][s] = vehicleNo;`
  - Releasing a slot is an atomic reset: `slots[f][s] = null;`
- **Active Tickets**:
  - Maintained in an active ticket repository `ParkingTicket[] activeTickets` or `Map<String, ParkingTicket>`.
  - When a vehicle exits, its ticket is retrieved by vehicle registration number or ticket ID, verified, billed, and archived.

---

## 4. Scaling the Facility Architecture (3 to 10 Floors, 50 to 200 Slots)

When scaling to 10 floors $\times$ 200 slots (2,000 total slots):
- Jagged array allocation scales linearly without architectural redesign:
  ```java
  int[] largeFacility = new int[10];
  Arrays.fill(largeFacility, 200);
  ParkingFacility p = new ParkingFacility(largeFacility);
  ```
- To accelerate allocation from $O(F \times S)$ sequential scanning to $O(1)$, we introduce a pointer/queue of available slots per floor: `Queue<Integer>[] freeSlotsPerFloor`.

---

## 5. Alternative Concept & Complexity Analysis

### Comparison: Reference Array (`String[][]`) vs. `BitSet` / Boolean Matrix

#### Alternative Representation:
Instead of storing references in the matrix, track slot occupancy using a compact `java.util.BitSet[]` (one bit per slot, where `0 = free`, `1 = occupied`) alongside a separate lookup table (such as a `HashMap<String, ParkingTicket>` or array) for active parking records:

```java
BitSet[] floorAvailability = new BitSet[floors];
// Allocation:
int nextFree = floorAvailability[floor].nextClearBit(0);
if (nextFree < slotsOnFloor) {
    floorAvailability[floor].set(nextFree); // Mark occupied
}
```

#### Memory Consumption Comparison (Facility: 5 floors $\times$ 100 slots = 500 slots):

| Parameter | Reference 2D Array (`String[5][100]`) | `java.util.BitSet[5]` Representation |
| :--- | :--- | :--- |
| **Data Structure Footprint** | 5 sub-arrays $\times$ 100 references. On 64-bit JVM with compressed oops (4 bytes/ref) + array object headers (16 bytes $\times$ 5): $\approx (5 \times 16) + (500 \times 4) + 24 = \mathbf{2,104\text{ bytes}}$. | 5 `BitSet` objects. Each holds a `long[]` of size 2 (128 bits): $(5 \times 24\text{ header}) + (5 \times 16\text{ byte long[]}) = \mathbf{200\text{ bytes}}$. |
| **Memory Saved** | Baseline | **$\approx 90.5\%$ reduction in matrix footprint** ($\sim 1.9\text{ KB}$ saved). |
| **Search Time for Free Slot** | Sequential scan loop checking `slots[f][s] == null`: Worst Case: $O(F \times S) = \mathbf{500\text{ iterations}}$. | `nextClearBit(0)` utilizes bitwise CPU hardware instructions (`CTZ` / count trailing zeros): **$O(1)$ amortized word lookup**. |
| **Slot Release Time** | Direct indexing `slots[f][s] = null`: $O(1)$. | Direct bit clearing `bitSet.clear(s)`: $O(1)$. |
| **Trade-offs** | Holds vehicle identity directly in the matrix cell. Simple, intuitive, beginner-friendly. | Decoupled identity: requires a secondary lookup structure (`Map` or array) to know *which* vehicle is parked at `[f][s]`. |

### Conclusion
For large-scale industrial parking systems (tens of thousands of slots), `BitSet` provides substantial hardware acceleration and minimal cache pressure. For modular educational OOP design, the jagged reference array strikes the ideal balance between code readability, flexibility, and direct slot occupancy tracking.
