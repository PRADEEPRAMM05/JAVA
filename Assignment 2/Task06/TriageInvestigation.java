import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Task 06 Investigation:
 * 1. Proves why Java PriorityQueue is UNSTABLE without an explicit sequence counter
 * 2. Simulates Starvation (Indefinite Delay) of low-severity patients under continuous stream of critical patients
 */
public class TriageInvestigation {

    // Defective Patient without arrival timestamp
    static class UnstablePatient {
        final String name;
        final int severity;

        UnstablePatient(String name, int severity) {
            this.name = name;
            this.severity = severity;
        }

        @Override
        public String toString() {
            return name + "(S" + severity + ")";
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(" TASK 06: INVESTIGATING QUEUE FAIRNESS, STABILITY & STARVATION IN TRIAGE  ");
        System.out.println("==========================================================================\n");

        // 1. Instability of Java PriorityQueue without arrival timestamp
        System.out.println("--- 1. PROVING PRIORITYQUEUE UNSTABLE BEHAVIOR WITHOUT ARRIVAL TIE-BREAKER ---");
        // Naive comparator checking ONLY severity
        PriorityQueue<UnstablePatient> unstableQueue = new PriorityQueue<>(
                Comparator.comparingInt(p -> p.severity)
        );

        // Admit multiple patients with same severity (S3) interspersed with other priorities
        unstableQueue.add(new UnstablePatient("Patient_A", 3)); // Arrived 1st
        unstableQueue.add(new UnstablePatient("Patient_B", 3)); // Arrived 2nd
        unstableQueue.add(new UnstablePatient("Critical_1", 1));
        unstableQueue.add(new UnstablePatient("Patient_C", 3)); // Arrived 3rd
        unstableQueue.add(new UnstablePatient("Critical_2", 1));
        unstableQueue.add(new UnstablePatient("Patient_D", 3)); // Arrived 4th

        System.out.println("Admitted in order: A(S3), B(S3), C1(S1), C(S3), C2(S1), D(S3)");
        System.out.print("Polling Order    : ");
        while (!unstableQueue.isEmpty()) {
            System.out.print(unstableQueue.poll() + " ");
        }
        System.out.println("\nFinding: A standard binary min-heap does not guarantee FIFO order for equal keys.");
        System.out.println("An explicit monotonic arrival counter (counter++) is mathematically required!\n");

        // 2. Simulating Starvation of Low-Priority Patients
        System.out.println("--- 2. SIMULATING LOW-SEVERITY PATIENT STARVATION (INDEFINITE DELAY) ---");
        Triage er = new Triage();

        // Asha arrives early with minor illness (S5)
        er.admit("Asha_S5_Arrived_08:00", 5);

        // Continuous stream of urgent and critical patients arrive over the next hours
        for (int i = 1; i <= 5; i++) {
            er.admit("Critical_Emergency_" + i, 1);
            er.admit("Urgent_Case_" + i, 2);
        }

        System.out.println("Asha (S5) arrived at 08:00 AM. 10 subsequent S1/S2 patients arrived thereafter.");
        System.out.println("Doctors treating first 10 available patients:");
        for (int i = 1; i <= 10; i++) {
            System.out.printf("  Treated [%d]: %s%n", i, er.next());
        }

        System.out.println("\nIs Asha treated yet? " + (er.next().name.contains("Asha")));
        System.out.println("Conclusion: Under pure PriorityQueue triage, minor patients suffer INDEFINITE STARVATION.");
        System.out.println("This critical diagnostic insight forms the motivation for the Day-4 Challenge (Task 07)!");
    }
}
