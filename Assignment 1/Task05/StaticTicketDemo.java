/**
 * Task 05: Apply static Members and the Object Life Cycle.
 * Demonstrates static state, static initialization blocks, factory methods,
 * object reference passing/returning, and garbage collection eligibility.
 */
public class StaticTicketDemo {

    public static class ParkingTicket {

        // Static constants (System-wide immutable limits)
        public static final int INITIAL_TICKET_ID = 1000;
        public static final int MAX_CAPACITY = 500;

        // Static counter shared by all instances
        private static int ticketCounter;

        // Instance variables unique to each individual ticket
        private final String ticketNumber;
        private final String vehicleNumber;

        // Static initialization block: executes once when class is loaded into JVM
        static {
            ticketCounter = INITIAL_TICKET_ID;
            System.out.println("[JVM] ParkingTicket class loaded. Static ticketCounter initialized to " + ticketCounter);
        }

        // Constructor increments shared static counter and assigns immutable ID
        public ParkingTicket(String vehicleNumber) {
            if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Vehicle number cannot be empty");
            }
            ticketCounter++;
            this.ticketNumber = "TKT-" + ticketCounter;
            this.vehicleNumber = vehicleNumber;
        }

        // Static method: acts on class level; cannot reference 'this'
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

    // Passing object reference to method
    public static void processExit(ParkingTicket ticket) {
        System.out.println("\n[Exit Gate] Processing departure for ticket:");
        ticket.displayTicket();
    }

    // Returning freshly instantiated object from method
    public static ParkingTicket issueTicket(String vehicleNumber) {
        return new ParkingTicket(vehicleNumber);
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 5: STATIC MEMBERS & OBJECT LIFECYCLE DEMO ===\n");

        // 1. Instantiating tickets via factory method
        ParkingTicket ticket1 = issueTicket("TN01AB1234");
        ParkingTicket ticket2 = issueTicket("TN02CD5678");
        ParkingTicket ticket3 = issueTicket("TN03EF9012");

        System.out.println("\n--- Active Tickets Issued ---");
        ticket1.displayTicket();
        ticket2.displayTicket();
        ticket3.displayTicket();

        // 2. Accessing static methods without object instantiation
        System.out.println("\n--- Static System Metrics ---");
        System.out.println("Total Tickets Issued So Far: " + ParkingTicket.getTotalTicketsIssued());
        System.out.println("Next Anticipated Ticket ID : TKT-" + ParkingTicket.getNextTicketSequence());

        // 3. Passing object to a method
        processExit(ticket1);

        // 4. Object Life Cycle & Garbage Collection Demonstration
        System.out.println("\n--- Object Lifecycle & Dereferencing ---");
        System.out.println("Nullifying 'ticket1' reference...");
        ticket1 = null; // Original ticket1 object in heap has no live stack references

        System.out.println("ticket1 reference is now null. Heap object is eligible for GC.");
        System.out.println("ticket2 remains active and reachable:");
        ticket2.displayTicket();

        System.out.println("\nTotal system tickets issued remains persistent: " + ParkingTicket.getTotalTicketsIssued());
    }
}
