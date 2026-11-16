package parking.model;

/**
 * Encapsulated ParkingSlot model.
 * State cannot be modified directly from outside; only manipulated
 * through controlled business methods.
 */
public class ParkingSlot {
    private final int slotId;
    private Vehicle currentVehicle;

    public ParkingSlot(int slotId) {
        if (slotId < 0) {
            throw new IllegalArgumentException("Slot ID cannot be negative");
        }
        this.slotId = slotId;
        this.currentVehicle = null;
    }

    public int getSlotId() {
        return slotId;
    }

    public boolean isOccupied() {
        return currentVehicle != null;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    // Package-private or public controlled mutating methods
    public boolean assignVehicle(Vehicle vehicle) {
        if (isOccupied() || vehicle == null) {
            return false;
        }
        this.currentVehicle = vehicle;
        return true;
    }

    public Vehicle vacateSlot() {
        Vehicle departed = this.currentVehicle;
        this.currentVehicle = null;
        return departed;
    }
}
