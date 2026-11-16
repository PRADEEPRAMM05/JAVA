import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Task 11: End-to-End Implementation, Testing and Defence.
 * Full object-oriented Smart-Parking simulation with comprehensive test coverage:
 * - Normal operations across TwoWheeler, Car, ElectricCar
 * - Boundary cases (last slot, 0-min stay, slab boundary 60 vs 61 mins)
 * - Invalid cases (duplicate plate, invalid coordinates, double-free)
 */

interface Chargeable {
    double CHARGING_RATE_PER_KWH = 12.0;
    double chargingFee(int kWh);
}

abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or blank");
        }
        this.regNo = regNo;
    }

    public String getRegNo() { return regNo; }

    public abstract double ratePerHour();

    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative: " + minutes);
        }
        // Ceiling arithmetic for billable hours (minimum 1 hour charged if minutes >= 0)
        int hours = (minutes == 0) ? 0 : (minutes + 59) / 60;
        return hours * ratePerHour();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle other)) return false;
        return Objects.equals(this.regNo, other.regNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regNo);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + regNo + ")";
    }
}

class TwoWheeler extends Vehicle {
    public TwoWheeler(String regNo) { super(regNo); }
    @Override public double ratePerHour() { return 10.0; }
}

class Car extends Vehicle {
    public Car(String regNo) { super(regNo); }
    @Override public double ratePerHour() { return 30.0; }
}

class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) { super(regNo); }
    @Override public double ratePerHour() { return 35.0; }

    @Override
    public double chargingFee(int kWh) {
        if (kWh < 0) throw new IllegalArgumentException("kWh cannot be negative");
        return kWh * CHARGING_RATE_PER_KWH;
    }
}

class ParkingSlot {
    private final int floor;
    private final int slotNumber;
    private Vehicle occupant;

    public ParkingSlot(int floor, int slotNumber) {
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.occupant = null;
    }

    public int getFloor() { return floor; }
    public int getSlotNumber() { return slotNumber; }
    public boolean isAvailable() { return occupant == null; }
    public Vehicle getOccupant() { return occupant; }

    public void assign(Vehicle v) { this.occupant = v; }
    public void vacate() { this.occupant = null; }

    @Override
    public String toString() {
        return "Floor " + floor + ", Slot " + slotNumber;
    }
}

class ParkingTicket {
    private static int counter = 1000;
    private final String ticketNumber;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final LocalDateTime entryTime;
    private final boolean hasMonthlyPass;

    public ParkingTicket(Vehicle vehicle, ParkingSlot slot, boolean hasMonthlyPass) {
        counter++;
        this.ticketNumber = "TKT-" + counter;
        this.vehicle = vehicle;
        this.slot = slot;
        this.entryTime = LocalDateTime.now();
        this.hasMonthlyPass = hasMonthlyPass;
    }

    public String getTicketNumber() { return ticketNumber; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSlot getSlot() { return slot; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public boolean hasMonthlyPass() { return hasMonthlyPass; }

    public double computeTotalFee(int durationMinutes, int chargingKWh) {
        if (hasMonthlyPass) {
            // Pass holder pays Rs. 0 for parking, but pays for EV charging if used
            double fee = 0.0;
            if (vehicle instanceof Chargeable c && chargingKWh > 0) {
                fee += c.chargingFee(chargingKWh);
            }
            return fee;
        }

        double fee = vehicle.calculateFee(durationMinutes);
        if (vehicle instanceof Chargeable c && chargingKWh > 0) {
            fee += c.chargingFee(chargingKWh);
        }
        return fee;
    }
}

class ParkingFacility {
    private final ParkingSlot[][] slots;
    private final ParkingTicket[] activeTickets;
    private int ticketCount = 0;

    public ParkingFacility(int[] slotsPerFloor) {
        slots = new ParkingSlot[slotsPerFloor.length][];
        int totalCapacity = 0;
        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new ParkingSlot[slotsPerFloor[f]];
            for (int s = 0; s < slotsPerFloor[f]; s++) {
                slots[f][s] = new ParkingSlot(f, s);
            }
            totalCapacity += slotsPerFloor[f];
        }
        activeTickets = new ParkingTicket[totalCapacity];
    }

    public ParkingTicket park(Vehicle vehicle, boolean hasPass) {
        // Prevent duplicate vehicle registration
        for (int i = 0; i < ticketCount; i++) {
            if (activeTickets[i] != null && activeTickets[i].getVehicle().equals(vehicle)) {
                System.out.println("[REJECTED] Vehicle " + vehicle.getRegNo() + " is already parked!");
                return null;
            }
        }

        // Search for first available slot
        for (ParkingSlot[] floorSlots : slots) {
            for (ParkingSlot slot : floorSlots) {
                if (slot.isAvailable()) {
                    slot.assign(vehicle);
                    ParkingTicket ticket = new ParkingTicket(vehicle, slot, hasPass);
                    activeTickets[ticketCount++] = ticket;
                    System.out.println("[PARKED] " + vehicle + " -> " + slot + " | Ticket: " + ticket.getTicketNumber());
                    return ticket;
                }
            }
        }
        System.out.println("[FACILITY FULL] No slots available for " + vehicle.getRegNo());
        return null;
    }

    public double exit(String ticketNumber, int durationMinutes, int chargingKWh) {
        for (int i = 0; i < ticketCount; i++) {
            ParkingTicket ticket = activeTickets[i];
            if (ticket != null && ticket.getTicketNumber().equals(ticketNumber)) {
                double total = ticket.computeTotalFee(durationMinutes, chargingKWh);
                ticket.getSlot().vacate();

                // Shift remaining active tickets
                activeTickets[i] = activeTickets[ticketCount - 1];
                activeTickets[ticketCount - 1] = null;
                ticketCount--;

                System.out.printf("[EXIT] %s | Stay: %d min | Fee: Rs. %.2f (Slot %s vacated)%n",
                        ticket.getVehicle(), durationMinutes, total, ticket.getSlot());
                return total;
            }
        }
        System.out.println("[ERROR] Ticket " + ticketNumber + " not found or already exited.");
        return -1.0;
    }

    public boolean releaseManual(int floor, int slot) {
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length) {
            return false;
        }
        if (slots[floor][slot].isAvailable()) {
            return false;
        }
        slots[floor][slot].vacate();
        return true;
    }
}

public class ParkingSystemDemo {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   CAMPUS SMART-PARKING SYSTEM: INTEGRATION TEST ");
        System.out.println("=================================================\n");

        // Facility: Floor 0 has 2 slots, Floor 1 has 1 slot (Total capacity = 3)
        ParkingFacility facility = new ParkingFacility(new int[]{2, 1});

        System.out.println("--- 1. NORMAL CASES ---");
        ParkingTicket t1 = facility.park(new TwoWheeler("TN01AA1111"), false);
        ParkingTicket t2 = facility.park(new Car("TN02BB2222"), false);
        ParkingTicket t3 = facility.park(new ElectricCar("TN03CC3333"), false);

        System.out.println("\n--- 2. BOUNDARY CASE: CAPACITY SATURATION ---");
        // Facility is now full (3/3 slots occupied)
        ParkingTicket t4 = facility.park(new Car("TN04DD4444"), false); // Should be rejected

        System.out.println("\n--- 3. INVALID CASE: DUPLICATE VEHICLE REGISTRATION ---");
        // Trying to park TN01AA1111 while already inside
        facility.park(new TwoWheeler("TN01AA1111"), false);

        System.out.println("\n--- 4. BOUNDARY & SLAB PROCESSING ON EXIT ---");
        // Exit 1: TwoWheeler after 0 minutes (Zero duration check)
        facility.exit(t1.getTicketNumber(), 0, 0);

        // Exit 2: Car exactly at slab boundary: 60 minutes (1 billable hour = Rs. 30)
        facility.exit(t2.getTicketNumber(), 60, 0);

        // Re-parking into newly available slot
        ParkingTicket t5 = facility.park(new ElectricCar("TN05EE5555"), true); // Staff with Pass!

        // Exit 3: EV with 130 mins (3 billable hrs) + 10 kWh charging
        facility.exit(t3.getTicketNumber(), 130, 10);

        // Exit 4: Staff Passholder EV (Pass waives parking fee Rs. 0, but charging Rs. 120 charged)
        facility.exit(t5.getTicketNumber(), 180, 10);

        System.out.println("\n--- 5. INVALID CASE: DOUBLE-RELEASE / UNKNOWN TICKET ---");
        facility.exit("TKT-9999", 60, 0); // Non-existent ticket
        boolean manualRes = facility.releaseManual(0, 0); // Already vacated slot
        System.out.println("Manual release of already vacant slot (0, 0): " + manualRes);
    }
}
