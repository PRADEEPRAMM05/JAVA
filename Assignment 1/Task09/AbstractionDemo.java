/**
 * Task 09: Introduce Abstraction.
 * Demonstrates:
 * 1. Abstract class (Vehicle) isolating core state and template method
 * 2. Interfaces (Chargeable, PassHolder) modeling cross-cutting capabilities
 * 3. Multiple interface implementation
 * 4. Extensibility: charging capability shared by both ElectricCar and EBike
 */

// Cross-cutting capability 1: Charging
interface Chargeable {
    double CHARGING_RATE_PER_KWH = 12.0; // Implicitly public static final

    double getBatteryPercentage();
    void charge(int kWh);
    double getChargingFee();
}

// Cross-cutting capability 2: Pass holding privileges
interface PassHolder {
    String getPassId();
    boolean isPassValid();
}

// Abstract base class: defines partial implementation & shared state
abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration cannot be empty");
        }
        this.regNo = regNo;
    }

    public String getRegNo() { return regNo; }

    // Abstract method: force subclasses to supply their specific rate
    public abstract double ratePerHour();

    // Template method for fee calculation
    public double calculateParkingFee(int minutes) {
        int billableHours = (minutes + 59) / 60;
        return billableHours * ratePerHour();
    }
}

// Concrete class 1: Standard Car
class StandardCar extends Vehicle {
    public StandardCar(String regNo) { super(regNo); }

    @Override
    public double ratePerHour() { return 30.0; }
}

// Concrete class 2: Electric Car (Extends Vehicle AND Implements Chargeable)
class ElectricCar extends Vehicle implements Chargeable {
    private double batteryLevel;
    private int energyConsumed;

    public ElectricCar(String regNo, double initialBattery) {
        super(regNo);
        this.batteryLevel = initialBattery;
        this.energyConsumed = 0;
    }

    @Override
    public double ratePerHour() { return 35.0; }

    @Override
    public double getBatteryPercentage() { return batteryLevel; }

    @Override
    public void charge(int kWh) {
        this.energyConsumed += kWh;
        this.batteryLevel = Math.min(100.0, this.batteryLevel + (kWh * 2.5));
    }

    @Override
    public double getChargingFee() {
        return energyConsumed * CHARGING_RATE_PER_KWH;
    }
}

// Concrete class 3: Staff Vehicle (Extends Vehicle AND Implements PassHolder)
class StaffVehicle extends Vehicle implements PassHolder {
    private final String passId;
    private final boolean active;

    public StaffVehicle(String regNo, String passId, boolean active) {
        super(regNo);
        this.passId = passId;
        this.active = active;
    }

    @Override
    public double ratePerHour() { return 30.0; }

    @Override
    public String getPassId() { return passId; }

    @Override
    public boolean isPassValid() { return active; }

    @Override
    public double calculateParkingFee(int minutes) {
        if (isPassValid()) {
            return 0.0; // Free parking for valid pass holders
        }
        return super.calculateParkingFee(minutes);
    }
}

public class AbstractionDemo {
    public static void main(String[] args) {
        System.out.println("=== TASK 9: ABSTRACTION VIA ABSTRACT CLASSES & INTERFACES ===\n");

        Vehicle[] fleet = {
            new StandardCar("TN01AA1000"),
            new ElectricCar("TN02BB2000", 35.0),
            new StaffVehicle("TN03CC3000", "PASS-STAFF-99", true)
        };

        int parkedMinutes = 120; // 2 hours

        for (Vehicle v : fleet) {
            System.out.println("Vehicle: " + v.getRegNo() + " (" + v.getClass().getSimpleName() + ")");
            
            // Check if vehicle is an EV charging subscriber
            if (v instanceof Chargeable ev) {
                System.out.println("  -> Initial Battery: " + ev.getBatteryPercentage() + "%");
                ev.charge(12); // Charge 12 kWh
                System.out.println("  -> Battery after charging: " + ev.getBatteryPercentage() + "%");
                System.out.printf("  -> EV Charging Cost: Rs. %.2f%n", ev.getChargingFee());
            }

            // Check if user has parking pass privileges
            if (v instanceof PassHolder ph) {
                System.out.println("  -> Monthly Pass: " + ph.getPassId() + " (Valid: " + ph.isPassValid() + ")");
            }

            double parkingFee = v.calculateParkingFee(parkedMinutes);
            System.out.printf("  -> Net Parking Fee: Rs. %.2f%n%n", parkingFee);
        }
    }
}
