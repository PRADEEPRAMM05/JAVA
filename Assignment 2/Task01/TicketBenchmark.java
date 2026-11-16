import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Task 01: Building Text Under Load - String, StringBuilder or StringBuffer?
 * Investigates text building performance in a Railway Reservation e-Ticket System:
 * - String (+) repeated concatenation: O(N^2) time due to defensive array copies
 * - StringBuilder: O(N) mutable char buffer, non-thread-safe (optimal per thread)
 * - StringBuffer: O(N) mutable char buffer with synchronized methods (for multi-threaded logs)
 */
public class TicketBenchmark {

    // Generates sample railway e-ticket fields
    public static String[] generateFields(int fieldCount) {
        String[] fields = new String[fieldCount];
        fields[0] = "PNR:4829104721";
        if (fieldCount > 1) fields[1] = "Train:12622-TamilNaduExp";
        if (fieldCount > 2) fields[2] = "Passenger:ArunKumar";
        if (fieldCount > 3) fields[3] = "Coach:B2";
        if (fieldCount > 4) fields[4] = "Berth:42-LB";
        for (int i = 5; i < fieldCount; i++) {
            fields[i] = "Field_" + i + ":Val_" + (100 + i);
        }
        return fields;
    }

    // 1. Repeated String concatenation using '+'
    public static String buildWithString(String[] fields) {
        String ticket = "";
        for (String field : fields) {
            ticket = ticket + field + " | ";
        }
        return ticket;
    }

    // 2. StringBuilder (unsynchronized mutable buffer)
    public static String buildWithStringBuilder(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            sb.append(field).append(" | ");
        }
        return sb.toString();
    }

    // 3. StringBuffer (thread-safe synchronized buffer)
    public static String buildWithStringBuffer(String[] fields) {
        StringBuffer sbf = new StringBuffer();
        for (String field : fields) {
            sbf.append(field).append(" | ");
        }
        return sbf.toString();
    }

    // Alternative approaches for complexity comparison
    public static String buildWithStringJoin(String[] fields) {
        return String.join(" | ", fields);
    }

    public static String buildWithStringJoiner(String[] fields) {
        StringJoiner joiner = new StringJoiner(" | ");
        for (String field : fields) {
            joiner.add(field);
        }
        return joiner.toString();
    }

    private static long getUsedMemoryKB() {
        System.gc();
        try { Thread.sleep(30); } catch (InterruptedException ignored) {}
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / 1024;
    }

    public static void runWorkload(String workloadName, int ticketCount, int fieldCount, int runs) {
        String[] fields = generateFields(fieldCount);

        // Warmup phase for JIT compiler optimization
        for (int w = 0; w < 300; w++) {
            buildWithString(fields);
            buildWithStringBuilder(fields);
            buildWithStringBuffer(fields);
        }

        // 1. Benchmark String (+)
        long memBeforeStr = getUsedMemoryKB();
        long timeStr = 0;
        for (int r = 0; r < runs; r++) {
            List<String> list = new ArrayList<>(ticketCount);
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                list.add(buildWithString(fields));
            }
            timeStr += (System.nanoTime() - start);
        }
        long memAfterStr = getUsedMemoryKB();
        double avgMsStr = (timeStr / (double) runs) / 1_000_000.0;
        long estStringsStr = (long) ticketCount * fieldCount * 2;

        // 2. Benchmark StringBuilder
        long memBeforeSb = getUsedMemoryKB();
        long timeSb = 0;
        for (int r = 0; r < runs; r++) {
            List<String> list = new ArrayList<>(ticketCount);
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                list.add(buildWithStringBuilder(fields));
            }
            timeSb += (System.nanoTime() - start);
        }
        long memAfterSb = getUsedMemoryKB();
        double avgMsSb = (timeSb / (double) runs) / 1_000_000.0;
        long estStringsSb = ticketCount;

        // 3. Benchmark StringBuffer
        long memBeforeSbf = getUsedMemoryKB();
        long timeSbf = 0;
        for (int r = 0; r < runs; r++) {
            List<String> list = new ArrayList<>(ticketCount);
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                list.add(buildWithStringBuffer(fields));
            }
            timeSbf += (System.nanoTime() - start);
        }
        long memAfterSbf = getUsedMemoryKB();
        double avgMsSbf = (timeSbf / (double) runs) / 1_000_000.0;
        long estStringsSbf = ticketCount;

        System.out.println("=========================================================================================");
        System.out.printf("WORKLOAD: %s | Tickets: %,d | Fields: %d | Iterations: %d%n",
                workloadName, ticketCount, fieldCount, runs);
        System.out.println("=========================================================================================");
        System.out.printf("%-15s | %-16s | %-26s | %-18s%n",
                "Approach", "Avg Time (ms)", "Est. Intermediate Objects", "Memory Delta (KB)");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-15s | %-16.3f | %-26d | %-18d%n",
                "String (+)", avgMsStr, estStringsStr, Math.max(0, memAfterStr - memBeforeStr));
        System.out.printf("%-15s | %-16.3f | %-26d | %-18d%n",
                "StringBuilder", avgMsSb, estStringsSb, Math.max(0, memAfterSb - memBeforeSb));
        System.out.printf("%-15s | %-16.3f | %-26d | %-18d%n",
                "StringBuffer", avgMsSbf, estStringsSbf, Math.max(0, memAfterSbf - memBeforeSbf));
        System.out.printf("Speedup: StringBuilder is %.1fx faster than String (+)%n%n", avgMsStr / avgMsSb);
    }

    public static void main(String[] args) {
        System.out.println("#########################################################################################");
        System.out.println("  TASK 01 BENCHMARK: STRING vs STRINGBUILDER vs STRINGBUFFER (RAILWAY E-TICKET SYSTEM)  ");
        System.out.println("#########################################################################################\n");

        // Workload 1: Short Ticket (100 tickets, 8 fields)
        runWorkload("Workload 1: Short Ticket (Light Load)", 100, 8, 10);

        // Workload 2: Standard Heavy Load (10,000 tickets, 50 fields)
        runWorkload("Workload 2: Standard Heavy Load", 10_000, 50, 10);

        // Workload 3: Peak Tatkal Rush (25,000 tickets, 80 fields)
        runWorkload("Workload 3: Peak Tatkal Rush (Custom Heavy)", 25_000, 80, 5);

        // Demonstrate Alternative Concepts
        System.out.println("--- ALTERNATIVE TEXT BUILDING PARADIGMS ---");
        String[] sample = generateFields(5);
        System.out.println("1. String.join():   " + buildWithStringJoin(sample));
        System.out.println("2. StringJoiner:    " + buildWithStringJoiner(sample));
        System.out.println("3. String.format(): " + String.format("%s | %s | %s", sample[0], sample[1], sample[2]));
    }
}
