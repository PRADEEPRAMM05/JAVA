import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Task 12: Day-4 Design Challenge - Re-evaluate and Adapt the Design.
 * Demonstrates:
 * 1. Extension to 5 floors with varying (jagged) slot capacities
 * 2. New vehicle types: EBike (flat fee) and DeliveryVan (peak hours surge pricing)
 * 3. Visitor OneDayPass abstraction
 * 4. Decoupled PricingRule strategy pattern
 * 5. High-performance Map-based active ticket registry
 */

// Strategy pattern for modular pricing rules
interface PricingRule {
    double calculate(Vehicle vehicle, LocalDateTime entry, int minutes, boolean monthlyPass, OneDayPass oneDayPass);
}

// Capability interface for battery charging
interface Chargeable {
    double chargingFee(int units);
}

// Abstract Vehicle base class
abstract class Vehicle {
    private final String regNo;

    public Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid registration number");
        }
        this.regNo = regNo;
    }

    public String getRegNo() { return regNo; }
    public abstract double getHourlyRate();
}

class TwoWheeler extends Vehicle {
    public TwoWheeler(String regNo) { super(regNo); }
    @Override public double getHourlyRate() { return 10.0; }
}

class Car extends Vehicle {
    public Car(String regNo) { super(regNo); }
    @Override public double getHourlyRate() { return 30.0; }
}

class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) { super(regNo); }

    @Override
    public double chargingFee(int units) {
        if (units < 0) throw new IllegalArgumentException("Invalid charging units");
        return units * 12.0;
    }
}

// New Category 1: Electric Bike (Flat fee)
class EBike extends Vehicle implements Chargeable {
    public EBike(String regNo) { super(regNo); }
    @Override public double getHourlyRate() { return 0.0; }

    @Override
    public double chargingFee(int units) {
        if (units < 0) throw new IllegalArgumentException("Invalid charging units");
        return units * 8.0; // E-bike battery charging rate: Rs. 8/unit
    }
}

// New Category 2: Commercial Delivery Van
class DeliveryVan extends Vehicle {
    public DeliveryVan(String regNo) { super(regNo); }
    @Override public double getHourlyRate() { return 30.0; } // Base rate equal to car
}

// New Pass Type: Visitor One-Day Pass
class OneDayPass {
    private final LocalDate validDate;

    public OneDayPass(LocalDate validDate) {
        if (validDate == null) throw new IllegalArgumentException("Valid date cannot be null");
        this.validDate = validDate;
    }

    public boolean isValid(LocalDate date) {
        return validDate.equals(date);
    }
}

// Pricing engine implementing the updated business rules
class StandardPricingRule implements PricingRule {
    private static final double EBIKE_FLAT_FEE = 20.0;

    @Override
    public double calculate(Vehicle vehicle, LocalDateTime entry, int minutes, boolean monthlyPass, OneDayPass oneDayPass) {
        if (minutes < 0) throw new IllegalArgumentException("Invalid duration");

        // 1. Pass holder waivers (Monthly Pass or One-Day Pass)
        if (monthlyPass || (oneDayPass != null && oneDayPass.isValid(entry.toLocalDate()))) {
            return 0.0;
        }

        // 2. EBike flat fee rule
        if (vehicle instanceof EBike) {
            return EBIKE_FLAT_FEE;
        }

        // 3. Hourly duration ceiling
        int hours = (minutes == 0) ? 0 : (minutes + 59) / 60;
        double rate = vehicle.getHourlyRate();

        // 4. Delivery Van peak hour surge pricing: 9 AM to 5 PM (09:00 - 17:00) -> Double Rate!
        if (vehicle instanceof DeliveryVan) {
            int entryHour = entry.getHour();
            if (entryHour >= 9 && entryHour < 17) {
                rate *= 2.0; // Rs. 60/hr
            }
        }

        return hours * rate;
    }
}

class ParkingSlot {
    private final int floor;
    private final int number;
    private Vehicle vehicle;

    public ParkingSlot(int floor, int number) {
        this.floor = floor;
        this.number = number;
    }

    public boolean isFree() { return vehicle == null; }
    public void occupy(Vehicle vehicle) {
        if (!isFree()) throw new IllegalStateException("Slot already occupied");
        this.vehicle = vehicle;
    }
    public void release() { this.vehicle = null; }
    public int getFloor() { return floor; }
    public int getNumber() { return number; }
}

class ParkingTicket {
    private final String ticketNumber;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final LocalDateTime entryTime;
    private final boolean monthlyPass;
    private final OneDayPass oneDayPass;

    public ParkingTicket(String ticketNumber, Vehicle vehicle, ParkingSlot slot,
                         LocalDateTime entryTime, boolean monthlyPass, OneDayPass oneDayPass) {
        this.ticketNumber = ticketNumber;
        this.vehicle = vehicle;
        this.slot = slot;
        this.entryTime = entryTime;
        this.monthlyPass = monthlyPass;
        this.oneDayPass = oneDayPass;
    }

    public String getTicketNumber() { return ticketNumber; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSlot getSlot() { return slot; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public boolean hasMonthlyPass() { return monthlyPass; }
    public OneDayPass getOneDayPass() { return oneDayPass; }
}

class ParkingSystem {
    private final ParkingSlot[][] slots;
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();
    private final PricingRule pricingRule = new StandardPricingRule();
    private int ticketCounter = 1001;
    private double dailyCollection = 0;

    public ParkingSystem(int[] capacities) {
        slots = new ParkingSlot[capacities.length][];
        for (int i = 0; i < capacities.length; i++) {
            if (capacities[i] <= 0) throw new IllegalArgumentException("Invalid capacity on floor " + i);
            slots[i] = new ParkingSlot[capacities[i]];
            for (int j = 0; j < capacities[i]; j++) {
                slots[i][j] = new ParkingSlot(i + 1, j + 1);
            }
        }
    }

    private Vehicle createVehicle(String regNo, String type) {
        if (type == null) throw new IllegalArgumentException("Unknown vehicle type");
        return switch (type.toUpperCase()) {
            case "BIKE" -> new TwoWheeler(regNo);
            case "CAR" -> new Car(regNo);
            case "EV" -> new ElectricCar(regNo);
            case "EBIKE" -> new EBike(regNo);
            case "VAN" -> new DeliveryVan(regNo);
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
        };
    }

    public void parkVehicle(String regNo, String type, int floor, int slot,
                            LocalDateTime entry, boolean monthlyPass, OneDayPass oneDayPass) {
        if (activeTickets.containsKey(regNo)) {
            throw new IllegalArgumentException("Duplicate registration: vehicle " + regNo + " already parked");
        }
        Vehicle vehicle = createVehicle(regNo, type);

        if (floor < 1 || floor > slots.length) throw new IllegalArgumentException("Invalid floor number: " + floor);
        if (slot < 1 || slot > slots[floor - 1].length) throw new IllegalArgumentException("Invalid slot: " + slot);

        ParkingSlot targetSlot = slots[floor - 1][slot - 1];
        if (!targetSlot.isFree()) throw new IllegalStateException("Slot occupied: Floor " + floor + ", Slot " + slot);

        String ticketNo = "T" + (ticketCounter++);
        targetSlot.occupy(vehicle);

        ParkingTicket ticket = new ParkingTicket(ticketNo, vehicle, targetSlot, entry, monthlyPass, oneDayPass);
        activeTickets.put(regNo, ticket);

        System.out.printf("[PARKED] %s | Type: %-5s | Ticket: %s | Floor: %d, Slot: %d%n",
                regNo, type, ticketNo, floor, slot);
    }

    public void exitVehicle(String regNo, int minutes, int chargingUnits) {
        ParkingTicket ticket = activeTickets.get(regNo);
        if (ticket == null) {
            throw new IllegalArgumentException("Active ticket not found for " + regNo);
        }

        Vehicle vehicle = ticket.getVehicle();
        if (chargingUnits > 0 && !(vehicle instanceof Chargeable)) {
            throw new IllegalArgumentException("Vehicle " + regNo + " does not support electric charging");
        }

        double parkingFee = pricingRule.calculate(vehicle, ticket.getEntryTime(), minutes,
                ticket.hasMonthlyPass(), ticket.getOneDayPass());
        double chargingFee = 0.0;
        if (vehicle instanceof Chargeable c && chargingUnits > 0) {
            chargingFee = c.chargingFee(chargingUnits);
        }
        double total = parkingFee + chargingFee;

        ticket.getSlot().release();
        activeTickets.remove(regNo);
        dailyCollection += total;

        System.out.printf("[EXITED] %s | Parking: Rs. %.2f | Charging: Rs. %.2f | Total: Rs. %.2f%n",
                regNo, parkingFee, chargingFee, total);
    }

    public double getDailyCollection() { return dailyCollection; }

    public void displayFloorCapacity() {
        System.out.println("--- FACILITY FLOOR CONFIGURATION (5 FLOORS) ---");
        for (int i = 0; i < slots.length; i++) {
            System.out.println("  Floor " + (i + 1) + ": " + slots[i].length + " slots");
        }
    }
}

public class ParkingSystemDemo {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" TASK 12: DAY-4 DESIGN CHALLENGE VALIDATION SUITE ");
        System.out.println("==================================================\n");

        // 5-Floor layout with uneven/jagged capacities
        ParkingSystem system = new ParkingSystem(new int[]{3, 4, 2, 5, 3});
        system.displayFloorCapacity();
        System.out.println();

        LocalDateTime peakEntry = LocalDateTime.of(2026, 11, 16, 10, 0); // 10:00 AM (Peak)
        LocalDateTime offPeakEntry = LocalDateTime.of(2026, 11, 16, 18, 0); // 6:00 PM (Off-Peak)

        System.out.println("--- 1. PARKING TESTING FOR NEW CATEGORIES & PASSES ---");
        // Test A: E-Bike (Flat fee expected: Rs. 20)
        system.parkVehicle("TN01EB1234", "EBIKE", 1, 1, peakEntry, false, null);

        // Test B: Delivery Van during Peak Hours (Double rate Rs. 60/hr expected)
        system.parkVehicle("TN02DV5678", "VAN", 2, 1, peakEntry, false, null);

        // Test C: Visitor with One-Day Pass (Waiver Rs. 0 expected)
        OneDayPass visitorPass = new OneDayPass(LocalDate.of(2026, 11, 16));
        system.parkVehicle("TN03CP9012", "CAR", 3, 1, peakEntry, false, visitorPass);

        // Test D: Electric Car with Charging
        system.parkVehicle("TN04EV3456", "EV", 4, 1, peakEntry, false, null);

        // Test E: Delivery Van during Off-Peak Hours (Standard rate Rs. 30/hr expected)
        system.parkVehicle("TN05DV9999", "VAN", 5, 1, offPeakEntry, false, null);

        System.out.println("\n--- 2. EXIT PROCESSING & FEE VERIFICATION ---");
        // Exit E-Bike (Stay: 120 mins) -> Should charge flat Rs. 20.00
        system.exitVehicle("TN01EB1234", 120, 0);

        // Exit Peak Van (Stay: 120 mins -> 2 hrs @ Rs. 60/hr = Rs. 120.00)
        system.exitVehicle("TN02DV5678", 120, 0);

        // Exit One-Day Pass Car (Stay: 120 mins -> Free waiver = Rs. 0.00)
        system.exitVehicle("TN03CP9012", 120, 0);

        // Exit EV (Stay: 60 mins @ Rs. 30 = Rs. 30 + 2 units charging @ Rs. 12 = Rs. 24 -> Total Rs. 54.00)
        system.exitVehicle("TN04EV3456", 60, 2);

        // Exit Off-Peak Van (Stay: 60 mins -> 1 hr @ Rs. 30/hr = Rs. 30.00)
        system.exitVehicle("TN05DV9999", 60, 0);

        System.out.println("\n==================================================");
        System.out.printf("  TOTAL DAILY COLLECTION: Rs. %.2f%n", system.getDailyCollection());
        System.out.println("==================================================");
    }
}
