# Task 05: Apply `static` Members and Object Life Cycle

## 1. Instance vs. `static` Members

In an object-oriented parking architecture:
- **Instance Variables** (`ticketNumber`, `vehicleNumber`, `entryTime`):
  Each ticket object represents an independent vehicle entry event with its own distinct state.
- **Static Variables** (`ticketCounter`):
  Belongs to the class `ParkingTicket` itself, residing in the Metaspace/Method Area. All instances share a single memory cell.
  - *Why an instance variable would be wrong*: If `ticketCounter` were an instance variable, each newly created ticket would initialize its own independent counter (e.g., at 1000), causing duplicate ticket IDs across vehicles and completely breaking audit traceability.

---

## 2. Static Methods and Invocations

Static methods (such as `getTotalTicketsIssued()` and `issueTicket()`) execute in a class-wide context:
- They can be invoked directly using the class name: `ParkingTicket.getTotalTicketsIssued()`.
- **Why static methods cannot access `this`**: The `this` keyword refers to the specific calling instance on the heap. Because static methods are invoked without any instance context, dereferencing `this` has no meaning and produces a compile-time error.

---

## 3. Object Life Cycle and Garbage Collection (GC)

In Java, object memory is managed automatically by the Garbage Collector (e.g., ZGC or G1GC):

1. **Instantiation**: `ParkingTicket t1 = new ParkingTicket(...)` allocates object memory on the Java Heap and places reference `t1` on the thread stack frame.
2. **Active Lifetime**: The object is referenced by active data structures, methods, or ticket registries.
3. **Dereferencing / Unreachability**: When a vehicle exits and the ticket is finalized:
   ```java
   t1 = null;
   ```
   The object on the heap no longer has any active references traversing from GC Roots (stack frames, static references).
4. **Garbage Collection**: The JVM garbage collector marks the memory block as reclaimed during subsequent collection cycles.

---

## 4. Complete Implementation (`StaticTicketDemo.java`)

```java
public class StaticTicketDemo {

    public static class ParkingTicket {

        public static final int INITIAL_TICKET_ID = 1000;
        public static final int MAX_CAPACITY = 500;

        private static int ticketCounter;

        private final String ticketNumber;
        private final String vehicleNumber;

        static {
            ticketCounter = INITIAL_TICKET_ID;
            System.out.println("[JVM] ParkingTicket class loaded. Static ticketCounter initialized to " + ticketCounter);
        }

        public ParkingTicket(String vehicleNumber) {
            if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Vehicle number cannot be empty");
            }
            ticketCounter++;
            this.ticketNumber = "TKT-" + ticketCounter;
            this.vehicleNumber = vehicleNumber;
        }

        public static int getTotalTicketsIssued() {
            return ticketCounter - INITIAL_TICKET_ID;
        }

        public static int getNextTicketSequence() {
            return ticketCounter + 1;
        }

        public String getTicketNumber() { return ticketNumber; }
        public String getVehicleNumber() { return vehicleNumber; }

        public void displayTicket() {
            System.out.println("  [Ticket] Number: " + ticketNumber + " | Vehicle: " + vehicleNumber);
        }
    }

    public static void processExit(ParkingTicket ticket) {
        System.out.println("\n[Exit Gate] Processing departure for ticket:");
        ticket.displayTicket();
    }

    public static ParkingTicket issueTicket(String vehicleNumber) {
        return new ParkingTicket(vehicleNumber);
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 5: STATIC MEMBERS & OBJECT LIFECYCLE DEMO ===\n");

        ParkingTicket ticket1 = issueTicket("TN01AB1234");
        ParkingTicket ticket2 = issueTicket("TN02CD5678");
        ParkingTicket ticket3 = issueTicket("TN03EF9012");

        System.out.println("\n--- Active Tickets Issued ---");
        ticket1.displayTicket();
        ticket2.displayTicket();
        ticket3.displayTicket();

        System.out.println("\n--- Static System Metrics ---");
        System.out.println("Total Tickets Issued So Far: " + ParkingTicket.getTotalTicketsIssued());
        System.out.println("Next Anticipated Ticket ID : TKT-" + ParkingTicket.getNextTicketSequence());

        processExit(ticket1);

        System.out.println("\n--- Object Lifecycle & Dereferencing ---");
        System.out.println("Nullifying 'ticket1' reference...");
        ticket1 = null;

        System.out.println("ticket1 reference is now null. Heap object is eligible for GC.");
        System.out.println("ticket2 remains active and reachable:");
        ticket2.displayTicket();

        System.out.println("\nTotal system tickets issued remains persistent: " + ParkingTicket.getTotalTicketsIssued());
    }
}
```

---

## 5. Execution Output

```text
=== TASK 5: STATIC MEMBERS & OBJECT LIFECYCLE DEMO ===

[JVM] ParkingTicket class loaded. Static ticketCounter initialized to 1000

--- Active Tickets Issued ---
  [Ticket] Number: TKT-1001 | Vehicle: TN01AB1234
  [Ticket] Number: TKT-1002 | Vehicle: TN02CD5678
  [Ticket] Number: TKT-1003 | Vehicle: TN03EF9012

--- Static System Metrics ---
Total Tickets Issued So Far: 3
Next Anticipated Ticket ID : TKT-1004

[Exit Gate] Processing departure for ticket:
  [Ticket] Number: TKT-1001 | Vehicle: TN01AB1234

--- Object Lifecycle & Dereferencing ---
Nullifying 'ticket1' reference...
ticket1 reference is now null. Heap object is eligible for GC.
ticket2 remains active and reachable:
  [Ticket] Number: TKT-1002 | Vehicle: TN02CD5678

Total system tickets issued remains persistent: 3
```
