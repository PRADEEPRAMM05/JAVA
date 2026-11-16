package parking.model;

/**
 * Encapsulated domain model representing a Vehicle.
 * Demonstrates:
 * - private fields
 * - public getters and defensive validation in setters
 * - package-private helper methods
 */
public class Vehicle {
    private final String regNo;
    private int parkingDurationMinutes;

    public Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty");
        }
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    public int getParkingDurationMinutes() {
        return parkingDurationMinutes;
    }

    public void setParkingDurationMinutes(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        this.parkingDurationMinutes = minutes;
    }

    public void display() {
        System.out.println("Vehicle Registration : " + regNo);
        System.out.println("Recorded Duration    : " + parkingDurationMinutes + " minutes");
    }
}
