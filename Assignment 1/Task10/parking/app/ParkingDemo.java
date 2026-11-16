package parking.app;

import parking.model.ParkingSlot;
import parking.model.Vehicle;
import parking.service.ParkingService;

/**
 * Task 10 Driver demonstrating:
 * 1. Package separation (model, service, app)
 * 2. Access modifier enforcement (private, package-private, protected, public)
 * 3. Encapsulation defending object invariants
 * 4. Wrapper classes, autoboxing/unboxing, and reference equality traps
 */
public class ParkingDemo {

    public static void main(String[] args) {
        System.out.println("=== TASK 10: PACKAGES AND ENCAPSULATION DEMO ===\n");

        // 1. Instantiating model classes across packages
        Vehicle vehicle = new Vehicle("TN01AB1234");
        vehicle.setParkingDurationMinutes(120);

        System.out.println("--- 1. Encapsulated Vehicle Details ---");
        vehicle.display();

        // 2. Controlled state modification via service layer
        System.out.println("\n--- 2. Service Layer State Management ---");
        ParkingSlot slot = new ParkingSlot(5);
        ParkingService service = new ParkingService();

        service.parkVehicle(vehicle, slot);
        System.out.println("Slot Occupancy State: " + slot.isOccupied());

        // 3. Wrapper classes, Autoboxing, and Integer Cache Analysis
        System.out.println("\n--- 3. Autoboxing & Wrapper Class Equality ---");
        Integer boxedDuration = 120; // Autoboxed from primitive int
        int unboxedDuration = boxedDuration; // Unboxed to primitive int
        System.out.println("Boxed Integer   : " + boxedDuration);
        System.out.println("Unboxed int     : " + unboxedDuration);

        // Demonstrating the Integer Cache (-128 to 127) vs Heap Allocation Trap
        Integer x1 = 127;
        Integer x2 = 127;
        System.out.println("\nComparing cached integers (127 == 127): " + (x1 == x2) + " (Cached pool reference)");

        Integer y1 = 1000;
        Integer y2 = 1000;
        System.out.println("Comparing uncached integers (1000 == 1000 with ==): " + (y1 == y2) + " (False! Distinct heap objects)");
        System.out.println("Comparing uncached integers (1000.equals(1000))   : " + y1.equals(y2) + " (True! Value equality)");

        // 4. Clean Release via Service
        System.out.println("\n--- 4. Slot Release & Encapsulated Mutability ---");
        service.releaseVehicle(slot);
        System.out.println("Slot Occupied after exit: " + slot.isOccupied());
    }
}
