import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task 07: Day-4 Design Challenge - Concurrent, Fault-Tolerant Semester Result Processor.
 * Demonstrates:
 * 1. ThreadPoolExecutor managing worker threads via ExecutorService
 * 2. Callable<FileOutcome> returning Future tasks
 * 3. Fault isolation: individual record corruption or I/O failure does not crash the batch
 * 4. Thread-safe collections (ConcurrentHashMap, AtomicInteger)
 * 5. Graceful shutdown lifecycle (shutdown & awaitTermination)
 */
public class SemesterResultProcessor {

    // Immutable outcome descriptor
    static class FileOutcome {
        final String studentId;
        final boolean success;
        final double gpa;
        final String classification;
        final String errorReason;

        static FileOutcome ok(String studentId, double gpa, String classification) {
            return new FileOutcome(studentId, true, gpa, classification, null);
        }

        static FileOutcome fail(String studentId, String errorReason) {
            return new FileOutcome(studentId, false, 0.0, null, errorReason);
        }

        private FileOutcome(String studentId, boolean success, double gpa, String classification, String errorReason) {
            this.studentId = studentId;
            this.success = success;
            this.gpa = gpa;
            this.classification = classification;
            this.errorReason = errorReason;
        }
    }

    // Callable task evaluating a student's grade records
    static class StudentEvaluationTask implements Callable<FileOutcome> {
        private final String studentId;
        private final int[] marks;
        private final boolean simulateTimeout;

        public StudentEvaluationTask(String studentId, int[] marks, boolean simulateTimeout) {
            this.studentId = studentId;
            this.marks = marks;
            this.simulateTimeout = simulateTimeout;
        }

        @Override
        public FileOutcome call() throws Exception {
            if (simulateTimeout) {
                Thread.sleep(80); // Simulates slow remote database fetch
            }

            if (marks == null || marks.length == 0) {
                throw new IllegalArgumentException("No subject marks found for student record");
            }

            int sum = 0;
            for (int mark : marks) {
                if (mark < 0 || mark > 100) {
                    throw new ArithmeticException("Corrupted mark value: " + mark + " (Must be between 0 and 100)");
                }
                sum += mark;
            }

            double average = sum / (double) marks.length;
            double gpa = (average / 10.0);
            String classification = (gpa >= 8.5) ? "First Class with Distinction" :
                                    (gpa >= 6.5) ? "First Class" : "Pass";

            return FileOutcome.ok(studentId, gpa, classification);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================================");
        System.out.println(" TASK 07 (DAY-4 CHALLENGE): CONCURRENT FAULT-TOLERANT RESULT PROCESSOR    ");
        System.out.println("==========================================================================\n");

        int poolSize = 4;
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger failureCounter = new AtomicInteger(0);
        ConcurrentHashMap<String, String> honorRoll = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> auditFailureLog = new ConcurrentHashMap<>();

        List<Future<FileOutcome>> futures = new ArrayList<>();

        // Creating test batch with normal, boundary, corrupted, and timeout records
        List<StudentEvaluationTask> tasks = List.of(
                new StudentEvaluationTask("STU-1001", new int[]{85, 90, 88, 92, 80, 89}, false), // Distinction
                new StudentEvaluationTask("STU-1002", new int[]{70, 65, 72, 68, 75, 71}, false), // First Class
                new StudentEvaluationTask("STU-1003", new int[]{45, 50, 999, 60, 55, 48}, false), // Corrupted mark (999)
                new StudentEvaluationTask("STU-1004", new int[]{95, 92, 98, 91, 94, 96}, false), // Distinction
                new StudentEvaluationTask("STU-1005", new int[]{}, false),                         // Empty marks array
                new StudentEvaluationTask("STU-1006", new int[]{80, 85, 82, 88, 84, 86}, true)   // Slow remote fetch
        );

        System.out.printf("Dispatching %,d student grading tasks across FixedThreadPool of %d workers...%n%n",
                tasks.size(), poolSize);

        long startTime = System.nanoTime();

        for (StudentEvaluationTask task : tasks) {
            futures.add(executor.submit(task));
        }

        // Collecting results with fault isolation and timeout enforcement
        for (int i = 0; i < futures.size(); i++) {
            Future<FileOutcome> future = futures.get(i);
            String candidateId = tasks.get(i).studentId;

            try {
                // Enforce 50ms SLA per student task
                FileOutcome outcome = future.get(50, TimeUnit.MILLISECONDS);
                if (outcome.success) {
                    successCounter.incrementAndGet();
                    System.out.printf("  [SUCCESS] %s | GPA: %.2f | %s%n",
                            outcome.studentId, outcome.gpa, outcome.classification);
                    if (outcome.gpa >= 8.5) {
                        honorRoll.put(outcome.studentId, outcome.classification);
                    }
                }
            } catch (ExecutionException ex) {
                failureCounter.incrementAndGet();
                Throwable rootCause = ex.getCause();
                System.out.printf("  [FAILED - RECORD CORRUPT] %s: %s%n", candidateId, rootCause.getMessage());
                auditFailureLog.put(candidateId, rootCause.getMessage());
            } catch (java.util.concurrent.TimeoutException ex) {
                failureCounter.incrementAndGet();
                future.cancel(true); // Terminate hung thread task
                System.out.printf("  [FAILED - TIMEOUT EXCEEDED] %s: Task exceeded 50ms SLA threshold%n", candidateId);
                auditFailureLog.put(candidateId, "Processing SLA Timed Out");
            }
        }

        // Graceful shutdown sequence
        executor.shutdown();
        boolean finishedCleanly = executor.awaitTermination(2, TimeUnit.SECONDS);

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("\n==========================================================================");
        System.out.println("  CONCURRENT BATCH PROCESSING EXECUTION AUDIT SUMMARY                     ");
        System.out.println("==========================================================================");
        System.out.println("Total Records Processed : " + tasks.size());
        System.out.println("Successful Evaluations  : " + successCounter.get());
        System.out.println("Isolated Failures Logged: " + failureCounter.get());
        System.out.println("Honor Roll Distinctions : " + honorRoll.keySet());
        System.out.println("Failure Audit Registry  : " + auditFailureLog);
        System.out.printf("Batch Execution Latency : %d ms (ThreadPool Terminated Cleanly: %b)%n",
                durationMs, finishedCleanly);
    }
}
