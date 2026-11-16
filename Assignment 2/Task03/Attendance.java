import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Task 03: Complete the Code - Engineer the hashCode-equals Contract.
 * Models university RFID gate scans stored in a HashSet.
 */
class Student {
    private final String regNo;
    private final String name;

    Student(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student other)) return false;
        return Objects.equals(this.regNo, other.regNo);
    }

    // TODO 1: Override hashCode() so that it is consistent with equals()
    @Override
    public int hashCode() {
        return Objects.hash(regNo);
    }

    @Override
    public String toString() {
        return regNo + " " + name;
    }
}

public class Attendance {
    public static void main(String[] args) {
        Set<Student> present = new HashSet<>();
        present.add(new Student("192311001", "Arun")); // scanned at Gate 1
        present.add(new Student("192311002", "Divya")); // scanned at Gate 2
        present.add(new Student("192311001", "Arun")); // scanned again at Gate 3

        System.out.println("Unique students: " + present.size());
    }
}
