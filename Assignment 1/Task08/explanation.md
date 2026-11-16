# Task 08: Apply Polymorphism (Compile-Time and Runtime)

## 1. Compile-Time vs. Runtime Polymorphism

Polymorphism allows objects of different concrete types to be treated uniformly through a shared interface or superclass reference.

| Dimension | Compile-Time Polymorphism (Static Binding) | Runtime Polymorphism (Dynamic Binding) |
| :--- | :--- | :--- |
| **Mechanism** | Method Overloading (same method name, different parameter signatures). | Method Overriding via dynamic method dispatch through the `vtable`. |
| **Resolution Time** | Resolved at compile time by the Java compiler based on static reference types. | Resolved at runtime by the JVM based on the actual object on the heap. |
| **Example in System** | `calculateFee(int minutes)` vs. `calculateFee(LocalTime in, LocalTime out)`. | `v.calculateFee(130)` invoking `TwoWheeler.ratePerHour()` or `Car.ratePerHour()`. |

---

## 2. Dynamic Method Dispatch & Heterogeneous Processing

In `FeeDemo.java`, vehicles are stored in a polymorphic array:
```java
Vehicle[] parked = { new TwoWheeler("TN01A1"), new Car("TN02B2"), new ElectricCar("TN03C3") };
for (Vehicle v : parked) {
    double fee = v.calculateFee(130);
    ...
}
```
- The loop operates on the base reference type `Vehicle`.
- At runtime, the JVM inspects the internal class pointer of each heap object and invokes the subclass's overridden `ratePerHour()` implementation.
- This satisfies the **Open/Closed Principle (OCP)**: new vehicle types can be introduced without modifying the billing loop.

---

## 3. Pattern Matching for `instanceof`

When an operation applies only to a specific capability (such as EV charging):
```java
if (v instanceof Chargeable c) {
    fee += c.chargingFee(10);
}
```
Java pattern matching combines type checking and safe downcasting into a single atomic statement, eliminating manual casting bugs (`(Chargeable) v`).

---

## 4. Complete Implementation (`FeeDemo.java`)

```java
interface Chargeable {
    double chargingFee(int kWh);
}

abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    /** Every started hour is charged in full. */
    public double calculateFee(int minutes) {
        // TODO 1: Compute billable hours as the ceiling of minutes / 60 using integer
        // arithmetic only, and return hours * ratePerHour()
        int hours = (minutes + 59) / 60;
        return hours * ratePerHour();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Vehicle && regNo.equals(((Vehicle) o).regNo);
    }

    @Override
    public int hashCode() {
        return regNo.hashCode();
    }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 10;
    }
}

class Car extends Vehicle {
    // TODO 2: Write the constructor using super, and override ratePerHour() to return 30
    Car(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 30;
    }
}

class ElectricCar extends Car implements Chargeable {
    ElectricCar(String regNo) {
        super(regNo);
    }

    // TODO 3: Implement chargingFee() at Rs 12 per kWh
    @Override
    public double chargingFee(int kWh) {
        return kWh * 12.0;
    }
}

public class FeeDemo {
    public static void main(String[] args) {
        Vehicle[] parked = {
            new TwoWheeler("TN01A1"),
            new Car("TN02B2"),
            new ElectricCar("TN03C3")
        };

        for (Vehicle v : parked) {
            double fee = v.calculateFee(130); // dynamic method dispatch
            if (v instanceof Chargeable c) {
                fee += c.chargingFee(10);
            }
            System.out.println(v.getClass().getSimpleName() + " " + v.getRegNo() + " -> Rs " + fee);
        }

        System.out.println(new Car("TN02B2").equals(new ElectricCar("TN02B2")));
    }
}
```

---

## 5. Execution Output

```text
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```

### Trace:
- **Duration**: 130 minutes $\rightarrow (130 + 59) / 60 = 3$ billable hours.
- **TwoWheeler**: $3 \times 10 = \text{Rs. } 30.0$.
- **Car**: $3 \times 30 = \text{Rs. } 90.0$.
- **ElectricCar**: $3 \times 30 = \text{Rs. } 90.0$ parking fee $+ 10\text{ kWh} \times 12.0 = \text{Rs. } 120.0$ charging fee $\rightarrow \text{Total: Rs. } 210.0$.
- **Identity**: `equals()` returns `true` because both vehicles share registration `"TN02B2"`.
