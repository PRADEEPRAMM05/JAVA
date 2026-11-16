# Task 07: Override and Specialise Behaviour

## 1. Principles of Method Overriding in Java

Method overriding occurs when a subclass provides a specific implementation of a method already defined in its superclass.

### Core Overriding Rules:
1. **Method Signature**: The method name, parameter types, and parameter order must be identical.
2. **Return Type Covariance**: The return type must be identical or a subtype of the return type declared in the superclass.
3. **Access Modifier Rules**: An overriding method cannot have a more restrictive access modifier (e.g., a `protected` method in a parent class can be overridden as `protected` or `public`, but never `private`).
4. **Exception Handling**: An overriding method cannot throw new or broader checked exceptions.

### The `@Override` Annotation:
The `@Override` annotation instructs the Java compiler to verify that the method actually overrides a superclass method. If the developer makes a typo in the method name or argument list, the compiler immediately flags an error rather than silently treating it as an overloaded method.

---

## 2. Specialized Behaviour in Parking Subclasses

- **`ratePerHour()`**:
  - `TwoWheeler` returns `10.0` (Rs. 10/hr).
  - `Car` returns `30.0` (Rs. 30/hr).
  - `ElectricCar` returns `35.0` (Rs. 35/hr priority rate).
- **Duration Ceiling Calculation**:
  Implemented in the base class using integer arithmetic:
  $$\text{billableHours} = \frac{\text{minutes} + 59}{60}$$
  For example, $75\text{ minutes} \implies (75 + 59) / 60 = 134 / 60 = 2\text{ hours}$.

---

## 3. The `equals()` and `hashCode()` Contract

In Java, hash-based collections (`HashSet`, `HashMap`) rely on the strict mathematical contract between `equals()` and `hashCode()`:
1. **Consistency**: If `v1.equals(v2) == true`, then `v1.hashCode()` **must strictly equal** `v2.hashCode()`.
2. **Business Key**: For vehicles, the real-world invariant is the registration number (`regNo`). Two distinct object references in memory holding the same license plate represent the same vehicle.
3. **Contract Enforcement**: By overriding both methods based on `regNo`, we prevent duplicate vehicle allocations and guarantee correct lookup in hash collections.

---

## 4. Complete Implementation (`OverrideDemo.java`)

```java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

abstract class Vehicle {
    protected final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or empty");
        }
        this.regNo = regNo;
    }

    public String getRegNo() { return regNo; }

    public abstract double ratePerHour();

    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        int hours = (minutes + 59) / 60; // Integer ceiling arithmetic
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
        return getClass().getSimpleName() + " [regNo=" + regNo + ", rate=Rs." + ratePerHour() + "/hr]";
    }
}

class TwoWheeler extends Vehicle {
    public TwoWheeler(String regNo) { super(regNo); }

    @Override
    public double ratePerHour() { return 10.0; }
}

class Car extends Vehicle {
    public Car(String regNo) { super(regNo); }

    @Override
    public double ratePerHour() { return 30.0; }
}

class ElectricCar extends Car {
    private final double chargingRatePerKWh = 12.0;

    public ElectricCar(String regNo) { super(regNo); }

    @Override
    public double ratePerHour() { return 35.0; }

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
        Vehicle car2 = new Car("KA02BB2222");
        ElectricCar ev = new ElectricCar("KA03CC3333");

        int durationMinutes = 75;
        System.out.println("1. Overridden Fee Calculations (Duration: " + durationMinutes + " mins -> 2 billable hrs):");
        System.out.printf("   %s -> Fee: Rs. %.2f%n", bike.getRegNo(), bike.calculateFee(durationMinutes));
        System.out.printf("   %s -> Fee: Rs. %.2f%n", car1.getRegNo(), car1.calculateFee(durationMinutes));
        System.out.printf("   %s -> Fee: Rs. %.2f (EV with 10kWh: Rs. %.2f)%n",
                ev.getRegNo(), ev.calculateFee(durationMinutes), ev.calculateFee(durationMinutes, 10));

        System.out.println("\n2. toString() Formatting:");
        System.out.println("   " + bike);
        System.out.println("   " + car1);
        System.out.println("   " + ev);

        System.out.println("\n3. Testing equals() and hashCode() Contract:");
        System.out.println("   car1.equals(car2): " + car1.equals(car2));
        System.out.println("   car1.hashCode() == car2.hashCode(): " + (car1.hashCode() == car2.hashCode()));

        Set<Vehicle> parkedVehicles = new HashSet<>();
        parkedVehicles.add(car1);
        boolean addedDuplicate = parkedVehicles.add(car2);
        System.out.println("   Set prevented duplicate vehicle entry: " + (!addedDuplicate));
        System.out.println("   Total unique vehicles in set: " + parkedVehicles.size());
    }
}
```

---

## 5. Execution Output

```text
=== TASK 7: METHOD OVERRIDING & CONTRACTS DEMO ===

1. Overridden Fee Calculations (Duration: 75 mins -> 2 billable hrs):
   KA01AA1111 -> Fee: Rs. 20.00
   KA02BB2222 -> Fee: Rs. 60.00
   KA03CC3333 -> Fee: Rs. 70.00 (EV with 10kWh: Rs. 190.00)

2. toString() Formatting:
   TwoWheeler [regNo=KA01AA1111, rate=Rs.10.0/hr]
   Car [regNo=KA02BB2222, rate=Rs.30.0/hr]
   ElectricCar [regNo=KA03CC3333, rate=Rs.35.0/hr] (EV Charging Capable @ Rs.12.0/kWh)

3. Testing equals() and hashCode() Contract:
   car1.equals(car2): true
   car1.hashCode() == car2.hashCode(): true
   Set prevented duplicate vehicle entry: true
   Total unique vehicles in set: 1
```
