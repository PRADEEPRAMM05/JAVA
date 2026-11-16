import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Task 07: Override and Specialise Behaviour.
 * Demonstrates:
 * 1. Method overriding with @Override annotation
 * 2. Specialized fee calculations for TwoWheeler, Car, and ElectricCar
 * 3. Covariant returns & access modifier widening rules
 * 4. The equals() and hashCode() contract based on registration number
 * 5. Meaningful toString() overrides for system logging
 */

abstract class Vehicle {
    protected final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or empty");
        }
        this.regNo = regNo;
    }

    public String getRegNo() { return regNo; }

    // Abstract method defining the hook for hourly rates
    public abstract double ratePerHour();

    // Standard fee calculation: ceiling of hours multiplied by rate
    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        int hours = (minutes + 59) / 60; // Integer ceiling arithmetic
        return hours * ratePerHour();
    }

    // Overriding equals() based on immutable business key (regNo)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle other)) return false;
        return Objects.equals(this.regNo, other.regNo);
    }

    // Overriding hashCode() consistent with equals()
    @Override
    public int hashCode() {
        return Objects.hash(regNo);
    }

    // Overriding toString() for clean log outputs
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [regNo=" + regNo + ", rate=Rs." + ratePerHour() + "/hr]";
    }
}

class TwoWheeler extends Vehicle {
    public TwoWheeler(String regNo) { super(regNo); }

    @Override
    public double ratePerHour() {
        return 10.0; // Two-wheelers: Rs. 10/hr
    }
}

class Car extends Vehicle {
    public Car(String regNo) { super(regNo); }

    @Override
    public double ratePerHour() {
        return 30.0; // Cars: Rs. 30/hr
    }
}

class ElectricCar extends Car {
    private final double chargingRatePerKWh = 12.0;

    public ElectricCar(String regNo) { super(regNo); }

    @Override
    public double ratePerHour() {
        return 35.0; // EVs: Rs. 35/hr priority slot rate
    }

    // Overloaded fee calculation for EV charging
    public double calculateFee(int minutes, int kWh) {
        return super.calculateFee(minutes) + (kWh * chargingRatePerKWh);
    }

    @Override
    public String toString() {
        return super.toString() + " (EV Charging Capable @ Rs." + chargingRatePerKWh + "/kWh)";
    }
}

public class OverrideDemo {
    public static void main(String[] args) {
        System.out.println("=== TASK 7: METHOD OVERRIDING & CONTRACTS DEMO ===\n");

        Vehicle bike = new TwoWheeler("KA01AA1111");
        Vehicle car1 = new Car("KA02BB2222");
        Vehicle car2 = new Car("KA02BB2222"); // Duplicate registration for equals check
        ElectricCar ev = new ElectricCar("KA03CC3333");

        // 1. Specialized fee calculations via overridden ratePerHour()
        int durationMinutes = 75; // 75 mins -> 2 hours ceiling
        System.out.println("1. Overridden Fee Calculations (Duration: " + durationMinutes + " mins -> 2 billable hrs):");
        System.out.printf("   %s -> Fee: Rs. %.2f%n", bike.getRegNo(), bike.calculateFee(durationMinutes));
        System.out.printf("   %s -> Fee: Rs. %.2f%n", car1.getRegNo(), car1.calculateFee(durationMinutes));
        System.out.printf("   %s -> Fee: Rs. %.2f (EV with 10kWh: Rs. %.2f)%n",
                ev.getRegNo(), ev.calculateFee(durationMinutes), ev.calculateFee(durationMinutes, 10));

        // 2. Demonstration of toString()
        System.out.println("\n2. toString() Formatting:");
        System.out.println("   " + bike);
        System.out.println("   " + car1);
        System.out.println("   " + ev);

        // 3. Demonstration of equals() and hashCode() Contract
        System.out.println("\n3. Testing equals() and hashCode() Contract:");
        System.out.println("   car1.equals(car2): " + car1.equals(car2));
        System.out.println("   car1.hashCode() == car2.hashCode(): " + (car1.hashCode() == car2.hashCode()));

        Set<Vehicle> parkedVehicles = new HashSet<>();
        parkedVehicles.add(car1);
        boolean addedDuplicate = parkedVehicles.add(car2); // Should return false
        System.out.println("   Set prevented duplicate vehicle entry: " + (!addedDuplicate));
        System.out.println("   Total unique vehicles in set: " + parkedVehicles.size());
    }
}
