package task04;

import java.io.*;
import java.sql.Connection;
import java.util.*;

/**
 * HostelSerializationDemo
 * Demonstrates object graph serialization, transient field exclusion
 * (passwords & live network sockets), and serialVersionUID class evolution.
 */
public class HostelSerializationDemo {

    // =========================================================================
    // 1. SERIALIZABLE OBJECT GRAPH
    // =========================================================================
    public static class Resident implements Serializable {
        private static final long serialVersionUID = 1001L;

        private final String regNo;
        private final String name;

        // Transient 1: Passwords must not be serialized to disk for security
        private transient String password;

        // Transient 2: Live network sockets / DB handles cannot be serialized
        private transient Connection liveConnection;

        // Evolved field (compatible with older streams via serialVersionUID)
        private String phoneNumber;

        public Resident(String regNo, String name, String password, Connection conn) {
            this.regNo = regNo;
            this.name = name;
            this.password = password;
            this.liveConnection = conn;
            this.phoneNumber = "Not Specified";
        }

        public String getRegNo() { return regNo; }
        public String getName() { return name; }
        public String getPassword() { return password; }
        public Connection getLiveConnection() { return liveConnection; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phone) { this.phoneNumber = phone; }

        @Override
        public String toString() {
            return String.format("Resident[RegNo=%s, Name=%s, Pwd=%s, Conn=%s, Phone=%s]",
                    regNo, name,
                    (password == null ? "[TRANSIENT_NULL]" : password),
                    (liveConnection == null ? "[TRANSIENT_NULL]" : "ACTIVE_SOCKET"),
                    phoneNumber);
        }
    }

    public static class Room implements Serializable {
        private static final long serialVersionUID = 1001L;

        private final int roomNumber;
        private final List<Resident> occupants = new ArrayList<>();

        public Room(int roomNumber) {
            this.roomNumber = roomNumber;
        }

        public void addResident(Resident r) { occupants.add(r); }
        public int getRoomNumber() { return roomNumber; }
        public List<Resident> getOccupants() { return occupants; }
    }

    public static class Hostel implements Serializable {
        private static final long serialVersionUID = 1001L;

        private final String hostelName;
        private final List<Room> rooms = new ArrayList<>();

        public Hostel(String name) {
            this.hostelName = name;
        }

        public void addRoom(Room r) { rooms.add(r); }
        public String getHostelName() { return hostelName; }
        public List<Room> getRooms() { return rooms; }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Running HostelSerializationDemo verification...");

        Hostel hostel = new Hostel("SIMATS Everest Block");
        Room r101 = new Room(101);
        // Note: liveConnection is simulated with null or active object, password is provided
        Resident res1 = new Resident("192311001", "Aarav Patel", "SuperSecretPwd!#", null);
        Resident res2 = new Resident("192311002", "Bhavna Sharma", "AnotherPass99", null);
        r101.addResident(res1);
        r101.addResident(res2);
        hostel.addRoom(r101);

        System.out.println("\n[BEFORE SERIALIZATION]:");
        System.out.println("Resident 1: " + res1);
        System.out.println("Password present: " + (res1.getPassword() != null));

        // Serialize Object Graph to memory byte buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(hostel);
            oos.flush();
        }

        byte[] serializedBytes = baos.toByteArray();
        System.out.printf("%nSerialized Graph Size: %d bytes%n", serializedBytes.length);

        // Deserialize Object Graph
        Hostel restoredHostel;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedBytes))) {
            restoredHostel = (Hostel) ois.readObject();
        }

        Resident restoredRes1 = restoredHostel.getRooms().get(0).getOccupants().get(0);
        System.out.println("\n[AFTER DESERIALIZATION]:");
        System.out.println("Hostel Name: " + restoredHostel.getHostelName());
        System.out.println("Restored Resident 1: " + restoredRes1);

        // Assert transient fields are null
        assert restoredRes1.getPassword() == null : "Transient password must be null";
        assert restoredRes1.getLiveConnection() == null : "Transient connection must be null";
        assert restoredRes1.getName().equals("Aarav Patel") : "Non-transient name must survive";

        System.out.println("\nVerification: Transient fields safely excluded, object graph intact.");
        System.out.println("HostelSerializationDemo verification PASSED.");
    }
}
