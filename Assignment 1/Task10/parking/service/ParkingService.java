package parking.service;

import parking.model.ParkingSlot;
import parking.model.Vehicle;

/**
 * Service orchestrator demonstrating encapsulated business logic.
 * Decouples caller code from internal data structures.
 */
public class ParkingService {

    public boolean parkVehicle(Vehicle vehicle, ParkingSlot slot) {
        if (vehicle == null || slot == null) {
            System.out.println("[Service Error] Invalid vehicle or slot provided.");
            return false;
        }

        if (slot.isOccupied()) {
            System.out.println("[Service Error] Slot " + slot.getSlotId() + " is already occupied!");
            return false;
        }

        boolean assigned = slot.assignVehicle(vehicle);
        if (assigned) {
            System.out.println("[Success] Vehicle " + vehicle.getRegNo() + " parked in Slot " + slot.getSlotId());
        }
        return assigned;
    }

    public Vehicle releaseVehicle(ParkingSlot slot) {
        if (slot == null || !slot.isOccupied()) {
            System.out.println("[Service Error] Cannot release slot; slot is vacant or null.");
            return null;
        }

        Vehicle released = slot.vacateSlot();
        System.out.println("[Success] Slot " + slot.getSlotId() + " vacated. Vehicle " + released.getRegNo() + " exited.");
        return released;
    }
}
