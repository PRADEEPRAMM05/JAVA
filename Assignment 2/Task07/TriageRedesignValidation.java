import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Task 07: Day-4 Design Challenge - Diagnose, Redesign and Validate.
 * 
 * Problem:
 * The Q6 PriorityQueue triage engine suffers from:
 * 1. O(log n) heap restructuring and repeated comparator comparisons on every admit/poll.
 * 2. Complete starvation of low-severity (S4/S5) patients under continuous emergency loads.
 * 
 * Redesign:
 * Bounded Multi-Level Bucketed Queue (5 FIFO ArrayDeque instances) with Aging-Based Escalation:
 * - O(1) amortized admission directly to bucket[severity].
 * - O(1) poll: checks aging threshold to prevent starvation, then polls highest-priority non-empty bucket.
 */
public class TriageRedesignValidation {

    static class Patient {
        final String name;
        final int severity; // 1 (critical) to 5 (minor)
        final long arrivalSeq;
        final long arrivalTimeMs;

        Patient(String name, int severity, long arrivalSeq, long arrivalTimeMs) {
            this.name = name;
            this.severity = severity;
            this.arrivalSeq = arrivalSeq;
            this.arrivalTimeMs = arrivalTimeMs;
        }

        @Override
        public String toString() {
            return name + "(S" + severity + ")";
        }
    }

    // =========================================================================
    // 1. ORIGINAL DESIGN (From Q6): Binary Min-Heap PriorityQueue
    // - Admission: O(log n)
    // - Dispatch: O(log n)
    // - Flaw: Starvation of low priorities under continuous critical load
    // =========================================================================
    static class OriginalHeapTriage {
        private long counter = 0;
        private final PriorityQueue<Patient> queue = new PriorityQueue<>(
                Comparator.comparingInt((Patient p) -> p.severity)
                        .thenComparingLong(p -> p.arrivalSeq)
        );

        void admit(String name, int severity, long nowMs) {
            queue.add(new Patient(name, severity, counter++, nowMs));
        }

        Patient next(long nowMs) {
            return queue.poll();
        }

        int size() {
            return queue.size();
        }
    }

    // =========================================================================
    // 2. REDESIGNED SOLUTION: Bounded Multi-Level Buckets + Aging Escalation
    // - 5 FIFO ArrayDeque instances (one per severity level 1..5)
    // - Admission: O(1)
    // - Dispatch: O(1) bounded (checks aging first to guarantee fairness)
    // =========================================================================
    static class BucketedFairTriage {
        @SuppressWarnings("unchecked")
        private final Deque<Patient>[] buckets = new ArrayDeque[6]; // indices 1..5
        private long counter = 0;
        private int totalSize = 0;
        private final long starvationThresholdMs;

        BucketedFairTriage(long starvationThresholdMs) {
            this.starvationThresholdMs = starvationThresholdMs;
            for (int i = 1; i <= 5; i++) {
                buckets[i] = new ArrayDeque<>();
            }
        }

        void admit(String name, int severity, long nowMs) {
            if (severity < 1 || severity > 5) {
                throw new IllegalArgumentException("Invalid severity: " + severity);
            }
            buckets[severity].addLast(new Patient(name, severity, counter++, nowMs));
            totalSize++;
        }

        Patient next(long nowMs) {
            if (totalSize == 0) return null;

            // Step 1: Fairness Aging Check
            // If the oldest patient in a lower bucket has waited beyond the threshold,
            // escalate and dispatch them immediately to prevent starvation.
            for (int sev = 5; sev >= 2; sev--) {
                Patient oldest = buckets[sev].peekFirst();
                if (oldest != null && (nowMs - oldest.arrivalTimeMs) >= starvationThresholdMs) {
                    totalSize--;
                    return buckets[sev].pollFirst();
                }
            }

            // Step 2: Normal Highest-Priority Dispatch (O(1) search across 5 pointers)
            for (int sev = 1; sev <= 5; sev++) {
                if (!buckets[sev].isEmpty()) {
                    totalSize--;
                    return buckets[sev].pollFirst();
                }
            }
            return null;
        }

        int size() {
            return totalSize;
        }
    }

    private static long getUsedMemoryKB() {
        System.gc();
        try { Thread.sleep(30); } catch (InterruptedException ignored) {}
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / 1024;
    }

    private static void comparePerformance(int backlogSize, int operations) {
        Random rand = new Random(42);
        int[] severities = new int[backlogSize + operations];
        for (int i = 0; i < severities.length; i++) {
            severities[i] = 1 + rand.nextInt(5);
        }

        // Benchmark Original Heap Triage
        long memBeforeOrig = getUsedMemoryKB();
        OriginalHeapTriage orig = new OriginalHeapTriage();
        for (int i = 0; i < backlogSize; i++) {
            orig.admit("P" + i, severities[i], i * 10L);
        }
        long start = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            orig.admit("N" + i, severities[backlogSize + i], (backlogSize + i) * 10L);
            orig.next((backlogSize + i) * 10L);
        }
        double origMs = (System.nanoTime() - start) / 1_000_000.0;
        long memAfterOrig = getUsedMemoryKB();

        // Benchmark Redesigned Bucketed Fair Triage
        long memBeforeRed = getUsedMemoryKB();
        BucketedFairTriage red = new BucketedFairTriage(30_000L); // 30s aging limit
        for (int i = 0; i < backlogSize; i++) {
            red.admit("P" + i, severities[i], i * 10L);
        }
        start = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            red.admit("N" + i, severities[backlogSize + i], (backlogSize + i) * 10L);
            red.next((backlogSize + i) * 10L);
        }
        double redMs = (System.nanoTime() - start) / 1_000_000.0;
        long memAfterRed = getUsedMemoryKB();

        double speedup = origMs / Math.max(0.001, redMs);
        System.out.printf("Backlog: %,7d | Ops: %,7d | Orig Heap: %7.3f ms (%d KB) | Redesigned: %7.3f ms (%d KB) | Speedup: %.2fx%n",
                backlogSize, operations,
                origMs, Math.max(0, memAfterOrig - memBeforeOrig),
                redMs, Math.max(0, memAfterRed - memBeforeRed),
                speedup);
    }

    private static void verifyStarvationFix() {
        OriginalHeapTriage orig = new OriginalHeapTriage();
        BucketedFairTriage red = new BucketedFairTriage(100L); // 100 ms aging limit

        // 1 Minor patient arrives at t = 0 ms
        orig.admit("Minor_Patient_S5", 5, 0L);
        red.admit("Minor_Patient_S5", 5, 0L);

        // 20 Critical (S1) patients arrive continuously between t = 10 and t = 200 ms
        for (int i = 1; i <= 20; i++) {
            long t = i * 10L;
            orig.admit("Critical_S1_" + i, 1, t);
            red.admit("Critical_S1_" + i, 1, t);
        }

        // Measure how many turns until Minor patient is treated
        int origWaitTurns = 0;
        while (orig.size() > 0) {
            origWaitTurns++;
            if (orig.next(250L).name.equals("Minor_Patient_S5")) break;
        }

        int redWaitTurns = 0;
        while (red.size() > 0) {
            redWaitTurns++;
            if (red.next(250L).name.equals("Minor_Patient_S5")) break;
        }

        System.out.println("\n=== STARVATION FAIRNESS VALIDATION (1 Minor S5 vs 20 Critical S1 arrivals) ===");
        System.out.println("Original PriorityQueue: Minor patient treated at turn #" + origWaitTurns + " (Last - Completely Starved!)");
        System.out.println("Redesigned Fair Queue : Minor patient treated at turn #" + redWaitTurns + " (Escalated via Aging Rule - Starvation Eliminated!)");
    }

    public static void main(String[] args) {
        System.out.println("########################################################################################");
        System.out.println("   TASK 07 (DAY-4 CHALLENGE): REDESIGN & VALIDATE EMERGENCY TRIAGE QUEUE               ");
        System.out.println("########################################################################################\n");

        // Warmup
        comparePerformance(1_000, 10_000);

        System.out.println("--- PERFORMANCE BENCHMARK (HEAP TRIAGE vs BUCKETED FAIR TRIAGE) ---");
        comparePerformance(1_000, 100_000);
        comparePerformance(50_000, 100_000);
        comparePerformance(250_000, 100_000);

        verifyStarvationFix();
    }
}
