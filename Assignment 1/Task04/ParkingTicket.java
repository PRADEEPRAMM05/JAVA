import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Task 04: Design Classes, Objects and Constructors.
 * Demonstrates field encapsulation, constructor chaining with this(),
 * defensive validation, copy constructor, and immutability.
 */
public class ParkingTicket {

    private final String ticketNumber;
    private final String registrationNumber;
    private final LocalDateTime entryTime;
    private final int floorNumber;
    private final int slotNumber;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 1. Default Constructor (chains to primary constructor)
    public ParkingTicket() {
        this("NOT-ISSUED", "NOT-ASSIGNED", LocalDateTime.now(), 0, 0);
    }

    // 2. Primary Parameterised Constructor (with strict defensive validation)
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

    // 3. Overloaded Constructor (convenience constructor defaulting entryTime to now)
    public ParkingTicket(String ticketNumber,
                         String registrationNumber,
                         int floorNumber,
                         int slotNumber) {
        this(ticketNumber, registrationNumber, LocalDateTime.now(), floorNumber, slotNumber);
    }

    // 4. Copy Constructor (creates safe independent snapshot)
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

    // Getters for encapsulated fields
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

        // Default Constructor
        System.out.println("\n[1] DEFAULT CONSTRUCTOR:");
        ParkingTicket t1 = new ParkingTicket();
        t1.displayTicket();

        // Parameterised Constructor
        System.out.println("[2] PARAMETERISED CONSTRUCTOR:");
        LocalDateTime fixedTime = LocalDateTime.of(2026, 11, 16, 9, 30, 0);
        ParkingTicket t2 = new ParkingTicket("TKT-101", "TN01AB1234", fixedTime, 1, 5);
        t2.displayTicket();

        // Overloaded Constructor
        System.out.println("[3] OVERLOADED CONSTRUCTOR (Auto Timestamp):");
        ParkingTicket t3 = new ParkingTicket("TKT-102", "TN02CD5678", 2, 10);
        t3.displayTicket();

        // Copy Constructor
        System.out.println("[4] COPY CONSTRUCTOR (Duplicate check):");
        ParkingTicket t4 = new ParkingTicket(t2);
        t4.displayTicket();

        // Validation Test
        System.out.println("[5] VALIDATION DEFENSIVE CHECK:");
        try {
            new ParkingTicket(null, "TN99ZZ9999", 0, -1);
        } catch (IllegalArgumentException ex) {
            System.out.println("Caught Expected Exception: " + ex.getMessage());
        }
    }
}
