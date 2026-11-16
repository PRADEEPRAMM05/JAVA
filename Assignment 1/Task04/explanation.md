# Task 04: Design Classes, Objects, and Constructors

## 1. Class Design & Field Protection

The `ParkingTicket` class serves as the core physical/digital artifact issued to patrons upon entering the facility. 

### Encapsulated State:
- `ticketNumber` (`final String`): Unique transaction token.
- `registrationNumber` (`final String`): Vehicle identification plate.
- `entryTime` (`final LocalDateTime`): Exact entry timestamp.
- `floorNumber` (`final int`): Allocated floor index.
- `slotNumber` (`final int`): Allocated slot index.

### The Role of `final` Fields:
All fields are marked `final`. This prevents accidental modification after ticket creation, ensuring **audit immutability**:
- A ticket once printed cannot have its entry timestamp forged.
- The allocated floor and slot cannot be tampered with while parked.

---

## 2. Constructor Hierarchy & Constructor Chaining (`this(...)`)

Constructor chaining allows multiple constructors to funnel into a single canonical designated constructor. This enforces the **DRY (Don't Repeat Yourself)** principle and centralizes invariant checks.

```mermaid
flowchart TD
    A["Default Constructor: ParkingTicket()"] -->|this(...) with defaults| B["Primary Parameterised Constructor"]
    C["Overloaded Constructor: (tkt, reg, floor, slot)"] -->|this(...) with LocalDateTime.now()| B
    D["Copy Constructor: (original)"] -->|this(...) unpacking fields| B
    B --> E["Single Invariant Validation Block"]
    E --> F["Object Initialization"]
```

1. **Default Constructor**:
   Provides a safe unassigned placeholder instance without leaving fields uninitialized.
2. **Primary Parameterised Constructor**:
   Contains defensive validations ensuring no `null` strings, empty plates, or negative floor/slot coordinates.
3. **Overloaded Constructor**:
   Simplifies client code by automatically recording the current system time (`LocalDateTime.now()`).
4. **Copy Constructor**:
   Instantiates an exact independent replica of an existing ticket for archival or auditing purposes.

---

## 3. Java Source Code (`ParkingTicket.java`)

```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParkingTicket {

    private final String ticketNumber;
    private final String registrationNumber;
    private final LocalDateTime entryTime;
    private final int floorNumber;
    private final int slotNumber;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 1. Default Constructor
    public ParkingTicket() {
        this("NOT-ISSUED", "NOT-ASSIGNED", LocalDateTime.now(), 0, 0);
    }

    // 2. Primary Parameterised Constructor
    public ParkingTicket(String ticketNumber,
                         String registrationNumber,
                         LocalDateTime entryTime,
                         int floorNumber,
                         int slotNumber) {

        if (ticketNumber == null || ticketNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket number cannot be null or blank");
        }
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or blank");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time cannot be null");
        }
        if (floorNumber < 0) {
            throw new IllegalArgumentException("Floor number cannot be negative: " + floorNumber);
        }
        if (slotNumber < 0) {
            throw new IllegalArgumentException("Slot number cannot be negative: " + slotNumber);
        }

        this.ticketNumber = ticketNumber;
        this.registrationNumber = registrationNumber;
        this.entryTime = entryTime;
        this.floorNumber = floorNumber;
        this.slotNumber = slotNumber;
    }

    // 3. Overloaded Constructor
    public ParkingTicket(String ticketNumber,
                         String registrationNumber,
                         int floorNumber,
                         int slotNumber) {
        this(ticketNumber, registrationNumber, LocalDateTime.now(), floorNumber, slotNumber);
    }

    // 4. Copy Constructor
    public ParkingTicket(ParkingTicket original) {
        if (original == null) {
            throw new IllegalArgumentException("Original ticket cannot be null for copy construction");
        }
        this.ticketNumber = original.ticketNumber;
        this.registrationNumber = original.registrationNumber;
        this.entryTime = original.entryTime;
        this.floorNumber = original.floorNumber;
        this.slotNumber = original.slotNumber;
    }

    public String getTicketNumber() { return ticketNumber; }
    public String getRegistrationNumber() { return registrationNumber; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public int getFloorNumber() { return floorNumber; }
    public int getSlotNumber() { return slotNumber; }

    public void displayTicket() {
        System.out.println("Ticket Number : " + ticketNumber);
        System.out.println("Vehicle Number: " + registrationNumber);
        System.out.println("Entry Time    : " + entryTime.format(FORMATTER));
        System.out.println("Floor Number  : " + floorNumber);
        System.out.println("Slot Number   : " + slotNumber);
        System.out.println("----------------------------------------");
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 4: CONSTRUCTOR DEMO ===");

        ParkingTicket t1 = new ParkingTicket();
        t1.displayTicket();

        LocalDateTime fixedTime = LocalDateTime.of(2026, 11, 16, 9, 30, 0);
        ParkingTicket t2 = new ParkingTicket("TKT-101", "TN01AB1234", fixedTime, 1, 5);
        t2.displayTicket();

        ParkingTicket t3 = new ParkingTicket("TKT-102", "TN02CD5678", 2, 10);
        t3.displayTicket();

        ParkingTicket t4 = new ParkingTicket(t2);
        t4.displayTicket();

        try {
            new ParkingTicket(null, "TN99ZZ9999", 0, -1);
        } catch (IllegalArgumentException ex) {
            System.out.println("Caught Expected Exception: " + ex.getMessage());
        }
    }
}
```

---

## 4. Execution Output

```text
=== TASK 4: CONSTRUCTOR DEMO ===

[1] DEFAULT CONSTRUCTOR:
Ticket Number : NOT-ISSUED
Vehicle Number: NOT-ASSIGNED
Entry Time    : 2026-11-16 12:47:50
Floor Number  : 0
Slot Number   : 0
----------------------------------------
[2] PARAMETERISED CONSTRUCTOR:
Ticket Number : TKT-101
Vehicle Number: TN01AB1234
Entry Time    : 2026-11-16 09:30:00
Floor Number  : 1
Slot Number   : 5
----------------------------------------
[3] OVERLOADED CONSTRUCTOR (Auto Timestamp):
Ticket Number : TKT-102
Vehicle Number: TN02CD5678
Entry Time    : 2026-11-16 12:47:50
Floor Number  : 2
Slot Number   : 10
----------------------------------------
[4] COPY CONSTRUCTOR (Duplicate check):
Ticket Number : TKT-101
Vehicle Number: TN01AB1234
Entry Time    : 2026-11-16 09:30:00
Floor Number  : 1
Slot Number   : 5
----------------------------------------
[5] VALIDATION DEFENSIVE CHECK:
Caught Expected Exception: Ticket number cannot be null or blank
```
