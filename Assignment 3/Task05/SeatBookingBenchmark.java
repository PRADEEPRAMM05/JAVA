import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task 05 Benchmark:
 * Compares 4 concurrency designs under high contention (100 threads, 10 seats):
 * 1. Unsynchronized: check-then-act data race causes severe overselling (> 10 seats!)
 * 2. Synchronized Method: coarse-grained monitor lock on screen
 * 3. Synchronized Block: dedicated lock object
 * 4. Lock-Free AtomicBoolean[]: fine-grained compareAndSet() per seat (optimal scalability)
 */
public class SeatBookingBenchmark {

    // 1. Unsynchronized (Flawed)
    static class UnsynchronizedBooking {
        private final boolean[] booked;
        private int bookingsMade = 0;

        UnsynchronizedBooking(int seats) {
            booked = new boolean[seats];
        }

        public boolean book(int seat) {
            if (!booked[seat]) {
                try {
                    Thread.sleep(1); // Simulates database/network latency between check and update
                } catch (InterruptedException ignored) {}
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    // 2. Synchronized Method (Coarse-grained)
    static class SyncMethodBooking {
        private final boolean[] booked;
        private int bookingsMade = 0;

        SyncMethodBooking(int seats) {
            booked = new boolean[seats];
        }

        public synchronized boolean book(int seat) {
            if (!booked[seat]) {
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    // 3. Fine-Grained AtomicBoolean[] (Lock-free hardware CAS)
    static class AtomicSeatBooking {
        private final AtomicBoolean[] booked;
        private final AtomicInteger bookingsMade = new AtomicInteger(0);

        AtomicSeatBooking(int seats) {
            booked = new AtomicBoolean[seats];
            for (int i = 0; i < seats; i++) {
                booked[i] = new AtomicBoolean(false);
            }
        }

        public boolean book(int seat) {
            // Atomic compare-and-swap: only ONE thread can transition from false to true
            if (booked[seat].compareAndSet(false, true)) {
                bookingsMade.incrementAndGet();
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================================");
        System.out.println(" TASK 05: RACE CONDITIONS & SYNCHRONIZATION BENCHMARK (CINEMA BOOKING)    ");
        System.out.println("==========================================================================\n");

        int seats = 10;
        int threadsCount = 100;

        // Test 1: Unsynchronized Over-Selling Demonstration (10 runs)
        System.out.println("--- 1. UNSYNCHRONIZED RUNS (Demonstrating Data Race / Overselling) ---");
        for (int run = 1; run <= 5; run++) {
            UnsynchronizedBooking unsync = new UnsynchronizedBooking(seats);
            Thread[] threads = new Thread[threadsCount];
            for (int i = 0; i < threadsCount; i++) {
                final int seat = i % seats;
                threads[i] = new Thread(() -> unsync.book(seat));
                threads[i].start();
            }
            for (Thread t : threads) t.join();
            System.out.printf("  Run #%d: Seats Sold = %2d / %d (OVERBOOKED by %d seats!)%n",
                    run, unsync.getBookingsMade(), seats, Math.max(0, unsync.getBookingsMade() - seats));
        }

        // Test 2: Synchronized Method Verification
        System.out.println("\n--- 2. SYNCHRONIZED METHOD (Coarse Lock on 'this') ---");
        SyncMethodBooking sync = new SyncMethodBooking(seats);
        Thread[] syncThreads = new Thread[threadsCount];
        long start = System.nanoTime();
        for (int i = 0; i < threadsCount; i++) {
            final int seat = i % seats;
            syncThreads[i] = new Thread(() -> sync.book(seat));
            syncThreads[i].start();
        }
        for (Thread t : syncThreads) t.join();
        long syncDuration = System.nanoTime() - start;
        System.out.printf("  Seats Sold: %d / %d | Execution Time: %,d ns (Thread-safe)%n",
                sync.getBookingsMade(), seats, syncDuration);

        // Test 3: Lock-Free AtomicBoolean[] Verification
        System.out.println("\n--- 3. FINE-GRAINED ATOMIC COMPARE-AND-SET (CAS) ---");
        AtomicSeatBooking atomic = new AtomicSeatBooking(seats);
        Thread[] atomicThreads = new Thread[threadsCount];
        start = System.nanoTime();
        for (int i = 0; i < threadsCount; i++) {
            final int seat = i % seats;
            atomicThreads[i] = new Thread(() -> atomic.book(seat));
            atomicThreads[i].start();
        }
        for (Thread t : atomicThreads) t.join();
        long atomicDuration = System.nanoTime() - start;
        System.out.printf("  Seats Sold: %d / %d | Execution Time: %,d ns (Lock-Free CAS)%n",
                atomic.getBookingsMade(), seats, atomicDuration);
    }
}
