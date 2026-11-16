import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Task 03 Investigation:
 * Deep dive into the hashCode-equals contract:
 * 1. Test 1: Where HashSet APPEARS to work (identical heap reference added twice)
 * 2. Test 2: Where the missing hashCode() bug is exposed (distinct objects, same business key)
 * 3. Test 3: The Fatal Post-Insertion Mutation Trap (changing a field used in hashCode)
 */
public class ContractInvestigation {

    // Defective Student: equals overridden, but hashCode NOT overridden
    static class BrokenStudent {
        String regNo;
        String name;

        BrokenStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BrokenStudent other)) return false;
            return Objects.equals(this.regNo, other.regNo);
        }
        // hashCode NOT overridden -> inherits Object.hashCode() based on heap address!
    }

    // Mutable Student: both equals & hashCode overridden, but fields are non-final
    static class MutableStudent {
        String regNo;
        String name;

        MutableStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MutableStudent other)) return false;
            return Objects.equals(this.regNo, other.regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(" TASK 03: INVESTIGATING THE HASHCODE-EQUALS CONTRACT IN HASH-BASED SETS   ");
        System.out.println("==========================================================================\n");

        // --- TEST 1: The Illusion of Working ---
        System.out.println("--- TEST 1: WHY DEFECTIVE CODE MAY APPEAR TO PASS NAIVE TESTS ---");
        Set<BrokenStudent> test1 = new HashSet<>();
        BrokenStudent s1 = new BrokenStudent("192311001", "Arun");
        test1.add(s1);
        test1.add(new BrokenStudent("192311002", "Divya"));
        test1.add(s1); // Re-adding the exact same object reference!
        System.out.println("Added: s1, Divya, s1 (same instance reference)");
        System.out.println("Predicted Set Size: 2 | Observed Set Size: " + test1.size());
        System.out.println("Reason: HashSet checks (e.hash == hash && ((k = e.key) == key || key.equals(k))).");
        System.out.println("Since s1 == s1 is true, reference equality masked the missing hashCode()!\n");

        // --- TEST 2: The Contract Violation Exposed ---
        System.out.println("--- TEST 2: DISTINCT OBJECTS SHARING THE SAME LOGICAL IDENTITY ---");
        Set<BrokenStudent> test2 = new HashSet<>();
        BrokenStudent scanGate1 = new BrokenStudent("192311001", "Arun");
        BrokenStudent scanGate2 = new BrokenStudent("192311002", "Divya");
        BrokenStudent scanGate3 = new BrokenStudent("192311001", "Arun"); // New instance, same student!

        test2.add(scanGate1);
        test2.add(scanGate2);
        test2.add(scanGate3);

        System.out.println("Added: scanGate1 (\"192311001\"), scanGate2 (\"192311002\"), scanGate3 (\"192311001\")");
        System.out.println("Expected Unique Count: 2 | Observed Defective Count: " + test2.size());
        System.out.println("Explanation: scanGate1 and scanGate3 generated DIFFERENT System.identityHashCode() values.");
        System.out.println("They were mapped into DIFFERENT hash buckets, so equals() was NEVER even called!\n");

        // --- TEST 3: The Mutable Key Hash Invalidation Trap ---
        System.out.println("--- TEST 3: THE POST-INSERTION MUTATION DISASTER ---");
        Set<MutableStudent> test3 = new HashSet<>();
        MutableStudent student = new MutableStudent("192311001", "Arun");
        test3.add(student);

        System.out.println("Inserted student with regNo '192311001'. Initial hash bucket determined.");
        System.out.println("contains(student) BEFORE mutation: " + test3.contains(student));

        // Mutating key field AFTER insertion!
        student.regNo = "192311999";
        System.out.println("\n[MUTATION] Modified student.regNo to '192311999' while inside the HashSet!");
        System.out.println("contains(student) AFTER mutation : " + test3.contains(student) + " (GHOST OBJECT TRAP!)");

        // Trying to re-add with the new ID
        test3.add(new MutableStudent("192311999", "Arun"));
        System.out.println("Set size after adding new student with '192311999': " + test3.size());
        System.out.println("Explanation: Mutation altered the hashCode()! When querying contains(), the HashSet");
        System.out.println("scans the bucket for hash('192311999'), while the object sits stranded in the bucket");
        System.out.println("for hash('192311001'). The object cannot be found, updated, or removed!");
    }
}
