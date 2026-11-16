import java.util.ArrayList;
import java.util.HashMap;

/**
 * Task 11 Benchmark:
 * Measures search performance for active ticket retrieval on vehicle exit:
 * 1. Fixed Array: O(N) linear scan
 * 2. ArrayList: O(N) linear iteration
 * 3. HashMap: O(1) amortized hash lookup
 */
public class LookupBenchmark {

    static class SimpleTicket {
        private final String regNo;
        private final String ticketId;

        public SimpleTicket(String regNo, String ticketId) {
            this.regNo = regNo;
            this.ticketId = ticketId;
        }

        public String getRegNo() { return regNo; }
        public String getTicketId() { return ticketId; }
    }

    public static void main(String[] args) {
        int n = 10000;
        int repetitions = 500;

        SimpleTicket[] array = new SimpleTicket[n];
        ArrayList<SimpleTicket> list = new ArrayList<>(n);
        HashMap<String, SimpleTicket> map = new HashMap<>(n);

        // Prepopulate structures
        for (int i = 0; i < n; i++) {
            String reg = "TN" + String.format("%06d", i);
            String tktId = "TKT-" + i;
            SimpleTicket t = new SimpleTicket(reg, tktId);
            array[i] = t;
            list.add(t);
            map.put(reg, t);
        }

        // Target: vehicle at worst-case position (last element)
        String targetReg = "TN" + String.format("%06d", n - 1);
        long checksum = 0;

        System.out.println("==================================================");
        System.out.println("  LOOKUP BENCHMARK (N = " + n + " vehicles, " + repetitions + " lookups)");
        System.out.println("==================================================");

        // 1. Array Linear Search O(N)
        long start = System.nanoTime();
        for (int r = 0; r < repetitions; r++) {
            SimpleTicket found = null;
            for (int i = 0; i < n; i++) {
                if (array[i].getRegNo().equals(targetReg)) {
                    found = array[i];
                    break;
                }
            }
            if (found != null) checksum += found.getTicketId().length();
        }
        long arrayTime = System.nanoTime() - start;
        System.out.printf("1. Fixed Array (Linear Search) : %,12d ns (Avg: %.2f µs/lookup)%n",
                arrayTime, (arrayTime / (double) repetitions) / 1000.0);

        // 2. ArrayList Linear Search O(N)
        start = System.nanoTime();
        for (int r = 0; r < repetitions; r++) {
            SimpleTicket found = null;
            for (SimpleTicket t : list) {
                if (t.getRegNo().equals(targetReg)) {
                    found = t;
                    break;
                }
            }
            if (found != null) checksum += found.getTicketId().length();
        }
        long listTime = System.nanoTime() - start;
        System.out.printf("2. ArrayList   (Iterator Search): %,12d ns (Avg: %.2f µs/lookup)%n",
                listTime, (listTime / (double) repetitions) / 1000.0);

        // 3. HashMap Direct Key Lookup O(1)
        start = System.nanoTime();
        for (int r = 0; r < repetitions; r++) {
            SimpleTicket found = map.get(targetReg);
            if (found != null) checksum += found.getTicketId().length();
        }
        long mapTime = System.nanoTime() - start;
        System.out.printf("3. HashMap     (Hash Lookup)    : %,12d ns (Avg: %.2f µs/lookup)%n",
                mapTime, (mapTime / (double) repetitions) / 1000.0);

        System.out.println("--------------------------------------------------");
        System.out.printf("Speedup of HashMap over Array: %.1fx faster!%n", (double) arrayTime / mapTime);
        System.out.println("(Verification Checksum: " + checksum + ")");
    }
}
