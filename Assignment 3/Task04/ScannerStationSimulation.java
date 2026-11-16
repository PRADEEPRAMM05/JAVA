import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task 04 (Q4): Thread Creation and the Thread Life Cycle.
 * Airport Baggage Security Scanner Station Simulation:
 * 1. Thread Creation: Extending Thread vs. Implementing Runnable
 * 2. Life Cycle State Tracking: NEW -> RUNNABLE -> TIMED_WAITING -> TERMINATED
 * 3. Thread Synchronization Mechanisms: join(), isAlive(), and Cooperative Interruption
 */
public class ScannerStationSimulation {

    // Approach 1: Extending java.lang.Thread
    static class StationThread extends Thread {
        private final int packagesToScan;
        private final AtomicInteger totalCounter;
        private final List<String> completionLog;

        public StationThread(String name, int packagesToScan, AtomicInteger totalCounter, List<String> completionLog) {
            super(name);
            this.packagesToScan = packagesToScan;
            this.totalCounter = totalCounter;
            this.completionLog = completionLog;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= packagesToScan; i++) {
                    Thread.sleep(15); // Enters TIMED_WAITING state
                    totalCounter.incrementAndGet();
                }
                completionLog.add(getName());
                System.out.printf("  [%s] Completed scanning %,d packages.%n", getName(), packagesToScan);
            } catch (InterruptedException e) {
                System.out.printf("  [%s] Interrupted! Safely shutting down scanner conveyor.%n", getName());
                Thread.currentThread().interrupt(); // Restore interrupt flag
            }
        }
    }

    // Approach 2: Implementing java.lang.Runnable (Clean separation of Task vs. Thread Worker)
    static class StationRunnable implements Runnable {
        private final String stationName;
        private final int packagesToScan;
        private final AtomicInteger totalCounter;
        private final List<String> completionLog;

        public StationRunnable(String stationName, int packagesToScan, AtomicInteger totalCounter, List<String> completionLog) {
            this.stationName = stationName;
            this.packagesToScan = packagesToScan;
            this.totalCounter = totalCounter;
            this.completionLog = completionLog;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= packagesToScan; i++) {
                    Thread.sleep(15); // Enters TIMED_WAITING state
                    totalCounter.incrementAndGet();
                }
                completionLog.add(stationName);
                System.out.printf("  [%s] Completed scanning %,d packages.%n", stationName, packagesToScan);
            } catch (InterruptedException e) {
                System.out.printf("  [%s] Interrupted! Clean task shutdown.%n", stationName);
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================================");
        System.out.println("  TASK 04: AIRPORT SCANNER STATION - THREAD LIFECYCLE & CREATION DEMO     ");
        System.out.println("==========================================================================\n");

        AtomicInteger totalScanned = new AtomicInteger(0);
        List<String> completionLog = Collections.synchronizedList(new ArrayList<>());

        // 1. Thread Life Cycle State Tracking
        System.out.println("--- 1. MONITORING THREAD LIFE CYCLE TRANSITIONS ---");
        StationThread scannerA = new StationThread("Scanner-A (Extends Thread)", 10, totalScanned, completionLog);
        System.out.println("State after instantiation               : " + scannerA.getState() + " (isAlive: " + scannerA.isAlive() + ")");

        scannerA.start();
        System.out.println("State immediately after start()         : " + scannerA.getState() + " (isAlive: " + scannerA.isAlive() + ")");

        // Brief sleep to catch Scanner-A in TIMED_WAITING
        Thread.sleep(10);
        System.out.println("State during Thread.sleep() in run loop : " + scannerA.getState());

        scannerA.join(); // Main thread transitions to WAITING until scannerA terminates
        System.out.println("State after join() completion           : " + scannerA.getState() + " (isAlive: " + scannerA.isAlive() + ")\n");

        // 2. Concurrent Processing: Extending Thread vs Implementing Runnable
        System.out.println("--- 2. CONCURRENT MULTI-STATION SCANNING ---");
        StationThread station1 = new StationThread("Station-1 (Thread Subclass)", 15, totalScanned, completionLog);
        Thread station2 = new Thread(new StationRunnable("Station-2 (Runnable Task)", 15, totalScanned, completionLog));

        // Priority adjustment
        station1.setPriority(Thread.NORM_PRIORITY);
        station2.setPriority(Thread.MAX_PRIORITY);

        station1.start();
        station2.start();

        station1.join();
        station2.join();

        System.out.println("\nTotal Packages Scanned Across Stations: " + totalScanned.get());
        System.out.println("Completion Sequence: " + completionLog + "\n");

        // 3. Cooperative Thread Interruption Demonstration
        System.out.println("--- 3. COOPERATIVE THREAD INTERRUPTION ---");
        StationThread emergencyHaltStation = new StationThread("Emergency-Halt-Scanner", 100, totalScanned, completionLog);
        emergencyHaltStation.start();

        Thread.sleep(30); // Let it process a few items
        System.out.println("Sending interrupt() signal to " + emergencyHaltStation.getName() + "...");
        emergencyHaltStation.interrupt();
        emergencyHaltStation.join();
        System.out.println("Halt scanner state: " + emergencyHaltStation.getState());
    }
}
