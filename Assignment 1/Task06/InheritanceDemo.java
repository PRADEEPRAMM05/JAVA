/**
 * Task 06: Design the Inheritance Hierarchy.
 * Demonstrates:
 * 1. Single Inheritance: Vehicle -> TwoWheeler
 * 2. Hierarchical Inheritance: Vehicle -> TwoWheeler, Vehicle -> Car
 * 3. Multilevel Inheritance: Vehicle -> Car -> ElectricCar
 * 4. Constructor chaining using super(regNo)
 * 5. Method specialization and super invocation
 */

// Root Superclass
class Vehicle {
    protected final String regNo;

    public Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty");
        }
        this.regNo = regNo;
        System.out.println("  [Vehicle Constructor] Initialized base regNo: " + regNo);
    }

    public String getRegNo() {
        return regNo;
    }

    public void display() {
        System.out.println("  Registration No : " + regNo);
    }
}

// Subclass 1: TwoWheeler (Hierarchical branch)
class TwoWheeler extends Vehicle {

    public TwoWheeler(String regNo) {
        super(regNo);
        System.out.println("  [TwoWheeler Constructor] TwoWheeler instantiated");
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Category        : Two-Wheeler (Motorcycle/Scooter)");
    }
}

// Subclass 2: Car (Hierarchical branch)
class Car extends Vehicle {

    public Car(String regNo) {
        super(regNo);
        System.out.println("  [Car Constructor] Car instantiated");
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Category        : Four-Wheeler (Car)");
    }
}

// Subclass 3: ElectricCar (Multilevel branch extending Car)
class ElectricCar extends Car {

    private static final double CHARGE_RATE_PER_KWH = 12.0;

    public ElectricCar(String regNo) {
        super(regNo);
        System.out.println("  [ElectricCar Constructor] ElectricCar instantiated");
    }

    // Specialized capability specific to Electric Vehicles
    public double chargingFee(int kWh) {
        if (kWh < 0) {
            throw new IllegalArgumentException("Charging energy cannot be negative");
        }
        return kWh * CHARGE_RATE_PER_KWH;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("  Powertrain      : Electric (EV with charging capability)");
    }
}

// Main Driver
public class InheritanceDemo {

    public static void main(String[] args) {
        System.out.println("=== TASK 6: INHERITANCE HIERARCHY DEMO ===\n");

        System.out.println("1. Instantiating TwoWheeler (Hierarchical Inheritance):");
        TwoWheeler bike = new TwoWheeler("TN01A1111");
        bike.display();

        System.out.println("\n2. Instantiating Car (Hierarchical Inheritance):");
        Car car = new Car("TN02B2222");
        car.display();

        System.out.println("\n3. Instantiating ElectricCar (Multilevel Inheritance):");
        ElectricCar ev = new ElectricCar("TN03C3333");
        ev.display();

        System.out.println("\n--- Specialized EV Charging Calculation ---");
        int unitsConsumed = 15;
        double chargingCost = ev.chargingFee(unitsConsumed);
        System.out.println("  Energy Consumed : " + unitsConsumed + " kWh");
        System.out.println("  Charging Rate   : Rs. 12.00 / kWh");
        System.out.println("  Total EV Fee    : Rs. " + chargingCost);
    }
}
